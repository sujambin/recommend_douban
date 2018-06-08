package com.demo.doubanApi.thread;

import com.demo.common.model.Book;
import com.demo.common.model.ProxyAdd;
import com.demo.doubanApi.HttpProxy;
import com.demo.doubanApi.Main;
import com.demo.doubanApi.dto.BookJson;
import com.demo.doubanApi.utils.EmojiFilterUtils;
import com.google.gson.JsonSyntaxException;

import java.util.Date;

public class ThreadSave implements Runnable{

    public static int num = 0;
    @Override
    public void run() {
        System.out.println("线程启动："+num++);
        ProxyAdd proxyAdd = ProxyAdd.dao.getOneProxy();
        for (int i=1;i<99;i++){
            Book book = Book.dao.getOneBook();
            if(book==null)
                return;
            long id = book.getBookId();
            //            String json = HttpKit.get(apiUrl+book.getId());
            String json = null;
            try {
//                json = HttpsUtils.sendGet(Main.apiUrl+book.getId(), "", true, proxyAdd.getHost(), proxyAdd.getPort());
                json = HttpProxy.proxyGet(proxyAdd.getHost(), proxyAdd.getPort(), Main.apiUrl+book.getBookId());
            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"    连接错误:");
                proxyAdd.setFlag(4).setErrorInfo(e.toString()+"\n\n\n\n"+json).setTotal(proxyAdd.getTotal()+i-1).setErrTotal(proxyAdd.getErrTotal()+1).update();
                System.err.println("当前还剩下线程数:"+(--num));
                return;
            }

            BookJson bookJson = null;

            try {
                bookJson = Main.parseJsonWithGson(json, BookJson.class);
                book = Main.jsonToObject(json, Book.class);
                if(book.getTitle()==null)
                    throw new Exception("标题为空");
            }catch (JsonSyntaxException je){
                System.err.println(proxyAdd.getHost()+"非json格式:");
                System.out.println(json);
                proxyAdd.setFlag(3).setErrorInfo(je.toString()+"\n\n\n\n"+json).setTotal(proxyAdd.getTotal()+i-1).setErrTotal(proxyAdd.getErrTotal()+1).update();
//                book.setFlag(4);
//                book.update();
                System.err.println("当前还剩下线程数:"+(--num));
                return;
            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"图书信息错误:");
                System.out.println(json);
                Main.saveErrorInfoTolocal(id, json);
                proxyAdd.setFlag(0).setErrorInfo(json+"\n\n\n\n"+e.toString()).setTotal(proxyAdd.getTotal()+i-1).setErrTotal(proxyAdd.getErrTotal()+1).update();
                book.setFlag(3).setBookId(id);
                book.update();
                System.err.println("当前还剩下线程数:"+(--num));
                return;
            }
            Main.formatTags(bookJson.getTags(), book);
            Main.formatAuthor(bookJson.getAuthor(), book);
            Main.formatTranslator(bookJson.getTranslator(), book);
            book.setFlag(1);
            try{
                if(book.getSummary().length()>10000)
                    book.setSummary(book.getSummary().substring(0,9990)+"......");
                book.setSummary(EmojiFilterUtils.filterEmoji(book.getSummary()));
                book.update();
            }catch (Exception e){
                System.out.println("图书更新信息失败："+book.getBookId());
                e.printStackTrace();
            }

            //本地json备份
            Main.saveStringTolocal(book.getBookId(), json);
//            System.out.println(book.getId()+"完成");
            try {
                Thread.sleep(1000L);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        proxyAdd.setFlag(2).setTime(new Date()).setTotal(proxyAdd.getTotal()+99).update();
        System.out.println(proxyAdd.getHost()+"使命完成");
        System.err.println("当前还剩下线程数:"+(--num));
    }

}

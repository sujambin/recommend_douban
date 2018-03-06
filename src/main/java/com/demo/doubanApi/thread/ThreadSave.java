package com.demo.doubanApi.thread;

import com.demo.common.model.Book;
import com.demo.common.model.ProxyAdd;
import com.demo.doubanApi.Main;
import com.demo.doubanApi.dto.BookJson;
import com.demo.doubanApi.proxy2.HttpsUtils;

import java.util.Date;

public class ThreadSave implements Runnable{

    public static int num = 0;
    @Override
    public void run() {
        System.out.println("线程启动："+num++);
        ProxyAdd proxyAdd = ProxyAdd.dao.getOneProxy();
        for (int i=1;i<99;i++){
            Book book = Book.dao.getOneBook();
            //            String json = HttpKit.get(apiUrl+book.getId());
            String json = null;
            try {
                json = HttpsUtils.sendGet(Main.apiUrl+book.getId(), "", false, proxyAdd.getHost(), proxyAdd.getPort());
            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"    连接错误:");
                proxyAdd.setFlag(4).setErrorInfo(e.toString()+"\n\n\n\n"+json).update();
                System.err.println("当前还剩下线程数:"+(--num));
                return;
            }

            BookJson bookJson = null;

            try {
                bookJson = Main.parseJsonWithGson(json, BookJson.class);
                book = Main.jsonToObject(json, Book.class);
                if(book.getTitle()==null)
                    throw new Exception("标题为空");

            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"    格式错误:");
                System.out.println(json);
                proxyAdd.setFlag(3).setErrorInfo(e.toString()+"\n\n\n\n"+json).update();
                System.err.println("当前还剩下线程数:"+(--num));
                return;
            }
            Main.formatTags(bookJson.getTags(), book);
            Main.formatAuthor(bookJson.getAuthor(), book);
            Main.formatTranslator(bookJson.getTranslator(), book);
            book.setFlag(1);
            book.update();
            //本地json备份
            Main.saveStringTolocal(book.getId(), json);
            System.out.println(book.getId()+"完成");
            try {
                Thread.sleep(2000L);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        proxyAdd.setFlag(2).setTime(new Date()).update();
        System.err.println("当前还剩下线程数:"+(--num));
    }

}

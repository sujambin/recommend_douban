package com.demo.doubanApi;

import com.demo.common.model.Book;
import com.demo.common.model.ProxyAdd;
import com.demo.common.model.Tag;
import com.demo.doubanApi.dto.BookJson;
import com.demo.doubanApi.proxy2.HttpsUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jfinal.json.FastJson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class Main {

    public final static String apiUrl = "https://api.douban.com/v2/book/";

    public static void setProxy(String host, String port){
        System.setProperty("http.proxySet", "true");
        // 如果不设置，只要代理IP和代理端口正确,此项不设置也可以
        System.getProperties().setProperty("http.proxyHost", host);
        System.getProperties().setProperty("http.proxyPort", port);
    }

    public static List<Book> getBooks(){
        return Book.dao.find("select id from book where flag=0 and id>1000706 limit 90");
    }

    //将Json数据解析成相应的映射对象
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) throws JsonSyntaxException {
        Gson gson = new Gson();
        T result = gson.fromJson(jsonData, type);
        return result;
    }

    /**
     * json转换成相应的类型
     * @author Jambin
     * @date 2017-7-13
     * @return
     * @version
     */
    public static <T> T  jsonToObject (String jsonString, Class<T> type){
        return FastJson.getJson().parse(jsonString, type);
    }

    public static void saveTolocal(String host, int port) throws Exception{

        List<Book> list = getBooks();
//        setProxy("27.44.196.246", "9999");
        for (Book book : list){
//            String json = HttpKit.get(apiUrl+book.getId());
            String json = null;
            try {
//                json = HttpProxy.sendGet(apiUrl, book.getId().toString());   //HttpKit.get(apiUrl+book.getId());
                json = HttpProxy.proxyGet(host, port,apiUrl+book.getId());
                if (json.length()==0)
                    throw new Exception("空白文件");
                try {
                    parseJsonWithGson(json, BookJson.class);
                }catch (Exception e){
                    System.err.println("非json格式");
                    System.out.println(json);
                    break;
                }
                String path = "/Users/jambin/code/data/bookInfo/"+book.getId()+".txt";
                File file = new File(path);
                if (file.exists()) {
                    System.err.println(book.getId()+"已存在");
                } else {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(json.getBytes());
                    // fileOutputStream.write(sbString.getBytes());
                    fileOutputStream.close();
                    book.setFlag(2).update();
                    System.out.println(book.getId()+"成功");
                    Thread.sleep(1000L);
                } catch (Exception e) {
                    System.err.println(book.getId()+"失败");
                    if(file.exists()){
                        file.delete();
                    }
                    e.printStackTrace();
                }
            }catch (IOException ioe){
                System.err.println("被禁用了");
                ioe.printStackTrace();
                break;
            }catch (Exception e){
                e.printStackTrace();
                break;
            }

//            Thread.sleep(1000l);
        }
    }
    public static void saveStringTolocal(long id , String json){
        String path = "/Users/jambin/code/data/bookInfo/"+id+".txt";
        File file = new File(path);
        if (file.exists()) {
            System.err.println(id+"已存在");
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(json.getBytes());
            // fileOutputStream.write(sbString.getBytes());
            fileOutputStream.close();
            Thread.sleep(1000L);
        } catch (Exception e) {
            System.err.println(id+"保存到本地失败");
            if(file.exists()){
                file.delete();
            }
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws  Exception{


    }

    public static void localToMysql(){

    }

    public static boolean toDbAndLocal(){
        ProxyAdd proxyAdd = ProxyAdd.dao.getOneProxy();
        for (int i=1;i<99;i++){
            Book book = Book.dao.getOneBook();
            //            String json = HttpKit.get(apiUrl+book.getId());
            String json = null;
            try {
                json = HttpsUtils.sendGet(apiUrl+book.getId(), "", true, proxyAdd.getHost(), proxyAdd.getPort());
            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"    连接错误:");
                proxyAdd.setFlag(4).setErrorInfo(json+"\n\n\n\n"+e.toString());
                return false;
            }

            BookJson bookJson = null;

            try {
                bookJson = parseJsonWithGson(json, BookJson.class);
                book = jsonToObject(json, Book.class);
                if(book.getTitle()==null)
                    throw new Exception("标题为空");

            }catch (Exception e){
                System.err.println(proxyAdd.getHost()+"    格式错误:");
                System.out.println(json);
                proxyAdd.setFlag(3).setErrorInfo(json+"\n\n\n\n"+e.toString());
                return false;
            }
            formatTags(bookJson.getTags(), book);
            formatAuthor(bookJson.getAuthor(), book);
            formatTranslator(bookJson.getTranslator(), book);
            book.setFlag(1);
            book.update();
            //本地json备份
            saveStringTolocal(book.getId(), json);
            System.out.println(book.getId()+"完成");
        }
        proxyAdd.setFlag(2).setTime(new Date()).update();
        return true;
    }

    public static void formatTags(List<BookJson.Tag> tags, Book book){
        StringBuffer sb = new StringBuffer();
        for (BookJson.Tag btag:tags){
            Tag tag = Tag.dao.findFirst("select id from tag where name=?", btag.getName());
            if (tag==null) {
                tag = new Tag();
                tag.setName(btag.getName());
                tag.setTitle(btag.getTitle());
                tag.save();
            }
            sb.append(tag.getName()+",");
        }
        book.setTags(sb.subSequence(0, sb.length()-1).toString());
    }

    public static void formatAuthor(List<String> authors, Book book){
        StringBuffer sb = new StringBuffer();
        for (String author:authors){
            sb.append(author+",");
        }
        book.setAuthor(sb.subSequence(0, sb.length()-1).toString());
    }
    public static void formatTranslator(List<String> translators, Book book){
        StringBuffer sb = new StringBuffer();
        for (String translator:translators){
            sb.append(translator+",");
        }
        book.setTranslator(sb.subSequence(0, sb.length()-1).toString());
    }
}

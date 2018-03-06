package com.demo.doubanApi;

import com.demo.common.model.Book;
import com.google.gson.Gson;
import com.jfinal.json.FastJson;
import com.jfinal.json.Json;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

public class Test {

    private final static String apiUrl = "https://api.douban.com/v2/book/";

    public static List<Record> getBooks(){
        return Db.find("select distinct bookId from rating");
    }
    //将Json数据解析成相应的映射对象
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        T result = gson.fromJson(jsonData, type);
        return result;
    }

    /**
     * json转换成相应的类型
     * @author Jambin
     * @date 2017-7-13
     * @return T
     * @version
     */
    public static <T> T  getJson(String jsonString, Class<T> type){
        return FastJson.getJson().parse(jsonString, type);
    }

    public static void main(String[] args) {
        String json = HttpKit.get(apiUrl+1000001);

        Book book = getJson(json, Book.class);
//        BookJson book2 = parseJsonWithGson(json, BookJson.class);
        System.out.println(book.toString());
//        System.out.println(JsonKit.toJson(book2));

        System.out.println();
        System.out.println();
        System.out.println();
//        System.out.println(json);
    }
}

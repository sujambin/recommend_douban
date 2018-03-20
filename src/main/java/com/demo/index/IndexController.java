package com.demo.index;

import com.demo.common.model.AddProxyAdd;
import com.demo.common.model.ProxyAdd;
import com.demo.doubanApi.Main;
import com.demo.doubanApi.thread.ThreadSave;
import com.demo.spark.mllib.JavaALS;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * IndexController
 */
public class IndexController extends Controller {
	public void index() {
		render("index.html");
	}

	public void getReadedBooksByUserId(){
		String userId = getPara("userId");
		List readedBook = Db.find("select r.bookId as id, r.rating, b.title, b.image from rating r left join book b on (r.bookId=b.id) where r.userId=?",userId);
		Map<String, Object> result = new HashMap<>();
		result.put("result", true);
		result.put("readedBook", readedBook);
		renderJson(result);
	}

	public void getRecdBooksByUserId(){
		String userId = getPara("userId");
		List recdBooks = Db.find("select p.bookId as id, p.rating, b.title, b.image from pred p left join book b on (p.bookId=b.id) where p.userId=?",userId);
		Map<String, Object> result = new HashMap<>();
		result.put("result", true);
		result.put("recdBooks", recdBooks);
		renderJson(result);
	}


	public void txt() throws Exception{
		String splitStr = ":";
		List<Record> records = new ArrayList<>();
        FileReader fileReader = new FileReader("/Users/jambin/IdeaProjects/recommend/src/main/java/com/demo/doubanApi/123");
        BufferedReader bf = new BufferedReader(fileReader);
        String str = null;
        while ((str = bf.readLine())!=null){
            if (!str.contains(splitStr))
                continue;
            str = str.replace("[","").replace("]","");
            records.add(Db.findFirst("select id , b.title, b.image, 5 as rating from book b where id=? limit 1", str.split(splitStr)[1].trim()));
        }
		Map<String, Object> result = new HashMap<>();
		result.put("result", true);
		result.put("recdBooks", records);
		renderJson(result);
	}
    public void pred() throws Exception{




        int userId = getParaToInt("userId");
        int num = getParaToInt("num", 10);
        List<String[]> list = JavaALS.getPred(userId, num);
        List<Record> records = new ArrayList<>();
        for (String[] arr : list) {
            records.add(Db.findFirst("select id , b.title, b.image, " + arr[1] + " as rating from book b where id=? limit 1", arr[0]));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("result", true);
        result.put("recdBooks", records);
        renderJson(result);
    }

	public void als() throws Exception{

		String userId = getPara("userId");
		List recdBooks = Db.find("select p.bookId as id, p.rating, b.title, b.image from als_rating p left join book b on (p.bookId=b.id) where p.userId=?",userId);
		Map<String, Object> result = new HashMap<>();
		result.put("result", true);
		result.put("recdBooks", recdBooks);
		renderJson(result);
	}

	public void test() throws  Exception{
		String host = getPara("host");
		int port = getParaToInt("port");
		Main.saveTolocal(host, port);
		renderJson(true);
	}

	public void proxyAdd() throws Exception{
		renderJson(AddProxyAdd.addProxyAdd());
	}

	public void getBookInfo(){
		for (int i=1;i<1000;i++){
			ThreadSave threadSave = new ThreadSave();
			new Thread(threadSave).start();
		}
		renderJson(true);

	}
	public void proxyadd2(){
		int num = 0;
		try {
			System.out.println("执行添加代理地址任务");
			String html = HttpKit.get("http://www.66ip.cn/getzh.php?getzh=2018030609990" +
					"&getnum=8000&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=2&api=https");
			System.out.println(html);
//			BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(html.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
			String[] arr = html.split("<br>");
			System.out.println("size:"+arr.length);
			for(String str :arr){
				if (!str.contains(":"))
					continue;
				ProxyAdd proxyAdd = new ProxyAdd();
				proxyAdd.setHost(str.split(":")[0].trim());
				proxyAdd.setPort(Integer.parseInt(str.split(":")[1].trim()));
				if (ProxyAdd.dao.findById(proxyAdd.getHost())==null){
					proxyAdd.save();
				}
				num++;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("添加代理地址:"+num);
		renderJson(num);
	}





}




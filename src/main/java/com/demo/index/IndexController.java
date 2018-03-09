package com.demo.index;

import com.demo.common.model.AddProxyAdd;
import com.demo.common.model.ProxyAdd;
import com.demo.doubanApi.Main;
import com.demo.doubanApi.thread.ThreadSave;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;

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




package com.demo.index;

import com.demo.common.model.AddProxyAdd;
import com.demo.doubanApi.Main;
import com.demo.doubanApi.thread.ThreadSave;
import com.jfinal.core.Controller;

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
		AddProxyAdd.addProxyAdd();
	}

	public void getBookInfo(){
		for (int i=1;i<2;i++){
			ThreadSave threadSave = new ThreadSave();
			new Thread(threadSave).start();
		}
		renderJson(true);

	}

}




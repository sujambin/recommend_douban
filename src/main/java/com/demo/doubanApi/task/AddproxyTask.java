package com.demo.doubanApi.task;

import com.demo.common.model.ProxyAdd;
import com.jfinal.kit.HttpKit;

public class AddproxyTask implements Runnable {

    @Override
    public void run() {
        int num = 0;
        try {
            System.out.println("执行添加代理地址任务");
            String html = HttpKit.get("http://www.66ip.cn/getzh.php?getzh=2018030609990" +
                    "&getnum=8000&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=2&api=https");
            System.out.println(html);

            String[] arr = html.split("<br>");
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
    }
}

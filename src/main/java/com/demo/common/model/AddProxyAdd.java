package com.demo.common.model;

import com.jfinal.kit.HttpKit;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class AddProxyAdd {


    private static String COLON_DELIMITER=":";
    private static Pattern COLON_DELIMITER_PATTERN=Pattern.compile(COLON_DELIMITER);

    public static void main(String[] args) throws Exception{
    }

    public static long addProxyAdd() throws  Exception{
        FileReader reader = new FileReader("/Users/jambin/IdeaProjects/recommend/src/main/java/com/demo/doubanApi/123");
        BufferedReader br = new BufferedReader(reader);

        String str = null;
        long row =0;
        while((str = br.readLine()) !=null){
            if (!str.contains(":"))
                continue;
            ProxyAdd proxyAdd = new ProxyAdd();
            proxyAdd.setHost(str.split(":")[0]);
            proxyAdd.setPort(Integer.parseInt(str.split(":")[1]));
            if (ProxyAdd.dao.findById(proxyAdd.getHost())==null){
                proxyAdd.save();
                row+=1;
            }

        }
        reader.close();
        br.close();


        System.out.println("total row:"+ row);
        return row;

    }

    public static long addProxyAdd2() throws  Exception{
        String html = HttpKit.get("http://www.66ip.cn/getzh.php?getzh=2018030609990" +
                "&getnum=8000&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=2&api=https");
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(html.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        String str = null;
        long row =0;
        while((str = br.readLine()) !=null){
            if (!str.contains(":"))
                continue;
            ProxyAdd proxyAdd = new ProxyAdd();
            proxyAdd.setHost(str.split(":")[0]);
            proxyAdd.setPort(Integer.parseInt(str.split(":")[1]));
            if (ProxyAdd.dao.findById(proxyAdd.getHost())==null){
                proxyAdd.save();
                row+=1;
            }

        }
        br.close();


        System.out.println("total row:"+ row);
        return row;

    }
}

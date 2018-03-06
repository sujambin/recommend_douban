package com.demo.common.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Pattern;

public class AddProxyAdd {


    private static String COLON_DELIMITER=":";
    private static Pattern COLON_DELIMITER_PATTERN=Pattern.compile(COLON_DELIMITER);

    public static void main(String[] args) throws Exception{
    }

    public static void addProxyAdd() throws  Exception{
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

    }
}

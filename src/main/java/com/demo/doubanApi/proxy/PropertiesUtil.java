package com.demo.doubanApi.proxy;

import java.io.IOException;
import java.util.Properties;


public class PropertiesUtil {
    public static final Properties properties = new Properties();
    static {
        try {
            properties.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            ECommonUtil.getLog().error("初始config配置文件失败");
        }
    }
}
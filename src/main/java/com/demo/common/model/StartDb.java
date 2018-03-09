package com.demo.common.model;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;

import java.util.List;

public class StartDb {
    public static void main(String[] args) {
        PropKit.use("a_little_config.txt");
        DruidPlugin dp = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
        // 所有映射在 MappingKit 中自动化搞定
        _MappingKit.mapping(arp);
        // 与 jfinal web 环境唯一的不同是要手动调用一次相关插件的start()方法
        dp.start();
        arp.start();
        updateRating();
    }

    public static void formatUser(){
        List<Record> list = Db.find("select distinct userName from rating");
        for (Record re : list){
            User user = new User();
            user.setUserName(re.getStr("userName"));
            user.save();
        }
    }

    public static void updateRating(){
        System.out.println("开启");
        List<User> list = User.dao.find("select * from user");
        int userNum = 0;
        int ratingNum = 0;
        for (User user: list){
            ratingNum += Db.update("update rating set userId=? where userName=?", user.getId(), user.getUserName());
            if (++userNum%100==0){
                System.out.println("处理完"+userNum%100+"条用户数据");
                System.out.println("处理完"+ratingNum+"条评分数据");
            }
        }
        System.out.println("结束");
    }

    public static void loadDataToCsv(){

    }

}

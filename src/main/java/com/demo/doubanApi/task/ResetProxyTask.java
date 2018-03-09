package com.demo.doubanApi.task;

import com.jfinal.plugin.activerecord.Db;

public class ResetProxyTask implements Runnable {

    @Override
    public void run() {
        try {
            System.out.println("执行重置代理任务");
            Db.update("UPDATE proxy_add set flag=0");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

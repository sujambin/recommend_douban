package com.demo.doubanApi.task;

import com.demo.doubanApi.thread.ThreadSave;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

/**
 * 添加线程
 */
public class AddThreadTask implements Runnable {

    @Override
    public void run() {
        System.out.println("执行添加线程任务");
        try {
            int free = 0, running = 0, stop2 = 0, stop3 = 0, stop4 = 0;
            List<Record> list = Db.find("SELECT flag,count(flag) as total from book GROUP BY flag");
            for (Record re:list){
                switch (re.getInt("flag")){
                    case 0:
                        free = Integer.parseInt(re.getLong("total").toString());
                        break;
                    case 1:
                        running = Integer.parseInt(re.getLong("total").toString());
                        break;
                    case 2:
                        stop2 = Integer.parseInt(re.getLong("total").toString());
                        break;
                    case 3:
                        stop3 = Integer.parseInt(re.getLong("total").toString());
                        break;
                    case 4:
                        stop4 = Integer.parseInt(re.getLong("total").toString());
                        break;
                }
            }
            if (free>500){
                for (int i=1;i<free+1&&i<1000;i++){
                    ThreadSave threadSave = new ThreadSave();
                    new Thread(threadSave).start();
                }
            }else if (stop3>500){
                Db.update("UPDATE proxy_add set flag=0 where flag=3;");
                for (int i=1;i<1000;i++){
                    ThreadSave threadSave = new ThreadSave();
                    new Thread(threadSave).start();
                }
            }else{
                Db.update("UPDATE proxy_add set flag=0 where flag!=1;");
                for (int i=1;i<1000;i++){
                    ThreadSave threadSave = new ThreadSave();
                    new Thread(threadSave).start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("执行添加线程任务结束");
    }
}

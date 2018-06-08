package com.demo.spark;

import com.demo.common.model.AlsRating;
import com.demo.common.model._MappingKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;

public class DoubanBookALS {

    public static class Rating implements Serializable {

        private static final long serialVersionUID = 1L;
        private int userId;
        private int bookId;
        private float rating;

        public Rating() {
        }

        public Rating(int userId, int bookId, float rating) {
            this.userId = userId;
            this.bookId = bookId;
            this.rating = rating;
        }

        public int getUserId() {
            return userId;
        }

        public int getBookId() {
            return bookId;
        }

        public float getRating() {
            return rating;
        }


        public static Rating parseRating(String str) {
            String[] fields = str.split(",");
            if (fields.length != 3) {
                throw new IllegalArgumentException("Each line must contain 3 fields");
            }
            int userId = Integer.parseInt(fields[0]);
            int bookId = Integer.parseInt(fields[1]);
            float rating = Float.parseFloat(fields[2]);
            return new Rating(userId, bookId, rating);
        }
    }

    public static void main(String[] args) throws Exception{
        // 使用本地所有可用线程local[*]
        SparkSession spark = SparkSession.builder().master("local[*]").appName("JavaALSExample").getOrCreate();
        // $example on$
        JavaRDD<Rating> ratingsRDD = spark
                .read().textFile("/Users/jambin/code/data/rating.csv").javaRDD()
                .map(Rating::parseRating);
        Dataset<Row> ratings = spark.createDataFrame(ratingsRDD, Rating.class);
        // 按比例随机拆分数据
        Dataset<Row>[] splits = ratings.randomSplit(new double[] { 0.8, 0.2 },2);
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

        // 对训练数据集使用ALS算法构建建议模型
        ALS als = new ALS().setRank(6).setMaxIter(5).setRegParam(0.02).setUserCol("userId").setItemCol("bookId")
                .setRatingCol("rating").setImplicitPrefs(true).setAlpha(1.0);
        //.setImplicitPrefs(true).setAlpha(1.0)
        ALSModel model = als.fit(training);



        // 注意下面使用冷启动策略drop，确保不会有NaN评估指标
        model.setColdStartStrategy("drop");
        Dataset<Row> predictions = model.transform(test);
        // 通过计算均方根误差RMSE(Root Mean Squared Error)对测试数据集评估模型
        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("rating")
                .setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        // 打印均方根误差
        System.out.println("Root-mean-square error = " + rmse);

        model = als.fit(ratings);
        Dataset<Row> userRecs = model.recommendForAllUsers(10);


//        predictUser(userRecs, 3);
//        userRecs.show(50000);
        saveToDb(userRecs);


    }

    public static void saveToDb(Dataset<Row> userRecs){
        try {
            PropKit.use("a_little_config.txt");
            DruidPlugin dp = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
            ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
            // 所有映射在 MappingKit 中自动化搞定
            _MappingKit.mapping(arp);
            // 与 jfinal web 环境唯一的不同是要手动调用一次相关插件的start()方法
            dp.start();
            arp.start();
            userRecs.javaRDD().foreach(
                    row->
                            row.getList(1).forEach(
                                    arr->{
                                        String str = arr.toString();
                                        str = str.substring(1,str.length()-1);
                                        int userId = row.getInt(0);
//                                        System.out.println(userId+","+str);
                                        try {
                                            AlsRating alsRating = new AlsRating();
                                            alsRating.setUserId(userId);
                                            alsRating.setBookId(Integer.parseInt(str.split(",")[0]));
                                            alsRating.setRating(Double.parseDouble(str.split(",")[1]));
                                            alsRating.save();
                                        }catch (Exception x){

                                        }


                                    })
            );
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
    }

    public static void predictUser(Dataset<Row> userRecs, int userId) {
        userRecs.javaRDD().filter(row -> row.getInt(0) == userId).foreach(row -> row.getList(1).forEach(line -> System.out.println(line)));
    }
}

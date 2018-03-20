package com.demo.spark;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;

public class JavaALSExample implements Serializable{

    public static class Rating implements Serializable {

        private static final long serialVersionUID = 1L;
        private int userId;
        private int movieId;
        private float rating;
        private long timestamp;

        public Rating() {
        }

        public Rating(int userId, int movieId, float rating, long timestamp) {
            this.userId = userId;
            this.movieId = movieId;
            this.rating = rating;
            this.timestamp = timestamp;
        }

        public int getUserId() {
            return userId;
        }

        public int getMovieId() {
            return movieId;
        }

        public float getRating() {
            return rating;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public static Rating parseRating(String str) {
            String[] fields = str.split("::");
            if (fields.length != 4) {
                throw new IllegalArgumentException("Each line must contain 4 fields");
            }
            int userId = Integer.parseInt(fields[0]);
            int movieId = Integer.parseInt(fields[1]);
            float rating = Float.parseFloat(fields[2]);
            long timestamp = Long.parseLong(fields[3]);
            return new Rating(userId, movieId, rating, timestamp);
        }
    }

    public static void main(String[] args) {
        // 测试数据文件路径
        String path = "ml-100k/u.data";
        // 使用本地所有可用线程local[*]
        SparkSession spark = SparkSession.builder().master("local[*]").appName("JavaALSExample").getOrCreate();
//        JavaRDD<Rating> ratingsRDD = spark.read().textFile(path).javaRDD().map(Rating::parseRating);
        // $example on$
        JavaRDD<Rating> ratingsRDD = spark
                .read().textFile("/Users/jambin/IdeaProjects/recommend/src/main/resources/ratings.dat").javaRDD()
                .map(Rating::parseRating);
        Dataset<Row> ratings = spark.createDataFrame(ratingsRDD, Rating.class);
        // 按比例随机拆分数据
        Dataset<Row>[] splits = ratings.randomSplit(new double[] { 0.8, 0.2 },2);
        Dataset<Row> training = splits[0];
        Dataset<Row> test = splits[1];

        // 对训练数据集使用ALS算法构建建议模型
        ALS als = new ALS().setMaxIter(5).setRegParam(0.01).setUserCol("userId").setItemCol("movieId")
                .setRatingCol("rating").setRank(20).setRegParam(0.1);
        ALSModel model = als.fit(training);

        // Evaluate the model by computing the RMSE on the test data
        // 通过计算均方根误差RMSE(Root Mean Squared Error)对测试数据集评估模型
        // 注意下面使用冷启动策略drop，确保不会有NaN评估指标
        model.setColdStartStrategy("drop");
        Dataset<Row> predictions = model.transform(test);



        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("rating")
                .setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        // 打印均方根误差
        System.out.println("Root-mean-square error = " + rmse);


    // Generate top 10 movie recommendations for each user
        Dataset<Row> userRecs = model.recommendForAllUsers(10);


        String path1 = "/Users/jambin/code/data/alsResult.csv";

        try {
            BufferedWriter bw= new BufferedWriter(new FileWriter(path1));
            userRecs.javaRDD().foreach(
                    row->
                            row.getList(1).forEach(
                                    arr->{
                                        String str = arr.toString();
                                        str = str.substring(1,str.length()-1);
                                        int userId = row.getInt(0);
                                        try {
                                            bw.write(userId+","+str);
                                            bw.newLine();
//                                            Files.write(Paths.get(path1), (userId+","+str).getBytes());
                                        }catch (Exception x){

                                        }


                                    })
            );
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }

//        Row[] rows = (Row[])userRecs.collect();
//        List<GenericRowWithSchema> list = rows[0].getList(1);
//        System.out.println(list.get(0).toString());
//        GenericRowWithSchema object = list.get(0);
//        Seq<Object> seq = object.toSeq();
//        System.out.println(object.toString());
//        System.out.println(object[0]+"ddd"+object[1]);


//        for (Row row: rows){
//            Integer userID = (Integer) row.get(0);
//            if (userID==22){
//                System.out.println(list);
//            }
//            List list2 = row.getList(1);
//            System.out.println(userID);
//            System.out.println("size"+list.size());
//        }
//        userRecs.write();
//        userRecs.printSchema();

//        userRecs.select("userId").show(10);
//        userRecs.select("movieId").show(10);
//        Dataset<Row> res =  userRecs.select("recommendations");
//        Column recommendations = userRecs.col("recommendations");
//        recommendations
//        res.printSchema();
//        res.show(10);
//        res.f
//        userRecs.select("recommendations").show(10);

//        userRecs.show(20);
//         |-- userId: integer (nullable = false)
//                |-- recommendations: array (nullable = true)
//                |    |-- element: struct (containsNull = true)
//                |    |    |-- movieId: integer (nullable = true)
//                |    |    |-- rating: float (nullable = true)
//        userRecs.javaRDD().saveAsTextFile("/Users/jambin/IdeaProjects/recommend/src/main/resources/result.csv");

//        userRecs.write().save("/Users/jambin/IdeaProjects/recommend/src/main/resources/result.csv");

    }

}

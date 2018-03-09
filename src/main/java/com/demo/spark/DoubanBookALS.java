package com.demo.spark;

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

    public static void main(String[] args) {
        // 测试数据文件路径
        // 使用本地所有可用线程local[*]
        SparkSession spark = SparkSession.builder().master("local[*]").appName("JavaALSExample").getOrCreate();
//        JavaRDD<Rating> ratingsRDD = spark.read().textFile(path).javaRDD().map(Rating::parseRating);
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
        ALS als = new ALS().setMaxIter(5).setRegParam(0.01).setUserCol("userId").setItemCol("bookId")
                .setRatingCol("rating").setRank(10).setMaxIter(10).setRegParam(0.1);
        ALSModel model = als.fit(training);

        // Evaluate the model by computing the RMSE on the test data
        // 通过计算均方根误差RMSE(Root Mean Squared Error)对测试数据集评估模型
        // 注意下面使用冷启动策略drop，确保不会有NaN评估指标
        model.setColdStartStrategy("drop");
        Dataset<Row> predictions = model.transform(test);

        // 打印predictions的schema
        predictions.printSchema();

        // predictions的schema输出
        // root
        // |-- movieId: integer (nullable = false)
        // |-- rating: float (nullable = false)
        // |-- timestamp: long (nullable = false)
        // |-- userId: integer (nullable = false)
        // |-- prediction: float (nullable = true)

        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("rating")
                .setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        // 打印均方根误差
        System.out.println("Root-mean-square error = " + rmse);

    // Generate top 10 movie recommendations for each user
        Dataset<Row> userRecs = model.recommendForAllUsers(10);
//        userRecs.show(50000);

    }

}

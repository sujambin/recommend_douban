/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.demo.spark.mllib;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.recommendation.ALS;
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel;
import org.apache.spark.mllib.recommendation.Rating;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Example using MLlib ALS from Java.
 */
public final class JavaALS {

  public static MatrixFactorizationModel alsModel;

  static class ParseRating implements Function<String, Rating> {
    private static final Pattern COMMA = Pattern.compile("::");

    @Override
    public Rating call(String line) {
      String[] tok = COMMA.split(line);
      int x = Integer.parseInt(tok[0]);
      int y = Integer.parseInt(tok[1]);
      double rating = Double.parseDouble(tok[2]);
      return new Rating(x, y, rating);
    }
  }

  static class ParseRating2 implements Function<String, Rating> {
    private static final Pattern COMMA = Pattern.compile(",");

    @Override
    public Rating call(String line) {
      String[] tok = COMMA.split(line);
      int y = Integer.parseInt(tok[1]);
      int x = Integer.parseInt(tok[0]);
      double rating = Double.parseDouble(tok[2]);
      return new Rating(x, y, rating);
    }
  }

  static class FeaturesToString implements Function<Tuple2<Object, double[]>, String> {
    @Override
    public String call(Tuple2<Object, double[]> element) {
      return element._1() + "," + Arrays.toString(element._2());
    }
  }

  public static void main(String[] args) {
    args = new String[]{"/Users/jambin/IdeaProjects/recommend/src/main/resources/ratings.dat",
                          "10", "10", "/Users/jambin/IdeaProjects/recommend/src/main/resources/result.csv"};

    if (args.length < 4) {
      System.err.println(
        "Usage: JavaALS <ratings_file> <rank> <iterations> <output_dir> [<blocks>]");
      System.exit(1);
    }
    SparkConf sparkConf = new SparkConf().setAppName("JavaALS").setMaster("local[*]");
    int rank = Integer.parseInt(args[1]);
    int iterations = Integer.parseInt(args[2]);
    String outputDir = args[3];
    int blocks = -1;
    if (args.length == 5) {
      blocks = Integer.parseInt(args[4]);
    }

    JavaSparkContext sc = new JavaSparkContext(sparkConf);
    JavaRDD<String> lines = sc.textFile(args[0]);

    JavaRDD<Rating> ratings = lines.map(new ParseRating());

    MatrixFactorizationModel model = ALS.train(ratings.rdd(), rank, iterations, 0.01, blocks);

    Rating[] ratings_2= model.recommendProducts(2, 10);
    System.out.println(ratings_2.length);
    alsModel = model;

//    model.userFeatures().toJavaRDD().map(new FeaturesToString()).saveAsTextFile(
//        outputDir + "/userFeatures");
//    model.productFeatures().toJavaRDD().map(new FeaturesToString()).saveAsTextFile(
//        outputDir + "/productFeatures");
    System.out.println("Final user/product features written to " + outputDir);

//    sc.stop();
  }

  public static List<String[]> getPred(int userId, int num){

    if(alsModel==null){
//      main(null);
      initAlsModel();
    }
    Rating[] ratings = alsModel.recommendProducts(userId, num);
    List<String[]> list = new ArrayList<>();
    for (Rating rating :ratings){
      list.add(new String[]{rating.product()+"",  String .format("%.2f", rating.rating())});
    }
    return list;
  }

  public static void pred2(){
//    // 封装rating的参数形式，user为0，product为电影id进行封装
//    JavaPairRDD<Integer, Integer> recommondList = JavaPairRDD.fromJavaRDD(movieIdList.map(new Function<Tuple2<Integer,String>, Tuple2<Integer,Integer>>() {
//
//      @Override
//      public Tuple2<Integer, Integer> call(Tuple2<Integer, String> v1) throws Exception {
//        return new Tuple2<Integer, Integer>(0, v1._1);
//      }
//    }));
//
//    //通过模型预测出user为0的各product(电影id)的评分，并按照评分进行排序，获取前10个电影id
//    final List<Integer> list = alsModel.predict(recommondList).sortBy(new Function<Rating, Double>() {
//
//      @Override
//      public Double call(Rating v1) throws Exception {
//        return v1.rating();
//      }
//    }, false, 1).map(new Function<Rating, Integer>() {
//
//      @Override
//      public Integer call(Rating v1) throws Exception {
//        return v1.product();
//      }
//    }).take(10);
  }

  public static void initAlsModel(){
    System.out.println("准备初始化model");
    SparkConf sparkConf = new SparkConf().setAppName("JavaALS").setMaster("local[*]");
    int rank = 8;
    int iterations = 20;
    int blocks = -1;
    String path = "/Users/jambin/code/data/rating.csv";

    JavaSparkContext sc = new JavaSparkContext(sparkConf);
    JavaRDD<String> lines = sc.textFile(path);

    JavaRDD<Rating> ratings = lines.map(new ParseRating2());

   alsModel = ALS.train(ratings.rdd(), rank, iterations, 0.1);




  }
}

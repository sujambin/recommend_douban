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

    if (args.length < 4) {
      args = new String[]{"/Users/jambin/code/data/rating.csv",
              "10", "10", "/Users/jambin/IdeaProjects/recommend/src/main/resources/result.csv"};
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
    MatrixFactorizationModel model = ALS.train(ratings.rdd(), rank, iterations, 0.2, blocks);

    Rating[] ratings_2= model.recommendProducts(2, 10);

//    model.userFeatures().toJavaRDD().map(new FeaturesToString()).saveAsTextFile(
//        outputDir + "/userFeatures");
//    model.productFeatures().toJavaRDD().map(new FeaturesToString()).saveAsTextFile(
//        outputDir + "/productFeatures");
    System.out.println("Final user/product features written to " + outputDir);
    recommendForAllUsers(model, ratings);
    sc.stop();
  }

  public static void recommendForAllUsers(MatrixFactorizationModel model, JavaRDD<Rating> ratings){
    ratings.map(rating -> rating.user()).distinct().foreach(id-> {
        model.recommendProducts(id, 10);
            }
    );

    System.out.println(ratings.collect().size());
    System.out.println(ratings.map(rating -> rating.user()).distinct().collect().size());

//    ratings.map(row ->row.user()).distinct()

  }


  public static List<String[]> getPred(int userId, int num){
    Rating[] ratings = alsModel.recommendProducts(userId, num);
    List<String[]> list = new ArrayList<>();
    for (Rating rating :ratings){
      list.add(new String[]{rating.product()+"",  String .format("%.2f", rating.rating())});
    }
    return list;
  }


}

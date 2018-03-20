import java.io.File

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD

import scala.util.Random

object DoubanAls {
  //1. Define a rating elicitation function

  //2. Define a RMSE computation function
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating]) = {
    val prediction = model.predict(data.map(x=>(x.user, x.product)))
    val predDataJoined = prediction.map(x=> ((x.user,x.product),x.rating)).join(data.map(x=> ((x.user,x.product),x.rating))).values
    new RegressionMetrics(predDataJoined).rootMeanSquaredError
  }

  //3. Main
  def main(args: Array[String]) {
    //3.1 Setup env
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)


    val conf = new SparkConf().setAppName("MovieLensALS")
      .set("spark.executor.memory","8000m").setMaster("local[*]")
    val sc = new SparkContext(conf)

    //3.2 Load ratings data and know your data
    val movieLensHomeDir="/Users/jambin/code/data/"

    val ratings = sc.textFile(new File(movieLensHomeDir, "traindata.csv").toString).map {line =>
      val fields = line.split(",")
      //timestamp, user, product, rating
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }

    val numRatings = ratings.count()
    val numUser = ratings.map(x=>x.user).distinct().count()
    val numMovie = ratings.map(_.product).distinct().count()

    println("Got "+numRatings+" ratings from "+numUser+" users on "+numMovie+" movies.")

    //3.3 Elicitate personal rating

    //3.4 Split data into train(60%), validation(20%) and test(20%)
    val numPartitions = 10
    val trainSet = ratings.repartition(numPartitions).persist()

    val testSet = sc.textFile(new File(movieLensHomeDir, "testdata.csv").toString).map {line =>
      val fields = line.split(",")
      //user, product, rating
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.persist()

    val numTrain = trainSet.count()
    val numTest = testSet.count()

    println("Training data: "+numTrain+" Test data: "+numTest)

    //3.5 Train model and optimize model with validation set
    val numRanks = List(10)
    val numIters = List(20)
    val numLambdas = List(1)
    var bestRmse = Double.MaxValue
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestRanks = -1
    var bestIters = 0
    var bestLambdas = -1.0
    for(rank <- numRanks; iter <- numIters; lambda <- numLambdas){
      val model = ALS.train(trainSet, rank, iter, lambda)
      val validationRmse = computeRmse(model, testSet)
      println("RMSE(validation) = "+validationRmse+" with ranks="+rank+", iter="+iter+", Lambda="+lambda)

      if (validationRmse < bestRmse) {
        bestModel = Some(model)
        bestRmse = validationRmse
        bestIters = iter
        bestLambdas = lambda
        bestRanks = rank
      }
    }

    //3.6 Evaluate model on test set
    val testRmse = computeRmse(bestModel.get, testSet)
    println("The best model was trained with rank="+bestRanks+", Iter="+bestIters+", Lambda="+bestLambdas+
      " and compute RMSE on test is "+testRmse)

    //3.7 Create a baseline and compare it with best model
    val meanRating = trainSet.map(_.rating).mean()
    val bestlineRmse = new RegressionMetrics(testSet.map(x=>(x.rating, meanRating))).rootMeanSquaredError
    val improvement = (bestlineRmse - testRmse)/bestlineRmse*100
    println("The best model improves the baseline by "+"%1.2f".format(improvement)+"%.")

    println(bestModel.get.userFeatures.first()._2.foreach(d=>println(d)));

    //3.8 Make a personal recommendation
    val movies = ratings.union(testSet).map(line=>line.product).distinct();
    val myMovies = ratings.union(testSet).filter(_.user==999999).map(line=>line.product).distinct();

//    val moviesId = myRatings.map(_.product)
//    val candidates = sc.parallelize(movies.filter(!myMovies.contains(_)).toSeq)
    val recommendations = bestModel.get
      .predict(movies.map(x=>(999999, x)))
      .sortBy(-_.rating)
      .take(50)

    var i = 0
    println("Movies recommended for you:")
    recommendations.foreach{ line=>
      println("%2d".format(i)+" :"+line.product)
      i += 1
    }
    sc.stop()
  }
}

import java.io.File

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.jblas.DoubleMatrix

import scala.util.Random

object BookAls {

  // 定义一个RMSE计算函数
  def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating]) = {
    val prediction = model.predict(data.map(x=>(x.user, x.product)))
    val predDataJoined = prediction.map(x=> ((x.user,x.product),x.rating)).join(data.map(x=> ((x.user,x.product),x.rating))).values
    new RegressionMetrics(predDataJoined).rootMeanSquaredError
  }

  def main(args: Array[String]) {
    //1.初始化环境
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    val conf = new SparkConf().setAppName("BookALS")
      .set("spark.executor.memory","8000m").setMaster("local[*]")
    val sc = new SparkContext(conf)

    //3.2 加载评分数据集
    val ratings = sc.textFile(new File("/Users/jambin/code/data/traindata.csv").toString).map {line =>
      val fields = line.split(",")
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }


    //3.4 Split data into train(60%), validation(20%) and test(20%)
    val numPartitions = 10
    val trainSet = ratings.repartition(numPartitions).persist()

    val testSet = sc.textFile(new File("/Users/jambin/code/data/testdata.csv").toString).map {line =>
      val fields = line.split(",")
      //user, product, rating
      Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble)
    }.persist()

    val numTrain = trainSet.count()
    val numTest = testSet.count()

    println("Training data: "+numTrain+" Test data: "+numTest)

    //3.5 Train model and optimize model with validation set
    val numRanks = List(8)
    val numIters = List(15)
    val numLambdas = List(1)
    var bestRmse = Double.MaxValue
    var bestModel: Option[MatrixFactorizationModel] = None
    var bestRanks = -1
    var bestIters = 0
    var bestLambdas = -1.0
    for(rank <- numRanks; iter <- numIters; lambda <- numLambdas){
//      val model = ALS.train(trainSet, rank, iter, lambda)

      val model = ALS.trainImplicit(trainSet, rank, iter, lambda, 1.5)

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


    println(bestModel.get.userFeatures.first()._2.foreach(d=>println(d)));

    //3.8 Make a personal recommendation
    val movies = ratings.union(testSet).map(line=>line.product).distinct();
    val myMovies = ratings.union(testSet).filter(_.user==999999).map(line=>line.product).distinct();



//    val moviesId = myRatings.map(_.product)
//    val candidates = sc.parallelize(movies.filter(!myMovies.contains(_)).toSeq)

//    val recommendations = bestModel.get
//      .predict(movies.map(x=>(999999, x)))
//      .sortBy(-_.rating)
//      .take(50)
//
//    var i = 0
//    println("Movies recommended for you:")
//    recommendations.foreach{ line=>
//      println("%2d".format(i)+" :"+line.product)
//      i += 1
//    }

//    alsImplict(ratings, bestModel)

    itemSim(bestModel, 1756093, 50 )

    sc.stop()
  }

  def alsImplict(ratings: RDD[Rating], bestModel:Option[MatrixFactorizationModel]) {
    // Evaluate the model on rating data
    val usersProducts = ratings.map { case Rating(user, product, rate) =>
      (user, product)
    }
    val predictions =
      bestModel.get.predict(usersProducts).map { case Rating(user, product, rate) =>
        ((user, product), rate)
      }
    val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
      ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) =>
      val err = (r1 - r2)
      err * err
    }.mean()
    println("Mean Squared Error = " + MSE)


    /**
      * recommend
      */
    val rs =bestModel.get.recommendProducts(3, 50)
    rs.foreach(rating => println(rating.product+","+rating.rating))
  }

  /* Compute the cosine similarity between two vectors */
  def cosineSimilarity(vec1: DoubleMatrix, vec2: DoubleMatrix): Double = {
    vec1.dot(vec2) / (vec1.norm2() * vec2.norm2())
  }
  def itemSim(model:Option[MatrixFactorizationModel],itemId:Int, K:Int): Unit ={
    val itemFactor = model.get.productFeatures.lookup(itemId).head
    //itemFactor: Array[Double] = Array(0.3660752773284912, 0.43573060631752014, -0.3421429991722107, 0.44382765889167786, -1.4875195026397705, 0.6274569630622864, -0.3264533579349518, -0.9939845204353333, -0.8710321187973022, -0.7578890323638916, -0.14621856808662415, -0.7254264950752258)
    val itemVector = new DoubleMatrix(itemFactor)
    //itemVector: org.jblas.DoubleMatrix = [0.366075; 0.435731; -0.342143; 0.443828; -1.487520; 0.627457; -0.326453; -0.993985; -0.871032; -0.757889; -0.146219; -0.725426]

    cosineSimilarity(itemVector, itemVector)
    // res99: Double = 0.9999999999999999
    val sims = model.get.productFeatures.map{ case (id, factor) =>
      val factorVector = new DoubleMatrix(factor)
      val sim = cosineSimilarity(factorVector, itemVector)
      (id, sim)
    }
    val sortedSims = sims.top(K)(Ordering.by[(Int, Double), Double] { case (id, similarity) => similarity })
    //sortedSims: Array[(Int, Double)] = Array((2055,0.9999999999999999), (2051,0.9138311231145874), (3520,0.8739823400539756), (2190,0.8718466671129721), (2050,0.8612639515847019), (1011,0.8466911667526461), (2903,0.8455764332511272), (3121,0.8227325520485377), (3674,0.8075743004357392), (2016,0.8063817280259447))
    println(sortedSims.mkString("\n"))

    val sortedSims2 = sims.top(K + 1)(Ordering.by[(Int, Double), Double] { case (id, similarity) => similarity })
    //sortedSims2: Array[(Int, Double)] = Array((2055,0.9999999999999999), (2051,0.9138311231145874), (3520,0.8739823400539756), (2190,0.8718466671129721), (2050,0.8612639515847019), (1011,0.8466911667526461), (2903,0.8455764332511272), (3121,0.8227325520485377), (3674,0.8075743004357392), (2016,0.8063817280259447), (3672,0.8016276723120674))

    sortedSims2.slice(1, 11).map{ case (id, sim) => (id, sim) }.mkString("\n")
  }
}


import org.apache.log4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.util.MLWritable
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}


object LogisticRegressionModel {

  def main(args: Array[String]) {

    val (sc: SparkContext, spark: SparkSession) = Utility.getOrCreateSparkSession

    val log: Logger = Utility.getLogger

    import spark.implicits._

    val updatedDataFrame: Dataset[Row] = Utility.readUpdatedData(spark)

    val (caseStatusIndexer: StringIndexer, jobTitleIndexer: StringIndexer, worksiteIndexer: StringIndexer, fullTimeBinarizer: StringIndexer) = Utility.createIndexers

    val output: DataFrame = Utility.transformedDataFrame(updatedDataFrame, caseStatusIndexer, jobTitleIndexer, worksiteIndexer, fullTimeBinarizer)

    val out = output.select("CaseIndexer", "features")

    val labelIndexer = new StringIndexer().setInputCol("CaseIndexer").setOutputCol("label")
    val temp_df = labelIndexer.fit(out).transform(out)

    val Array(trainingData, testData) = temp_df.randomSplit(Array(0.7, 0.3))

    val startTime = System.nanoTime();

    val rf = new LogisticRegression().setMaxIter(1000)

    val model1 = rf.fit(trainingData)

    model1.asInstanceOf[MLWritable].write.save(Constants.SAVE_MODEL_PATH)

    val predictions = model1.transform(testData)

    val predictedDataFrame = predictions.select("label", "prediction")
    val rdd = predictedDataFrame.as[(Double, Double)].rdd
    val metrics = new MulticlassMetrics(rdd)
    val confusionMatrix = sc.parallelize(metrics.confusionMatrix.toArray)
    confusionMatrix.repartition(2).saveAsTextFile(Constants.SAVE_CONFUSION_METRICS)

    log.error("ACCURACY: " + metrics.accuracy)
    log.error("PRECISION: " + metrics.weightedPrecision)
    log.error("RECALL: " + metrics.weightedRecall)
    log.error("Total time taken: " + (System.nanoTime() - startTime) / 1e9d + " seconds")

    spark.close()
    sc.stop()

  }


}


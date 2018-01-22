import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}

object Utility {

  def getOrCreateSparkSession = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("H1B")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate
    (sc, spark)
  }

  def readUpdatedData(spark: SparkSession) = {
    val reader1 = spark.read.option("delimiter", ",").option("inferSchema", "true").option("mode", "DROPMALFORMED")
    reader1.csv(Constants.FILTERED_DATA_FILE_PATH)
  }


  def transformedDataFrame(names: Dataset[Row], caseStatusIndexer: StringIndexer, jobTitleIndexer: StringIndexer, worksiteIndexer: StringIndexer, fullTimeBinarizer: StringIndexer) = {
    val pipeline = new Pipeline().setStages(Array(caseStatusIndexer, jobTitleIndexer, worksiteIndexer, fullTimeBinarizer))
    val df = pipeline.fit(names).transform(names)
    val assembler = new VectorAssembler().setInputCols(Array("JobTitleIndexer", "WorksiteIndexer", "FulltimePosition_Binarized")).setOutputCol("features")
    assembler.transform(df)
  }

  def createIndexers = {
    val caseStatusIndexer = new StringIndexer().setInputCol("_c1").setOutputCol("CaseIndexer")
    val jobTitleIndexer = new StringIndexer().setInputCol("_c4").setOutputCol("JobTitleIndexer")
    val worksiteIndexer = new StringIndexer().setInputCol("_c8").setOutputCol("WorksiteIndexer")
    val fullTimeBinarizer = new StringIndexer().setInputCol("_c5").setOutputCol("FulltimePosition_Binarized")
    (caseStatusIndexer, jobTitleIndexer, worksiteIndexer, fullTimeBinarizer)
  }

  def getLogger = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    LogManager.getRootLogger
  }

}

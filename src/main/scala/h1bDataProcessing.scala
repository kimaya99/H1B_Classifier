
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

object h1bDataProcessing {

  def main(args: Array[String]) {

    createSchema

    val (sc: SparkContext, spark: SparkSession) = Utility.getOrCreateSparkSession

    saveFilteredDataToFile(spark, Constants.H1B_FILE_PATH)

    spark.close()
    sc.stop()

  }

  protected def createSchema = {
    val schema = new StructType(Array(
      new StructField("Case_No", StringType, false),
      new StructField("Case_Status", StringType, false),
      new StructField("Employer_Name", StringType, true),
      new StructField("Soc_Name", StringType, true),
      new StructField("Job_Title", StringType, true),
      new StructField("Full_Time_Position", StringType, true),
      new StructField("Prevailing_Wage", LongType, true),
      new StructField("Year", IntegerType, true),
      new StructField("Worksite", StringType, true),
      new StructField("Longitude", LongType, true),
      new StructField("Latitude", LongType, true)
    ))
  }

  private def saveFilteredDataToFile(spark: SparkSession, filePath: String) = {
    val reader = spark.read.option("header", true).option("delimiter", ",").option("inferSchema", "true")
    val dataFrame = reader.csv(filePath)
    val cleanedData = dataFrame.na.fill("NA", Seq("CASE_STATUS", "JOB_TITLE", "WORKSITE", "FULL_TIME_POSITION"))
    val filteredData = cleanedData.filter(cleanedData("JOB_TITLE").contains("SOFTWARE"))
    filteredData.coalesce(1).write.format("csv").save(Constants.FILTERED_DATA_FILE_PATH)
  }

}

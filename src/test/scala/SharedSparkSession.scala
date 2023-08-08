import org.apache.spark.sql.SparkSession
import org.scalatest.{BeforeAndAfterAll,Suite}

trait SharedSparkSession extends BeforeAndAfterAll {

  self: Suite =>
  System.setProperty("hadoop.home.dir","C://")
  val spark: SparkSession = SparkSession
    .builder()
    .appName("SharedSessionForTesting")
    .master(s"local[1]")
    .getOrCreate()
}

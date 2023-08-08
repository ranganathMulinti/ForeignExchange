import com.example.FxOrdersMatchingEngine
import org.apache.spark.sql.DataFrame
import org.scalatest.FlatSpec

class FxOrdersMatchingEngineTest extends FlatSpec with SharedSparkSession {

  val buyData: Seq[(String, String, Int, String, Int, Int)] = Seq(("1","Ben",1623239771,"BUY",8,148),
    ("2","Sarah",1623239774,"BUY",72,5039),
    ("3","Alicia",1623239783,"BUY",89,8771),
    ("4","Jasmine",1623239772,"BUY",72,5239),
    ("5","Bob",1623239785,"BUY",89,8771))

  val sellData: Seq[(String, String, Int, String, Int, Int)] = Seq(("6", "Steve", 1623239775, "SELL", 72, 1550),
    ("7", "Steve", 1623239776, "SELL", 24, 6612),
    ("8", "Kim", 1623239773, "SELL", 98, 435),
    ("9", "Jennifer", 1623239778, "SELL", 29, 204),
    ("10", "James", 1623239781, "SELL", 89, 4352))

  val columns: Seq[String] = Seq("orderId","userName","orderTime","orderType","quantity","price")

  val buyOrders: DataFrame =  spark.createDataFrame(buyData).toDF(columns:_*)
  val sellOrders: DataFrame = spark.createDataFrame(sellData).toDF(columns:_*)

  val targetDF: DataFrame = FxOrdersMatchingEngine.matchOrders(buyOrders,sellOrders)

  "Target DataFrame" should "have correct rows and columns" in {

    val expectedNoOfRows = 2
    val expectedNumberOfColumns = 10
    val expectedColumns = List(
      "buy_orderId",
      "buy_userName",
      "buy_orderTime",
      "buy_quantity",
      "buy_price",
      "sell_orderId",
      "sell_userName",
      "sell_orderTime",
      "sell_quantity",
      "sell_price"
    )

    assertResult(expectedNoOfRows){
      targetDF.count()
    }

    assertResult(expectedNumberOfColumns){
      targetDF.columns.length
    }

    for (column <- targetDF.columns)
      assert(expectedColumns contains column)

  }
}

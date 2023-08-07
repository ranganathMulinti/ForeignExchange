package com.example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object FxOrdersMatchingEngine {

  def main(args : Array[String]): Unit = {

    // creating spark variable
    val spark = SparkSession.builder().master("local[*]").appName("FXOrdersMatchingEngine").getOrCreate()

    import spark.implicits

    // creating schema to read orders from csv file
    val ordersSchema = StructType(
      Array(
        StructField("orderId", StringType, false),
        StructField("userName", StringType, false),
        StructField("orderTime", LongType, false),
        StructField("orderType", StringType, false),
        StructField("quantity", DoubleType, false),
        StructField("price", DoubleType, false)
      )
    )

    // get the resource file path
    val resourcePath = getClass.getResource(s"/exampleOrders.csv")
    val filePath = resourcePath.getPath

    // read the exampleOrders.csv file and create ordersDF dataframe
    val ordersDF = spark.read.option("header", "false").schema(ordersSchema).csv(filePath)

    // separate BUY and SELL orders
    val buyOrdersDF = ordersDF.filter(col("orderType") === "BUY")
    val sellOrdersDF = ordersDF.filter(col("orderType") === "SELL")

    val orderEncoder = org.apache.spark.sql.Encoders.product[Order]

    val buyOrders = buyOrdersDF.as[Order](orderEncoder)
    val sellOrders = sellOrdersDF.as[Order](orderEncoder)

    // orders matching based on quantity and best price and first come basis
    val matchedOrders = buyOrders.joinWith(sellOrders,buyOrders("quantity") === sellOrders("quantity"),"inner")


  }

}

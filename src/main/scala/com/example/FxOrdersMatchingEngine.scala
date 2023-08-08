package com.example

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, _}
import org.apache.spark.sql.types._

object FxOrdersMatchingEngine {

  def main(args: Array[String]): Unit = {

    // creating spark variable, make sure to add master if you are running the application locally
    val spark = SparkSession.builder().appName("FXOrdersMatchingEngine").getOrCreate()

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

    val targetDF = matchOrders(buyOrdersDF,sellOrdersDF)
    sinkWriter(targetDF)

  }

  def matchOrders(buyOrders: DataFrame, sellOrders: DataFrame): DataFrame = {

    // join BUY and SELL orders on quantity and find the best price
    val matchedOrdersDF = buyOrders.as("buy")
      .join(sellOrders.as("sell"), col("buy.quantity") === col("sell.quantity"), "inner")
      .filter(col("buy.price") >= col("sell.price"))
      .withColumn("firstOrder", row_number().over(Window.partitionBy("buy.quantity").orderBy("buy.orderTime")))
      .filter(col("firstOrder") === 1)
      .select(
        col("buy.orderId").alias("buy_orderId"),
        col("buy.userName").alias("buy_userName"),
        col("buy.orderTime").alias("buy_orderTime"),
        col("buy.quantity").alias("buy_quantity"),
        col("buy.price").alias("buy_price"),
        col("sell.orderId").alias("sell_orderId"),
        col("sell.userName").alias("sell_userName"),
        col("sell.orderTime").alias("sell_orderTime"),
        col("sell.quantity").alias("sell_quantity"),
        col("sell.price").alias("sell_price")
      )
    matchedOrdersDF

  }

  def sinkWriter(targetDF: DataFrame): Unit = {

    // write matched orders to a output csv file, make sure to change the path to desired location
    targetDF.repartition(1).write.mode("overwrite").csv("path")
  }

}

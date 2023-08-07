package com.example

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object FxOrdersMatchingEngine extends App{

  // creating spark variable
  val spark = SparkSession.builder().appName("FXOrdersMatchingEngine").getOrCreate()

  // creating schema to read orders from csv file
  val ordersSchema = StructType(
    Array(
      StructField("orderId",StringType,false),
      StructField("userName",StringType,false),
      StructField("orderTime",LongType,false),
      StructField("orderType",StringType,false),
      StructField("quantity",DoubleType,false),
      StructField("price",DoubleType,false)
    )
  )

  // get the resource file path
  val resourcePath = getClass.getResource("exampleOrders.csv")
  val filePath = resourcePath.getPath

  // read the exampleOrders.csv file and create ordersDF dataframe
  val ordersDF = spark.read.option("header","false").schema(ordersSchema).csv(filePath)

  // separate BUY and SELL orders
  val buyOrdersDF = ordersDF.filter(col("orderType") === "BUY")
  val sellOrdersDF = ordersDF.filter(col("orderType") === "SELL")

  // join BUY and SELL orders on quantity and find the best price
  val matchedOrdersDF = buyOrdersDF.as("buy")
    .join(sellOrdersDF.as("sell"),Seq("quantity"))
    .where(col("buy.price") === min(col("buy.price")).over(Window.partitionBy("buy.quantity")))
    .where(col("sell.price") === max(col("sell.price")).over(Window.partitionBy("sell.quantity")))
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


}

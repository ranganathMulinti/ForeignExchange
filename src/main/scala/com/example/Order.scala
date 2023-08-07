package com.example

case class Order(
                  orderId: String,
                  userName: String,
                  orderTime: Long,
                  orderType: String,
                  quantity: Double,
                  price: Double
                )
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val date: String,
    val month: String,
    val productId: String,
    val model: String,
    val variant: String,
    val dpAtSale: Double,
    val sellingPrice: Double,
    val cashback: Double,
    val profit: Double,
    val memoNo: String,
    val customerName: String?,
    val customerPhone: String?,
    val imei: String?
)

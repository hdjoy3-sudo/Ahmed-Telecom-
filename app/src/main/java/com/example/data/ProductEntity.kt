package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val model: String,
    val variant: String,
    val dp: Double,
    val mrp: Double,
    val stock: Int
)

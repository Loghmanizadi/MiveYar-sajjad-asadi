package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., میوه، صیفی‌جات، سبزیجات، مرکبات، تره‌بار، خشکبار
    val unit: String = "کیلوگرم", // کیلوگرم، عدد، جعبه، بسته
    val salePrice: Long, // Toman per unit
    val referenceBuyPrice: Long = 0, // Toman reference cost
    val minStockAlert: Double = 15.0, // Warning threshold in unit (kg)
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

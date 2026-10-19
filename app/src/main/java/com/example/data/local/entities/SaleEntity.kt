package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [Index("shamsiDate"), Index("branchName")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shamsiDate: String, // e.g., "1405/07/26"
    val timestamp: Long = System.currentTimeMillis(),
    val branchName: String = "شعبه اصلی",
    val totalAmount: Long, // Toman
    val paymentMethod: String = "کارتخوان ملت", // نقدی، کارتخوان ملت، کارتخوان پارسیان، کارت به کارت، سایر
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Double, // Weight in kg or count
    val unit: String = "کیلوگرم",
    val unitPrice: Long, // Toman at time of sale
    val totalPrice: Long, // quantity * unitPrice or direct non-weighted amount
    val isWeighted: Boolean = true // true: فروش وزنی, false: فروش غیروزنی (مبلغی/بسته‌ای بدون کسر وزن از موجودی)
)

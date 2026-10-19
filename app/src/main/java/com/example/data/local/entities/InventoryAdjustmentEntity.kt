package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_adjustments",
    indices = [Index("productId"), Index("shamsiDate")]
)
data class InventoryAdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val shamsiDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quantity: Double, // Positive = addition / opening balance, Negative = wastage / spoilage / shrinkage / lost weight
    val reason: String, // "ضایعات و پلاسیدگی", "افت رطوبت و وزن", "خرابی بار", "موجودی اولیه افتتاحیه", "اصلاح شمارش"
    val branchName: String = "شعبه اصلی",
    val notes: String = ""
)

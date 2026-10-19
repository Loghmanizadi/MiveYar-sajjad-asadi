package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [Index("shamsiDate"), Index("supplierId"), Index("productId")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shamsiDate: String, // e.g. "1405/07/26"
    val timestamp: Long = System.currentTimeMillis(),
    val supplierId: Long,
    val supplierName: String,
    val productId: Long,
    val productName: String,
    val marketLocation: String = "میدان تره‌بار مرکزی کرج",
    val phase: String = "فاز ۱",
    val weight: Double, // Weight in kg
    val packageCount: Int = 0, // Number of boxes/crates/baskets
    val buyPricePerKg: Long, // Toman per kg
    val baseCost: Long, // weight * buyPricePerKg
    val allocatedBranch: String = "شعبه اصلی", // کاج، ماهان، مشترک
    val dashtPerPackage: Long = 0L, // هزینه دشت به ازای هر جعبه
    val totalDashtCost: Long = 0L,
    val transportCost: Long = 0L, // کرایه نیسان یا حمل
    val unloadingCost: Long = 0L, // هزینه تخلیه بار
    val commissionCost: Long = 0L, // حق‌العمل / کمیسیون
    val miscCost: Long = 0L, // متفرقه
    val totalCost: Long, // All item costs included
    val effectiveCostPerKg: Long, // totalCost / weight
    val paidAmount: Long = 0L, // Paid at purchase time
    val remainingDebt: Long = 0L, // added to supplier balance
    val paymentMethod: String = "نقدی",
    val notes: String = ""
)

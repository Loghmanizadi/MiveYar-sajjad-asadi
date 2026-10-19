package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // e.g., حاج علی رضایی - حجره ۴۵
    val phone: String = "",
    val location: String = "میدان مرکزی کرج",
    val initialDebt: Long = 0L, // Positive = our debt to supplier, Negative = supplier owes us
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "supplier_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId"), Index("shamsiDate")]
)
data class SupplierTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val shamsiDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "PURCHASE_DEBT" (بدهی بابت خرید بار), "PAYMENT" (پرداخت وجه به فروشنده), "ADJUSTMENT" (تعدیل/اصلاح حساب)
    val amount: Long, // Toman
    val paymentMethod: String = "انتقال بانکی", // نقدی، انتقال بانکی (پایا/ساتنا)، چک، کارتخوان
    val relatedPurchaseId: Long? = null,
    val notes: String = ""
)

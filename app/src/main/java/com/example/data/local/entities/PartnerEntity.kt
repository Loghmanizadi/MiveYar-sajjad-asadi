package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "partners")
data class PartnerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val sharePercentage: Double = 0.0, // سهم شراکت به درصد
    val initialBalance: Long = 0L, // سرمایه یا طلب/بدهی اولیه به تومان
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "partner_transactions",
    foreignKeys = [
        ForeignKey(
            entity = PartnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["partnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("partnerId"), Index("shamsiDate")]
)
data class PartnerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partnerId: Long,
    val shamsiDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "WITHDRAWAL" (برداشت سود/سرمایه), "DEPOSIT" (واریز سرمایه به مغازه), "SETTLEMENT" (تسویه سود دوره)
    val amount: Long, // Toman
    val paymentMethod: String = "نقدی",
    val notes: String = ""
)

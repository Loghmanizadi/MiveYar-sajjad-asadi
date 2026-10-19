package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_receipts",
    indices = [
        Index("shamsiDate"),
        Index(value = ["shamsiDate", "branchName"])
    ]
)
data class DailyReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shamsiDate: String, // e.g. "1405/07/26"
    val branchName: String = "شعبه اصلی",
    val posMellat: Long = 0L, // کارتخوان ملت (برای سازگاری داده‌های قبلی)
    val posParsian: Long = 0L, // کارتخوان پارسیان (برای سازگاری داده‌های قبلی)
    val posTotal: Long = 0L, // مجموع مبالغ کارت‌خوان‌های پویا
    val cash: Long = 0L, // وجه نقد
    val cardToCard: Long = 0L, // کارت به کارت
    val other: Long = 0L, // سایر دریافتی‌ها
    val notes: String = "",
    val isSettled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalPosReceipts: Long
        get() = if (posTotal > 0L) posTotal else (posMellat + posParsian)

    val totalActualReceipts: Long
        get() = totalPosReceipts + cash + cardToCard + other
}

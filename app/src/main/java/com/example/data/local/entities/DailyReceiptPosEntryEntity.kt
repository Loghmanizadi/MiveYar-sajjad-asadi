package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_receipt_pos_entries",
    indices = [
        Index("receiptId"),
        Index("shamsiDate"),
        Index(value = ["shamsiDate", "branchName"])
    ]
)
data class DailyReceiptPosEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptId: Long = 0L,
    val shamsiDate: String, // e.g. "1405/07/26"
    val branchName: String, // e.g. "شعبه کاج"
    val terminalId: Long,
    val terminalName: String, // نام کارتخوان در لحظه ثبت برای حفظ سوابق در صورت تغییر نام یا غیرفعال‌شدن
    val amount: Long = 0L // مبلغ دریافتی به تومان
)

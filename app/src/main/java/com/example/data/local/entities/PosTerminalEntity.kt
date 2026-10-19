package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_terminals")
data class PosTerminalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // نام کارتخوان (مثلاً: کارتخوان ملت ۱، پوز پاسارگاد، پوز سامان)
    val bankName: String = "", // نام بانک (مثلاً: ملت، پارسیان، سامان، ملی)
    val terminalNumber: String = "", // شماره پایانه یا شماره سریال
    val isActive: Boolean = true, // فعال یا غیرفعال (غیرفعال کردن بدون حذف سوابق تاریخی)
    val createdAt: Long = System.currentTimeMillis()
)

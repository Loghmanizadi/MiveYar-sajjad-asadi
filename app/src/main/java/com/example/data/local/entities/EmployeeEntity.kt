package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val weeklyWage: Long, // Toman per week
    val startDateShamsi: String = "",
    val isActive: Boolean = true,
    val notes: String = ""
)

@Entity(
    tableName = "employee_attendance",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId"), Index("shamsiDate")]
)
data class EmployeeAttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val shamsiDate: String, // e.g. "1405/07/26"
    val status: String, // "PRESENT" (حاضر), "ABSENT" (غایب), "LEAVE" (مرخصی), "HOLIDAY" (تعطیل), "HALF_DAY" (نیمه‌وقت)
    val notes: String = ""
)

@Entity(
    tableName = "employee_payments",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId"), Index("shamsiDate")]
)
data class EmployeePaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val shamsiDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "ADVANCE" (مساعده), "DEDUCTION" (کسورات/جریمه), "BONUS" (پاداش), "SALARY_SETTLEMENT" (تسویه حقوق هفتگی)
    val amount: Long, // Toman
    val forWeekShamsi: String = "", // e.g. "هفته ۴ مهر ۱۴۰۵"
    val paymentMethod: String = "نقدی",
    val notes: String = ""
)

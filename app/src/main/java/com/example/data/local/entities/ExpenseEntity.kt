package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_categories")
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "expenses",
    indices = [Index("shamsiDate"), Index("category")]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // اجاره، آب و برق، دستمزد کارگر روزمزد، خوراک، لوازم فروشگاه (نایلون و سبد)، تعمیرات، بانکی، سایر
    val amount: Long, // Toman
    val shamsiDate: String,
    val timestamp: Long = System.currentTimeMillis(),
    val paymentMethod: String = "نقدی",
    val branchName: String = "شعبه اصلی",
    val notes: String = ""
)

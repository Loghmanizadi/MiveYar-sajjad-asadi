package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val storeName: String = "میوه و تره‌بار میوهیار",
    val ownerName: String = "مدیریت",
    val phone: String = "",
    val city: String = "کرج",
    val allowNegativeStock: Boolean = true,
    val daysPerWeekForAbsence: Int = 7 // 6 or 7 days to calculate daily deduction for absences
)

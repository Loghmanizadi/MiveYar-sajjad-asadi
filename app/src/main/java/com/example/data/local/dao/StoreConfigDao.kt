package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.BranchEntity
import com.example.data.local.entities.PaymentMethodEntity
import com.example.data.local.entities.StoreSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreConfigDao {
    @Query("SELECT * FROM branches ORDER BY id ASC")
    fun getAllBranches(): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveBranches(): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBranches(branches: List<BranchEntity>)

    @androidx.room.Update
    suspend fun updateBranch(branch: BranchEntity)

    @Delete
    suspend fun deleteBranch(branch: BranchEntity)

    @Query("SELECT * FROM payment_methods ORDER BY id ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPaymentMethods(methods: List<PaymentMethodEntity>)

    @Delete
    suspend fun deletePaymentMethod(method: PaymentMethodEntity)

    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StoreSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: StoreSettingsEntity)

    @Query("DELETE FROM branches")
    suspend fun clearBranches()

    @Query("DELETE FROM payment_methods")
    suspend fun clearPaymentMethods()
}

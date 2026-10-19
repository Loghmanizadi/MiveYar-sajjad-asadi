package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.InventoryAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_adjustments ORDER BY id DESC")
    fun getAllAdjustments(): Flow<List<InventoryAdjustmentEntity>>

    @Query("SELECT * FROM inventory_adjustments WHERE productId = :productId ORDER BY id DESC")
    fun getAdjustmentsByProduct(productId: Long): Flow<List<InventoryAdjustmentEntity>>

    @Query("SELECT * FROM inventory_adjustments WHERE shamsiDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getAdjustmentsByDateRange(startDate: String, endDate: String): Flow<List<InventoryAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: InventoryAdjustmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(adjustments: List<InventoryAdjustmentEntity>)

    @androidx.room.Update
    suspend fun updateAdjustment(adjustment: InventoryAdjustmentEntity)

    @Delete
    suspend fun deleteAdjustment(adjustment: InventoryAdjustmentEntity)

    @Query("DELETE FROM inventory_adjustments")
    suspend fun clearAllAdjustments()
}

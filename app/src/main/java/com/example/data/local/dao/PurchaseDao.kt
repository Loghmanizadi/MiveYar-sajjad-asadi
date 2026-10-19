package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY id DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE shamsiDate = :date ORDER BY id DESC")
    fun getPurchasesByDate(date: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE shamsiDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getPurchasesByDateRange(startDate: String, endDate: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY id DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchases: List<PurchaseEntity>)

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases")
    suspend fun clearAllPurchases()
}

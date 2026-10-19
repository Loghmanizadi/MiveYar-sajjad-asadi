package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DailyReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReceiptDao {
    @Query("SELECT * FROM daily_receipts ORDER BY shamsiDate DESC, id DESC")
    fun getAllReceipts(): Flow<List<DailyReceiptEntity>>

    @Query("SELECT * FROM daily_receipts WHERE shamsiDate = :date ORDER BY id DESC")
    fun getReceiptsByDate(date: String): Flow<List<DailyReceiptEntity>>

    @Query("SELECT * FROM daily_receipts WHERE shamsiDate = :date AND branchName = :branch LIMIT 1")
    suspend fun getReceiptByDateAndBranchDirect(date: String, branch: String): DailyReceiptEntity?

    @Query("SELECT * FROM daily_receipts WHERE shamsiDate = :date AND branchName = :branch LIMIT 1")
    fun getReceiptByDateAndBranchFlow(date: String, branch: String): Flow<DailyReceiptEntity?>

    @Query("SELECT * FROM daily_receipts WHERE shamsiDate BETWEEN :startDate AND :endDate ORDER BY shamsiDate DESC")
    fun getReceiptsByDateRange(startDate: String, endDate: String): Flow<List<DailyReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: DailyReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: DailyReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: DailyReceiptEntity)

    @Query("DELETE FROM daily_receipts")
    suspend fun clearAllReceipts()
}

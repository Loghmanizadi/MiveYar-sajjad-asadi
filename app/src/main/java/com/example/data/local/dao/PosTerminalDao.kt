package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.DailyReceiptPosEntryEntity
import com.example.data.local.entities.PosTerminalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosTerminalDao {

    // --- مدیریت کارت‌خوان‌ها ---
    @Query("SELECT * FROM pos_terminals ORDER BY id ASC")
    fun getAllTerminals(): Flow<List<PosTerminalEntity>>

    @Query("SELECT * FROM pos_terminals WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveTerminals(): Flow<List<PosTerminalEntity>>

    @Query("SELECT * FROM pos_terminals WHERE id = :id LIMIT 1")
    suspend fun getTerminalById(id: Long): PosTerminalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerminal(terminal: PosTerminalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTerminals(list: List<PosTerminalEntity>)

    @Update
    suspend fun updateTerminal(terminal: PosTerminalEntity)

    @Delete
    suspend fun deleteTerminal(terminal: PosTerminalEntity)

    // --- اقلام دریافتی هر کارت‌خوان در تسویه روزانه ---
    @Query("SELECT * FROM daily_receipt_pos_entries WHERE receiptId = :receiptId ORDER BY id ASC")
    fun getPosEntriesForReceipt(receiptId: Long): Flow<List<DailyReceiptPosEntryEntity>>

    @Query("SELECT * FROM daily_receipt_pos_entries WHERE shamsiDate = :shamsiDate AND branchName = :branchName ORDER BY id ASC")
    fun getPosEntriesForDateAndBranch(shamsiDate: String, branchName: String): Flow<List<DailyReceiptPosEntryEntity>>

    @Query("SELECT * FROM daily_receipt_pos_entries WHERE shamsiDate = :shamsiDate AND branchName = :branchName ORDER BY id ASC")
    suspend fun getPosEntriesForDateAndBranchDirect(shamsiDate: String, branchName: String): List<DailyReceiptPosEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosEntry(entry: DailyReceiptPosEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPosEntries(entries: List<DailyReceiptPosEntryEntity>)

    @Query("DELETE FROM daily_receipt_pos_entries WHERE receiptId = :receiptId")
    suspend fun deletePosEntriesForReceipt(receiptId: Long)

    @Query("DELETE FROM daily_receipt_pos_entries WHERE shamsiDate = :shamsiDate AND branchName = :branchName")
    suspend fun deletePosEntriesForDateAndBranch(shamsiDate: String, branchName: String)

    @Query("DELETE FROM daily_receipt_pos_entries")
    suspend fun clearAllPosEntries()
}

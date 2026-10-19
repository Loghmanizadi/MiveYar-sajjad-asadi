package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.PartnerEntity
import com.example.data.local.entities.PartnerTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartnerDao {
    @Query("SELECT * FROM partners ORDER BY fullName ASC")
    fun getAllPartners(): Flow<List<PartnerEntity>>

    @Query("SELECT * FROM partners WHERE isActive = 1 ORDER BY fullName ASC")
    fun getActivePartners(): Flow<List<PartnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartner(partner: PartnerEntity): Long

    @Update
    suspend fun updatePartner(partner: PartnerEntity)

    @Delete
    suspend fun deletePartner(partner: PartnerEntity)

    @Query("SELECT * FROM partner_transactions ORDER BY shamsiDate DESC, id DESC")
    fun getAllTransactions(): Flow<List<PartnerTransactionEntity>>

    @Query("SELECT * FROM partner_transactions WHERE partnerId = :partnerId ORDER BY shamsiDate DESC, id DESC")
    fun getTransactionsByPartner(partnerId: Long): Flow<List<PartnerTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: PartnerTransactionEntity): Long

    @Update
    suspend fun updateTransaction(tx: PartnerTransactionEntity)

    @Delete
    suspend fun deleteTransaction(tx: PartnerTransactionEntity)

    @Query("DELETE FROM partners")
    suspend fun clearPartners()

    @Query("DELETE FROM partner_transactions")
    suspend fun clearPartnerTransactions()
}

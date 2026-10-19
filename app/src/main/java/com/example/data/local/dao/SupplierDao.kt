package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuppliers(suppliers: List<SupplierEntity>)

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("SELECT * FROM supplier_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<SupplierTransactionEntity>>

    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY id DESC")
    fun getTransactionsBySupplier(supplierId: Long): Flow<List<SupplierTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: SupplierTransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: SupplierTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: SupplierTransactionEntity)

    @Query("DELETE FROM suppliers")
    suspend fun clearAllSuppliers()

    @Query("DELETE FROM supplier_transactions")
    suspend fun clearAllTransactions()
}

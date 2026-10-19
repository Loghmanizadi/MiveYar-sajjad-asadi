package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.SaleItemEntity
import kotlinx.coroutines.flow.Flow

data class SaleWithItems(
    val sale: SaleEntity,
    val items: List<SaleItemEntity>
)

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE shamsiDate = :date ORDER BY id DESC")
    fun getSalesByDate(date: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE shamsiDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getSalesByDateRange(startDate: String, endDate: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItemsBySaleId(saleId: Long): Flow<List<SaleItemEntity>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItemsBySaleIdDirect(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items ORDER BY id DESC")
    fun getAllSaleItems(): Flow<List<SaleItemEntity>>

    @Query("""
        SELECT sale_items.* FROM sale_items 
        INNER JOIN sales ON sale_items.saleId = sales.id 
        WHERE sales.shamsiDate BETWEEN :startDate AND :endDate
    """)
    fun getSaleItemsByDateRange(startDate: String, endDate: String): Flow<List<SaleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Update
    suspend fun updateSaleItem(item: SaleItemEntity)

    @Delete
    suspend fun deleteSaleItem(item: SaleItemEntity)

    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteSaleItemsBySaleId(saleId: Long)

    @Transaction
    suspend fun recordCompleteSale(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        val saleId = insertSale(sale)
        val linkedItems = items.map { it.copy(saleId = saleId) }
        insertSaleItems(linkedItems)
        return saleId
    }

    @Transaction
    suspend fun updateCompleteSale(sale: SaleEntity, items: List<SaleItemEntity>) {
        updateSale(sale)
        deleteSaleItemsBySaleId(sale.id)
        val linkedItems = items.map { it.copy(saleId = sale.id) }
        insertSaleItems(linkedItems)
    }

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales")
    suspend fun clearAllSales()
}

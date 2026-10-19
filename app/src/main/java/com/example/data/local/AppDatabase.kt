package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailyReceiptDao
import com.example.data.local.dao.EmployeeDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.PartnerDao
import com.example.data.local.dao.PosTerminalDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StoreConfigDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entities.BranchEntity
import com.example.data.local.entities.DailyReceiptEntity
import com.example.data.local.entities.DailyReceiptPosEntryEntity
import com.example.data.local.entities.EmployeeAttendanceEntity
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.EmployeePaymentEntity
import com.example.data.local.entities.ExpenseCategoryEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.InventoryAdjustmentEntity
import com.example.data.local.entities.PartnerEntity
import com.example.data.local.entities.PartnerTransactionEntity
import com.example.data.local.entities.PaymentMethodEntity
import com.example.data.local.entities.PosTerminalEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.PurchaseEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.SaleItemEntity
import com.example.data.local.entities.StoreSettingsEntity
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierTransactionEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE sale_items ADD COLUMN isWeighted INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE branches ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_receipts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `shamsiDate` TEXT NOT NULL,
                `branchName` TEXT NOT NULL,
                `posMellat` INTEGER NOT NULL DEFAULT 0,
                `posParsian` INTEGER NOT NULL DEFAULT 0,
                `cash` INTEGER NOT NULL DEFAULT 0,
                `cardToCard` INTEGER NOT NULL DEFAULT 0,
                `other` INTEGER NOT NULL DEFAULT 0,
                `notes` TEXT NOT NULL DEFAULT '',
                `timestamp` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_receipts_shamsiDate` ON `daily_receipts` (`shamsiDate`)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `partners` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `fullName` TEXT NOT NULL,
                `phone` TEXT NOT NULL DEFAULT '',
                `sharePercentage` REAL NOT NULL DEFAULT 0.0,
                `initialBalance` INTEGER NOT NULL DEFAULT 0,
                `notes` TEXT NOT NULL DEFAULT '',
                `isActive` INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `partner_transactions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `partnerId` INTEGER NOT NULL,
                `shamsiDate` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL DEFAULT 0,
                `type` TEXT NOT NULL,
                `amount` INTEGER NOT NULL,
                `paymentMethod` TEXT NOT NULL DEFAULT 'نقدی',
                `notes` TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(`partnerId`) REFERENCES `partners`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_partner_transactions_partnerId` ON `partner_transactions` (`partnerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_partner_transactions_shamsiDate` ON `partner_transactions` (`shamsiDate`)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Upgrade daily_receipts for dynamic card readers & settlement tracking
        db.execSQL("ALTER TABLE daily_receipts ADD COLUMN posTotal INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE daily_receipts ADD COLUMN isSettled INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_receipts_shamsiDate_branchName` ON `daily_receipts` (`shamsiDate`, `branchName`)")

        // Dynamic POS terminals table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `pos_terminals` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `bankName` TEXT NOT NULL DEFAULT '',
                `terminalNumber` TEXT NOT NULL DEFAULT '',
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // Structured POS entries for daily receipts
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_receipt_pos_entries` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `receiptId` INTEGER NOT NULL DEFAULT 0,
                `shamsiDate` TEXT NOT NULL,
                `branchName` TEXT NOT NULL,
                `terminalId` INTEGER NOT NULL,
                `terminalName` TEXT NOT NULL,
                `amount` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_receipt_pos_entries_receiptId` ON `daily_receipt_pos_entries` (`receiptId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_receipt_pos_entries_shamsiDate` ON `daily_receipt_pos_entries` (`shamsiDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_receipt_pos_entries_shamsiDate_branchName` ON `daily_receipt_pos_entries` (`shamsiDate`, `branchName`)")
    }
}

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        SupplierEntity::class,
        SupplierTransactionEntity::class,
        InventoryAdjustmentEntity::class,
        ExpenseCategoryEntity::class,
        ExpenseEntity::class,
        EmployeeEntity::class,
        EmployeeAttendanceEntity::class,
        EmployeePaymentEntity::class,
        BranchEntity::class,
        PaymentMethodEntity::class,
        StoreSettingsEntity::class,
        DailyReceiptEntity::class,
        PartnerEntity::class,
        PartnerTransactionEntity::class,
        PosTerminalEntity::class,
        DailyReceiptPosEntryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun supplierDao(): SupplierDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun storeConfigDao(): StoreConfigDao
    abstract fun dailyReceiptDao(): DailyReceiptDao
    abstract fun partnerDao(): PartnerDao
    abstract fun posTerminalDao(): PosTerminalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miveyar_fruit_shop.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

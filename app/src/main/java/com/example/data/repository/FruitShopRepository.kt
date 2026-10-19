package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.BranchEntity
import com.example.data.local.entities.EmployeeAttendanceEntity
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.EmployeePaymentEntity
import com.example.data.local.entities.ExpenseCategoryEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.InventoryAdjustmentEntity
import com.example.data.local.entities.PaymentMethodEntity
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.PurchaseEntity
import com.example.data.local.entities.SaleEntity
import com.example.data.local.entities.SaleItemEntity
import com.example.data.local.entities.DailyReceiptEntity
import com.example.data.local.entities.DailyReceiptPosEntryEntity
import com.example.data.local.entities.PosTerminalEntity
import com.example.data.local.entities.StoreSettingsEntity
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierTransactionEntity
import com.example.util.JalaliCalendarUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

data class ProductStockInfo(
    val product: ProductEntity,
    val totalPurchasedWeight: Double,
    val totalSoldWeight: Double,
    val totalAdjustmentsWeight: Double,
    val totalWastageWeight: Double,
    val currentStock: Double,
    val isLowStock: Boolean
)

data class SupplierSummary(
    val supplier: SupplierEntity,
    val totalPurchasesAmount: Long,
    val totalPaymentsAmount: Long,
    val currentBalance: Long // Positive = We owe supplier (بدهی ما), Negative = Supplier owes us (طلب ما)
)

data class EmployeePayrollSummary(
    val employee: EmployeeEntity,
    val presentDays: Int,
    val absentDays: Int,
    val leaveDays: Int,
    val halfDays: Int,
    val baseWage: Long,
    val absentDeduction: Long,
    val totalAdvances: Long, // مجموع مساعده‌ها
    val totalDeductions: Long, // سایر کسورات
    val totalBonuses: Long, // پاداش‌ها
    val previousPaidSalary: Long, // پرداختی‌های قبلی این دوره
    val netPayable: Long // خالص قابل پرداخت
)

class FruitShopRepository(private val db: AppDatabase) {

    val allProducts: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = db.productDao().getActiveProducts()

    val allSales: Flow<List<SaleEntity>> = db.saleDao().getAllSales()
    val allSaleItems: Flow<List<SaleItemEntity>> = db.saleDao().getAllSaleItems()

    val allPurchases: Flow<List<PurchaseEntity>> = db.purchaseDao().getAllPurchases()

    val allSuppliers: Flow<List<SupplierEntity>> = db.supplierDao().getAllSuppliers()
    val allSupplierTransactions: Flow<List<SupplierTransactionEntity>> = db.supplierDao().getAllTransactions()

    val allInventoryAdjustments: Flow<List<InventoryAdjustmentEntity>> = db.inventoryDao().getAllAdjustments()

    val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val allExpenseCategories: Flow<List<ExpenseCategoryEntity>> = db.expenseDao().getAllCategories()

    val allEmployees: Flow<List<EmployeeEntity>> = db.employeeDao().getAllEmployees()
    val allPayments: Flow<List<EmployeePaymentEntity>> = db.employeeDao().getAllPayments()
    val allAttendance: Flow<List<EmployeeAttendanceEntity>> = db.employeeDao().getAllAttendance()

    val allBranches: Flow<List<BranchEntity>> = db.storeConfigDao().getAllBranches()
    val allPaymentMethods: Flow<List<PaymentMethodEntity>> = db.storeConfigDao().getAllPaymentMethods()
    val storeSettings: Flow<StoreSettingsEntity?> = db.storeConfigDao().getSettings()

    val allDailyReceipts: Flow<List<com.example.data.local.entities.DailyReceiptEntity>> = db.dailyReceiptDao().getAllReceipts()
    val allPosTerminals: Flow<List<PosTerminalEntity>> = db.posTerminalDao().getAllTerminals()
    val activePosTerminals: Flow<List<PosTerminalEntity>> = db.posTerminalDao().getActiveTerminals()
    val allPartners: Flow<List<com.example.data.local.entities.PartnerEntity>> = db.partnerDao().getAllPartners()
    val activePartners: Flow<List<com.example.data.local.entities.PartnerEntity>> = db.partnerDao().getActivePartners()
    val allPartnerTransactions: Flow<List<com.example.data.local.entities.PartnerTransactionEntity>> = db.partnerDao().getAllTransactions()

    // -------------------------------------------------------------------------
    // Sales Operations
    // -------------------------------------------------------------------------
    suspend fun recordSale(sale: SaleEntity, items: List<SaleItemEntity>): Long {
        return db.saleDao().recordCompleteSale(sale, items)
    }

    suspend fun deleteSale(sale: SaleEntity) {
        db.saleDao().deleteSale(sale)
    }

    fun getSalesByDateRange(startDate: String, endDate: String): Flow<List<SaleEntity>> {
        return db.saleDao().getSalesByDateRange(startDate, endDate)
    }

    fun getSaleItemsByDateRange(startDate: String, endDate: String): Flow<List<SaleItemEntity>> {
        return db.saleDao().getSaleItemsByDateRange(startDate, endDate)
    }

    fun getSaleItemsForSale(saleId: Long): Flow<List<SaleItemEntity>> {
        return db.saleDao().getSaleItemsBySaleId(saleId)
    }

    // -------------------------------------------------------------------------
    // Purchases Operations (خرید و ورود بار)
    // -------------------------------------------------------------------------
    suspend fun recordPurchase(purchase: PurchaseEntity): Long {
        val purchaseId = db.purchaseDao().insertPurchase(purchase)

        // Automatically update supplier ledger without double-counting!
        // 1. Record total debt incurred from this purchase
        if (purchase.supplierId > 0) {
            val debtTx = SupplierTransactionEntity(
                supplierId = purchase.supplierId,
                shamsiDate = purchase.shamsiDate,
                type = "PURCHASE_DEBT",
                amount = purchase.totalCost,
                paymentMethod = purchase.paymentMethod,
                relatedPurchaseId = purchaseId,
                notes = "خرید بار: ${purchase.productName} (${purchase.weight} کیلو) فاکتور #$purchaseId"
            )
            db.supplierDao().insertTransaction(debtTx)

            // 2. If immediate payment was made at purchase time, record payment
            if (purchase.paidAmount > 0) {
                val paymentTx = SupplierTransactionEntity(
                    supplierId = purchase.supplierId,
                    shamsiDate = purchase.shamsiDate,
                    type = "PAYMENT",
                    amount = purchase.paidAmount,
                    paymentMethod = purchase.paymentMethod,
                    relatedPurchaseId = purchaseId,
                    notes = "پرداخت همزمان با خرید بار #${purchaseId}"
                )
                db.supplierDao().insertTransaction(paymentTx)
            }
        }
        return purchaseId
    }

    suspend fun deletePurchase(purchase: PurchaseEntity) {
        db.purchaseDao().deletePurchase(purchase)
    }

    fun getPurchasesByDateRange(startDate: String, endDate: String): Flow<List<PurchaseEntity>> {
        return db.purchaseDao().getPurchasesByDateRange(startDate, endDate)
    }

    // -------------------------------------------------------------------------
    // Product Operations
    // -------------------------------------------------------------------------
    suspend fun insertProduct(product: ProductEntity): Long = db.productDao().insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = db.productDao().updateProduct(product)
    suspend fun updateProductPrice(productId: Long, newPrice: Long) = db.productDao().updatePrice(productId, newPrice)
    suspend fun deleteProduct(product: ProductEntity) = db.productDao().deleteProduct(product)

    // -------------------------------------------------------------------------
    // Supplier Operations
    // -------------------------------------------------------------------------
    suspend fun insertSupplier(supplier: SupplierEntity): Long = db.supplierDao().insertSupplier(supplier)
    suspend fun updateSupplier(supplier: SupplierEntity) = db.supplierDao().updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) = db.supplierDao().deleteSupplier(supplier)
    suspend fun recordSupplierTransaction(tx: SupplierTransactionEntity): Long = db.supplierDao().insertTransaction(tx)
    suspend fun deleteSupplierTransaction(tx: SupplierTransactionEntity) = db.supplierDao().deleteTransaction(tx)

    // -------------------------------------------------------------------------
    // Inventory & Spoilage Adjustments
    // -------------------------------------------------------------------------
    suspend fun recordInventoryAdjustment(adjustment: InventoryAdjustmentEntity): Long {
        return db.inventoryDao().insertAdjustment(adjustment)
    }

    suspend fun deleteInventoryAdjustment(adjustment: InventoryAdjustmentEntity) {
        db.inventoryDao().deleteAdjustment(adjustment)
    }

    // -------------------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------------------
    suspend fun recordExpense(expense: ExpenseEntity): Long = db.expenseDao().insertExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = db.expenseDao().deleteExpense(expense)
    suspend fun insertExpenseCategory(category: ExpenseCategoryEntity): Long = db.expenseDao().insertCategory(category)
    suspend fun deleteExpenseCategory(category: ExpenseCategoryEntity) = db.expenseDao().deleteCategory(category)
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>> {
        return db.expenseDao().getExpensesByDateRange(startDate, endDate)
    }

    // -------------------------------------------------------------------------
    // Employees & Payroll
    // -------------------------------------------------------------------------
    suspend fun insertEmployee(employee: EmployeeEntity): Long = db.employeeDao().insertEmployee(employee)
    suspend fun updateEmployee(employee: EmployeeEntity) = db.employeeDao().updateEmployee(employee)
    suspend fun deleteEmployee(employee: EmployeeEntity) = db.employeeDao().deleteEmployee(employee)

    suspend fun recordAttendance(attendance: EmployeeAttendanceEntity): Long = db.employeeDao().recordAttendance(attendance)
    fun getAttendanceByDate(date: String): Flow<List<EmployeeAttendanceEntity>> = db.employeeDao().getAttendanceByDate(date)
    fun getAttendanceByEmployee(empId: Long): Flow<List<EmployeeAttendanceEntity>> = db.employeeDao().getAttendanceByEmployee(empId)
    fun getAttendanceByDateRange(startDate: String, endDate: String): Flow<List<EmployeeAttendanceEntity>> {
        return db.employeeDao().getAttendanceByDateRange(startDate, endDate)
    }

    suspend fun recordEmployeePayment(payment: EmployeePaymentEntity): Long = db.employeeDao().insertPayment(payment)
    suspend fun deleteEmployeePayment(payment: EmployeePaymentEntity) = db.employeeDao().deletePayment(payment)
    fun getEmployeePayments(empId: Long): Flow<List<EmployeePaymentEntity>> = db.employeeDao().getPaymentsByEmployee(empId)

    // -------------------------------------------------------------------------
    // Store Config & Branches
    // -------------------------------------------------------------------------
    suspend fun insertBranch(branch: BranchEntity): Long = db.storeConfigDao().insertBranch(branch)
    suspend fun deleteBranch(branch: BranchEntity) = db.storeConfigDao().deleteBranch(branch)
    suspend fun insertPaymentMethod(method: PaymentMethodEntity): Long = db.storeConfigDao().insertPaymentMethod(method)
    suspend fun deletePaymentMethod(method: PaymentMethodEntity) = db.storeConfigDao().deletePaymentMethod(method)
    suspend fun saveStoreSettings(settings: StoreSettingsEntity) = db.storeConfigDao().saveSettings(settings)

    // -------------------------------------------------------------------------
    // Daily Receipts & Settlements (تسویه و انطباق صندوق و پوز)
    // -------------------------------------------------------------------------
    suspend fun recordDailyReceipt(receipt: DailyReceiptEntity): Long = db.dailyReceiptDao().insertReceipt(receipt)
    suspend fun updateDailyReceipt(receipt: DailyReceiptEntity) = db.dailyReceiptDao().updateReceipt(receipt)
    suspend fun deleteDailyReceipt(receipt: DailyReceiptEntity) = db.dailyReceiptDao().deleteReceipt(receipt)
    fun getDailyReceiptsByDate(date: String): Flow<List<DailyReceiptEntity>> = db.dailyReceiptDao().getReceiptsByDate(date)
    suspend fun getDailyReceiptDirect(date: String, branch: String): DailyReceiptEntity? = db.dailyReceiptDao().getReceiptByDateAndBranchDirect(date, branch)
    fun getDailyReceiptFlow(date: String, branch: String): Flow<DailyReceiptEntity?> = db.dailyReceiptDao().getReceiptByDateAndBranchFlow(date, branch)

    // POS Terminals
    suspend fun insertPosTerminal(terminal: PosTerminalEntity): Long = db.posTerminalDao().insertTerminal(terminal)
    suspend fun updatePosTerminal(terminal: PosTerminalEntity) = db.posTerminalDao().updateTerminal(terminal)
    suspend fun deletePosTerminal(terminal: PosTerminalEntity) = db.posTerminalDao().deleteTerminal(terminal)

    fun getPosEntriesForReceipt(receiptId: Long): Flow<List<DailyReceiptPosEntryEntity>> = db.posTerminalDao().getPosEntriesForReceipt(receiptId)
    fun getPosEntriesForDateAndBranch(date: String, branch: String): Flow<List<DailyReceiptPosEntryEntity>> = db.posTerminalDao().getPosEntriesForDateAndBranch(date, branch)
    suspend fun getPosEntriesForDateAndBranchDirect(date: String, branch: String): List<DailyReceiptPosEntryEntity> = db.posTerminalDao().getPosEntriesForDateAndBranchDirect(date, branch)

    /**
     * ذخیره‌سازی امن تسویه پایان روز:
     * - اگر رکوردی برای این تاریخ و شعبه وجود داشته باشد، همان رکورد بروزرسانی می‌شود (جلوگیری از ثبت تکراری)
     * - مبالغ کارت‌خوان‌ها به‌صورت ساختاریافته در جدول اختصاصی ذخیره می‌گردند
     * - مجموع کارت‌خوان‌ها در posTotal انتیتی تسویه ذخیره می‌شود
     */
    suspend fun saveDailySettlement(
        receipt: DailyReceiptEntity,
        posEntries: List<DailyReceiptPosEntryEntity>
    ): Long {
        val existing = db.dailyReceiptDao().getReceiptByDateAndBranchDirect(receipt.shamsiDate, receipt.branchName)
        val receiptIdToUse = existing?.id ?: 0L

        val posTotalSum = posEntries.sumOf { it.amount }
        val finalReceipt = receipt.copy(
            id = receiptIdToUse,
            posTotal = posTotalSum,
            isSettled = true,
            timestamp = System.currentTimeMillis()
        )

        val savedReceiptId = if (receiptIdToUse != 0L) {
            db.dailyReceiptDao().updateReceipt(finalReceipt)
            receiptIdToUse
        } else {
            db.dailyReceiptDao().insertReceipt(finalReceipt)
        }

        // جایگزینی اقلام ساختاریافته پوز
        db.posTerminalDao().deletePosEntriesForReceipt(savedReceiptId)
        db.posTerminalDao().deletePosEntriesForDateAndBranch(receipt.shamsiDate, receipt.branchName)
        val entriesWithId = posEntries.map {
            it.copy(
                receiptId = savedReceiptId,
                shamsiDate = receipt.shamsiDate,
                branchName = receipt.branchName
            )
        }
        db.posTerminalDao().insertAllPosEntries(entriesWithId)

        return savedReceiptId
    }

    // -------------------------------------------------------------------------
    // Partners & Capital (شرکا و تقسیم سود)
    // -------------------------------------------------------------------------
    suspend fun insertPartner(partner: com.example.data.local.entities.PartnerEntity): Long = db.partnerDao().insertPartner(partner)
    suspend fun updatePartner(partner: com.example.data.local.entities.PartnerEntity) = db.partnerDao().updatePartner(partner)
    suspend fun deletePartner(partner: com.example.data.local.entities.PartnerEntity) = db.partnerDao().deletePartner(partner)
    suspend fun recordPartnerTransaction(tx: com.example.data.local.entities.PartnerTransactionEntity): Long = db.partnerDao().insertTransaction(tx)
    suspend fun deletePartnerTransaction(tx: com.example.data.local.entities.PartnerTransactionEntity) = db.partnerDao().deleteTransaction(tx)
    fun getPartnerTransactions(partnerId: Long): Flow<List<com.example.data.local.entities.PartnerTransactionEntity>> = db.partnerDao().getTransactionsByPartner(partnerId)

    // -------------------------------------------------------------------------
    // Initial Seeding / Setup
    // -------------------------------------------------------------------------
    suspend fun ensureDefaultSetup() {
        val existingProducts = allProducts.firstOrNull() ?: emptyList()
        if (existingProducts.isEmpty()) {
            seedDefaultProducts()
        }

        val existingBranches = allBranches.firstOrNull() ?: emptyList()
        if (existingBranches.isEmpty()) {
            seedDefaultBranches()
        }

        val existingPaymentMethods = allPaymentMethods.firstOrNull() ?: emptyList()
        if (existingPaymentMethods.isEmpty()) {
            seedDefaultPaymentMethods()
        }

        val existingCategories = allExpenseCategories.firstOrNull() ?: emptyList()
        if (existingCategories.isEmpty()) {
            seedDefaultExpenseCategories()
        }

        val existingTerminals = allPosTerminals.firstOrNull() ?: emptyList()
        if (existingTerminals.isEmpty()) {
            seedDefaultPosTerminals()
        }

        val existingSettings = storeSettings.firstOrNull()
        if (existingSettings == null) {
            db.storeConfigDao().saveSettings(
                StoreSettingsEntity(
                    id = 1,
                    storeName = "میوه و تره‌بار میوهیار",
                    ownerName = "مدیریت فروشگاه",
                    phone = "۰۲۶-۳۴۵۶۷۸۹۰",
                    city = "کرج",
                    allowNegativeStock = true,
                    daysPerWeekForAbsence = 7
                )
            )
        }
    }

    suspend fun seedDefaultProducts() {
        val defaultList = listOf(
            ProductEntity(name = "سیب‌زمینی جیرفت", category = "صیفی‌جات", unit = "کیلوگرم", salePrice = 28000, referenceBuyPrice = 22000, minStockAlert = 50.0),
            ProductEntity(name = "پیاز سفید شیراز", category = "صیفی‌جات", unit = "کیلوگرم", salePrice = 32000, referenceBuyPrice = 25000, minStockAlert = 40.0),
            ProductEntity(name = "گوجه فرنگی بوته‌ای", category = "تره‌بار", unit = "کیلوگرم", salePrice = 45000, referenceBuyPrice = 36000, minStockAlert = 30.0),
            ProductEntity(name = "خیار بوته‌ای ورامین", category = "تره‌بار", unit = "کیلوگرم", salePrice = 38000, referenceBuyPrice = 30000, minStockAlert = 25.0),
            ProductEntity(name = "موز اکوادور درجه یک", category = "میوه", unit = "کیلوگرم", salePrice = 95000, referenceBuyPrice = 82000, minStockAlert = 20.0),
            ProductEntity(name = "سیب قرمز دماوند", category = "میوه", unit = "کیلوگرم", salePrice = 65000, referenceBuyPrice = 52000, minStockAlert = 30.0),
            ProductEntity(name = "پرتقال تامسون شمال", category = "مرکبات", unit = "کیلوگرم", salePrice = 55000, referenceBuyPrice = 42000, minStockAlert = 40.0),
            ProductEntity(name = "انگور بی‌دانه ملایر", category = "میوه", unit = "کیلوگرم", salePrice = 75000, referenceBuyPrice = 60000, minStockAlert = 20.0),
            ProductEntity(name = "هویج دزفول", category = "تره‌بار", unit = "کیلوگرم", salePrice = 22000, referenceBuyPrice = 16000, minStockAlert = 30.0),
            ProductEntity(name = "فلفل دلمه‌ای رنگی", category = "تره‌بار", unit = "کیلوگرم", salePrice = 70000, referenceBuyPrice = 54000, minStockAlert = 15.0),
            ProductEntity(name = "گل‌کلم دزفول", category = "سبزیجات", unit = "کیلوگرم", salePrice = 35000, referenceBuyPrice = 26000, minStockAlert = 20.0),
            ProductEntity(name = "لوبیا سبز محلی", category = "تره‌بار", unit = "کیلوگرم", salePrice = 60000, referenceBuyPrice = 48000, minStockAlert = 15.0)
        )
        db.productDao().insertAll(defaultList)
    }

    suspend fun seedDefaultBranches() {
        val list = listOf(
            BranchEntity(name = "شعبه کاج", isDefault = true),
            BranchEntity(name = "شعبه ماهان", isDefault = false),
            BranchEntity(name = "انبار مرکزی", isDefault = false),
            BranchEntity(name = "مشترک", isDefault = false)
        )
        db.storeConfigDao().insertAllBranches(list)
    }

    suspend fun seedDefaultPaymentMethods() {
        val list = listOf(
            PaymentMethodEntity(name = "کارتخوان ملت", isDefault = true),
            PaymentMethodEntity(name = "کارتخوان پارسیان", isDefault = false),
            PaymentMethodEntity(name = "نقدی", isDefault = false),
            PaymentMethodEntity(name = "کارت به کارت", isDefault = false),
            PaymentMethodEntity(name = "سایر", isDefault = false)
        )
        db.storeConfigDao().insertAllPaymentMethods(list)
    }

    suspend fun seedDefaultPosTerminals() {
        val list = listOf(
            PosTerminalEntity(name = "کارتخوان ملت", bankName = "ملت"),
            PosTerminalEntity(name = "کارتخوان پارسیان", bankName = "پارسیان"),
            PosTerminalEntity(name = "کارتخوان سامان", bankName = "سامان")
        )
        db.posTerminalDao().insertAllTerminals(list)
    }

    suspend fun seedDefaultExpenseCategories() {
        val list = listOf(
            ExpenseCategoryEntity(name = "اجاره مغازه", isDefault = true),
            ExpenseCategoryEntity(name = "قبوض (آب، برق، گاز)", isDefault = true),
            ExpenseCategoryEntity(name = "نایلون، سبد و بسته‌بندی", isDefault = true),
            ExpenseCategoryEntity(name = "حمل و نقل و نیسان متفرقه", isDefault = true),
            ExpenseCategoryEntity(name = "خوراک، صبحانه و چای فروشگاه", isDefault = true),
            ExpenseCategoryEntity(name = "تعمیرات ترازو، یخچال و لامپ", isDefault = true),
            ExpenseCategoryEntity(name = "کارمزد و هزینه‌های بانکی", isDefault = true),
            ExpenseCategoryEntity(name = "هزینه‌های میدان و ورودی", isDefault = true),
            ExpenseCategoryEntity(name = "سایر هزینه‌های متفرقه", isDefault = true)
        )
        db.expenseDao().insertAllCategories(list)
    }

    // -------------------------------------------------------------------------
    // Clear and Reset Data for testing or real use
    // -------------------------------------------------------------------------
    suspend fun clearTransactionalData() {
        db.saleDao().clearAllSales()
        db.purchaseDao().clearAllPurchases()
        db.inventoryDao().clearAllAdjustments()
        db.expenseDao().clearAllExpenses()
        db.employeeDao().clearAllAttendance()
        db.employeeDao().clearAllPayments()
        db.supplierDao().clearAllTransactions()
    }

    suspend fun clearAllData() {
        clearTransactionalData()
        db.productDao().clearAllProducts()
        db.supplierDao().clearAllSuppliers()
        db.employeeDao().clearAllEmployees()
    }

    // -------------------------------------------------------------------------
    // Load Comprehensive Demo Data (برای حالت آزمایشی)
    // -------------------------------------------------------------------------
    suspend fun loadComprehensiveDemoData() {
        val today = JalaliCalendarUtil.getToday().toFormattedString()
        val yesterday = JalaliCalendarUtil.getYesterday().toFormattedString()

        // 1. Suppliers
        val sup1 = db.supplierDao().insertSupplier(
            SupplierEntity(name = "حاج علی رضایی - حجره ۴۵ میدان کرج", phone = "۰۹۱۲۳۴۵۶۷۸۹", location = "فاز ۱، حجره ۴۵", initialDebt = 12000000L)
        )
        val sup2 = db.supplierDao().insertSupplier(
            SupplierEntity(name = "مشهدی کاظم احمدی - میدان مرکزی", phone = "۰۹۱۹۸۷۶۵۴۳۲", location = "فاز ۲، حجره ۱۸", initialDebt = 5000000L)
        )

        // 2. Purchases
        val products = db.productDao().getActiveProducts().firstOrNull() ?: emptyList()
        val potato = products.find { it.name.contains("سیب‌زمینی") } ?: products.getOrNull(0)
        val tomato = products.find { it.name.contains("گوجه") } ?: products.getOrNull(1)
        val banana = products.find { it.name.contains("موز") } ?: products.getOrNull(2)

        if (potato != null) {
            recordPurchase(
                PurchaseEntity(
                    shamsiDate = yesterday,
                    supplierId = sup1,
                    supplierName = "حاج علی رضایی - حجره ۴۵ میدان کرج",
                    productId = potato.id,
                    productName = potato.name,
                    marketLocation = "میدان تره‌بار کرج",
                    phase = "فاز ۱",
                    weight = 450.0,
                    packageCount = 18,
                    buyPricePerKg = 22000,
                    baseCost = 9900000L,
                    allocatedBranch = "شعبه کاج",
                    dashtPerPackage = 5000,
                    totalDashtCost = 90000L,
                    transportCost = 350000L,
                    unloadingCost = 150000L,
                    totalCost = 10490000L,
                    effectiveCostPerKg = 23311,
                    paidAmount = 5000000L,
                    remainingDebt = 5490000L,
                    paymentMethod = "کارتخوان ملت"
                )
            )
        }

        if (tomato != null) {
            recordPurchase(
                PurchaseEntity(
                    shamsiDate = today,
                    supplierId = sup2,
                    supplierName = "مشهدی کاظم احمدی - میدان مرکزی",
                    productId = tomato.id,
                    productName = tomato.name,
                    marketLocation = "میدان مرکزی کرج",
                    phase = "فاز ۲",
                    weight = 200.0,
                    packageCount = 10,
                    buyPricePerKg = 36000,
                    baseCost = 7200000L,
                    allocatedBranch = "شعبه ماهان",
                    dashtPerPackage = 4000,
                    totalDashtCost = 40000L,
                    transportCost = 250000L,
                    unloadingCost = 80000L,
                    totalCost = 7570000L,
                    effectiveCostPerKg = 37850,
                    paidAmount = 7570000L,
                    remainingDebt = 0L,
                    paymentMethod = "انتقال بانکی"
                )
            )
        }

        // 3. Sales
        if (potato != null && tomato != null) {
            // Sale 1 - Cash
            val saleItems1 = listOf(
                SaleItemEntity(saleId = 0, productId = potato.id, productName = potato.name, quantity = 4.5, unitPrice = potato.salePrice, totalPrice = (4.5 * potato.salePrice).toLong()),
                SaleItemEntity(saleId = 0, productId = tomato.id, productName = tomato.name, quantity = 2.0, unitPrice = tomato.salePrice, totalPrice = (2.0 * tomato.salePrice).toLong())
            )
            val total1 = saleItems1.sumOf { it.totalPrice }
            recordSale(
                SaleEntity(
                    shamsiDate = today,
                    branchName = "شعبه کاج",
                    totalAmount = total1,
                    paymentMethod = "نقدی",
                    notes = "مشتری حضوری"
                ),
                saleItems1
            )

            // Sale 2 - POS Mellat
            val saleItems2 = listOf(
                SaleItemEntity(saleId = 0, productId = potato.id, productName = potato.name, quantity = 12.0, unitPrice = potato.salePrice, totalPrice = (12.0 * potato.salePrice).toLong())
            )
            val total2 = saleItems2.sumOf { it.totalPrice }
            recordSale(
                SaleEntity(
                    shamsiDate = today,
                    branchName = "شعبه کاج",
                    totalAmount = total2,
                    paymentMethod = "کارتخوان ملت"
                ),
                saleItems2
            )

            // Sale 3 - POS Parsian
            if (banana != null) {
                val saleItems3 = listOf(
                    SaleItemEntity(saleId = 0, productId = banana.id, productName = banana.name, quantity = 3.2, unitPrice = banana.salePrice, totalPrice = (3.2 * banana.salePrice).toLong())
                )
                val total3 = saleItems3.sumOf { it.totalPrice }
                recordSale(
                    SaleEntity(
                        shamsiDate = today,
                        branchName = "شعبه ماهان",
                        totalAmount = total3,
                        paymentMethod = "کارتخوان پارسیان"
                    ),
                    saleItems3
                )
            }
        }

        // 4. Expenses
        recordExpense(
            ExpenseEntity(
                title = "خرید نایلون دسته‌دار و کیسه میوه",
                category = "نایلون، سبد و بسته‌بندی",
                amount = 450000L,
                shamsiDate = today,
                paymentMethod = "نقدی",
                branchName = "شعبه کاج"
            )
        )
        recordExpense(
            ExpenseEntity(
                title = "صبحانه و چای پرسنل",
                category = "خوراک، صبحانه و چای فروشگاه",
                amount = 180000L,
                shamsiDate = today,
                paymentMethod = "کارتخوان ملت",
                branchName = "شعبه کاج"
            )
        )

        // 5. Wastage / Spoilage Adjustment
        if (tomato != null) {
            recordInventoryAdjustment(
                InventoryAdjustmentEntity(
                    productId = tomato.id,
                    productName = tomato.name,
                    shamsiDate = today,
                    quantity = -4.5,
                    reason = "ضایعات و پلاسیدگی",
                    branchName = "شعبه کاج",
                    notes = "دورریز گوجه نرم‌شده ته جعبه"
                )
            )
        }

        // 6. Employees
        val emp1 = insertEmployee(
            EmployeeEntity(
                fullName = "رضا محمدی",
                phone = "۰۹۳۵۱۱۱۲۲۳۳",
                weeklyWage = 3500000L,
                startDateShamsi = "1405/01/15",
                notes = "مسئول چیدمان و فروشندگی"
            )
        )
        val emp2 = insertEmployee(
            EmployeeEntity(
                fullName = "مجتبی قنبری",
                phone = "۰۹۱۲۹۹۹۸۸۷۷",
                weeklyWage = 4000000L,
                startDateShamsi = "1405/03/01",
                notes = "صندوقدار و تخلیه بار"
            )
        )

        // Attendance & Advances
        recordAttendance(EmployeeAttendanceEntity(employeeId = emp1, shamsiDate = today, status = "PRESENT"))
        recordAttendance(EmployeeAttendanceEntity(employeeId = emp2, shamsiDate = today, status = "PRESENT"))

        recordEmployeePayment(
            EmployeePaymentEntity(
                employeeId = emp1,
                shamsiDate = today,
                type = "ADVANCE",
                amount = 500000L,
                forWeekShamsi = "هفته جاری",
                paymentMethod = "نقدی",
                notes = "مساعده میان‌هفته"
            )
        )
    }
}

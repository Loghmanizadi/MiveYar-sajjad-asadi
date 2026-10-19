package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.FruitShopRepository
import com.example.data.repository.ProductStockInfo
import com.example.data.repository.SupplierSummary
import com.example.util.BackupRestoreUtil
import com.example.util.JalaliCalendarUtil
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DateFilterType(val title: String) {
    TODAY("امروز"),
    YESTERDAY("دیروز"),
    THIS_WEEK("هفته جاری"),
    THIS_MONTH("ماه جاری"),
    THIS_YEAR("سال جاری"),
    CUSTOM("بازه دلخواه"),
    ALL_TIME("کل سوابق")
}

data class DashboardMetrics(
    val todaySalesTotal: Long = 0L,
    val todayCashSales: Long = 0L,
    val todayMellatSales: Long = 0L,
    val todayParsianSales: Long = 0L,
    val todayOtherSales: Long = 0L,
    val todayExpensesTotal: Long = 0L,
    val todayPurchasesTotal: Long = 0L,
    val todayEstimatedGrossProfit: Long = 0L,
    val todayTotalWeightSold: Double = 0.0,
    val totalSupplierDebt: Long = 0L,
    val lowStockCount: Int = 0,
    val pendingEmployeePaymentsCount: Int = 0
)

class FruitShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FruitShopRepository(db)

    // Navigation state
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Notification / Toast message
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Date Filter for Reports
    private val _selectedFilterType = MutableStateFlow(DateFilterType.TODAY)
    val selectedFilterType: StateFlow<DateFilterType> = _selectedFilterType.asStateFlow()

    private val _customDateRange = MutableStateFlow(
        Pair(JalaliCalendarUtil.getToday().toFormattedString(), JalaliCalendarUtil.getToday().toFormattedString())
    )
    val customDateRange: StateFlow<Pair<String, String>> = _customDateRange.asStateFlow()

    // Data streams
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeProducts = repository.activeProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val saleItems = repository.allSaleItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val supplierTransactions = repository.allSupplierTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryAdjustments = repository.allInventoryAdjustments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val expenseCategories = repository.allExpenseCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees = repository.allEmployees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val employeePayments = repository.allPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val employeeAttendances = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val branches = repository.allBranches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val paymentMethods = repository.allPaymentMethods.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val storeSettings = repository.storeSettings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyReceipts = repository.allDailyReceipts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val posTerminals = repository.allPosTerminals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activePosTerminals = repository.activePosTerminals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val partners = repository.allPartners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activePartners = repository.activePartners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val partnerTransactions = repository.allPartnerTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stock computation for all products
    val productStockList: StateFlow<List<ProductStockInfo>> = combine(
        products,
        purchases,
        saleItems,
        inventoryAdjustments
    ) { prodList, purchList, soldItems, adjustments ->
        prodList.map { prod ->
            val bought = purchList.filter { it.productId == prod.id }.sumOf { it.weight }
            val sold = soldItems.filter { it.productId == prod.id }.sumOf { it.quantity }
            val adj = adjustments.filter { it.productId == prod.id }.sumOf { it.quantity }
            val wastage = adjustments.filter { it.productId == prod.id && it.quantity < 0 }.sumOf { kotlin.math.abs(it.quantity) }
            val currentStock = bought - sold + adj
            ProductStockInfo(
                product = prod,
                totalPurchasedWeight = bought,
                totalSoldWeight = sold,
                totalAdjustmentsWeight = adj,
                totalWastageWeight = wastage,
                currentStock = currentStock,
                isLowStock = currentStock <= prod.minStockAlert
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Supplier balances
    val supplierSummaries: StateFlow<List<SupplierSummary>> = combine(
        suppliers,
        purchases,
        supplierTransactions
    ) { supList, purchList, txList ->
        supList.map { sup ->
            val totalPurchases = purchList.filter { it.supplierId == sup.id }.sumOf { it.totalCost }
            val totalPayments = txList.filter { it.supplierId == sup.id && it.type == "PAYMENT" }.sumOf { it.amount }
            val debts = txList.filter { it.supplierId == sup.id && it.type == "PURCHASE_DEBT" }.sumOf { it.amount }
            val adjustments = txList.filter { it.supplierId == sup.id && it.type == "ADJUSTMENT" }.sumOf { it.amount }
            val netDebt = sup.initialDebt + debts - totalPayments + adjustments
            SupplierSummary(
                supplier = sup,
                totalPurchasesAmount = totalPurchases,
                totalPaymentsAmount = totalPayments,
                currentBalance = netDebt
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Transactions flow
    private data class DailyData(
        val salesList: List<SaleEntity>,
        val saleItemList: List<SaleItemEntity>,
        val expList: List<ExpenseEntity>,
        val purchList: List<PurchaseEntity>
    )

    private val dailyData = combine(
        sales,
        saleItems,
        expenses,
        purchases
    ) { s, si, e, p -> DailyData(s, si, e, p) }

    // Dashboard daily metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        dailyData,
        supplierSummaries,
        productStockList,
        employeePayments
    ) { daily, supSummaries, stockList, empPayments ->
        val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
        val todaySales = daily.salesList.filter { it.shamsiDate == todayStr }
        val todaySalesTotal = todaySales.sumOf { it.totalAmount }

        val todayCash = todaySales.filter { it.paymentMethod.contains("نقدی") }.sumOf { it.totalAmount }
        val todayMellat = todaySales.filter { it.paymentMethod.contains("ملت") }.sumOf { it.totalAmount }
        val todayParsian = todaySales.filter { it.paymentMethod.contains("پارسیان") }.sumOf { it.totalAmount }
        val todayOther = todaySalesTotal - todayCash - todayMellat - todayParsian

        val todayExpensesTotal = daily.expList.filter { it.shamsiDate == todayStr }.sumOf { it.amount }
        val todayPurchasesTotal = daily.purchList.filter { it.shamsiDate == todayStr }.sumOf { it.totalCost }

        // Today's sold items & weight
        val todaySaleIds = todaySales.map { it.id }.toSet()
        val todaySoldItems = daily.saleItemList.filter { it.saleId in todaySaleIds }
        val todayWeight = todaySoldItems.sumOf { it.quantity }

        // Estimated profit calculation:
        val estimatedCogs = todaySoldItems.sumOf { item ->
            val prod = stockList.find { it.product.id == item.productId }?.product
            val refCost = if (prod != null && prod.referenceBuyPrice > 0) {
                prod.referenceBuyPrice
            } else {
                (item.unitPrice * 0.78).toLong()
            }
            (item.quantity * refCost).toLong()
        }
        val estimatedGrossProfit = if (todaySalesTotal > 0) {
            todaySalesTotal - estimatedCogs - todayExpensesTotal
        } else 0L

        val totalSupplierDebt = supSummaries.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
        val lowStockCount = stockList.count { it.isLowStock }

        DashboardMetrics(
            todaySalesTotal = todaySalesTotal,
            todayCashSales = todayCash,
            todayMellatSales = todayMellat,
            todayParsianSales = todayParsian,
            todayOtherSales = todayOther,
            todayExpensesTotal = todayExpensesTotal,
            todayPurchasesTotal = todayPurchasesTotal,
            todayEstimatedGrossProfit = estimatedGrossProfit,
            todayTotalWeightSold = todayWeight,
            totalSupplierDebt = totalSupplierDebt,
            lowStockCount = lowStockCount,
            pendingEmployeePaymentsCount = empPayments.filter { it.type == "ADVANCE" }.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    init {
        viewModelScope.launch {
            repository.ensureDefaultSetup()
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun setDateFilter(filterType: DateFilterType) {
        _selectedFilterType.value = filterType
        val today = JalaliCalendarUtil.getToday()
        when (filterType) {
            DateFilterType.TODAY -> {
                _customDateRange.value = Pair(today.toFormattedString(), today.toFormattedString())
            }
            DateFilterType.YESTERDAY -> {
                val y = JalaliCalendarUtil.getYesterday()
                _customDateRange.value = Pair(y.toFormattedString(), y.toFormattedString())
            }
            DateFilterType.THIS_WEEK -> {
                val week = JalaliCalendarUtil.getCurrentWeekRange()
                _customDateRange.value = Pair(week.first.toFormattedString(), week.second.toFormattedString())
            }
            DateFilterType.THIS_MONTH -> {
                val month = JalaliCalendarUtil.getCurrentMonthRange()
                _customDateRange.value = Pair(month.first.toFormattedString(), month.second.toFormattedString())
            }
            DateFilterType.THIS_YEAR -> {
                val year = JalaliCalendarUtil.getCurrentYearRange()
                _customDateRange.value = Pair(year.first.toFormattedString(), year.second.toFormattedString())
            }
            DateFilterType.CUSTOM -> {
                // Keep the current custom range
            }
            DateFilterType.ALL_TIME -> {
                _customDateRange.value = Pair("1300/01/01", "1500/12/29")
            }
        }
    }

    fun setCustomDateRange(start: String, end: String) {
        _selectedFilterType.value = DateFilterType.CUSTOM
        _customDateRange.value = Pair(start, end)
    }

    // -------------------------------------------------------------------------
    // Sales Operations
    // -------------------------------------------------------------------------
    fun registerSale(
        branchName: String,
        paymentMethod: String,
        notes: String,
        items: List<Pair<ProductEntity, Double>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                _userMessage.emit("هیچ کالایی برای فروش انتخاب نشده است")
                return@launch
            }

            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val saleItems = items.map { (prod, qty) ->
                SaleItemEntity(
                    saleId = 0,
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    unit = prod.unit,
                    unitPrice = prod.salePrice,
                    totalPrice = (qty * prod.salePrice).toLong()
                )
            }
            val totalAmount = saleItems.sumOf { it.totalPrice }

            val sale = SaleEntity(
                shamsiDate = todayStr,
                branchName = branchName,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                notes = notes
            )

            repository.recordSale(sale, saleItems)
            _userMessage.emit("فروش به مبلغ ${com.example.util.PersianFormatUtil.formatCurrencyToman(totalAmount)} با موفقیت ثبت شد")
            onSuccess()
        }
    }

    fun deleteSale(sale: SaleEntity) {
        viewModelScope.launch {
            repository.deleteSale(sale)
            _userMessage.emit("فاکتور فروش شماره ${sale.id} حذف شد")
        }
    }

    // -------------------------------------------------------------------------
    // Purchases Operations
    // -------------------------------------------------------------------------
    fun registerPurchase(
        shamsiDate: String,
        supplierId: Long,
        supplierName: String,
        productId: Long,
        productName: String,
        marketLocation: String,
        phase: String,
        weight: Double,
        packageCount: Int,
        buyPricePerKg: Long,
        allocatedBranch: String,
        dashtPerPackage: Long,
        transportCost: Long,
        unloadingCost: Long,
        commissionCost: Long,
        miscCost: Long,
        paidAmount: Long,
        paymentMethod: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (weight <= 0 || buyPricePerKg <= 0) {
                _userMessage.emit("وزن و نرخ خرید باید بزرگتر از صفر باشند")
                return@launch
            }

            val baseCost = (weight * buyPricePerKg).toLong()
            val totalDasht = dashtPerPackage * packageCount
            val totalCost = baseCost + totalDasht + transportCost + unloadingCost + commissionCost + miscCost
            val effectiveCost = if (weight > 0) (totalCost / weight).toLong() else buyPricePerKg
            val remainingDebt = (totalCost - paidAmount).coerceAtLeast(0L)

            val purchase = PurchaseEntity(
                shamsiDate = shamsiDate,
                supplierId = supplierId,
                supplierName = supplierName,
                productId = productId,
                productName = productName,
                marketLocation = marketLocation,
                phase = phase,
                weight = weight,
                packageCount = packageCount,
                buyPricePerKg = buyPricePerKg,
                baseCost = baseCost,
                allocatedBranch = allocatedBranch,
                dashtPerPackage = dashtPerPackage,
                totalDashtCost = totalDasht,
                transportCost = transportCost,
                unloadingCost = unloadingCost,
                commissionCost = commissionCost,
                miscCost = miscCost,
                totalCost = totalCost,
                effectiveCostPerKg = effectiveCost,
                paidAmount = paidAmount,
                remainingDebt = remainingDebt,
                paymentMethod = paymentMethod,
                notes = notes
            )

            repository.recordPurchase(purchase)
            _userMessage.emit("خرید بار $productName به وزن $weight کیلو با موفقیت ثبت شد")
            onSuccess()
        }
    }

    fun deletePurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            repository.deletePurchase(purchase)
            _userMessage.emit("فاکتور خرید بار حذف شد")
        }
    }

    // -------------------------------------------------------------------------
    // Products
    // -------------------------------------------------------------------------
    fun saveProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (product.name.isBlank() || product.salePrice <= 0) {
                _userMessage.emit("نام محصول و نرخ فروش الزامی است")
                return@launch
            }
            if (product.id == 0L) {
                repository.insertProduct(product)
                _userMessage.emit("محصول «${product.name}» افزوده شد")
            } else {
                repository.updateProduct(product)
                _userMessage.emit("محصول «${product.name}» بروزرسانی شد")
            }
            onSuccess()
        }
    }

    fun updateProductPrice(productId: Long, newPrice: Long) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, newPrice)
            _userMessage.emit("نرخ فروش بروزرسانی شد")
        }
    }

    fun batchUpdatePricesPercent(category: String?, percentChange: Double) {
        viewModelScope.launch {
            val list = products.value
            val targetList = if (category.isNullOrBlank() || category == "همه") list else list.filter { it.category == category }
            targetList.forEach { p ->
                val newPrice = (p.salePrice * (1.0 + percentChange / 100.0)).toLong().coerceAtLeast(1000L)
                // Round to nearest 500 Toman for clean store pricing
                val roundedPrice = (newPrice / 500L) * 500L
                repository.updateProductPrice(p.id, roundedPrice)
            }
            _userMessage.emit("تغییر دسته‌جمعی نرخ‌ها با موفقیت اعمال شد")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _userMessage.emit("محصول «${product.name}» حذف شد")
        }
    }

    // -------------------------------------------------------------------------
    // Inventory Adjustments & Spoilage
    // -------------------------------------------------------------------------
    fun recordAdjustment(
        productId: Long,
        productName: String,
        quantity: Double,
        reason: String,
        branchName: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val adj = InventoryAdjustmentEntity(
                productId = productId,
                productName = productName,
                shamsiDate = todayStr,
                quantity = quantity,
                reason = reason,
                branchName = branchName,
                notes = notes
            )
            repository.recordInventoryAdjustment(adj)
            val msg = if (quantity < 0) "ضایعات و افت بار ثبت شد" else "اصلاح موجودی ثبت شد"
            _userMessage.emit(msg)
            onSuccess()
        }
    }

    // -------------------------------------------------------------------------
    // Expenses
    // -------------------------------------------------------------------------
    fun recordExpense(
        title: String,
        category: String,
        amount: Long,
        paymentMethod: String,
        branchName: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (title.isBlank() || amount <= 0) {
                _userMessage.emit("عنوان هزینه و مبلغ باید مشخص باشد")
                return@launch
            }
            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val exp = ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                shamsiDate = todayStr,
                paymentMethod = paymentMethod,
                branchName = branchName,
                notes = notes
            )
            repository.recordExpense(exp)
            _userMessage.emit("هزینه با موفقیت ثبت گردید")
            onSuccess()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.emit("هزینه حذف شد")
        }
    }

    fun addExpenseCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertExpenseCategory(ExpenseCategoryEntity(name = name.trim()))
                _userMessage.emit("دسته‌بندی «$name» افزوده شد")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Suppliers & Ledger
    // -------------------------------------------------------------------------
    fun saveSupplier(supplier: SupplierEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (supplier.name.isBlank()) {
                _userMessage.emit("نام فروشنده الزامی است")
                return@launch
            }
            if (supplier.id == 0L) {
                repository.insertSupplier(supplier)
                _userMessage.emit("فروشنده جدید ثبت شد")
            } else {
                repository.updateSupplier(supplier)
                _userMessage.emit("اطلاعات فروشنده بروزرسانی شد")
            }
            onSuccess()
        }
    }

    fun recordSupplierPayment(
        supplierId: Long,
        amount: Long,
        paymentMethod: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("مبلغ پرداختی باید بزرگتر از صفر باشد")
                return@launch
            }
            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val tx = SupplierTransactionEntity(
                supplierId = supplierId,
                shamsiDate = todayStr,
                type = "PAYMENT",
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes.ifBlank { "پرداخت وجه به فروشنده" }
            )
            repository.recordSupplierTransaction(tx)
            _userMessage.emit("پرداخت به فروشنده ثبت شد")
            onSuccess()
        }
    }

    // -------------------------------------------------------------------------
    // Employees & Payroll
    // -------------------------------------------------------------------------
    fun saveEmployee(employee: EmployeeEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (employee.fullName.isBlank() || employee.weeklyWage <= 0) {
                _userMessage.emit("نام کارمند و حقوق هفتگی الزامی است")
                return@launch
            }
            if (employee.id == 0L) {
                repository.insertEmployee(employee)
                _userMessage.emit("کارمند جدید افزوده شد")
            } else {
                repository.updateEmployee(employee)
                _userMessage.emit("اطلاعات کارمند بروزرسانی شد")
            }
            onSuccess()
        }
    }

    fun recordAttendance(employeeId: Long, date: String, status: String) {
        viewModelScope.launch {
            repository.recordAttendance(
                EmployeeAttendanceEntity(
                    employeeId = employeeId,
                    shamsiDate = date,
                    status = status
                )
            )
            _userMessage.emit("وضعیت حضور و غیاب ثبت شد")
        }
    }

    fun recordEmployeePayment(
        employeeId: Long,
        type: String, // ADVANCE, DEDUCTION, BONUS, SALARY_SETTLEMENT
        amount: Long,
        forWeek: String,
        paymentMethod: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("مبلغ باید مشخص باشد")
                return@launch
            }
            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val payment = EmployeePaymentEntity(
                employeeId = employeeId,
                shamsiDate = todayStr,
                type = type,
                amount = amount,
                forWeekShamsi = forWeek,
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.recordEmployeePayment(payment)
            _userMessage.emit("تراکنش مالی پرسنل ثبت شد")
            onSuccess()
        }
    }

    // -------------------------------------------------------------------------
    // Store Config (Branches & Payment Methods)
    // -------------------------------------------------------------------------
    fun addBranch(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertBranch(BranchEntity(name = name.trim()))
                _userMessage.emit("محل / شعبه «$name» افزوده شد")
            }
        }
    }

    fun addPaymentMethod(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.insertPaymentMethod(PaymentMethodEntity(name = name.trim()))
                _userMessage.emit("روش پرداخت «$name» افزوده شد")
            }
        }
    }

    fun updateStoreSettings(settings: StoreSettingsEntity) {
        viewModelScope.launch {
            repository.saveStoreSettings(settings)
            _userMessage.emit("تنظیمات فروشگاه ذخیره شد")
        }
    }

    // -------------------------------------------------------------------------
    // Daily Receipts & Settlement (تسویه و انطباق صندوق و پوز)
    // -------------------------------------------------------------------------
    fun recordDailyReceipt(
        shamsiDate: String,
        branchName: String,
        posMellat: Long,
        posParsian: Long,
        cash: Long,
        cardToCard: Long,
        other: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val receipt = DailyReceiptEntity(
                shamsiDate = shamsiDate,
                branchName = branchName,
                posMellat = posMellat,
                posParsian = posParsian,
                cash = cash,
                cardToCard = cardToCard,
                other = other,
                notes = notes
            )
            repository.recordDailyReceipt(receipt)
            _userMessage.emit("دریافتی‌های روزانه و بستن صندوق ثبت شد")
            onSuccess()
        }
    }

    fun deleteDailyReceipt(receipt: DailyReceiptEntity) {
        viewModelScope.launch {
            repository.deleteDailyReceipt(receipt)
            _userMessage.emit("گزارش دریافتی صندوق حذف گردید")
        }
    }

    fun addPosTerminal(name: String, bankName: String = "", terminalNumber: String = "") {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("نام کارتخوان الزامی است")
                return@launch
            }
            repository.insertPosTerminal(
                PosTerminalEntity(
                    name = name.trim(),
                    bankName = bankName.trim(),
                    terminalNumber = terminalNumber.trim()
                )
            )
            _userMessage.emit("کارتخوان «$name» با موفقیت افزوده شد")
        }
    }

    fun updatePosTerminal(terminal: PosTerminalEntity) {
        viewModelScope.launch {
            if (terminal.name.isBlank()) {
                _userMessage.emit("نام کارتخوان الزامی است")
                return@launch
            }
            repository.updatePosTerminal(terminal)
            _userMessage.emit("اطلاعات کارتخوان «${terminal.name}» بروزرسانی شد")
        }
    }

    fun togglePosTerminalActive(terminal: PosTerminalEntity) {
        viewModelScope.launch {
            val updated = terminal.copy(isActive = !terminal.isActive)
            repository.updatePosTerminal(updated)
            val stateText = if (updated.isActive) "فعال" else "غیرفعال"
            _userMessage.emit("کارتخوان «${terminal.name}» $stateText شد")
        }
    }

    fun saveDailySettlement(
        shamsiDate: String,
        branchName: String,
        cash: Long,
        cardToCard: Long,
        other: Long,
        posEntries: List<DailyReceiptPosEntryEntity>,
        notes: String = "",
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val receipt = DailyReceiptEntity(
                shamsiDate = shamsiDate,
                branchName = branchName,
                cash = cash,
                cardToCard = cardToCard,
                other = other,
                notes = notes,
                posTotal = posEntries.sumOf { it.amount },
                isSettled = true
            )
            val savedId = repository.saveDailySettlement(receipt, posEntries)
            _userMessage.emit("تسویه پایان روز ($shamsiDate - $branchName) با موفقیت ذخیره شد")
            onSuccess(savedId)
        }
    }

    suspend fun getSettlementData(shamsiDate: String, branchName: String): Pair<DailyReceiptEntity?, List<DailyReceiptPosEntryEntity>> {
        val receipt = repository.getDailyReceiptDirect(shamsiDate, branchName)
        val entries = repository.getPosEntriesForDateAndBranchDirect(shamsiDate, branchName)
        return Pair(receipt, entries)
    }

    // -------------------------------------------------------------------------
    // Partners & Capital (شرکا و تقسیم سود)
    // -------------------------------------------------------------------------
    fun savePartner(partner: com.example.data.local.entities.PartnerEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (partner.fullName.isBlank()) {
                _userMessage.emit("نام شریک الزامی است")
                return@launch
            }
            if (partner.id == 0L) {
                repository.insertPartner(partner)
                _userMessage.emit("شریک جدید افزوده شد")
            } else {
                repository.updatePartner(partner)
                _userMessage.emit("اطلاعات شریک بروزرسانی شد")
            }
            onSuccess()
        }
    }

    fun deletePartner(partner: com.example.data.local.entities.PartnerEntity) {
        viewModelScope.launch {
            repository.deletePartner(partner)
            _userMessage.emit("شریک حذف شد")
        }
    }

    fun recordPartnerTransaction(
        partnerId: Long,
        type: String,
        amount: Long,
        paymentMethod: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _userMessage.emit("مبلغ باید مشخص باشد")
                return@launch
            }
            val todayStr = JalaliCalendarUtil.getToday().toFormattedString()
            val tx = com.example.data.local.entities.PartnerTransactionEntity(
                partnerId = partnerId,
                shamsiDate = todayStr,
                type = type,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.recordPartnerTransaction(tx)
            _userMessage.emit("تراکنش مالی شریک ثبت شد")
            onSuccess()
        }
    }

    fun deletePartnerTransaction(tx: com.example.data.local.entities.PartnerTransactionEntity) {
        viewModelScope.launch {
            repository.deletePartnerTransaction(tx)
            _userMessage.emit("تراکنش شریک حذف شد")
        }
    }

    // -------------------------------------------------------------------------
    // Demo Data Management & Reset
    // -------------------------------------------------------------------------
    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadComprehensiveDemoData()
            _userMessage.emit("اطلاعات نمونه آزمایشی با موفقیت بارگذاری شد")
        }
    }

    fun clearTestData() {
        viewModelScope.launch {
            repository.clearTransactionalData()
            _userMessage.emit("تمام تراکنش‌های آزمایشی پاکسازی شد")
        }
    }

    // -------------------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------------------
    suspend fun getBackupJson(): String {
        return BackupRestoreUtil.createBackupJson(db)
    }

    fun restoreBackup(jsonContent: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = BackupRestoreUtil.restoreBackupFromJson(jsonContent, db)
            if (success) {
                _userMessage.emit("اطلاعات پشتیبان با موفقیت بازیابی شد")
            } else {
                _userMessage.emit("خطا در بازیابی فایل پشتیبان. فرمت نامعتبر است")
            }
            onComplete(success)
        }
    }
}

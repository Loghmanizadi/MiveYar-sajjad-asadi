package com.example.util

import android.content.Context
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
import com.example.data.local.entities.StoreSettingsEntity
import com.example.data.local.entities.SupplierEntity
import com.example.data.local.entities.SupplierTransactionEntity
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupRestoreUtil {

    private const val BACKUP_VERSION = 1

    suspend fun createBackupJson(db: AppDatabase): String {
        val root = JSONObject()
        root.put("app", "MiveYar")
        root.put("version", BACKUP_VERSION)
        root.put("backupDateShamsi", JalaliCalendarUtil.getToday().toFormattedString())
        root.put("timestamp", System.currentTimeMillis())

        // 1. Products
        val products = db.productDao().getAllProducts().firstOrNull() ?: emptyList()
        val productsArr = JSONArray()
        products.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("category", p.category)
            obj.put("unit", p.unit)
            obj.put("salePrice", p.salePrice)
            obj.put("referenceBuyPrice", p.referenceBuyPrice)
            obj.put("minStockAlert", p.minStockAlert)
            obj.put("description", p.description)
            obj.put("isActive", p.isActive)
            productsArr.put(obj)
        }
        root.put("products", productsArr)

        // 2. Sales & Items
        val sales = db.saleDao().getAllSales().firstOrNull() ?: emptyList()
        val salesArr = JSONArray()
        sales.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("shamsiDate", s.shamsiDate)
            obj.put("timestamp", s.timestamp)
            obj.put("branchName", s.branchName)
            obj.put("totalAmount", s.totalAmount)
            obj.put("paymentMethod", s.paymentMethod)
            obj.put("notes", s.notes)

            val items = db.saleDao().getSaleItemsBySaleIdDirect(s.id)
            val itemsArr = JSONArray()
            items.forEach { item ->
                val iObj = JSONObject()
                iObj.put("productId", item.productId)
                iObj.put("productName", item.productName)
                iObj.put("quantity", item.quantity)
                iObj.put("unit", item.unit)
                iObj.put("unitPrice", item.unitPrice)
                iObj.put("totalPrice", item.totalPrice)
                itemsArr.put(iObj)
            }
            obj.put("items", itemsArr)
            salesArr.put(obj)
        }
        root.put("sales", salesArr)

        // 3. Purchases
        val purchases = db.purchaseDao().getAllPurchases().firstOrNull() ?: emptyList()
        val purchasesArr = JSONArray()
        purchases.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("shamsiDate", p.shamsiDate)
            obj.put("timestamp", p.timestamp)
            obj.put("supplierId", p.supplierId)
            obj.put("supplierName", p.supplierName)
            obj.put("productId", p.productId)
            obj.put("productName", p.productName)
            obj.put("marketLocation", p.marketLocation)
            obj.put("phase", p.phase)
            obj.put("weight", p.weight)
            obj.put("packageCount", p.packageCount)
            obj.put("buyPricePerKg", p.buyPricePerKg)
            obj.put("baseCost", p.baseCost)
            obj.put("allocatedBranch", p.allocatedBranch)
            obj.put("dashtPerPackage", p.dashtPerPackage)
            obj.put("totalDashtCost", p.totalDashtCost)
            obj.put("transportCost", p.transportCost)
            obj.put("unloadingCost", p.unloadingCost)
            obj.put("commissionCost", p.commissionCost)
            obj.put("miscCost", p.miscCost)
            obj.put("totalCost", p.totalCost)
            obj.put("effectiveCostPerKg", p.effectiveCostPerKg)
            obj.put("paidAmount", p.paidAmount)
            obj.put("remainingDebt", p.remainingDebt)
            obj.put("paymentMethod", p.paymentMethod)
            obj.put("notes", p.notes)
            purchasesArr.put(obj)
        }
        root.put("purchases", purchasesArr)

        // 4. Suppliers & Transactions
        val suppliers = db.supplierDao().getAllSuppliers().firstOrNull() ?: emptyList()
        val suppliersArr = JSONArray()
        suppliers.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("phone", s.phone)
            obj.put("location", s.location)
            obj.put("initialDebt", s.initialDebt)
            obj.put("notes", s.notes)
            obj.put("isActive", s.isActive)
            suppliersArr.put(obj)
        }
        root.put("suppliers", suppliersArr)

        val transactions = db.supplierDao().getAllTransactions().firstOrNull() ?: emptyList()
        val txArr = JSONArray()
        transactions.forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("supplierId", t.supplierId)
            obj.put("shamsiDate", t.shamsiDate)
            obj.put("timestamp", t.timestamp)
            obj.put("type", t.type)
            obj.put("amount", t.amount)
            obj.put("paymentMethod", t.paymentMethod)
            obj.put("relatedPurchaseId", t.relatedPurchaseId ?: -1L)
            obj.put("notes", t.notes)
            txArr.put(obj)
        }
        root.put("supplierTransactions", txArr)

        // 5. Inventory Adjustments
        val adjustments = db.inventoryDao().getAllAdjustments().firstOrNull() ?: emptyList()
        val adjArr = JSONArray()
        adjustments.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("productId", a.productId)
            obj.put("productName", a.productName)
            obj.put("shamsiDate", a.shamsiDate)
            obj.put("timestamp", a.timestamp)
            obj.put("quantity", a.quantity)
            obj.put("reason", a.reason)
            obj.put("branchName", a.branchName)
            obj.put("notes", a.notes)
            adjArr.put(obj)
        }
        root.put("inventoryAdjustments", adjArr)

        // 6. Expenses
        val expenses = db.expenseDao().getAllExpenses().firstOrNull() ?: emptyList()
        val expArr = JSONArray()
        expenses.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("title", e.title)
            obj.put("category", e.category)
            obj.put("amount", e.amount)
            obj.put("shamsiDate", e.shamsiDate)
            obj.put("timestamp", e.timestamp)
            obj.put("paymentMethod", e.paymentMethod)
            obj.put("branchName", e.branchName)
            obj.put("notes", e.notes)
            expArr.put(obj)
        }
        root.put("expenses", expArr)

        // 7. Employees, Attendance, Payments
        val employees = db.employeeDao().getAllEmployees().firstOrNull() ?: emptyList()
        val empArr = JSONArray()
        employees.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("fullName", e.fullName)
            obj.put("phone", e.phone)
            obj.put("weeklyWage", e.weeklyWage)
            obj.put("startDateShamsi", e.startDateShamsi)
            obj.put("isActive", e.isActive)
            obj.put("notes", e.notes)
            empArr.put(obj)
        }
        root.put("employees", empArr)

        val payments = db.employeeDao().getAllPayments().firstOrNull() ?: emptyList()
        val payArr = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("employeeId", p.employeeId)
            obj.put("shamsiDate", p.shamsiDate)
            obj.put("timestamp", p.timestamp)
            obj.put("type", p.type)
            obj.put("amount", p.amount)
            obj.put("forWeekShamsi", p.forWeekShamsi)
            obj.put("paymentMethod", p.paymentMethod)
            obj.put("notes", p.notes)
            payArr.put(obj)
        }
        root.put("employeePayments", payArr)

        return root.toString(2)
    }

    suspend fun restoreBackupFromJson(jsonString: String, db: AppDatabase): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || root.getString("app") != "MiveYar") {
                return false
            }

            // Restore products
            if (root.has("products")) {
                val arr = root.getJSONArray("products")
                val products = mutableListOf<ProductEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    products.add(
                        ProductEntity(
                            id = o.optLong("id", 0),
                            name = o.getString("name"),
                            category = o.optString("category", "میوه"),
                            unit = o.optString("unit", "کیلوگرم"),
                            salePrice = o.getLong("salePrice"),
                            referenceBuyPrice = o.optLong("referenceBuyPrice", 0),
                            minStockAlert = o.optDouble("minStockAlert", 15.0),
                            description = o.optString("description", ""),
                            isActive = o.optBoolean("isActive", true)
                        )
                    )
                }
                if (products.isNotEmpty()) {
                    db.productDao().clearAllProducts()
                    db.productDao().insertAll(products)
                }
            }

            // Restore sales
            if (root.has("sales")) {
                val arr = root.getJSONArray("sales")
                db.saleDao().clearAllSales()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val sale = SaleEntity(
                        shamsiDate = o.getString("shamsiDate"),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        branchName = o.optString("branchName", "شعبه اصلی"),
                        totalAmount = o.getLong("totalAmount"),
                        paymentMethod = o.optString("paymentMethod", "کارتخوان"),
                        notes = o.optString("notes", "")
                    )
                    val items = mutableListOf<SaleItemEntity>()
                    if (o.has("items")) {
                        val iArr = o.getJSONArray("items")
                        for (j in 0 until iArr.length()) {
                            val io = iArr.getJSONObject(j)
                            items.add(
                                SaleItemEntity(
                                    saleId = 0,
                                    productId = io.optLong("productId", 0),
                                    productName = io.getString("productName"),
                                    quantity = io.getDouble("quantity"),
                                    unit = io.optString("unit", "کیلوگرم"),
                                    unitPrice = io.getLong("unitPrice"),
                                    totalPrice = io.getLong("totalPrice")
                                )
                            )
                        }
                    }
                    db.saleDao().recordCompleteSale(sale, items)
                }
            }

            // Restore purchases
            if (root.has("purchases")) {
                val arr = root.getJSONArray("purchases")
                db.purchaseDao().clearAllPurchases()
                val purchases = mutableListOf<PurchaseEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    purchases.add(
                        PurchaseEntity(
                            id = o.optLong("id", 0),
                            shamsiDate = o.getString("shamsiDate"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            supplierId = o.optLong("supplierId", 0),
                            supplierName = o.getString("supplierName"),
                            productId = o.optLong("productId", 0),
                            productName = o.getString("productName"),
                            marketLocation = o.optString("marketLocation", "میدان کرج"),
                            phase = o.optString("phase", "فاز ۱"),
                            weight = o.getDouble("weight"),
                            packageCount = o.optInt("packageCount", 0),
                            buyPricePerKg = o.getLong("buyPricePerKg"),
                            baseCost = o.getLong("baseCost"),
                            allocatedBranch = o.optString("allocatedBranch", "شعبه اصلی"),
                            dashtPerPackage = o.optLong("dashtPerPackage", 0),
                            totalDashtCost = o.optLong("totalDashtCost", 0),
                            transportCost = o.optLong("transportCost", 0),
                            unloadingCost = o.optLong("unloadingCost", 0),
                            commissionCost = o.optLong("commissionCost", 0),
                            miscCost = o.optLong("miscCost", 0),
                            totalCost = o.getLong("totalCost"),
                            effectiveCostPerKg = o.getLong("effectiveCostPerKg"),
                            paidAmount = o.optLong("paidAmount", 0),
                            remainingDebt = o.optLong("remainingDebt", 0),
                            paymentMethod = o.optString("paymentMethod", "نقدی"),
                            notes = o.optString("notes", "")
                        )
                    )
                }
                if (purchases.isNotEmpty()) {
                    db.purchaseDao().insertAll(purchases)
                }
            }

            // Restore suppliers
            if (root.has("suppliers")) {
                val arr = root.getJSONArray("suppliers")
                db.supplierDao().clearAllSuppliers()
                val suppliers = mutableListOf<SupplierEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    suppliers.add(
                        SupplierEntity(
                            id = o.optLong("id", 0),
                            name = o.getString("name"),
                            phone = o.optString("phone", ""),
                            location = o.optString("location", "میدان کرج"),
                            initialDebt = o.optLong("initialDebt", 0),
                            notes = o.optString("notes", ""),
                            isActive = o.optBoolean("isActive", true)
                        )
                    )
                }
                if (suppliers.isNotEmpty()) {
                    db.supplierDao().insertAllSuppliers(suppliers)
                }
            }

            // Restore expenses
            if (root.has("expenses")) {
                val arr = root.getJSONArray("expenses")
                db.expenseDao().clearAllExpenses()
                val expenses = mutableListOf<ExpenseEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    expenses.add(
                        ExpenseEntity(
                            id = o.optLong("id", 0),
                            title = o.getString("title"),
                            category = o.optString("category", "سایر"),
                            amount = o.getLong("amount"),
                            shamsiDate = o.getString("shamsiDate"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                            paymentMethod = o.optString("paymentMethod", "نقدی"),
                            branchName = o.optString("branchName", "شعبه اصلی"),
                            notes = o.optString("notes", "")
                        )
                    )
                }
                if (expenses.isNotEmpty()) {
                    db.expenseDao().insertAllExpenses(expenses)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Generates CSV for Sales Report
     */
    fun generateSalesCsv(sales: List<SaleEntity>): String {
        val sb = StringBuilder()
        sb.append("شناسه,تاریخ شمسی,شعبه,مبلغ کل (تومان),روش پرداخت,توضیحات\n")
        sales.forEach { s ->
            sb.append("${s.id},${s.shamsiDate},${s.branchName},${s.totalAmount},${s.paymentMethod},\"${s.notes}\"\n")
        }
        return sb.toString()
    }

    /**
     * Generates CSV for Purchases Report
     */
    fun generatePurchasesCsv(purchases: List<PurchaseEntity>): String {
        val sb = StringBuilder()
        sb.append("شناسه,تاریخ شمسی,فروشنده,محصول,وزن (کیلو),نرخ خرید,هزینه کل,پرداخت شده,باقی مانده,میدان\n")
        purchases.forEach { p ->
            sb.append("${p.id},${p.shamsiDate},\"${p.supplierName}\",\"${p.productName}\",${p.weight},${p.buyPricePerKg},${p.totalCost},${p.paidAmount},${p.remainingDebt},\"${p.marketLocation}\"\n")
        }
        return sb.toString()
    }

    /**
     * Generates CSV for Expenses Report
     */
    fun generateExpensesCsv(expenses: List<ExpenseEntity>): String {
        val sb = StringBuilder()
        sb.append("شناسه,تاریخ شمسی,عنوان هزینه,دسته‌بندی,مبلغ (تومان),روش پرداخت,شعبه,توضیحات\n")
        expenses.forEach { e ->
            sb.append("${e.id},${e.shamsiDate},\"${e.title}\",\"${e.category}\",${e.amount},${e.paymentMethod},${e.branchName},\"${e.notes}\"\n")
        }
        return sb.toString()
    }
}

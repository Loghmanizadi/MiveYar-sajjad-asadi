package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.PartnerEntity
import com.example.data.local.entities.PartnerTransactionEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.DateFilterType
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.BackupRestoreUtil
import com.example.util.JalaliCalendarUtil
import com.example.util.PersianFormatUtil
import com.example.util.PdfReportUtil
import com.example.util.SmsReportUtil

@Composable
fun ReportsScreen(viewModel: FruitShopViewModel) {
    val context = LocalContext.current

    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val employeePayments by viewModel.employeePayments.collectAsStateWithLifecycle()
    val employeeAttendances by viewModel.employeeAttendances.collectAsStateWithLifecycle()
    val dailyReceipts by viewModel.dailyReceipts.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val partners by viewModel.partners.collectAsStateWithLifecycle()
    val partnerTransactions by viewModel.partnerTransactions.collectAsStateWithLifecycle()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()

    val selectedFilter by viewModel.selectedFilterType.collectAsStateWithLifecycle()
    val customRange by viewModel.customDateRange.collectAsStateWithLifecycle()

    // 0: گزارش فروش جامع, 1: گزارش روزانه شرکا, 2: عملکرد پرسنل, 3: هزینه‌های حاشیه و غرفه, 4: فروش هر محصول, 5: خرید و مقایسه اوزان
    var activeTab by remember { mutableStateOf(0) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var startDateInput by remember { mutableStateOf(customRange.first) }
    var endDateInput by remember { mutableStateOf(customRange.second) }

    // Branch filter state (همه غرفه‌ها or specific branch)
    var selectedBranchFilter by remember { mutableStateOf("همه غرفه‌ها") }

    // Dialog state for SMS
    var showSmsDialog by remember { mutableStateOf(false) }
    var smsTargetPhone by remember { mutableStateOf("") }
    var smsMessageBody by remember { mutableStateOf("") }
    var smsDialogTitle by remember { mutableStateOf("ارسال گزارش پیامکی") }

    // Dialog state for partner add/edit & transactions
    var showPartnerDialog by remember { mutableStateOf(false) }
    var editingPartner by remember { mutableStateOf<PartnerEntity?>(null) }
    var partnerNameInput by remember { mutableStateOf("") }
    var partnerPhoneInput by remember { mutableStateOf("") }
    var partnerShareInput by remember { mutableStateOf("") }
    var partnerNotesInput by remember { mutableStateOf("") }

    var showPartnerTxDialog by remember { mutableStateOf(false) }
    var targetPartnerForTx by remember { mutableStateOf<PartnerEntity?>(null) }
    var txTypeInput by remember { mutableStateOf("WITHDRAWAL") } // WITHDRAWAL or DEPOSIT
    var txAmountInput by remember { mutableStateOf("") }
    var txNotesInput by remember { mutableStateOf("") }

    // Toggle for previous period comparison
    var showComparisonWithPrevPeriod by remember { mutableStateOf(true) }

    // Product sort in Tab 4 (0: بر اساس بیشترین مبلغ, 1: بر اساس بیشترین وزن)
    var productSortMode by remember { mutableStateOf(0) }

    // Filtered data based on chosen range & branch
    val startDate = customRange.first
    val endDate = customRange.second

    val isAllBranches = selectedBranchFilter == "همه غرفه‌ها" || selectedBranchFilter == "همه"

    val filteredSales = sales.filter { 
        it.shamsiDate in startDate..endDate && (isAllBranches || it.branchName == selectedBranchFilter) 
    }
    val filteredSaleIds = filteredSales.map { it.id }.toSet()
    val filteredSaleItems = saleItems.filter { it.saleId in filteredSaleIds }
    val filteredPurchases = purchases.filter { it.shamsiDate in startDate..endDate }
    val filteredExpenses = expenses.filter { 
        it.shamsiDate in startDate..endDate && (isAllBranches || it.branchName == selectedBranchFilter) 
    }
    val filteredReceipts = dailyReceipts.filter { 
        it.shamsiDate in startDate..endDate && (isAllBranches || it.branchName == selectedBranchFilter) 
    }
    val filteredPayments = employeePayments.filter { it.shamsiDate in startDate..endDate }
    val filteredAttendances = employeeAttendances.filter { it.shamsiDate in startDate..endDate }
    val filteredPartnerTransactions = partnerTransactions.filter { it.shamsiDate in startDate..endDate }

    // Aggregate Calculations for current period
    val totalSalesAmount = filteredSales.sumOf { it.totalAmount }
    val weightedItems = filteredSaleItems.filter { it.isWeighted }
    val nonWeightedItems = filteredSaleItems.filter { !it.isWeighted }

    val totalWeightSold = weightedItems.sumOf { it.quantity }
    val totalWeightedSalesAmount = weightedItems.sumOf { it.totalPrice }
    val totalNonWeightedSalesAmount = nonWeightedItems.sumOf { it.totalPrice }
    val totalRowsCount = filteredSaleItems.size
    val totalInvoicesCount = filteredSales.size

    val totalPurchasesAmount = filteredPurchases.sumOf { it.totalCost }
    val totalWeightPurchased = filteredPurchases.sumOf { it.weight }
    val totalExpensesAmount = filteredExpenses.sumOf { it.amount }

    // Payment methods breakdown (ملت، پارسیان، نقد، کارت‌به‌کارت و سایر)
    val totalMellat = filteredSales.filter { it.paymentMethod.contains("ملت") }.sumOf { it.totalAmount }
    val totalParsian = filteredSales.filter { it.paymentMethod.contains("پارسیان") }.sumOf { it.totalAmount }
    val totalCash = filteredSales.filter { it.paymentMethod.contains("نقدی") || it.paymentMethod == "نقد" }.sumOf { it.totalAmount }
    val totalCardToCard = filteredSales.filter { it.paymentMethod.contains("کارت به کارت") || it.paymentMethod.contains("کارت‌به‌کارت") }.sumOf { it.totalAmount }
    val totalOtherPayments = totalSalesAmount - (totalMellat + totalParsian + totalCash + totalCardToCard)

    // Daily receipts recorded (مجموع دریافتی‌های ثبت‌شده در انطباق صندوق)
    val totalReceiptsDeposited: Long = filteredReceipts.sumOf { it.totalActualReceipts }
    val receiptDates = filteredReceipts.map { it.shamsiDate }.toSet()
    val matchingSalesAmountForReceipts: Long = filteredSales.filter { it.shamsiDate in receiptDates }.sumOf { it.totalAmount }
    val hasReceiptsData = filteredReceipts.isNotEmpty()
    val reconciliationDiff: Long = if (hasReceiptsData) {
        totalReceiptsDeposited - matchingSalesAmountForReceipts
    } else {
        0L
    }

    // Previous period comparison
    val prevPeriod = JalaliCalendarUtil.getPreviousPeriod(startDate, endDate)
    val prevSales = if (prevPeriod != null) {
        sales.filter { it.shamsiDate in prevPeriod.first..prevPeriod.second }
    } else emptyList()
    val prevTotalSalesAmount = prevSales.sumOf { it.totalAmount }
    val prevSalesDiff = totalSalesAmount - prevTotalSalesAmount
    val prevSalesGrowthPct = if (prevTotalSalesAmount > 0) {
        ((prevSalesDiff.toDouble() / prevTotalSalesAmount) * 100).toInt()
    } else null

    // Cost of goods sold (COGS) estimation & profit
    val estimatedCogs = filteredSaleItems.sumOf { item ->
        val prod = products.find { it.id == item.productId }
        val unitCost = if (prod != null && prod.referenceBuyPrice > 0) prod.referenceBuyPrice else (item.unitPrice * 0.78).toLong()
        (item.quantity * unitCost).toLong()
    }
    val estimatedGrossProfit = if (totalSalesAmount > 0) totalSalesAmount - estimatedCogs - totalExpensesAmount else 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Date Range Filter Selector (روز، هفته، ماه، سال و بازه دلخواه)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بازه گزارش: ${selectedFilter.title}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldPrimary
                        )

                        TextButton(onClick = {
                            startDateInput = customRange.first
                            endDateInput = customRange.second
                            showCustomDateDialog = true
                        }) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتخاب بازه دلخواه", fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "از تاریخ شمسی $startDate تا $endDate" + if (!isAllBranches) " • غرفه: $selectedBranchFilter" else "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DateFilterType.values()) { type ->
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = {
                                    if (type == DateFilterType.CUSTOM) {
                                        showCustomDateDialog = true
                                    } else {
                                        viewModel.setDateFilter(type)
                                    }
                                },
                                label = { Text(type.title, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // فیلتر غرفه و شعبه
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فیلتر غرفه / شعبه:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedBranchFilter == "همه غرفه‌ها",
                                onClick = { selectedBranchFilter = "همه غرفه‌ها" },
                                label = { Text("همه غرفه‌ها", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(branches) { branch ->
                            FilterChip(
                                selected = selectedBranchFilter == branch.name,
                                onClick = { selectedBranchFilter = branch.name },
                                label = { Text(branch.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Report Sub-tabs (شامل گزارش‌های جامع فروش، شرکا، پرسنل، هزینه‌های غرفه و محصولات)
        item {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent
            ) {
                val tabTitles = listOf(
                    "گزارش جامع فروش",
                    "گزارش روزانه شرکا",
                    "عملکرد پرسنل",
                    "هزینه‌های حاشیه و غرفه",
                    "فروش هر محصول",
                    "خرید و مقایسه اوزان"
                )
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }
            }
        }

        // =====================================================================
        // TAB 0: گزارش فروش جامع (روز، هفته، ماه، سال، دریافتی‌ها، تطبیق، شعب)
        // =====================================================================
        if (activeTab == 0) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "خلاصه کل فروش در دوره انتخابی",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("جمع کل فروش ثبت‌شده:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalSalesAmount), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("فروش وزنی (${PersianFormatUtil.formatWeightKg(totalWeightSold)}):", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalWeightedSalesAmount), color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("فروش غیروزنی (بسته‌ای/مبلغی):", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalNonWeightedSalesAmount), color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تعداد فاکتورها و ردیف‌های اقلام:", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            Text("${PersianFormatUtil.toPersianDigits(totalInvoicesCount.toString())} فاکتور (${PersianFormatUtil.toPersianDigits(totalRowsCount.toString())} ردیف کالا)", color = Color.White)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color.White.copy(alpha = 0.3f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("سود ناخالص تخمینی دوره:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = PersianFormatUtil.formatCurrencyToman(estimatedGrossProfit),
                                color = if (estimatedGrossProfit >= 0) Color(0xFFC7EED8) else Color(0xFFFFB4AB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Payment Methods Breakdown (تفکیک روش‌های دریافتی)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تفکیک دریافتی‌ها به روش‌های پرداخت",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        listOf(
                            Triple("کارتخوان ملت", totalMellat, Color(0xFFC62828)),
                            Triple("کارتخوان پارسیان", totalParsian, Color(0xFF1565C0)),
                            Triple("وجه نقدی", totalCash, EmeraldPrimary),
                            Triple("کارت‌به‌کارت", totalCardToCard, GoldSecondary),
                            Triple("سایر روش‌ها", totalOtherPayments, Color.Gray)
                        ).forEach { (title, amount, color) ->
                            if (amount > 0 || totalSalesAmount == 0L) {
                                val pct = if (totalSalesAmount > 0) (amount * 100 / totalSalesAmount) else 0L
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, shape = RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("$title (${PersianFormatUtil.toPersianDigits(pct.toString())}٪):", fontSize = 13.sp)
                                    }
                                    Text(PersianFormatUtil.formatCurrencyToman(amount), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (hasReceiptsData) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("اختلاف تطبیق واریزی با فروش سیستم:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                val diffColor = when {
                                    reconciliationDiff == 0L -> ProfitGreen
                                    reconciliationDiff > 0 -> ProfitGreen
                                    else -> LossRed
                                }
                                val sign = if (reconciliationDiff > 0) "+" else ""
                                Text(
                                    "$sign${PersianFormatUtil.formatCurrencyToman(reconciliationDiff)}",
                                    fontWeight = FontWeight.Bold,
                                    color = diffColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Sales Breakdown by Stall / Branch (فروش به تفکیک غرفه یا شعبه)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فروش به تفکیک غرفه / شعبه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        val branchSalesMap = filteredSales.groupBy { it.branchName }
                        if (branchSalesMap.isEmpty()) {
                            Text("داده‌ای برای تفکیک شعب در این دوره یافت نشد.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            branchSalesMap.forEach { (branchName, list) ->
                                val sum = list.sumOf { it.totalAmount }
                                val pct = if (totalSalesAmount > 0) (sum * 100 / totalSalesAmount) else 0L
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("$branchName (${PersianFormatUtil.toPersianDigits(pct.toString())}٪ کل):", fontSize = 13.sp)
                                    Text(PersianFormatUtil.formatCurrencyToman(sum), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Day-by-Day or Period Breakdown (بررسی روزبه‌روز فروش در طول دوره)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "روند روز‌به‌روز فروش در این دوره",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val dailyGroup = filteredSales.groupBy { it.shamsiDate }.toSortedMap()
                        if (dailyGroup.isEmpty()) {
                            Text("فروشی در روزهای این بازه ثبت نشده است.", color = Color.Gray, fontSize = 12.sp)
                        } else {
                            dailyGroup.forEach { (dateStr, daySales) ->
                                val dayTotal = daySales.sumOf { it.totalAmount }
                                val dayInvoices = daySales.size
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$dateStr (${PersianFormatUtil.toPersianDigits(dayInvoices.toString())} فاکتور)", fontSize = 12.sp)
                                    Text(PersianFormatUtil.formatCurrencyToman(dayTotal), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Comparison with Previous Period (مقایسه با دوره قبلی هم‌طول)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = GoldSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مقایسه با دوره قبل هم‌طول",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Checkbox(
                                checked = showComparisonWithPrevPeriod,
                                onCheckedChange = { showComparisonWithPrevPeriod = it }
                            )
                        }

                        if (showComparisonWithPrevPeriod && prevPeriod != null) {
                            Text("دوره قبلی: از ${prevPeriod.first} تا ${prevPeriod.second}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("فروش دوره جاری:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(totalSalesAmount), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("فروش دوره قبل:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(prevTotalSalesAmount), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تغییرات رشد / افت فروش:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                val growthColor = if (prevSalesDiff >= 0) ProfitGreen else LossRed
                                val growthText = if (prevSalesGrowthPct != null) {
                                    val sign = if (prevSalesDiff >= 0) "+" else ""
                                    "$sign${PersianFormatUtil.toPersianDigits(prevSalesGrowthPct.toString())}٪ (${PersianFormatUtil.formatCurrencyToman(prevSalesDiff)})"
                                } else {
                                    PersianFormatUtil.formatCurrencyToman(prevSalesDiff)
                                }
                                Text(growthText, fontWeight = FontWeight.Bold, color = growthColor, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Export & Sharing Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "خروجی‌ها و اشتراک‌گذاری گزارش فروش",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = EmeraldPrimary
                        )

                        // 1. PDF Export (فارسی و راست‌چین با قابلیت اشتراک‌گذاری استاندارد)
                        Button(
                            onClick = {
                                val storeName = storeSettings?.storeName?.ifBlank { "میوه‌یار" } ?: "میوه‌یار"
                                val dateRangeStr = if (startDate == endDate) startDate else "از $startDate تا $endDate"
                                val pdfFile = PdfReportUtil.createSalesReportPdf(
                                    context = context,
                                    storeName = storeName,
                                    dateRangeStr = dateRangeStr,
                                    sales = filteredSales,
                                    totalWeightedSales = totalWeightedSalesAmount,
                                    totalNonWeightedSales = totalNonWeightedSalesAmount,
                                    totalSales = totalSalesAmount,
                                    dailyReceipt = filteredReceipts.firstOrNull(),
                                    branchName = if (isAllBranches) "همه شعب و غرفه‌ها" else selectedBranchFilter
                                )
                                PdfReportUtil.sharePdf(context, pdfFile, "اشتراک‌گذاری گزارش فروش و صندوق")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("خروجی و اشتراک‌گذاری PDF گزارش فروش (فارسی و راست‌چین)")
                        }

                        // 2. SMS Daily Report (ارسال پیامک با بازکردن پیام‌رسان گوشی)
                        OutlinedButton(
                            onClick = {
                                val storeName = storeSettings?.storeName?.ifBlank { "میوه‌یار" } ?: "میوه‌یار"
                                val discrepancy = reconciliationDiff
                                val expenses = totalExpensesAmount
                                val remaining = totalSalesAmount - expenses
                                val body = SmsReportUtil.buildDailySettlementSmsBody(
                                    storeName = storeName,
                                    shamsiDate = if (startDate == endDate) startDate else "$startDate تا $endDate",
                                    branchName = if (isAllBranches) "همه غرفه‌ها" else selectedBranchFilter,
                                    totalSales = totalSalesAmount,
                                    totalReceipts = if (totalReceiptsDeposited > 0) totalReceiptsDeposited else totalSalesAmount,
                                    discrepancy = discrepancy,
                                    expenses = expenses,
                                    remainingBalance = remaining
                                )
                                smsMessageBody = body
                                smsTargetPhone = storeSettings?.phone ?: ""
                                smsDialogTitle = "ارسال پیامک گزارش فروش و تسویه روزانه"
                                showSmsDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ارسال پیامک گزارش روزانه به مدیر یا همکاران")
                        }

                        // 3. CSV Export
                        OutlinedButton(
                            onClick = {
                                val csv = BackupRestoreUtil.generateSalesCsv(filteredSales)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                    type = "text/plain"
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "ارسال یا ذخیره خروجی اکسل فروش").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("خروجی اکسل فروش (CSV)")
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 1: گزارش روزانه شرکا (فروش، هزینه‌ها، دریافتی‌ها و سهم هر شریک)
        // =====================================================================
        if (activeTab == 1) {
            val netDistributable = (totalSalesAmount - totalExpensesAmount).coerceAtLeast(0L)

            // Summary Card for Partners
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "خلاصه سهم و تراز روزانه شرکا",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (startDate == endDate) startDate else "$startDate تا $endDate",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کل فروش غرفه در دوره:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalSalesAmount), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کل هزینه‌های پرداختی غرفه:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Text("− " + PersianFormatUtil.formatCurrencyToman(totalExpensesAmount), color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("کل دریافتی‌های صندوق و پوز:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(if (totalReceiptsDeposited > 0) totalReceiptsDeposited else totalSalesAmount), color = Color.White)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مانده قابل تقسیم بین شرکا:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(netDistributable), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فهرست شرکا (${PersianFormatUtil.toPersianDigits(partners.size.toString())} شریک)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Button(
                        onClick = {
                            editingPartner = null
                            partnerNameInput = ""
                            partnerPhoneInput = ""
                            partnerShareInput = "50"
                            partnerNotesInput = ""
                            showPartnerDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن شریک", fontSize = 12.sp)
                    }
                }
            }

            if (partners.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "هنوز شریکی در سیستم ثبت نشده است.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "با افزودن شرکا و تعیین درصد سهم، سود خالص و سهم هر شریک به‌صورت خودکار محاسبه و گزارش روزانه پیامکی و PDF صادر می‌شود.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(partners) { partner ->
                    val pShareAmount = (netDistributable * (partner.sharePercentage / 100.0)).toLong()
                    val pTxs = filteredPartnerTransactions.filter { it.partnerId == partner.id }
                    val withdrawals = pTxs.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
                    val deposits = pTxs.filter { it.type == "DEPOSIT" }.sumOf { it.amount }
                    val settlements = pTxs.filter { it.type == "SETTLEMENT" }.sumOf { it.amount }
                    val partnerNetBalance = pShareAmount - withdrawals + deposits - settlements

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(partner.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text(
                                    text = "سهم: ${PersianFormatUtil.toPersianDigits(partner.sharePercentage.toString())}٪",
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            if (partner.phone.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("شماره تماس: ${PersianFormatUtil.toPersianDigits(partner.phone)}", fontSize = 11.sp, color = Color.Gray)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("سهم ریالی از فروش و سود دوره:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(pShareAmount), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("برداشت‌های شریک در دوره:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(withdrawals), color = LossRed)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("واریزی‌ها و آورده شریک:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(deposits), color = ProfitGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("تراز و مانده حساب نهایی شریک:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val bColor = if (partnerNetBalance >= 0) ProfitGreen else LossRed
                                val bText = if (partnerNetBalance >= 0) {
                                    "طلبکار: ${PersianFormatUtil.formatCurrencyToman(partnerNetBalance)}"
                                } else {
                                    "بدهکار: ${PersianFormatUtil.formatCurrencyToman(-partnerNetBalance)}"
                                }
                                Text(bText, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = bColor)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: PDF, SMS, Quick Transaction
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // PDF Report
                                Button(
                                    onClick = {
                                        val storeName = storeSettings?.storeName?.ifBlank { "میوه‌یار" } ?: "میوه‌یار"
                                        val branchName = if (isAllBranches) "همه شعب و غرفه‌ها" else selectedBranchFilter
                                        val pdfFile = PdfReportUtil.createPartnerDailyReportPdf(
                                            context = context,
                                            storeName = storeName,
                                            partner = partner,
                                            dateRangeStr = if (startDate == endDate) startDate else "$startDate تا $endDate",
                                            branchName = branchName,
                                            totalSales = totalSalesAmount,
                                            totalExpenses = totalExpensesAmount,
                                            totalReceipts = if (totalReceiptsDeposited > 0) totalReceiptsDeposited else totalSalesAmount,
                                            netRemainingToShare = netDistributable,
                                            partnerShareAmount = pShareAmount,
                                            partnerTransactions = pTxs
                                        )
                                        PdfReportUtil.sharePdf(context, pdfFile, "گزارش سهم شریک - ${partner.fullName}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("گزارش PDF", fontSize = 11.sp)
                                }

                                // SMS Report
                                OutlinedButton(
                                    onClick = {
                                        val storeName = storeSettings?.storeName?.ifBlank { "میوه‌یار" } ?: "میوه‌یار"
                                        val branchName = if (isAllBranches) "همه غرفه‌ها" else selectedBranchFilter
                                        val body = SmsReportUtil.buildDailyPartnerSmsBody(
                                            storeName = storeName,
                                            partnerName = partner.fullName,
                                            shamsiDate = if (startDate == endDate) startDate else "$startDate تا $endDate",
                                            branchName = branchName,
                                            sharePercentage = partner.sharePercentage,
                                            totalSales = totalSalesAmount,
                                            expenses = totalExpensesAmount,
                                            remainingNet = netDistributable,
                                            partnerShareAmount = pShareAmount
                                        )
                                        smsMessageBody = body
                                        smsTargetPhone = partner.phone
                                        smsDialogTitle = "ارسال پیامک گزارش به ${partner.fullName}"
                                        showSmsDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ارسال پیامک", fontSize = 11.sp)
                                }

                                // Record Tx
                                OutlinedButton(
                                    onClick = {
                                        targetPartnerForTx = partner
                                        txTypeInput = "WITHDRAWAL"
                                        txAmountInput = ""
                                        txNotesInput = ""
                                        showPartnerTxDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ثبت برداشت/واریز", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 2: گزارش عملکرد پرسنل (حضور، غیبت، مرخصی، حقوق، مساعده، تسویه)
        // =====================================================================
        if (activeTab == 2) {
            item {
                Text(
                    text = "گزارش عملکرد و کارکرد پرسنل در بازه ($startDate تا $endDate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (employees.isEmpty()) {
                item {
                    Text("هنوز هیچ کارمندی در سیستم تعریف نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(employees) { emp ->
                    val empAttendances = filteredAttendances.filter { it.employeeId == emp.id }
                    val empPayments = filteredPayments.filter { it.employeeId == emp.id }

                    val presentDates = empAttendances.filter { it.status == "PRESENT" }.map { it.shamsiDate }
                    val absentDates = empAttendances.filter { it.status == "ABSENT" }.map { it.shamsiDate }
                    val leaveDates = empAttendances.filter { it.status == "LEAVE" }.map { it.shamsiDate }
                    val holidayDates = empAttendances.filter { it.status == "HOLIDAY" }.map { it.shamsiDate }
                    val halfDayDates = empAttendances.filter { it.status == "HALF_DAY" }.map { it.shamsiDate }

                    val presentDaysCount = presentDates.size
                    val absentDaysCount = absentDates.size
                    val leaveDaysCount = leaveDates.size
                    val holidayDaysCount = holidayDates.size
                    val halfDaysCount = halfDayDates.size

                    // Effective work days = present + (halfDay * 0.5)
                    val effectiveWorkDays = presentDaysCount + (halfDaysCount * 0.5)

                    val advances = empPayments.filter { it.type == "ADVANCE" }.sumOf { it.amount }
                    val settledSalaries = empPayments.filter { it.type == "SALARY_SETTLEMENT" }.sumOf { it.amount }
                    val deductions = empPayments.filter { it.type == "DEDUCTION" }.sumOf { it.amount }
                    val bonuses = empPayments.filter { it.type == "BONUS" }.sumOf { it.amount }

                    // Period duration in days
                    val startJ = JalaliCalendarUtil.parseJalali(startDate)
                    val endJ = JalaliCalendarUtil.parseJalali(endDate)
                    val totalPeriodDays = if (startJ != null && endJ != null) {
                        (JalaliCalendarUtil.daysBetween(startJ, endJ) + 1).coerceAtLeast(1)
                    } else 7

                    // Pro-rated base wage for this period: (weeklyWage / 7) * periodDays
                    val dailyRate = emp.weeklyWage / 7.0
                    val baseWagePeriod = (dailyRate * totalPeriodDays).toLong()

                    // Absent deduction (if not present or on leave/holiday)
                    val absentDeduction = (dailyRate * absentDaysCount).toLong()
                    val halfDayDeduction = (dailyRate * 0.5 * halfDaysCount).toLong()

                    // Payable wage for the period
                    val payableSalary = (baseWagePeriod - absentDeduction - halfDayDeduction + bonuses).coerceAtLeast(0L)
                    // Remaining balance: payable - advances - deductions - settled
                    val remainingBalance = payableSalary - advances - deductions - settledSalaries
                    val isSettled = remainingBalance <= 0 && (settledSalaries > 0 || advances >= payableSalary)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(emp.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                                Text(
                                    text = if (isSettled) "تسویه شده" else "دارای مانده تسویه",
                                    color = if (isSettled) ProfitGreen else GoldSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Attendance Summary Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("روزهای حضور: ${PersianFormatUtil.toPersianDigits(presentDaysCount.toString())} روز", fontSize = 12.sp, color = ProfitGreen)
                                Text("مرخصی: ${PersianFormatUtil.toPersianDigits(leaveDaysCount.toString())} روز", fontSize = 12.sp)
                                Text("غیبت: ${PersianFormatUtil.toPersianDigits(absentDaysCount.toString())} روز", fontSize = 12.sp, color = LossRed)
                                Text("تعطیل: ${PersianFormatUtil.toPersianDigits(holidayDaysCount.toString())} روز", fontSize = 12.sp)
                            }

                            if (absentDates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "تاریخ‌های غیبت: ${absentDates.joinToString(" ، ")}",
                                    fontSize = 11.sp,
                                    color = LossRed
                                )
                            }
                            if (leaveDates.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "تاریخ‌های مرخصی: ${leaveDates.joinToString(" ، ")}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Financial breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("حقوق پایه دوره (${PersianFormatUtil.toPersianDigits(totalPeriodDays.toString())} روز):", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(baseWagePeriod), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("حقوق قابل پرداخت پس از کسر غیبت:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(payableSalary), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مجموع مساعده‌های دریافتی:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(advances), color = LossRed)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مجموع تسویه‌های پرداخت‌شده:", fontSize = 12.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(settledSalaries), color = ProfitGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("مانده طلب/بدهکاری کارمند:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                val balanceColor = when {
                                    remainingBalance > 0 -> GoldSecondary
                                    remainingBalance == 0L -> ProfitGreen
                                    else -> LossRed
                                }
                                val balanceLabel = when {
                                    remainingBalance > 0 -> "طلبکار: ${PersianFormatUtil.formatCurrencyToman(remainingBalance)}"
                                    remainingBalance == 0L -> "تسویه کامل (۰ تومان)"
                                    else -> "بدهکار به فروشگاه: ${PersianFormatUtil.formatCurrencyToman(-remainingBalance)}"
                                }
                                Text(balanceLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = balanceColor)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val pdfFile = PdfReportUtil.createEmployeeWeeklyReportPdf(
                                            context = context,
                                            employee = emp,
                                            weekDateRange = "$startDate تا $endDate",
                                            workDaysCount = presentDaysCount,
                                            absentDaysCount = absentDaysCount,
                                            leaveDaysCount = leaveDaysCount,
                                            baseWage = baseWagePeriod,
                                            earnedWage = payableSalary,
                                            totalAdvances = advances,
                                            totalPayments = settledSalaries,
                                            netBalance = remainingBalance,
                                            paymentsList = empPayments,
                                            attendanceSummary = "حضور: $presentDaysCount روز، غیبت: $absentDaysCount، مرخصی: $leaveDaysCount"
                                        )
                                        PdfReportUtil.sharePdf(context, pdfFile, "صورت‌حساب پرسنل - ${emp.fullName}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("صورت‌حساب PDF", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val smsBody = SmsReportUtil.buildEmployeeWeeklySmsBody(
                                            employeeName = emp.fullName,
                                            startDate = startDate,
                                            endDate = endDate,
                                            workDaysCount = presentDaysCount,
                                            totalAdvances = advances,
                                            totalPayments = settledSalaries,
                                            netBalance = remainingBalance,
                                            attendanceDetails = "حضور: $presentDaysCount روز، غیبت: $absentDaysCount، مرخصی: $leaveDaysCount"
                                        )
                                        smsMessageBody = smsBody
                                        smsTargetPhone = emp.phone
                                        smsDialogTitle = "ارسال پیامک کارکرد به ${emp.fullName}"
                                        showSmsDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ارسال پیامک", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 3: هزینه‌های حاشیه و غرفه (دسته‌بندی، سهم هر غرفه، هزینه‌های مشترک)
        // =====================================================================
        if (activeTab == 3) {
            val totalPeriodExpenses = filteredExpenses.sumOf { it.amount }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "مجموع هزینه‌های حاشیه غرفه و جاری دوره",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalPeriodExpenses),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                    }
                }
            }

            // خروجی PDF هزینه‌ها
            item {
                Button(
                    onClick = {
                        val storeName = storeSettings.storeName.ifBlank { "میوه‌یار" }
                        val dateRangeStr = if (startDate == endDate) startDate else "$startDate تا $endDate"
                        val pdfFile = PdfReportUtil.createExpensesReportPdf(
                            context = context,
                            storeName = storeName,
                            dateRangeStr = dateRangeStr,
                            expenses = filteredExpenses,
                            totalAmount = totalPeriodExpenses
                        )
                        PdfReportUtil.sharePdf(context, pdfFile, "گزارش هزینه‌های غرفه")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("خروجی و اشتراک‌گذاری PDF هزینه‌ها (فارسی و راست‌چین)")
                }
            }

            // Expenses by Category (دسته‌بندی‌ها با درصد و بالاترین هزینه)
            val catExpenses = filteredExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .toList()
                .sortedByDescending { it.second }

            item {
                Text(
                    text = "سهم دسته‌بندی‌های هزینه (مرتب‌شده از بیشترین)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (catExpenses.isEmpty()) {
                item {
                    Text("هزینه‌ای در این بازه ثبت نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(catExpenses) { (catName, sumAmount) ->
                    val pct = if (totalPeriodExpenses > 0) (sumAmount * 100 / totalPeriodExpenses) else 0L
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(catName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("سهم: ${PersianFormatUtil.toPersianDigits(pct.toString())}٪ از کل هزینه‌ها", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(PersianFormatUtil.formatCurrencyToman(sumAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LossRed)
                        }
                    }
                }
            }

            // Expenses by Branch / Stall & Shared Expenses (هزینه‌ها به تفکیک غرفه یا مشترک)
            val branchExpenses = filteredExpenses.groupBy {
                val b = it.branchName.trim()
                if (b.isBlank() || b.contains("مشترک") || b.contains("عمومی")) "هزینه‌های مشترک / کلی" else b
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "هزینه‌ها به تفکیک غرفه‌ها و هزینه‌های مشترک",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            items(branchExpenses.entries.toList()) { entry ->
                val branchTitle = entry.key
                val bSum = entry.value.sumOf { it.amount }
                val bPct = if (totalPeriodExpenses > 0) (bSum * 100 / totalPeriodExpenses) else 0L
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("$branchTitle (${PersianFormatUtil.toPersianDigits(bPct.toString())}٪)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(PersianFormatUtil.formatCurrencyToman(bSum), fontWeight = FontWeight.Bold, color = LossRed)
                    }
                }
            }

            // Recent Expense items list
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ریز موارد هزینه ثبت‌شده در دوره",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            items(filteredExpenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${exp.shamsiDate} • ${exp.category} • ${exp.branchName}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(PersianFormatUtil.formatCurrencyToman(exp.amount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LossRed)
                    }
                }
            }
        }

        // =====================================================================
        // TAB 4: فروش هر محصول (مرتب‌سازی بر اساس وزن یا مبلغ + سهم هر محصول)
        // =====================================================================
        if (activeTab == 4) {
            val productSalesMap = filteredSaleItems.groupBy { it.productId }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فروش محصولات (${PersianFormatUtil.toPersianDigits(productSalesMap.size.toString())} محصول)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row {
                        FilterChip(
                            selected = productSortMode == 0,
                            onClick = { productSortMode = 0 },
                            label = { Text("بیشترین مبلغ", fontSize = 11.sp) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = productSortMode == 1,
                            onClick = { productSortMode = 1 },
                            label = { Text("بیشترین وزن", fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (productSalesMap.isEmpty()) {
                item {
                    Text("فروشی در این بازه یافت نشد.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                val sortedProducts = productSalesMap.entries.toList().sortedByDescending { entry ->
                    if (productSortMode == 0) {
                        entry.value.sumOf { it.totalPrice }
                    } else {
                        (entry.value.sumOf { it.quantity } * 1000).toLong()
                    }
                }

                items(sortedProducts) { entry ->
                    val itemsList = entry.value
                    val prodName = itemsList.firstOrNull()?.productName ?: "محصول"
                    val sumWeight = itemsList.sumOf { it.quantity }
                    val sumAmount = itemsList.sumOf { it.totalPrice }
                    val sharePct = if (totalSalesAmount > 0) (sumAmount * 100 / totalSalesAmount) else 0L

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prodName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "وزن فروخته‌شده: ${PersianFormatUtil.formatWeightKg(sumWeight)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "سهم از کل فروش: ${PersianFormatUtil.toPersianDigits(sharePct.toString())}٪",
                                    fontSize = 11.sp,
                                    color = GoldSecondary
                                )
                            }
                            Text(
                                text = PersianFormatUtil.formatCurrencyToman(sumAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // TAB 5: خرید و مقایسه اوزان
        // =====================================================================
        if (activeTab == 5) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "مقایسه اوزان خرید و فروش",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع وزن بار خریداری‌شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatWeightKg(totalWeightPurchased), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع وزن بار فروخته‌شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatWeightKg(totalWeightSold), fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val weightDiff = totalWeightPurchased - totalWeightSold
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("اختلاف وزن (موجودی انبار + ضایعات):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(PersianFormatUtil.formatWeightKg(weightDiff), fontWeight = FontWeight.Bold, color = GoldSecondary)
                        }
                    }
                }
            }

            items(filteredPurchases) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${p.productName} • ${p.supplierName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${p.shamsiDate} • ${PersianFormatUtil.formatWeightKg(p.weight)}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(PersianFormatUtil.formatCurrencyToman(p.totalCost), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                    }
                }
            }
        }
    }

    // Custom Date Range Dialog
    if (showCustomDateDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDateDialog = false },
            title = { Text("تنظیم بازه تاریخ دلخواه شمسی", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = { startDateInput = it },
                        label = { Text("از تاریخ شمسی (مثال: 1405/07/01)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = { endDateInput = it },
                        label = { Text("تا تاریخ شمسی (مثال: 1405/07/30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val s = startDateInput.trim()
                        val e = endDateInput.trim()
                        if (s.isNotBlank() && e.isNotBlank()) {
                            viewModel.setCustomDateRange(s, e)
                        }
                        showCustomDateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("اعمال بازه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDateDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // SMS Sender Dialog
    if (showSmsDialog) {
        AlertDialog(
            onDismissRequest = { showSmsDialog = false },
            title = { Text(smsDialogTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = smsTargetPhone,
                        onValueChange = { smsTargetPhone = it },
                        label = { Text("شماره همراه گیرنده پیامک") },
                        placeholder = { Text("09xxxxxxxxx") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "متن آماده گزارش:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldPrimary
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = smsMessageBody,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        SmsReportUtil.openSmsApp(context, smsTargetPhone, smsMessageBody)
                        showSmsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بازکردن پیام‌رسان گوشی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSmsDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Add / Edit Partner Dialog
    if (showPartnerDialog) {
        AlertDialog(
            onDismissRequest = { showPartnerDialog = false },
            title = {
                Text(
                    text = if (editingPartner == null) "تعریف شریک جدید" else "ویرایش اطلاعات شریک",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = partnerNameInput,
                        onValueChange = { partnerNameInput = it },
                        label = { Text("نام و نام خانوادگی شریک") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = partnerPhoneInput,
                        onValueChange = { partnerPhoneInput = it },
                        label = { Text("شماره تماس (جهت ارسال پیامک)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = partnerShareInput,
                        onValueChange = { partnerShareInput = it },
                        label = { Text("درصد سهم شراکت (مثال: 50)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = partnerNotesInput,
                        onValueChange = { partnerNotesInput = it },
                        label = { Text("توضیحات / یادداشت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = partnerNameInput.trim()
                        if (name.isNotBlank()) {
                            val sharePct = PersianFormatUtil.toEnglishDigits(partnerShareInput).toDoubleOrNull() ?: 50.0
                            val partner = PartnerEntity(
                                id = editingPartner?.id ?: 0L,
                                fullName = name,
                                phone = partnerPhoneInput.trim(),
                                sharePercentage = sharePct,
                                notes = partnerNotesInput.trim(),
                                isActive = true
                            )
                            viewModel.savePartner(partner) {
                                showPartnerDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره اطلاعات شریک")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartnerDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Record Partner Transaction Dialog (برداشت یا واریز)
    if (showPartnerTxDialog && targetPartnerForTx != null) {
        val partner = targetPartnerForTx!!
        AlertDialog(
            onDismissRequest = { showPartnerTxDialog = false },
            title = {
                Text(
                    text = "ثبت تراکنش شریک: ${partner.fullName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("نوع تراکنش مالی:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = txTypeInput == "WITHDRAWAL",
                            onClick = { txTypeInput = "WITHDRAWAL" },
                            label = { Text("برداشت سود/سرمایه", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LossRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = txTypeInput == "DEPOSIT",
                            onClick = { txTypeInput = "DEPOSIT" },
                            label = { Text("واریز سرمایه به صندوق", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ProfitGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = txAmountInput,
                        onValueChange = { txAmountInput = it },
                        label = { Text("مبلغ تراکنش (تومان)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = txNotesInput,
                        onValueChange = { txNotesInput = it },
                        label = { Text("شرح / توضیحات تراکنش") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = PersianFormatUtil.toEnglishDigits(txAmountInput).toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.recordPartnerTransaction(
                                partnerId = partner.id,
                                type = txTypeInput,
                                amount = amount,
                                paymentMethod = "نقدی",
                                notes = txNotesInput.trim(),
                                onSuccess = {
                                    showPartnerTxDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ثبت در حساب شریک")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartnerTxDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

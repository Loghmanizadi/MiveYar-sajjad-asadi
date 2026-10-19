package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.DailyReceiptEntity
import com.example.data.local.entities.DailyReceiptPosEntryEntity
import com.example.data.local.entities.PosTerminalEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.JalaliCalendarUtil
import com.example.util.JalaliDate
import com.example.util.PersianFormatUtil
import com.example.util.PdfReportUtil
import com.example.util.SmsReportUtil
import kotlinx.coroutines.launch

@Composable
fun SettlementScreen(viewModel: FruitShopViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storeSettings by viewModel.storeSettings.collectAsStateWithLifecycle()

    // 1. Core State
    val todayJalali = remember { JalaliCalendarUtil.getToday() }
    var selectedDateStr by remember { mutableStateOf(todayJalali.toFormattedString()) }

    val branches by viewModel.branches.collectAsStateWithLifecycle()
    var selectedBranch by remember { mutableStateOf("شعبه کاج") }

    // When branches load, ensure default selection is valid
    LaunchedEffect(branches) {
        if (branches.isNotEmpty() && branches.none { it.name == selectedBranch }) {
            selectedBranch = branches.firstOrNull { it.isDefault }?.name ?: branches.first().name
        }
    }

    // Terminals & Database state
    val allTerminals by viewModel.posTerminals.collectAsStateWithLifecycle()
    val activeTerminals by viewModel.activePosTerminals.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleItems by viewModel.saleItems.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    // Form Inputs for Receipts
    var cashInput by remember { mutableStateOf("0") }
    var cardToCardInput by remember { mutableStateOf("0") }
    var otherInput by remember { mutableStateOf("0") }
    var notesInput by remember { mutableStateOf("") }

    // Dynamic map for POS terminal amounts: terminalId -> amount string
    val posAmountsMap = remember { mutableStateMapOf<Long, String>() }

    // Tracking existing settlement record from DB
    var existingReceipt by remember { mutableStateOf<DailyReceiptEntity?>(null) }
    var isLoadingExisting by remember { mutableStateOf(false) }

    // Dialogs
    var showManageTerminalsDialog by remember { mutableStateOf(false) }
    var showAddTerminalDialog by remember { mutableStateOf(false) }
    var showEditTerminalDialog by remember { mutableStateOf<PosTerminalEntity?>(null) }
    var newTerminalName by remember { mutableStateOf("") }
    var newTerminalBank by remember { mutableStateOf("") }
    var newTerminalNumber by remember { mutableStateOf("") }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var customDateInput by remember { mutableStateOf(selectedDateStr) }

    // Function to reload settlement data for current date & branch
    fun reloadSettlementData() {
        coroutineScope.launch {
            isLoadingExisting = true
            val (rec, entries) = viewModel.getSettlementData(selectedDateStr, selectedBranch)
            existingReceipt = rec
            if (rec != null) {
                cashInput = rec.cash.toString()
                cardToCardInput = rec.cardToCard.toString()
                otherInput = rec.other.toString()
                notesInput = rec.notes

                // Populate POS entries
                posAmountsMap.clear()
                entries.forEach { entry ->
                    posAmountsMap[entry.terminalId] = entry.amount.toString()
                }
            } else {
                // Reset inputs for new settlement
                cashInput = "0"
                cardToCardInput = "0"
                otherInput = "0"
                notesInput = ""
                posAmountsMap.clear()
            }
            isLoadingExisting = false
        }
    }

    // Trigger reload when date or branch changes
    LaunchedEffect(selectedDateStr, selectedBranch) {
        reloadSettlementData()
    }

    // Ensure all active terminals have an entry in the map if empty
    LaunchedEffect(activeTerminals) {
        activeTerminals.forEach { term ->
            if (!posAmountsMap.containsKey(term.id)) {
                posAmountsMap[term.id] = "0"
            }
        }
    }

    // --- Calculations ---
    // 1. Sales Calculation for selected date and branch
    val filteredSales = remember(sales, selectedDateStr, selectedBranch) {
        sales.filter { sale ->
            sale.shamsiDate == selectedDateStr &&
                    (selectedBranch == "همه شعب" || sale.branchName == selectedBranch)
        }
    }
    val filteredSaleIds = remember(filteredSales) { filteredSales.map { it.id }.toSet() }
    val filteredSaleItems = remember(saleItems, filteredSaleIds) {
        saleItems.filter { it.saleId in filteredSaleIds }
    }

    val totalRecordedSalesAmount = remember(filteredSales) {
        filteredSales.sumOf { it.totalAmount }
    }
    val totalWeightedSalesAmount = remember(filteredSaleItems) {
        filteredSaleItems.filter { it.isWeighted }.sumOf { it.totalPrice }
    }
    val totalWeightedKg = remember(filteredSaleItems) {
        filteredSaleItems.filter { it.isWeighted }.sumOf { it.quantity }
    }
    val totalNonWeightedSalesAmount = remember(filteredSaleItems) {
        filteredSaleItems.filter { !it.isWeighted }.sumOf { it.totalPrice }
    }
    val invoicesCount = filteredSales.size
    val itemsRowCount = filteredSaleItems.size

    // 2. User Receipts Calculation
    val cashAmount = remember(cashInput) { cashInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val cardToCardAmount = remember(cardToCardInput) { cardToCardInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }
    val otherAmount = remember(otherInput) { otherInput.filter { it.isDigit() }.toLongOrNull() ?: 0L }

    val posTotalAmount = remember(posAmountsMap.values.toList()) {
        posAmountsMap.values.sumOf { it.filter { ch -> ch.isDigit() }.toLongOrNull() ?: 0L }
    }

    // Total actual receipts = Cash + PosTotal + CardToCard + Other
    val totalActualReceipts = cashAmount + posTotalAmount + cardToCardAmount + otherAmount

    // Settlement Discrepancy = Total Actual Receipts - Total Recorded Sales
    val settlementDiscrepancy = totalActualReceipts - totalRecordedSalesAmount

    // 3. Daily Expenses for selected date and branch (excludes wholesale cargo purchases)
    val filteredExpenses = remember(expenses, selectedDateStr, selectedBranch) {
        expenses.filter { exp ->
            exp.shamsiDate == selectedDateStr &&
                    (selectedBranch == "همه شعب" || exp.branchName == selectedBranch || exp.branchName == "مشترک")
        }
    }
    val dailyExpensesTotal = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    // Remaining receipts after paying daily expenses
    val remainingAfterExpenses = totalActualReceipts - dailyExpensesTotal

    // UI Content
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settlement_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header & Date/Branch Selector ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "تسویه پایان روز و بستن صندوق",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "انطباق فروش سیستم با وجوه نقدی و پوزها",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Reload Button
                        IconButton(
                            onClick = { reloadSettlementData() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "بارگذاری مجدد",
                                tint = EmeraldPrimary
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Date Navigator: [Previous Day] [Date Display / Picker] [Next Day]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val current = JalaliCalendarUtil.parseJalali(selectedDateStr) ?: JalaliCalendarUtil.getToday()
                                selectedDateStr = JalaliCalendarUtil.addDays(current, -1).toFormattedString()
                            }
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "روز قبل")
                        }

                        Surface(
                            onClick = {
                                customDateInput = selectedDateStr
                                showDatePickerDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                val parsed = JalaliCalendarUtil.parseJalali(selectedDateStr)
                                val dateLabel = parsed?.toPersianSpokenString() ?: selectedDateStr
                                Text(
                                    text = "$dateLabel (${PersianFormatUtil.toPersianDigits(selectedDateStr)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                val current = JalaliCalendarUtil.parseJalali(selectedDateStr) ?: JalaliCalendarUtil.getToday()
                                selectedDateStr = JalaliCalendarUtil.addDays(current, 1).toFormattedString()
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "روز بعد")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Branch Selector Chips
                    Text("انتخاب غرفه / شعبه تسویه:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val branchList = if (branches.isEmpty()) listOf("شعبه کاج", "شعبه ماهان") else branches.map { it.name }
                        items(branchList) { branchName ->
                            FilterChip(
                                selected = selectedBranch == branchName,
                                onClick = { selectedBranch = branchName },
                                label = { Text(branchName, fontSize = 12.sp) },
                                leadingIcon = if (selectedBranch == branchName) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Existing record indicator badge
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (existingReceipt != null) ProfitGreen.copy(alpha = 0.1f) else GoldSecondary.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (existingReceipt != null) ProfitGreen else GoldSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingReceipt != null) {
                                "این تاریخ و غرفه دارای سابقه تسویه قبلی است (ویرایش و بروزرسانی رکورد)"
                            } else {
                                "در انتظار ثبت تسویه جدید برای این روز و غرفه"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (existingReceipt != null) ProfitGreen else GoldSecondary
                        )
                    }
                }
            }
        }

        // --- 2. System Sales Card (فروش‌های ثبت‌شده سیستم) ---
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
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("۱. فروش ثبت‌شده در سیستم (فاکتورها)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "${PersianFormatUtil.toPersianDigits(invoicesCount.toString())} فاکتور",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("جمع کل فروش ثبت‌شده:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalRecordedSalesAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmeraldPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "فروش وزنی (${PersianFormatUtil.formatWeightKg(totalWeightedKg)}):",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalWeightedSalesAmount),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "فروش غیروزنی (بسته‌ای/مبلغی):",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalNonWeightedSalesAmount),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- 3. Dynamic Card Readers Section (کارتخوان‌ها به تفکیک) ---
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
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("۲. مبالغ دریافتی کارت‌خوان‌ها", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Manage POS button
                        OutlinedButton(
                            onClick = { showManageTerminalsDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مدیریت دستگاه‌ها", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "مبالغ تراکنش‌های موفق هر دستگاه پوز را از گزارش رول دستگاه وارد کنید:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeTerminals.isEmpty()) {
                        Text(
                            text = "هیچ کارتخوانی فعال نیست. برای شروع، روی «مدیریت دستگاه‌ها» ضربه بزنید.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        activeTerminals.forEach { term ->
                            val currentVal = posAmountsMap[term.id] ?: "0"
                            val numVal = currentVal.filter { it.isDigit() }.toLongOrNull() ?: 0L

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = term.name + if (term.bankName.isNotBlank()) " (${term.bankName})" else "",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    if (numVal > 0) {
                                        Text(
                                            text = PersianFormatUtil.formatCurrencyToman(numVal),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { newVal ->
                                        posAmountsMap[term.id] = newVal.filter { it.isDigit() }
                                    },
                                    placeholder = { Text("مبلغ به تومان") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pos_input_${term.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    trailingIcon = {
                                        Text("تومان", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("جمع کل مبالغ کارت‌خوان‌ها:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(posTotalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            }
        }

        // --- 4. Cash, Card-to-Card & Other Receipts Section ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("۳. نقدی، کارت‌به‌کارت و سایر دریافتی‌ها", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Cash
                    Text("مبلغ نقدی دریافتی پایان روز (شمارش صندوق):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cashInput,
                        onValueChange = { cashInput = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("مبلغ نقد موجود") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_input"),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Text("تومان", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                        }
                    )
                    if (cashAmount > 0) {
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(cashAmount),
                            fontSize = 11.sp,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. Card to Card
                    Text("دریافتی‌های کارت‌به‌کارت (مشتریان اعتباری/فیش‌ها):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = cardToCardInput,
                        onValueChange = { cardToCardInput = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("مبلغ کارت به کارت") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_to_card_input"),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Text("تومان", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                        }
                    )
                    if (cardToCardAmount > 0) {
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(cardToCardAmount),
                            fontSize = 11.sp,
                            color = GoldSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 3. Other
                    Text("سایر دریافتی‌ها (چک روز، تسویه نسیه قبلی و ...):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = otherInput,
                        onValueChange = { otherInput = it.filter { ch -> ch.isDigit() } },
                        placeholder = { Text("سایر دریافتی‌ها") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("other_input"),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Text("تومان", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
                        }
                    )
                }
            }
        }

        // --- 5. Settlement Formulas & Reconciliation Card (نتیجه انطباق و اختلاف تسویه) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "۴. نتیجه تجمیع دریافتی‌ها و اختلاف تسویه",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Total receipts breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع کل دریافتی‌های صندوق:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalActualReceipts),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Text(
                        text = "(نقدی: ${PersianFormatUtil.formatCurrencyToman(cashAmount)} + کارت‌خوان‌ها: ${PersianFormatUtil.formatCurrencyToman(posTotalAmount)} + کارت‌به‌کارت: ${PersianFormatUtil.formatCurrencyToman(cardToCardAmount)} + سایر: ${PersianFormatUtil.formatCurrencyToman(otherAmount)})",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع فروش ثبت‌شده سیستم:", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalRecordedSalesAmount),
                            color = Color.White.copy(alpha = 0.95f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )

                    // Settlement discrepancy: Total Receipts - Total Sales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("اختلاف تسویه (دریافتی منهای فروش):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val diffExplanation = when {
                                settlementDiscrepancy > 0 -> "مازاد دریافتی نسبت به فروش ثبت‌شده"
                                settlementDiscrepancy < 0 -> "کسری دریافتی نسبت به فروش ثبت‌شده"
                                else -> "تطبیق کامل و برابر"
                            }
                            Text(diffExplanation, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        }

                        val diffSign = if (settlementDiscrepancy > 0) "+" else ""
                        val badgeBg = when {
                            settlementDiscrepancy == 0L -> ProfitGreen
                            settlementDiscrepancy > 0 -> Color(0xFF2E7D32)
                            else -> LossRed
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$diffSign${PersianFormatUtil.formatCurrencyToman(settlementDiscrepancy)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 6. Daily Expenses & Remaining Balance Card ---
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
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = LossRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("۵. هزینه‌های روز و مانده دریافتی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "${PersianFormatUtil.toPersianDigits(filteredExpenses.size.toString())} مورد هزینه",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مجموع هزینه‌های پرداخت‌شده امروز غرفه:", fontSize = 13.sp)
                        Text(
                            text = "− " + PersianFormatUtil.formatCurrencyToman(dailyExpensesTotal),
                            fontWeight = FontWeight.Bold,
                            color = LossRed,
                            fontSize = 13.sp
                        )
                    }

                    if (filteredExpenses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            filteredExpenses.take(5).forEach { exp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${exp.title} (${exp.category})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(PersianFormatUtil.formatCurrencyToman(exp.amount), fontSize = 11.sp)
                                }
                            }
                            if (filteredExpenses.size > 5) {
                                Text("+ ${filteredExpenses.size - 5} هزینه دیگر...", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("مانده دریافتی پس از کسر هزینه‌های روز:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("وجوه نقد و پوز باقی‌مانده پس از پرداخت هزینه‌ها", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(remainingAfterExpenses),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (remainingAfterExpenses >= 0) ProfitGreen else LossRed
                        )
                    }
                }
            }
        }

        // --- 7. Notes and Save Button ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("توضیحات و یادداشت تسویه (اختیاری):", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        placeholder = { Text("مثلاً: مانده نقد تحویل آقای قنبری شد / کسری به دلیل خرابی پوز ملت") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val posEntries = activeTerminals.map { term ->
                                val amt = (posAmountsMap[term.id] ?: "0").filter { it.isDigit() }.toLongOrNull() ?: 0L
                                DailyReceiptPosEntryEntity(
                                    receiptId = existingReceipt?.id ?: 0L,
                                    shamsiDate = selectedDateStr,
                                    branchName = selectedBranch,
                                    terminalId = term.id,
                                    terminalName = term.name,
                                    amount = amt
                                )
                            }

                            viewModel.saveDailySettlement(
                                shamsiDate = selectedDateStr,
                                branchName = selectedBranch,
                                cash = cashAmount,
                                cardToCard = cardToCardAmount,
                                other = otherAmount,
                                posEntries = posEntries,
                                notes = notesInput.trim(),
                                onSuccess = {
                                    reloadSettlementData()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_settlement_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingReceipt != null) "بروزرسانی اطلاعات تسویه پایان روز" else "ثبت قطعی تسویه پایان روز و بستن صندوق",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Share PDF button
                        OutlinedButton(
                            onClick = {
                                val storeName = storeSettings?.storeName ?: "میوه و تره‌بار میوهیار"
                                val receiptForPdf = existingReceipt ?: DailyReceiptEntity(
                                    shamsiDate = selectedDateStr,
                                    branchName = selectedBranch,
                                    cash = cashAmount,
                                    cardToCard = cardToCardAmount,
                                    other = otherAmount,
                                    posTotal = posTotalAmount,
                                    notes = notesInput
                                )
                                val pdfFile = PdfReportUtil.createSalesReportPdf(
                                    context = context,
                                    storeName = storeName,
                                    dateRangeStr = selectedDateStr,
                                    sales = filteredSales,
                                    totalWeightedSales = totalWeightedSalesAmount,
                                    totalNonWeightedSales = totalNonWeightedSalesAmount,
                                    totalSales = totalRecordedSalesAmount,
                                    dailyReceipt = receiptForPdf,
                                    branchName = selectedBranch
                                )
                                PdfReportUtil.sharePdf(context, pdfFile, "اشتراک‌گذاری گزارش تسویه روزانه")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("گزارش PDF", fontSize = 12.sp)
                        }

                        // Send SMS button
                        OutlinedButton(
                            onClick = {
                                val storeName = storeSettings?.storeName ?: "میوه و تره‌بار میوهیار"
                                val smsBody = SmsReportUtil.buildDailySettlementSmsBody(
                                    storeName = storeName,
                                    shamsiDate = selectedDateStr,
                                    branchName = selectedBranch,
                                    totalSales = totalRecordedSalesAmount,
                                    totalReceipts = totalActualReceipts,
                                    discrepancy = settlementDiscrepancy,
                                    expenses = dailyExpensesTotal,
                                    remainingBalance = remainingAfterExpenses
                                )
                                val targetPhone = storeSettings?.phone ?: ""
                                SmsReportUtil.openSmsApp(context, targetPhone, smsBody)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ارسال پیامک", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Manage Card Readers Dialog
    if (showManageTerminalsDialog) {
        AlertDialog(
            onDismissRequest = { showManageTerminalsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مدیریت کارت‌خوان‌ها (پوزها)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "می‌توانید بدون محدودیت کارت‌خوان‌های جدید با نام دلخواه اضافه، ویرایش یا فعال/غیرفعال کنید:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            newTerminalName = ""
                            newTerminalBank = ""
                            newTerminalNumber = ""
                            showAddTerminalDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("افزودن کارت‌خوان جدید")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    if (allTerminals.isEmpty()) {
                        Text("هیچ کارت‌خوانی تعریف نشده است.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allTerminals) { term ->
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(term.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (term.bankName.isNotBlank() || term.terminalNumber.isNotBlank()) {
                                                Text(
                                                    text = "بانک: ${term.bankName.ifBlank { "نامشخص" }} • پایانه: ${term.terminalNumber.ifBlank { "ثبت‌نشده" }}",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Text(
                                                text = if (term.isActive) "وضعیت: فعال" else "وضعیت: غیرفعال",
                                                fontSize = 10.sp,
                                                color = if (term.isActive) ProfitGreen else Color.Gray
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    showEditTerminalDialog = term
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = GoldSecondary, modifier = Modifier.size(18.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.togglePosTerminalActive(term)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (term.isActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "تغییر وضعیت",
                                                    tint = if (term.isActive) ProfitGreen else Color.Gray,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageTerminalsDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // 2. Add Terminal Dialog
    if (showAddTerminalDialog) {
        AlertDialog(
            onDismissRequest = { showAddTerminalDialog = false },
            title = { Text("افزودن کارت‌خوان جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTerminalName,
                        onValueChange = { newTerminalName = it },
                        label = { Text("نام کارت‌خوان (مثال: کارتخوان سامان ۱)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTerminalBank,
                        onValueChange = { newTerminalBank = it },
                        label = { Text("نام بانک (اختیاری، مثال: سامان)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTerminalNumber,
                        onValueChange = { newTerminalNumber = it },
                        label = { Text("شماره پایانه یا حساب (اختیاری)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTerminalName.isNotBlank()) {
                            viewModel.addPosTerminal(newTerminalName, newTerminalBank, newTerminalNumber)
                            showAddTerminalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTerminalDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // 3. Edit Terminal Dialog
    showEditTerminalDialog?.let { termToEdit ->
        var editName by remember(termToEdit) { mutableStateOf(termToEdit.name) }
        var editBank by remember(termToEdit) { mutableStateOf(termToEdit.bankName) }
        var editNumber by remember(termToEdit) { mutableStateOf(termToEdit.terminalNumber) }

        AlertDialog(
            onDismissRequest = { showEditTerminalDialog = null },
            title = { Text("ویرایش کارت‌خوان", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("نام کارت‌خوان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editBank,
                        onValueChange = { editBank = it },
                        label = { Text("نام بانک") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editNumber,
                        onValueChange = { editNumber = it },
                        label = { Text("شماره پایانه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updatePosTerminal(
                                termToEdit.copy(
                                    name = editName.trim(),
                                    bankName = editBank.trim(),
                                    terminalNumber = editNumber.trim()
                                )
                            )
                            showEditTerminalDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTerminalDialog = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // 4. Custom Date Picker Dialog
    if (showDatePickerDialog) {
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("انتخاب تاریخ شمسی تسویه", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تاریخ مورد نظر را با فرمت سال/ماه/روز وارد کنید:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = customDateInput,
                        onValueChange = { customDateInput = it },
                        label = { Text("تاریخ شمسی (مثال: 1405/07/26)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = JalaliCalendarUtil.parseJalali(customDateInput)
                        if (parsed != null) {
                            selectedDateStr = parsed.toFormattedString()
                            showDatePickerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("تأیید تاریخ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

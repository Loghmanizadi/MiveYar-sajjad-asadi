package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.PurchaseEntity
import com.example.data.local.entities.SupplierEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.JalaliCalendarUtil
import com.example.util.PersianFormatUtil

@Composable
fun PurchaseScreen(viewModel: FruitShopViewModel) {
    val purchasesHistory by viewModel.purchases.collectAsStateWithLifecycle()
    val products by viewModel.activeProducts.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: ورود بار جدید, 1: سوابق خریدها

    // Form State
    var selectedDate by remember { mutableStateOf(JalaliCalendarUtil.getToday().toFormattedString()) }
    var selectedSupplier by remember { mutableStateOf<SupplierEntity?>(null) }
    var newSupplierName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var marketLocation by remember { mutableStateOf("میدان تره‌بار مرکزی کرج") }
    var phase by remember { mutableStateOf("فاز ۱") }
    var allocatedBranch by remember { mutableStateOf("شعبه کاج") }

    // Numeric fields
    var weightInput by remember { mutableStateOf("") }
    var packageCountInput by remember { mutableStateOf("") }
    var buyPricePerKgInput by remember { mutableStateOf("") }
    var dashtPerPackageInput by remember { mutableStateOf("5000") }
    var transportCostInput by remember { mutableStateOf("0") }
    var unloadingCostInput by remember { mutableStateOf("0") }
    var commissionCostInput by remember { mutableStateOf("0") }
    var miscCostInput by remember { mutableStateOf("0") }
    var paidAmountInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("انتقال بانکی") }
    var notesInput by remember { mutableStateOf("") }

    // Dialogs
    var purchaseToDelete by remember { mutableStateOf<PurchaseEntity?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("purchase_screen")
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("ثبت ورود بار جدید", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Text(
                        "سوابق خریدها (${PersianFormatUtil.toPersianDigits(purchasesHistory.size.toString())})",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
            ) {
                // 1. Inbound Shipment Basic Info
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "اطلاعات تأمین‌کننده و میدان بار",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Date
                            OutlinedTextField(
                                value = selectedDate,
                                onValueChange = { selectedDate = it },
                                label = { Text("تاریخ شمسی") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Supplier Selector / Quick Entry
                            Text("انتخاب فروشنده یا حجره‌دار:", fontSize = 12.sp, color = Color.Gray)
                            if (suppliers.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    items(suppliers) { sup ->
                                        FilterChip(
                                            selected = selectedSupplier?.id == sup.id,
                                            onClick = {
                                                selectedSupplier = sup
                                                newSupplierName = sup.name
                                            },
                                            label = { Text(sup.name, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = EmeraldPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = newSupplierName,
                                onValueChange = {
                                    newSupplierName = it
                                    selectedSupplier = suppliers.find { s -> s.name == it }
                                },
                                label = { Text("نام فروشنده / حجره‌دار میدان") },
                                placeholder = { Text("مثال: حاج احمد کرمی - حجره ۱۲") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = marketLocation,
                                    onValueChange = { marketLocation = it },
                                    label = { Text("میدان بار") },
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = phase,
                                    onValueChange = { phase = it },
                                    label = { Text("فاز / سالن") },
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }
                    }
                }

                // 2. Product and Weight
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "محصول و اوزان خریداری شده",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("انتخاب محصول:", fontSize = 12.sp, color = Color.Gray)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(products) { prod ->
                                    FilterChip(
                                        selected = selectedProduct?.id == prod.id,
                                        onClick = { selectedProduct = prod },
                                        label = { Text(prod.name, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldSecondary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = weightInput,
                                    onValueChange = { weightInput = it },
                                    label = { Text("وزن کل بار (کیلو)") },
                                    placeholder = { Text("مثال: ۵۰۰") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("purchase_weight_input")
                                )

                                OutlinedTextField(
                                    value = packageCountInput,
                                    onValueChange = { packageCountInput = it },
                                    label = { Text("تعداد جعبه / سبد") },
                                    placeholder = { Text("مثال: ۲۵") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = buyPricePerKgInput,
                                onValueChange = { buyPricePerKgInput = it },
                                label = { Text("نرخ خرید هر کیلو (تومان)") },
                                placeholder = { Text("مثال: ۲۴۰۰۰") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("purchase_price_input")
                            )
                        }
                    }
                }

                // 3. Incidental Costs (هزینه‌های جانبی بار: دشت، کرایه نیسان، تخلیه، کمیسیون)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "هزینه‌های جانبی و سربار بار (محاسبه قیمت تمام‌شده)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dashtPerPackageInput,
                                    onValueChange = { dashtPerPackageInput = it },
                                    label = { Text("دشت به ازای هر جعبه") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = transportCostInput,
                                    onValueChange = { transportCostInput = it },
                                    label = { Text("کرایه نیسان / حمل") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = unloadingCostInput,
                                    onValueChange = { unloadingCostInput = it },
                                    label = { Text("هزینه تخلیه بار") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = commissionCostInput,
                                    onValueChange = { commissionCostInput = it },
                                    label = { Text("حق‌العمل / کمیسیون") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 4. Financial Calculations Summary Card
                val weight = PersianFormatUtil.parseDoubleWeight(weightInput)
                val rate = PersianFormatUtil.parseLongAmount(buyPricePerKgInput)
                val packageCount = packageCountInput.toIntOrNull() ?: 0
                val dashtPerPkg = PersianFormatUtil.parseLongAmount(dashtPerPackageInput)
                val totalDasht = dashtPerPkg * packageCount
                val transport = PersianFormatUtil.parseLongAmount(transportCostInput)
                val unloading = PersianFormatUtil.parseLongAmount(unloadingCostInput)
                val commission = PersianFormatUtil.parseLongAmount(commissionCostInput)
                val misc = PersianFormatUtil.parseLongAmount(miscCostInput)

                val baseCost = (weight * rate).toLong()
                val totalCost = baseCost + totalDasht + transport + unloading + commission + misc
                val costPerKg = if (weight > 0) (totalCost / weight).toLong() else 0L

                val paidAmount = PersianFormatUtil.parseLongAmount(paidAmountInput)
                val remainingDebt = (totalCost - paidAmount).coerceAtLeast(0L)

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مبلغ پایه بار (وزن × نرخ):", fontSize = 13.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(baseCost), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("جمع کل هزینه‌های جانبی بار:", fontSize = 13.sp)
                                Text(PersianFormatUtil.formatCurrencyToman(totalDasht + transport + unloading + commission + misc), fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("هزینه کل فاکتور بار:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(PersianFormatUtil.formatCurrencyToman(totalCost), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("قیمت تمام شده واقعی هر کیلو:", fontSize = 13.sp, color = Color(0xFF00391E))
                                Text(PersianFormatUtil.formatCurrencyToman(costPerKg), fontWeight = FontWeight.Bold, color = Color(0xFF00391E))
                            }
                        }
                    }
                }

                // 5. Payment details and Save
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "تسویه و پرداخت به فروشنده",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = paidAmountInput,
                                onValueChange = { paidAmountInput = it },
                                label = { Text("مبلغ پرداخت شده نقدی / حواله") },
                                placeholder = { Text("مثال: ${PersianFormatUtil.formatCurrencyToman(totalCost)}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "مانده بدهی که در حساب فروشنده ثبت می‌شود: ${PersianFormatUtil.formatCurrencyToman(remainingDebt)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remainingDebt > 0) LossRed else EmeraldPrimary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val prod = selectedProduct
                                    val supName = newSupplierName.ifBlank { "فروشنده متفرقه میدان" }
                                    val supId = selectedSupplier?.id ?: 0L

                                    if (prod == null) {
                                        return@Button
                                    }

                                    viewModel.registerPurchase(
                                        shamsiDate = selectedDate,
                                        supplierId = supId,
                                        supplierName = supName,
                                        productId = prod.id,
                                        productName = prod.name,
                                        marketLocation = marketLocation,
                                        phase = phase,
                                        weight = weight,
                                        packageCount = packageCount,
                                        buyPricePerKg = rate,
                                        allocatedBranch = allocatedBranch,
                                        dashtPerPackage = dashtPerPkg,
                                        transportCost = transport,
                                        unloadingCost = unloading,
                                        commissionCost = commission,
                                        miscCost = misc,
                                        paidAmount = paidAmount,
                                        paymentMethod = selectedPaymentMethod,
                                        notes = notesInput,
                                        onSuccess = {
                                            weightInput = ""
                                            packageCountInput = ""
                                            buyPricePerKgInput = ""
                                            paidAmountInput = ""
                                            showSuccessDialog = true
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_purchase_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ثبت بار در انبار و حسابداری", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Purchase History Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
            ) {
                if (purchasesHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("هنوز هیچ خریدی ثبت نشده است.", color = Color.Gray)
                        }
                    }
                } else {
                    items(purchasesHistory) { purchase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${purchase.productName} • ${PersianFormatUtil.formatWeightKg(purchase.weight)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${purchase.supplierName} • ${purchase.shamsiDate}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = PersianFormatUtil.formatCurrencyToman(purchase.totalCost),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "کیلویی: ${PersianFormatUtil.formatCurrencyToman(purchase.effectiveCostPerKg)}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    IconButton(onClick = { purchaseToDelete = purchase }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف خرید",
                                            tint = LossRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (purchase.remainingDebt > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "مانده بدهی این بار: ${PersianFormatUtil.formatCurrencyToman(purchase.remainingDebt)}",
                                        fontSize = 12.sp,
                                        color = LossRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("ورود بار با موفقیت ثبت شد", fontWeight = FontWeight.Bold) },
            text = {
                Text("بار به موجودی انبار افزوده شد و حسابداری فروشنده به روز رسانی گردید.")
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("متوجه شدم")
                }
            }
        )
    }

    purchaseToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text("تأیید حذف خرید بار", fontWeight = FontWeight.Bold, color = LossRed) },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید فاکتور خرید ${p.productName} به مبلغ ${PersianFormatUtil.formatCurrencyToman(p.totalCost)} را حذف کنید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchase(p)
                        purchaseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("حذف فاکتور")
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.local.entities.SupplierEntity
import com.example.data.repository.SupplierSummary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.PersianFormatUtil

@Composable
fun SupplierLedgerScreen(viewModel: FruitShopViewModel) {
    val supplierSummaries by viewModel.supplierSummaries.collectAsStateWithLifecycle()
    val allTransactions by viewModel.supplierTransactions.collectAsStateWithLifecycle()

    var showNewSupplierDialog by remember { mutableStateOf(false) }
    var selectedSupplierForDetail by remember { mutableStateOf<SupplierSummary?>(null) }
    var showPaymentDialogForSupplier by remember { mutableStateOf<SupplierSummary?>(null) }

    // Dialog state for new supplier
    var supplierNameInput by remember { mutableStateOf("") }
    var supplierPhoneInput by remember { mutableStateOf("") }
    var supplierLocationInput by remember { mutableStateOf("میدان تره‌بار کرج") }
    var supplierInitialDebtInput by remember { mutableStateOf("0") }

    // Dialog state for payment
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentMethodInput by remember { mutableStateOf("انتقال بانکی (پایا)") }
    var paymentNotesInput by remember { mutableStateOf("") }

    if (selectedSupplierForDetail != null) {
        // Detail view for single supplier ledger
        val summary = selectedSupplierForDetail!!
        val supId = summary.supplier.id
        val transactions = allTransactions.filter { it.supplierId == supId }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedSupplierForDetail = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                    Text(
                        text = "صورت‌حساب و گردش ${summary.supplier.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("بدهی اولیه ثبت شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(summary.supplier.initialDebt), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع خریدهای ثبت‌شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(summary.totalPurchasesAmount), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع پرداختی‌ها:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(summary.totalPaymentsAmount), fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("مانده حساب فعلی:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = PersianFormatUtil.formatCurrencyToman(summary.currentBalance),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.currentBalance > 0) LossRed else ProfitGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showPaymentDialogForSupplier = summary
                                paymentAmountInput = ""
                                paymentNotesInput = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت واریز وجه به این فروشنده")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ریز تراکنش‌ها و گردش حساب",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text("هیچ تراکنشی ثبت نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(transactions) { tx ->
                    val isPayment = tx.type == "PAYMENT"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPayment) Color(0xFFF1FDF5) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPayment) "پرداخت وجه (کاهش بدهی)" else "خرید بار (افزایش بدهی)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isPayment) ProfitGreen else LossRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${tx.shamsiDate} • ${tx.paymentMethod}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                if (tx.notes.isNotBlank()) {
                                    Text(text = tx.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = (if (isPayment) "− " else "+ ") + PersianFormatUtil.formatCurrencyToman(tx.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isPayment) ProfitGreen else LossRed
                            )
                        }
                    }
                }
            }
        }
    } else {
        // List of all suppliers
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("supplier_ledger_screen")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            // Header with total debt and + Add Supplier button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دفتر حساب بارفروشان و تأمین‌کنندگان",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Button(
                        onClick = { showNewSupplierDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("فروشنده جدید", fontSize = 12.sp)
                    }
                }
            }

            val totalDebt = supplierSummaries.filter { it.currentBalance > 0 }.sumOf { it.currentBalance }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (totalDebt > 0) Color(0xFFFFF0EE) else Color(0xFFEDF8F1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مجموع کل بدهی مغازه به بارفروشان:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = PersianFormatUtil.formatCurrencyToman(totalDebt),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (totalDebt > 0) LossRed else EmeraldPrimary
                        )
                    }
                }
            }

            if (supplierSummaries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هنوز هیچ فروشنده‌ای ثبت نشده است.", color = Color.Gray)
                    }
                }
            } else {
                items(supplierSummaries) { summary ->
                    val s = summary.supplier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSupplierForDetail = summary },
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
                                        text = s.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${s.location} • ${s.phone.ifBlank { "بدون تلفن" }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "مانده بدهی",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = PersianFormatUtil.formatCurrencyToman(summary.currentBalance),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (summary.currentBalance > 0) LossRed else ProfitGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showPaymentDialogForSupplier = summary
                                        paymentAmountInput = ""
                                        paymentNotesInput = ""
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ثبت واریزی", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { selectedSupplierForDetail = summary },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("مشاهده گردش", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Supplier Dialog
    if (showNewSupplierDialog) {
        AlertDialog(
            onDismissRequest = { showNewSupplierDialog = false },
            title = { Text("تعریف تأمین‌کننده / حجره‌دار جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supplierNameInput,
                        onValueChange = { supplierNameInput = it },
                        label = { Text("نام و نام خانوادگی / شماره حجره") },
                        placeholder = { Text("مثال: حاج قاسم رضایی - حجره ۳۵") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplierPhoneInput,
                        onValueChange = { supplierPhoneInput = it },
                        label = { Text("شماره همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplierLocationInput,
                        onValueChange = { supplierLocationInput = it },
                        label = { Text("میدان / آدرس") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = supplierInitialDebtInput,
                        onValueChange = { supplierInitialDebtInput = it },
                        label = { Text("بدهی اولیه قبلی (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (supplierNameInput.isNotBlank()) {
                            val initDebt = PersianFormatUtil.parseLongAmount(supplierInitialDebtInput)
                            viewModel.saveSupplier(
                                SupplierEntity(
                                    name = supplierNameInput.trim(),
                                    phone = supplierPhoneInput.trim(),
                                    location = supplierLocationInput.trim(),
                                    initialDebt = initDebt
                                ),
                                onSuccess = {
                                    supplierNameInput = ""
                                    supplierPhoneInput = ""
                                    showNewSupplierDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره فروشنده")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewSupplierDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Register Payment Dialog
    showPaymentDialogForSupplier?.let { summary ->
        AlertDialog(
            onDismissRequest = { showPaymentDialogForSupplier = null },
            title = { Text("واریز به حساب ${summary.supplier.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "مانده بدهی فعلی: ${PersianFormatUtil.formatCurrencyToman(summary.currentBalance)}",
                        fontWeight = FontWeight.Bold,
                        color = LossRed,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("مبلغ واریزی (تومان)") },
                        placeholder = { Text("مثال: ${PersianFormatUtil.formatCurrencyToman(summary.currentBalance, false)}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supplier_payment_input")
                    )

                    OutlinedTextField(
                        value = paymentMethodInput,
                        onValueChange = { paymentMethodInput = it },
                        label = { Text("روش پرداخت") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = paymentNotesInput,
                        onValueChange = { paymentNotesInput = it },
                        label = { Text("شماره پیگیری یا توضیحات") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = PersianFormatUtil.parseLongAmount(paymentAmountInput)
                        if (amount > 0) {
                            viewModel.recordSupplierPayment(
                                supplierId = summary.supplier.id,
                                amount = amount,
                                paymentMethod = paymentMethodInput,
                                notes = paymentNotesInput,
                                onSuccess = {
                                    showPaymentDialogForSupplier = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ثبت واریزی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForSupplier = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

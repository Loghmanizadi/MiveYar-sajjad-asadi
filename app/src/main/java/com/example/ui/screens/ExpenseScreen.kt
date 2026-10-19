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
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.entities.ExpenseEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.PersianFormatUtil

@Composable
fun ExpenseScreen(viewModel: FruitShopViewModel) {
    val expensesList by viewModel.expenses.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()

    var titleInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("نایلون، سبد و بسته‌بندی") }
    var amountInput by remember { mutableStateOf("") }
    var selectedBranch by remember { mutableStateOf("شعبه کاج") }
    var selectedPaymentMethod by remember { mutableStateOf("نقدی") }
    var notesInput by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }

    val totalExpenses = expensesList.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("expense_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. New Expense Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ثبت هزینه روزانه فروشگاه",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("عنوان هزینه") },
                        placeholder = { Text("مثال: خرید کیسه نایلونی ۵ کیلویی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("مبلغ هزینه به تومان") },
                        placeholder = { Text("مثال: ۳۵۰۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Categories chips with + Add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("دسته‌بندی هزینه:", fontSize = 12.sp, color = Color.Gray)
                        TextButton(
                            onClick = { showAddCategoryDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("دسته‌بندی جدید", fontSize = 11.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat.name,
                                onClick = { selectedCategory = cat.name },
                                label = { Text(cat.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method & Branch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Payment methods chips
                        Column(modifier = Modifier.weight(1f)) {
                            Text("روش پرداخت:", fontSize = 11.sp, color = Color.Gray)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val pms = if (paymentMethods.isEmpty()) listOf("نقدی", "کارتخوان") else paymentMethods.map { it.name }
                                items(pms) { pm ->
                                    FilterChip(
                                        selected = selectedPaymentMethod == pm,
                                        onClick = { selectedPaymentMethod = pm },
                                        label = { Text(pm, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("توضیحات اختیاری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val parsedAmount = PersianFormatUtil.parseLongAmount(amountInput)
                            if (titleInput.isNotBlank() && parsedAmount > 0) {
                                viewModel.recordExpense(
                                    title = titleInput,
                                    category = selectedCategory,
                                    amount = parsedAmount,
                                    paymentMethod = selectedPaymentMethod,
                                    branchName = selectedBranch,
                                    notes = notesInput,
                                    onSuccess = {
                                        titleInput = ""
                                        amountInput = ""
                                        notesInput = ""
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_expense_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ثبت هزینه در حساب‌ها", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Expenses Total Summary Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مجموع کل هزینه‌های ثبت‌شده",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = PersianFormatUtil.formatCurrencyToman(totalExpenses),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LossRed
                    )
                }
            }
        }

        // 3. Recent Expenses List
        item {
            Text(
                text = "ریز هزینه‌های ثبت شده",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (expensesList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ هزینه‌ای ثبت نشده است.", color = Color.Gray)
                }
            }
        } else {
            items(expensesList) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exp.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${exp.shamsiDate} • ${exp.category} • ${exp.paymentMethod}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (exp.notes.isNotBlank()) {
                                Text(
                                    text = exp.notes,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = PersianFormatUtil.formatCurrencyToman(exp.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LossRed
                            )
                            IconButton(onClick = { expenseToDelete = exp }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف هزینه",
                                    tint = LossRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add category dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("تعریف دسته‌بندی جدید هزینه", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("نام دسته‌بندی") },
                    placeholder = { Text("مثال: شارژ ساختمان و نگهبانی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addExpenseCategory(newCategoryName)
                            selectedCategory = newCategoryName
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Delete confirm dialog
    expenseToDelete?.let { exp ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("تأیید حذف هزینه", fontWeight = FontWeight.Bold, color = LossRed) },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید هزینه «${exp.title}» به مبلغ ${PersianFormatUtil.formatCurrencyToman(exp.amount)} را حذف کنید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense(exp)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

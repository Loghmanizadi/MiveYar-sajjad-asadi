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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
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
import com.example.data.local.entities.EmployeeEntity
import com.example.data.local.entities.EmployeePaymentEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.JalaliCalendarUtil
import com.example.util.PersianFormatUtil

@Composable
fun StaffScreen(viewModel: FruitShopViewModel) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val allPayments by viewModel.employeePayments.collectAsStateWithLifecycle()

    var showNewEmployeeDialog by remember { mutableStateOf(false) }
    var selectedEmployeeForDetail by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showPaymentDialogForEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var paymentType by remember { mutableStateOf("ADVANCE") } // ADVANCE (مساعده) or SALARY_SETTLEMENT (تسویه حقوق)

    // Form inputs for new employee
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var weeklyWageInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }

    // Form inputs for payment
    var paymentAmountInput by remember { mutableStateOf("") }
    var forWeekInput by remember { mutableStateOf("هفته جاری") }
    var paymentMethodInput by remember { mutableStateOf("نقدی") }
    var paymentNotesInput by remember { mutableStateOf("") }

    // Attendance quick register
    var showAttendanceDialogForEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    val todayStr = JalaliCalendarUtil.getToday().toFormattedString()

    if (selectedEmployeeForDetail != null) {
        val emp = selectedEmployeeForDetail!!
        val empPayments = allPayments.filter { it.employeeId == emp.id }
        val totalAdvances = empPayments.filter { it.type == "ADVANCE" }.sumOf { it.amount }
        val totalSettled = empPayments.filter { it.type == "SALARY_SETTLEMENT" }.sumOf { it.amount }

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
                    IconButton(onClick = { selectedEmployeeForDetail = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                    Text(
                        text = "پرونده و حقوق هفتگی ${emp.fullName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Summary Card
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
                            Text("دستمزد ثابت هفتگی:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(emp.weeklyWage), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع مساعده‌های دریافت‌شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalAdvances), fontWeight = FontWeight.Bold, color = LossRed)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تسویه‌های حقوق پرداخت شده:", fontSize = 13.sp)
                            Text(PersianFormatUtil.formatCurrencyToman(totalSettled), fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val netEst = (emp.weeklyWage - totalAdvances).coerceAtLeast(0L)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مانده تخمینی قابل پرداخت این هفته:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(PersianFormatUtil.formatCurrencyToman(netEst), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    showPaymentDialogForEmployee = emp
                                    paymentType = "ADVANCE"
                                    paymentAmountInput = ""
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("پرداخت مساعده", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    showPaymentDialogForEmployee = emp
                                    paymentType = "SALARY_SETTLEMENT"
                                    paymentAmountInput = netEst.toString()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("تسویه هفتگی", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "سوابق پرداخت‌ها و مساعده‌ها",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (empPayments.isEmpty()) {
                item {
                    Text("هیچ پرداختی ثبت نشده است.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(empPayments) { pay ->
                    val isAdv = pay.type == "ADVANCE"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAdv) "مساعده" else "تسویه حقوق هفتگی",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isAdv) GoldSecondary else ProfitGreen
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pay.shamsiDate} • بابت: ${pay.forWeekShamsi} • ${pay.paymentMethod}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                if (pay.notes.isNotBlank()) {
                                    Text(pay.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = PersianFormatUtil.formatCurrencyToman(pay.amount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Staff List Screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("staff_screen")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مدیریت کارکنان و پرسنل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Button(
                        onClick = { showNewEmployeeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("کارمند جدید", fontSize = 12.sp)
                    }
                }
            }

            if (employees.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("هنوز کارمندی ثبت نشده است.", color = Color.Gray)
                    }
                }
            } else {
                items(employees) { emp ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEmployeeForDetail = emp },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = emp.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "تلفن: ${emp.phone.ifBlank { "بدون تلفن" }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "حقوق هفتگی",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = PersianFormatUtil.formatCurrencyToman(emp.weeklyWage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { showAttendanceDialogForEmployee = emp },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حضور و غیاب امروز", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { selectedEmployeeForDetail = emp },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("حساب و تسویه", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Employee Dialog
    if (showNewEmployeeDialog) {
        AlertDialog(
            onDismissRequest = { showNewEmployeeDialog = false },
            title = { Text("تعریف کارمند جدید", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("نام و نام خانوادگی") },
                        placeholder = { Text("مثال: رضا محمدی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("شماره همراه") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = weeklyWageInput,
                        onValueChange = { weeklyWageInput = it },
                        label = { Text("دستمزد ثابت هفتگی (تومان)") },
                        placeholder = { Text("مثال: ۳۵۰۰۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("مسئولیت یا توضیحات") },
                        placeholder = { Text("مثال: کارگر چیدمان و بارگیری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wage = PersianFormatUtil.parseLongAmount(weeklyWageInput)
                        if (nameInput.isNotBlank() && wage > 0) {
                            viewModel.saveEmployee(
                                EmployeeEntity(
                                    fullName = nameInput.trim(),
                                    phone = phoneInput.trim(),
                                    weeklyWage = wage,
                                    startDateShamsi = todayStr,
                                    notes = notesInput.trim()
                                ),
                                onSuccess = {
                                    nameInput = ""
                                    phoneInput = ""
                                    weeklyWageInput = ""
                                    notesInput = ""
                                    showNewEmployeeDialog = false
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره کارمند")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewEmployeeDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Attendance Dialog
    showAttendanceDialogForEmployee?.let { emp ->
        var currentStatus by remember { mutableStateOf("PRESENT") }
        AlertDialog(
            onDismissRequest = { showAttendanceDialogForEmployee = null },
            title = { Text("حضور و غیاب امروز: ${emp.fullName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تاریخ: $todayStr", fontSize = 13.sp, color = Color.Gray)
                    listOf(
                        Pair("PRESENT", "حاضر"),
                        Pair("ABSENT", "غایب (کسر از حقوق)"),
                        Pair("LEAVE", "مرخصی"),
                        Pair("HOLIDAY", "تعطیل رسمی"),
                        Pair("HALF_DAY", "نیمه‌وقت")
                    ).forEach { (statusKey, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentStatus = statusKey }
                                .padding(vertical = 4.dp)
                        ) {
                            FilterChip(
                                selected = currentStatus == statusKey,
                                onClick = { currentStatus = statusKey },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (statusKey == "ABSENT") LossRed else EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordAttendance(emp.id, todayStr, currentStatus)
                        showAttendanceDialogForEmployee = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ثبت وضعیت")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttendanceDialogForEmployee = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Payment / Advance / Settlement Dialog
    showPaymentDialogForEmployee?.let { emp ->
        AlertDialog(
            onDismissRequest = { showPaymentDialogForEmployee = null },
            title = {
                Text(
                    text = if (paymentType == "ADVANCE") "ثبت مساعده برای ${emp.fullName}" else "تسویه حقوق هفتگی ${emp.fullName}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paymentAmountInput,
                        onValueChange = { paymentAmountInput = it },
                        label = { Text("مبلغ پرداختی (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("staff_payment_input")
                    )
                    OutlinedTextField(
                        value = forWeekInput,
                        onValueChange = { forWeekInput = it },
                        label = { Text("بابت دوره / هفته") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                        label = { Text("توضیحات اختیاری") },
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
                            viewModel.recordEmployeePayment(
                                employeeId = emp.id,
                                type = paymentType,
                                amount = amount,
                                forWeek = forWeekInput,
                                paymentMethod = paymentMethodInput,
                                notes = paymentNotesInput,
                                onSuccess = {
                                    showPaymentDialogForEmployee = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ثبت در حساب پرسنل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialogForEmployee = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

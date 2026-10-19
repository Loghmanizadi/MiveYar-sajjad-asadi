package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BranchEntity
import com.example.data.local.entities.PaymentMethodEntity
import com.example.data.local.entities.StoreSettingsEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.PersianFormatUtil
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: FruitShopViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val settings by viewModel.storeSettings.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()

    var storeNameInput by remember(settings) { mutableStateOf(settings?.storeName ?: "میوه و تره‌بار میوهیار") }
    var ownerNameInput by remember(settings) { mutableStateOf(settings?.ownerName ?: "مدیریت فروشگاه") }
    var phoneInput by remember(settings) { mutableStateOf(settings?.phone ?: "") }
    var cityInput by remember(settings) { mutableStateOf(settings?.city ?: "کرج") }

    // Dialogs
    var showAddBranchDialog by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }

    var showAddPaymentMethodDialog by remember { mutableStateOf(false) }
    var newPaymentMethodName by remember { mutableStateOf("") }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showBackupCreatedDialog by remember { mutableStateOf(false) }
    var backupJsonContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Store Profile Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشخصات مغازه و کسب‌وکار",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = storeNameInput,
                        onValueChange = { storeNameInput = it },
                        label = { Text("نام فروشگاه / تره‌بار") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("store_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = ownerNameInput,
                            onValueChange = { ownerNameInput = it },
                            label = { Text("نام صاحب مغازه / مسئول") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("شهر") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("شماره تماس مغازه") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateStoreSettings(
                                StoreSettingsEntity(
                                    id = 1,
                                    storeName = storeNameInput.trim(),
                                    ownerName = ownerNameInput.trim(),
                                    phone = phoneInput.trim(),
                                    city = cityInput.trim()
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ذخیره تغییرات مشخصات")
                    }
                }
            }
        }

        // 2. Branches & Locations Management
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
                        Text(
                            text = "شعب و محل‌های فروش (کاج، ماهان و ...)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { showAddBranchDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("شعبه جدید", fontSize = 12.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(branches) { branch ->
                            OutlinedCard(shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(branch.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.repository.apply { coroutineScope.launch { deleteBranch(branch) } } },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = LossRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Payment Methods Management
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
                        Text(
                            text = "دستگاه‌های پوز و روش‌های دریافت",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { showAddPaymentMethodDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("روش جدید", fontSize = 12.sp)
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        items(paymentMethods) { pm ->
                            OutlinedCard(shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(pm.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.repository.apply { coroutineScope.launch { deletePaymentMethod(pm) } } },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = LossRed, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Backup & Restore (پشتیبان‌گیری کامل JSON و بازیابی آفلاین)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پشتیبان‌گیری و بازیابی داده‌ها (JSON)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اطلاعات مالی، فروش‌ها، خرید بار و حساب‌های شما کاملاً امن و محلی روی گوشی ذخیره هستند. جهت اطمینان، مرتباً نسخه پشتیبان تهیه کنید.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.getBackupJson()
                                    backupJsonContent = json
                                    showBackupCreatedDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تهیه پشتیبان", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                restoreJsonInput = ""
                                showRestoreDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بازیابی اطلاعات", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 5. Test & Demo Data Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "مدیریت اطلاعات نمونه و آزمایشی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "می‌توانید برای تست و بررسی نرم‌افزار، داده‌های نمونه بارگذاری کنید و هر زمان خواستید آنها را با یک کلیک پاک کنید تا مغازه با داده‌های واقعی شروع به کار کند.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.loadDemoData() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldSecondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("بارگذاری نمونه تستی", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = LossRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پاکسازی داده‌های تستی", fontSize = 11.sp, color = LossRed)
                        }
                    }
                }
            }
        }

        // 6. About App Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اپلیکیشن مدیریت میوه و تره‌بار «میوهیار» (MiveYar)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "نسخه ۱٫۰٫۰ • کاملاً آفلاین و بومی اندروید • بدون هزینه جاری",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Add Branch Dialog
    if (showAddBranchDialog) {
        AlertDialog(
            onDismissRequest = { showAddBranchDialog = false },
            title = { Text("تعریف شعبه یا محل فروش", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    label = { Text("نام محل / شعبه") },
                    placeholder = { Text("مثال: شعبه عظیمیه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            viewModel.addBranch(newBranchName)
                            newBranchName = ""
                            showAddBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBranchDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Add Payment Method Dialog
    if (showAddPaymentMethodDialog) {
        AlertDialog(
            onDismissRequest = { showAddPaymentMethodDialog = false },
            title = { Text("تعریف روش دریافت / کارتخوان جدید", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPaymentMethodName,
                    onValueChange = { newPaymentMethodName = it },
                    label = { Text("نام روش پرداخت یا کارتخوان") },
                    placeholder = { Text("مثال: کارتخوان سامان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPaymentMethodName.isNotBlank()) {
                            viewModel.addPaymentMethod(newPaymentMethodName)
                            newPaymentMethodName = ""
                            showAddPaymentMethodDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("افزودن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPaymentMethodDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Backup Created Dialog
    if (showBackupCreatedDialog) {
        AlertDialog(
            onDismissRequest = { showBackupCreatedDialog = false },
            title = { Text("فایل پشتیبان با موفقیت ساخته شد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("پشتیبان کامل شامل تمام محصولات، فاکتورهای فروش، خرید بار، هزینه‌ها، کارکنان و حساب‌های فروشندگان آماده است.")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("MiveYar Backup", backupJsonContent)
                                clipboard.setPrimaryClip(clip)
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("کپی در حافظه", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, backupJsonContent)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "ارسال یا ذخیره فایل پشتیبان میوهیار")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اشتراک‌گذاری/ذخیره", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupCreatedDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("بازیابی اطلاعات از فایل پشتیبان", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("متن JSON فایل پشتیبان را در کادر زیر وارد کنید یا از کلیپ‌بورد الصاق نمایید:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        label = { Text("محتوای فایل پشتیبان JSON") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isNotBlank()) {
                            viewModel.restoreBackup(restoreJsonInput) {
                                showRestoreDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("شروع بازیابی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Clear confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("تأیید پاکسازی داده‌های تستی", fontWeight = FontWeight.Bold, color = LossRed) },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید تمام فاکتورهای فروش، خریدهای بار، هزینه‌ها و تراکنش‌های آزمایشی را حذف کنید؟ فهرست پایه‌ای محصولات باقی خواهد ماند.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTestData()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("بله، پاکسازی کن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

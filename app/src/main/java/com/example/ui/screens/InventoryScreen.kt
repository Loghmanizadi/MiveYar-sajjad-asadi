package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.ProductStockInfo
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.PersianFormatUtil

@Composable
fun InventoryScreen(viewModel: FruitShopViewModel) {
    val stockList by viewModel.productStockList.collectAsStateWithLifecycle()
    val adjustmentsHistory by viewModel.inventoryAdjustments.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var filterOnlyLowStock by remember { mutableStateOf(false) }

    // Dialog for recording wastage / spoilage / adjustment
    var productForAdjustment by remember { mutableStateOf<ProductStockInfo?>(null) }
    var adjustmentWeightInput by remember { mutableStateOf("") }
    var isWastage by remember { mutableStateOf(true) } // true: ضایعات و دورریز, false: موجودی اولیه یا اصلاح مثبت
    var selectedReason by remember { mutableStateOf("ضایعات و پلاسیدگی") }
    var adjustmentNotes by remember { mutableStateOf("") }

    val filteredList = stockList.filter { item ->
        val matchesSearch = item.product.name.contains(searchQuery, ignoreCase = true) || item.product.category.contains(searchQuery)
        val matchesLow = !filterOnlyLowStock || item.isLowStock
        matchesSearch && matchesLow
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inventory_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Search & Filter Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجوی نام میوه یا دسته‌بندی...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterOnlyLowStock,
                    onClick = { filterOnlyLowStock = !filterOnlyLowStock },
                    label = { Text("فقط کالاهای کم‌موجودی", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFDAD6),
                        selectedLabelColor = LossRed
                    )
                )

                Text(
                    text = "تعداد: ${PersianFormatUtil.toPersianDigits(filteredList.size.toString())} محصول",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 2. Inventory Items List
        items(filteredList) { stockItem ->
            val p = stockItem.product
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stockItem.isLowStock) Color(0xFFFFF7F6) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = p.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(p.category, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "نرخ فروش: ${PersianFormatUtil.formatCurrencyToman(p.salePrice)}",
                                fontSize = 12.sp,
                                color = EmeraldPrimary
                            )
                        }

                        // Current stock badge
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = PersianFormatUtil.formatWeightKg(stockItem.currentStock),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (stockItem.isLowStock) LossRed else EmeraldPrimary
                            )
                            if (stockItem.isLowStock) {
                                Text(
                                    text = "کمبود بار (حداقل ${PersianFormatUtil.formatWeightKg(p.minStockAlert)})",
                                    fontSize = 10.sp,
                                    color = LossRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stock Details Row: Bought, Sold, Wastage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ورود: ${PersianFormatUtil.formatWeightKg(stockItem.totalPurchasedWeight, false)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "فروش: ${PersianFormatUtil.formatWeightKg(stockItem.totalSoldWeight, false)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "ضایعات: ${PersianFormatUtil.formatWeightKg(stockItem.totalWastageWeight, false)}",
                            fontSize = 11.sp,
                            color = if (stockItem.totalWastageWeight > 0) LossRed else Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action: Record Spoilage or Adjust Stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                productForAdjustment = stockItem
                                isWastage = true
                                selectedReason = "ضایعات و پلاسیدگی"
                                adjustmentWeightInput = ""
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp), tint = LossRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ثبت ضایعات", fontSize = 12.sp, color = LossRed)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                productForAdjustment = stockItem
                                isWastage = false
                                selectedReason = "موجودی اولیه افتتاحیه"
                                adjustmentWeightInput = ""
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اصلاح موجودی", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog for Wastage & Adjustment
    productForAdjustment?.let { stockItem ->
        val p = stockItem.product
        AlertDialog(
            onDismissRequest = { productForAdjustment = null },
            title = {
                Text(
                    text = if (isWastage) "ثبت ضایعات و دورریز برای ${p.name}" else "اصلاح موجودی انبار برای ${p.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isWastage) LossRed else EmeraldPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "موجودی فعلی در سیستم: ${PersianFormatUtil.formatWeightKg(stockItem.currentStock)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = adjustmentWeightInput,
                        onValueChange = { adjustmentWeightInput = it },
                        label = { Text("وزن ${if (isWastage) "ضایعات و دورریز" else "اضافه/افتتاحیه"} (کیلو)") },
                        placeholder = { Text("مثال: ۲٫۵ یا ۱۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("adjustment_weight_input")
                    )

                    Text("علت رویداد انبار:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val reasons = if (isWastage) {
                        listOf("ضایعات و پلاسیدگی", "خرابی بار در حمل", "افت رطوبت و وزن", "دورریز میوه خراب")
                    } else {
                        listOf("موجودی اولیه افتتاحیه", "اصلاح خطای شمارش", "بار هدیه یا رایگان")
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        reasons.forEach { r ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedReason = r }
                                    .padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedReason == r) EmeraldPrimary else Color.LightGray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(r, fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = adjustmentNotes,
                        onValueChange = { adjustmentNotes = it },
                        label = { Text("توضیحات اختیاری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = PersianFormatUtil.parseDoubleWeight(adjustmentWeightInput)
                        if (parsed > 0) {
                            val finalQty = if (isWastage) -parsed else parsed
                            viewModel.recordAdjustment(
                                productId = p.id,
                                productName = p.name,
                                quantity = finalQty,
                                reason = selectedReason,
                                branchName = "شعبه اصلی",
                                notes = adjustmentNotes,
                                onSuccess = {
                                    productForAdjustment = null
                                    adjustmentWeightInput = ""
                                    adjustmentNotes = ""
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWastage) LossRed else EmeraldPrimary
                    )
                ) {
                    Text(if (isWastage) "ثبت دورریز" else "ثبت اصلاحیه")
                }
            },
            dismissButton = {
                TextButton(onClick = { productForAdjustment = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

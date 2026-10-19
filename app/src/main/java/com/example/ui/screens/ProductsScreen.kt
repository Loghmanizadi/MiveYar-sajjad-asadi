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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Search
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
import com.example.data.local.entities.ProductEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.PersianFormatUtil

@Composable
fun ProductsScreen(viewModel: FruitShopViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("همه") }

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showBatchPriceDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Form inputs
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("تره‌بار") }
    var unitInput by remember { mutableStateOf("کیلوگرم") }
    var salePriceInput by remember { mutableStateOf("") }
    var refBuyPriceInput by remember { mutableStateOf("") }
    var minStockInput by remember { mutableStateOf("20") }
    var descInput by remember { mutableStateOf("") }

    // Batch price input
    var batchPercentInput by remember { mutableStateOf("10") }

    val categories = listOf("همه", "میوه", "تره‌بار", "صیفی‌جات", "سبزیجات", "مرکبات", "خشکبار")

    val filtered = products.filter { prod ->
        val matchQuery = prod.name.contains(searchQuery, ignoreCase = true)
        val matchCat = selectedCategoryFilter == "همه" || prod.category == selectedCategoryFilter
        matchQuery && matchCat
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("products_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Search Bar & Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعریف محصولات و مدیریت نرخ‌ها",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row {
                    OutlinedButton(
                        onClick = { showBatchPriceDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغییر درصدی نرخ", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = {
                            editingProduct = null
                            nameInput = ""
                            categoryInput = "تره‌بار"
                            unitInput = "کیلوگرم"
                            salePriceInput = ""
                            refBuyPriceInput = ""
                            minStockInput = "20"
                            descInput = ""
                            showProductDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("محصول جدید", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجوی نام میوه یا محصول...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // 2. Products List
        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("محصولی یافت نشد.", color = Color.Gray)
                }
            }
        } else {
            items(filtered) { prod ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = prod.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${prod.category})",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "نرخ فروش: ${PersianFormatUtil.formatCurrencyToman(prod.salePrice)} به ازای هر ${prod.unit}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                            if (prod.referenceBuyPrice > 0) {
                                Text(
                                    text = "قیمت خرید مرجع: ${PersianFormatUtil.formatCurrencyToman(prod.referenceBuyPrice)}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                editingProduct = prod
                                nameInput = prod.name
                                categoryInput = prod.category
                                unitInput = prod.unit
                                salePriceInput = prod.salePrice.toString()
                                refBuyPriceInput = prod.referenceBuyPrice.toString()
                                minStockInput = prod.minStockAlert.toString()
                                descInput = prod.description
                                showProductDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "ویرایش", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                            }

                            IconButton(onClick = { productToDelete = prod }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = LossRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showProductDialog) {
        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = {
                Text(
                    text = if (editingProduct == null) "تعریف محصول جدید" else "ویرایش ${editingProduct!!.name}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("نام میوه / محصول") },
                        placeholder = { Text("مثال: توت فرنگی کرج") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input")
                    )

                    OutlinedTextField(
                        value = salePriceInput,
                        onValueChange = { salePriceInput = it },
                        label = { Text("نرخ فروش (تومان)") },
                        placeholder = { Text("مثال: ۵۵۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("product_price_input")
                    )

                    OutlinedTextField(
                        value = refBuyPriceInput,
                        onValueChange = { refBuyPriceInput = it },
                        label = { Text("قیمت خرید مرجع / تخمینی (تومان)") },
                        placeholder = { Text("مثال: ۴۰۰۰۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("دسته‌بندی") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitInput,
                            onValueChange = { unitInput = it },
                            label = { Text("واحد") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = minStockInput,
                        onValueChange = { minStockInput = it },
                        label = { Text("حداقل موجودی هشدار (کیلو)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val salePrice = PersianFormatUtil.parseLongAmount(salePriceInput)
                        val refBuy = PersianFormatUtil.parseLongAmount(refBuyPriceInput)
                        val minStock = PersianFormatUtil.parseDoubleWeight(minStockInput)

                        if (nameInput.isNotBlank() && salePrice > 0) {
                            val prod = ProductEntity(
                                id = editingProduct?.id ?: 0L,
                                name = nameInput.trim(),
                                category = categoryInput.trim(),
                                unit = unitInput.trim(),
                                salePrice = salePrice,
                                referenceBuyPrice = refBuy,
                                minStockAlert = if (minStock > 0) minStock else 20.0,
                                description = descInput
                            )
                            viewModel.saveProduct(prod, onSuccess = {
                                showProductDialog = false
                            })
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("ذخیره محصول")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Batch Price Adjust Dialog
    if (showBatchPriceDialog) {
        var isIncrease by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { showBatchPriceDialog = false },
            title = { Text("تغییر دسته‌جمعی نرخ‌های فروش", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "دسته‌بندی هدف: ${if (selectedCategoryFilter == "همه") "تمام محصولات مغازه" else selectedCategoryFilter}",
                        fontSize = 13.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isIncrease,
                            onClick = { isIncrease = true },
                            label = { Text("افزایش قیمت (+)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = !isIncrease,
                            onClick = { isIncrease = false },
                            label = { Text("کاهش قیمت (−)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LossRed,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = batchPercentInput,
                        onValueChange = { batchPercentInput = it },
                        label = { Text("درصد تغییر (٪)") },
                        placeholder = { Text("مثال: ۱۰") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "نرخ‌های جدید برای راحتی صندوق‌دار به صورت خودکار به مضرب ۵۰۰ تومان گرد خواهند شد.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pct = batchPercentInput.toDoubleOrNull() ?: 0.0
                        if (pct > 0) {
                            val finalPct = if (isIncrease) pct else -pct
                            viewModel.batchUpdatePricesPercent(
                                category = if (selectedCategoryFilter == "همه") null else selectedCategoryFilter,
                                percentChange = finalPct
                            )
                            showBatchPriceDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("اعمال تغییرات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchPriceDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Delete Product Dialog
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("تأیید حذف محصول", fontWeight = FontWeight.Bold, color = LossRed) },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید محصول «${prod.name}» را حذف کنید؟ سوابق مالی قبلی محفوظ خواهند ماند.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("حذف محصول")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

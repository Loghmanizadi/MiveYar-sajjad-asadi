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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.local.entities.ProductEntity
import com.example.data.local.entities.SaleEntity
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LossRed
import com.example.ui.viewmodel.FruitShopViewModel
import com.example.util.JalaliCalendarUtil
import com.example.util.PersianFormatUtil

data class CartItem(
    val product: ProductEntity,
    val quantity: Double,
    val unitPrice: Long
) {
    val totalPrice: Long get() = (quantity * unitPrice).toLong()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: FruitShopViewModel) {
    val products by viewModel.activeProducts.collectAsStateWithLifecycle()
    val branches by viewModel.branches.collectAsStateWithLifecycle()
    val paymentMethods by viewModel.paymentMethods.collectAsStateWithLifecycle()
    val salesHistory by viewModel.sales.collectAsStateWithLifecycle()
    val stockList by viewModel.productStockList.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: ثبت فروش سریع, 1: سوابق فاکتورها

    // Cart State
    val cart = remember { mutableStateListOf<CartItem>() }
    var selectedBranch by remember { mutableStateOf("شعبه کاج") }
    var selectedPaymentMethod by remember { mutableStateOf("کارتخوان ملت") }
    var saleNotes by remember { mutableStateOf("") }

    // Dialog state for item entry
    var selectedProductForAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var inputWeight by remember { mutableStateOf("") }
    var showSaleSuccessDialog by remember { mutableStateOf(false) }
    var lastRecordedAmount by remember { mutableStateOf(0L) }

    // Product search and filter in sales screen
    var productSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("همه") }
    var showQuickNewProductDialog by remember { mutableStateOf(false) }

    // Delete confirmation dialog
    var saleToDelete by remember { mutableStateOf<SaleEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sales_screen")
    ) {
        // Tab row: Register Sale vs Sales History
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("ثبت فروش جدید", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Text(
                        "فاکتورهای اخیر (${PersianFormatUtil.toPersianDigits(salesHistory.size.toString())})",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            // New Sale Screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
            ) {
                // 1. Branch and Payment Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "شعبه و روش پرداخت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Branches chips
                            Text("انتخاب محل / شعبه:", fontSize = 12.sp, color = Color.Gray)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                val branchList = if (branches.isEmpty()) listOf("شعبه کاج", "شعبه ماهان", "انبار مرکزی") else branches.map { it.name }
                                items(branchList) { bName ->
                                    FilterChip(
                                        selected = selectedBranch == bName,
                                        onClick = { selectedBranch = bName },
                                        label = { Text(bName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            // Payment methods chips
                            Text("روش پرداخت:", fontSize = 12.sp, color = Color.Gray)
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                val pmList = if (paymentMethods.isEmpty()) listOf("کارتخوان ملت", "کارتخوان پارسیان", "نقدی", "کارت به کارت") else paymentMethods.map { it.name }
                                items(pmList) { pmName ->
                                    FilterChip(
                                        selected = selectedPaymentMethod == pmName,
                                        onClick = { selectedPaymentMethod = pmName },
                                        label = { Text(pmName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = GoldSecondary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Fast Product Catalog Grid / Chips for Quick Adding (بدون محدودیت، با جستجو و دسته‌بندی)
                item {
                    val categories = listOf("همه", "میوه", "تره‌بار", "صیفی‌جات", "سبزیجات", "سبزی خوردن", "مرکبات", "خشکبار", "خرما", "سایر")
                    val filteredProducts = products.filter { prod ->
                        val matchQuery = prod.name.contains(productSearchQuery, ignoreCase = true)
                        val matchCat = selectedCategoryFilter == "همه" || prod.category == selectedCategoryFilter
                        matchQuery && matchCat
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فهرست محصولات (${PersianFormatUtil.toPersianDigits(filteredProducts.size.toString())} محصول)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        TextButton(
                            onClick = { showQuickNewProductDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعریف محصول جدید", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("جستجوی نام میوه یا محصول (مثال: سیب، هویج)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredProducts.isEmpty()) {
                        Text(
                            text = "محصولی با این مشخصات یافت نشد.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredProducts) { prod ->
                                val currentStock = stockList.find { it.product.id == prod.id }?.currentStock ?: 0.0
                                OutlinedCard(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clickable {
                                            selectedProductForAdd = prod
                                            inputWeight = ""
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = prod.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "(${prod.category})",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = PersianFormatUtil.formatCurrencyToman(prod.salePrice),
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "موجودی: ${PersianFormatUtil.formatWeightKg(currentStock, false)}",
                                            fontSize = 10.sp,
                                            color = if (currentStock <= prod.minStockAlert) LossRed else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Current Cart Items List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اقلام سبد فروش (${PersianFormatUtil.toPersianDigits(cart.size.toString())} قلم)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        if (cart.isNotEmpty()) {
                            TextButton(onClick = { cart.clear() }) {
                                Text("پاکسازی سبد", color = LossRed, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (cart.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "سبد فروش خالی است. از لیست بالا میوه یا محصول را انتخاب کنید.",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(cart.toList()) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${PersianFormatUtil.formatWeightKg(item.quantity)} × ${PersianFormatUtil.formatCurrencyToman(item.unitPrice)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = PersianFormatUtil.formatCurrencyToman(item.totalPrice),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EmeraldPrimary
                                )

                                IconButton(onClick = { cart.remove(item) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف از سبد",
                                        tint = LossRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Optional Notes & Submit Button
                    item {
                        OutlinedTextField(
                            value = saleNotes,
                            onValueChange = { saleNotes = it },
                            label = { Text("توضیحات اختیاری فاکتور") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val totalAmount = cart.sumOf { it.totalPrice }
                        val totalWeight = cart.sumOf { it.quantity }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "مجموع وزن: ${PersianFormatUtil.formatWeightKg(totalWeight)}",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "جمع کل قابل پرداخت",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedPaymentMethod,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = PersianFormatUtil.formatCurrencyToman(totalAmount),
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val itemsToRegister = cart.map { Pair(it.product, it.quantity) }
                                        viewModel.registerSale(
                                            branchName = selectedBranch,
                                            paymentMethod = selectedPaymentMethod,
                                            notes = saleNotes,
                                            items = itemsToRegister,
                                            onSuccess = {
                                                lastRecordedAmount = totalAmount
                                                cart.clear()
                                                saleNotes = ""
                                                showSaleSuccessDialog = true
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("submit_sale_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldSecondary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تأیید و ثبت نهایی فروش", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 2. Sales History Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
            ) {
                if (salesHistory.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("هنوز هیچ فاکتور فروشی ثبت نشده است.", color = Color.Gray)
                        }
                    }
                } else {
                    items(salesHistory) { sale ->
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
                                    Column {
                                        Text(
                                            text = "فاکتور #${sale.id} • ${sale.branchName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${sale.shamsiDate} • ${sale.paymentMethod}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = PersianFormatUtil.formatCurrencyToman(sale.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = EmeraldPrimary
                                        )
                                        IconButton(onClick = { saleToDelete = sale }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف فاکتور",
                                                tint = LossRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (sale.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "توضیح: ${sale.notes}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet / Dialog to add product with weight
    selectedProductForAdd?.let { prod ->
        AlertDialog(
            onDismissRequest = { selectedProductForAdd = null },
            title = {
                Text(
                    text = "ورود وزن برای ${prod.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "نرخ فعلی: ${PersianFormatUtil.formatCurrencyToman(prod.salePrice)} به ازای هر ${prod.unit}",
                        fontSize = 13.sp,
                        color = EmeraldPrimary
                    )

                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("وزن یا مقدار (${prod.unit})") },
                        placeholder = { Text("مثال: ۲٫۵ یا ۳") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("weight_input_field")
                    )

                    // Quick weight buttons (+1 kg, +2 kg, +0.5 kg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("0.5", "1", "2", "3", "5").forEach { w ->
                            OutlinedButton(
                                onClick = { inputWeight = w },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text(PersianFormatUtil.toPersianDigits(w), fontSize = 11.sp)
                            }
                        }
                    }

                    val parsedWeight = PersianFormatUtil.parseDoubleWeight(inputWeight)
                    if (parsedWeight > 0) {
                        val computed = (parsedWeight * prod.salePrice).toLong()
                        Text(
                            text = "مبلغ: ${PersianFormatUtil.formatCurrencyToman(computed)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldPrimary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = PersianFormatUtil.parseDoubleWeight(inputWeight)
                        if (w > 0) {
                            cart.add(CartItem(product = prod, quantity = w, unitPrice = prod.salePrice))
                            selectedProductForAdd = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("افزودن به سبد")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForAdd = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    // Success Dialog after sale
    if (showSaleSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSaleSuccessDialog = false },
            title = { Text("فروش با موفقیت ثبت شد", fontWeight = FontWeight.Bold) },
            text = {
                Text("مبلغ ${PersianFormatUtil.formatCurrencyToman(lastRecordedAmount)} در دخل ثبت گردید و موجودی کالاها به روز شد.")
            },
            confirmButton = {
                Button(
                    onClick = { showSaleSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("متوجه شدم")
                }
            }
        )
    }

    // Delete confirmation dialog
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("تأیید حذف فاکتور فروش", fontWeight = FontWeight.Bold, color = LossRed) },
            text = {
                Text("آیا مطمئن هستید که می‌خواهید فاکتور فروش شماره ${sale.id} به مبلغ ${PersianFormatUtil.formatCurrencyToman(sale.totalAmount)} را حذف کنید؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSale(sale)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed)
                ) {
                    Text("حذف فاکتور")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "داشبورد", Icons.Default.Dashboard)
    data object Sales : Screen("sales", "ثبت فروش", Icons.Default.ShoppingCart)
    data object Purchase : Screen("purchase", "خرید و بار", Icons.Default.LocalShipping)
    data object Inventory : Screen("inventory", "موجودی انبار", Icons.Default.Inventory)
    data object Expenses : Screen("expenses", "هزینه‌ها", Icons.Default.ReceiptLong)
    data object Staff : Screen("staff", "پرسنل و حقوق", Icons.Default.People)
    data object Suppliers : Screen("suppliers", "حساب فروشندگان", Icons.Default.MonetizationOn)
    data object Reports : Screen("reports", "گزارش‌ها", Icons.Default.Assessment)
    data object Products : Screen("products", "محصولات و نرخ‌ها", Icons.Default.Category)
    data object Settlement : Screen("settlement", "تسویه پایان روز", Icons.Default.PointOfSale)
    data object Settings : Screen("settings", "تنظیمات و پشتیبان", Icons.Default.Settings)

    companion object {
        val allScreens: List<Screen>
            get() = ScreenLists.all

        val bottomNavScreens: List<Screen>
            get() = ScreenLists.bottomNav
    }
}

private object ScreenLists {
    val all: List<Screen> by lazy {
        listOf(
            Screen.Dashboard,
            Screen.Sales,
            Screen.Settlement,
            Screen.Purchase,
            Screen.Inventory,
            Screen.Expenses,
            Screen.Staff,
            Screen.Suppliers,
            Screen.Reports,
            Screen.Products,
            Screen.Settings
        ).filterNotNull()
    }

    val bottomNav: List<Screen> by lazy {
        listOf(
            Screen.Dashboard,
            Screen.Sales,
            Screen.Purchase,
            Screen.Inventory,
            Screen.Reports
        ).filterNotNull()
    }
}

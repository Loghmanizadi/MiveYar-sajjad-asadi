package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppScaffold
import com.example.ui.theme.MiveYarTheme
import com.example.ui.viewmodel.FruitShopViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FruitShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiveYarTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

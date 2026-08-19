package com.example.groceryshopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.ui.theme.BillingScreen
import com.example.groceryshopapp.ui.theme.ShopInventoryScreen
import com.example.groceryshopapp.ui.theme.ShopViewModel
import com.example.groceryshopapp.ui.theme.GroceryShopAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroceryShopAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(shopViewModel: ShopViewModel = viewModel()) {
    // 0 = Inventory Screen, 1 = Billing Counter Screen
    var selectedTab by remember { mutableStateOf(0) }
    val inventoryItemsState = shopViewModel.filteredItems.collectAsState(initial = emptyList<GroceryItem>())
    val inventoryItems = inventoryItemsState.value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Inventory Icon") },
                    label = { Text("Inventory") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Billing Icon") },
                    label = { Text("Billing") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ShopInventoryScreen(viewModel = shopViewModel)
                1 -> BillingScreen(inventoryItems = inventoryItems)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GroceryShopAppTheme {
        MainScreen()
    }
}
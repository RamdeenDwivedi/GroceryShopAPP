package com.example.groceryshopapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.util.ReceiptManager

@Composable
fun BillingScreen(
    inventoryItems: List<GroceryItem>,
    billingViewModel: BillingViewModel = viewModel()
) {
    val cartState = billingViewModel.cart.collectAsState(initial = emptyList<CartItem>())
    val cart = cartState.value
    val grandTotalState = billingViewModel.grandTotal.collectAsState(initial = 0.0)
    val grandTotal = grandTotalState.value
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("🧾 Billing & Checkout Counter", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // 1. SELECTABLE INVENTORY LIST
        Text("Tap an item to add to bill:", style = MaterialTheme.typography.titleSmall)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(inventoryItems) { item ->
                Card(
                    onClick = { billingViewModel.addToCart(item) },
                    enabled = item.stock > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text("In Stock: ${item.stock}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("$${"%.2f".format(item.price)}", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 2. ACTIVE CART ITEMS
        Text("Cart Items (${cart.size}):", style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(cart, key = { it.item.id }) { cartItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cartItem.item.name, style = MaterialTheme.typography.bodyLarge)
                        Text("$${"%.2f".format(cartItem.item.price)} each", style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { billingViewModel.updateQuantity(cartItem, -1) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(32.dp)
                        ) { Text("-") }

                        Text(
                            text = "${cartItem.quantity}",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedButton(
                            onClick = { billingViewModel.updateQuantity(cartItem, 1) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(32.dp)
                        ) { Text("+") }
                    }

                    Text(
                        text = "$${"%.2f".format(cartItem.totalPrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.width(70.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }

        // 3. BILLING SUMMARY & ACTIONS
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grand Total:", style = MaterialTheme.typography.titleLarge)
                    Text("$${"%.2f".format(grandTotal)}", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (cart.isNotEmpty()) {
                                ReceiptManager.shareReceipt(context, cart, grandTotal)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("📲 Share")
                    }

                    Button(
                        onClick = {
                            if (cart.isNotEmpty()) {
                                ReceiptManager.printReceipt(context, cart, grandTotal)
                                billingViewModel.checkoutAndDeductStock()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("🖨️ Print & Complete")
                    }
                }
            }
        }
    }
}
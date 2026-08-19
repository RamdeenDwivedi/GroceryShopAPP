package com.example.groceryshopapp.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.groceryshopapp.data.GroceryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopInventoryScreen(
    viewModel: ShopViewModel = viewModel()
) {
    val inventoryItemsState = viewModel.filteredItems.collectAsState(initial = emptyList<GroceryItem>())
    val inventoryItems = inventoryItemsState.value
    val searchQueryState = viewModel.searchQuery.collectAsState(initial = "")
    val searchQuery = searchQueryState.value

    var nameInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🏬 Inventory Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔍 REAL-TIME SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery: String -> viewModel.onSearchQueryChange(newQuery) },
            label = { Text("Search by name or category...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ➕ ADD NEW PRODUCT FORM
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add New Stock", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.addDemoItems() }) {
                        Text("Add Demo Items")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = stockInput,
                        onValueChange = { stockInput = it },
                        label = { Text("Stock Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("Category (e.g. Dairy, Snacks)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        val stock = stockInput.toIntOrNull() ?: 0
                        if (nameInput.isNotBlank()) {
                            viewModel.addItem(nameInput, price, stock, categoryInput)
                            nameInput = ""
                            priceInput = ""
                            stockInput = ""
                            categoryInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Product to Firestore")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📋 INVENTORY LIST
        Text(
            text = "Inventory List (${inventoryItems.size} items)",
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(inventoryItems, key = { it.id }) { item ->
                GroceryItemRow(
                    item = item,
                    onDelete = { viewModel.deleteItem(item.id) }
                )
            }
        }
    }
}

@Composable
fun GroceryItemRow(
    item: GroceryItem,
    onDelete: () -> Unit
) {
    val isLowStock = item.stock <= 5

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) { 
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Category: ${item.category} | Price: $${"%.2f".format(item.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Stock: ${item.stock} available",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
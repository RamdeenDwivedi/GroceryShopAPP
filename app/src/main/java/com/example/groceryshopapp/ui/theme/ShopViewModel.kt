package com.example.groceryshopapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.data.GroceryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShopViewModel(
    private val repository: GroceryRepository = GroceryRepository()
) : ViewModel() {

    // 1. Search query input state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow = _searchQuery.asStateFlow()

    // 2. Real-time list directly from Firebase Firestore
    private val _allItems = repository.getGroceryItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 3. Dynamic search filter: updates instantly when query or database changes
    val filteredItems: StateFlow> = combine(_allItems, _searchQuery) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            items.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun addItem(name: String, price: Double, stock: Int, category: String) {
        viewModelScope.launch {
            val newItem = GroceryItem(
                name = name.trim(),
                price = price,
                stock = stock,
                category = category.ifBlank { "General" }
            )
            repository.addGroceryItem(newItem)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteGroceryItem(itemId)
        }
    }

    fun addDemoItems() {
        val demoItems = listOf(
            GroceryItem(name = "Apple", price = 1.5, stock = 50, category = "Fruits"),
            GroceryItem(name = "Milk", price = 2.0, stock = 20, category = "Dairy"),
            GroceryItem(name = "Bread", price = 2.5, stock = 30, category = "Bakery"),
            GroceryItem(name = "Eggs", price = 3.0, stock = 15, category = "Dairy"),
            GroceryItem(name = "Chocolate", price = 1.0, stock = 100, category = "Snacks")
        )
        viewModelScope.launch {
            demoItems.forEach { repository.addGroceryItem(it) }
        }
    }
}
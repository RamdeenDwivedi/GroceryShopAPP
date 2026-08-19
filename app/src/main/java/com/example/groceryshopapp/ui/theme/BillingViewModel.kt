package com.example.groceryshopapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.data.GroceryRepository
import com.example.groceryshopapp.data.IGroceryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartItem(
    val item: GroceryItem,
    val quantity: Int = 1
) {
    val totalPrice: Double get() = item.price * quantity
}

class BillingViewModel(
    private val repository: IGroceryRepository = GroceryRepository()
) : ViewModel() {

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Real-time calculated grand total
    val grandTotal: StateFlow<Double> = _cart.map { list ->
        list.sumOf { it.totalPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addToCart(groceryItem: GroceryItem) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == groceryItem.id }

        if (index != -1) {
            val existing = current[index]
            if (existing.quantity < groceryItem.stock) {
                current[index] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            if (groceryItem.stock > 0) {
                current.add(CartItem(item = groceryItem, quantity = 1))
            }
        }
        _cart.value = current
    }

    fun updateQuantity(cartItem: CartItem, delta: Int) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.item.id == cartItem.item.id }
        if (index != -1) {
            val newQty = current[index].quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else if (newQty <= cartItem.item.stock) {
                current[index] = current[index].copy(quantity = newQty)
            }
        }
        _cart.value = current
    }

    // Deducts purchased quantities from Firebase stock and clears the cart
    fun checkoutAndDeductStock() {
        viewModelScope.launch {
            _cart.value.forEach { cartItem ->
                val updatedStock = (cartItem.item.stock - cartItem.quantity).coerceAtLeast(0)
                repository.updateStock(cartItem.item.id, updatedStock)
            }
            _cart.value = emptyList()
        }
    }
}
package com.example.groceryshopapp.data

import com.google.firebase.firestore.DocumentId

/**
 * Data class representing a single grocery item in your store inventory.
 * Default parameter values are required for Firebase deserialization.
 */
data class GroceryItem(
    @DocumentId
    val id: String = "",          // Automatically populated with Firebase document ID
    val name: String = "",        // Item name (e.g., "Whole Wheat Bread")
    val price: Double = 0.0,      // Price per unit or kg
    val stock: Int = 0,           // Quantity currently available in store
    val category: String = "General" // Category (e.g., "Dairy", "Bakery", "Snacks")
)
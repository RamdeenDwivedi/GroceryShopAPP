package com.example.groceryshopapp.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GroceryRepository {

    private val db = FirebaseFirestore.getInstance()
    private val groceryCollection = db.collection("groceries")

    // ==========================================
    // 1. CREATE: Add new grocery item to Firestore
    // ==========================================
    suspend fun addGroceryItem(item: GroceryItem): Result<Unit> {
        return try {
            if (item.id.isEmpty()) {
                // Let Firestore auto-generate a document ID
                groceryCollection.add(item).await()
            } else {
                // Set explicitly if custom ID provided
                groceryCollection.document(item.id).set(item).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. READ: Listen for real-time stock updates
    // ==========================================
    fun getGroceryItems(): Flow<List<GroceryItem>> = callbackFlow {
        val subscription = groceryCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val items = snapshot.toObjects(GroceryItem::class.java)
                trySend(items) // Sends the updated list to the UI in real-time
            }
        }

        // Clean up the listener when the UI leaves the screen
        awaitClose { subscription.remove() }
    }

    // ==========================================
    // 3. UPDATE: Edit item price, name, or stock
    // ==========================================
    suspend fun updateGroceryItem(item: GroceryItem): Result<Unit> {
        return try {
            groceryCollection.document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Quick stock update method (useful during sales/checkout)
    suspend fun updateStock(itemId: String, newStock: Int): Result<Unit> {
        return try {
            groceryCollection.document(itemId).update("stock", newStock).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 4. DELETE: Remove item from inventory
    // ==========================================
    suspend fun deleteGroceryItem(itemId: String): Result<Unit> {
        return try {
            groceryCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
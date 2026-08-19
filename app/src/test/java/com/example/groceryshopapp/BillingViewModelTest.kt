package com.example.groceryshopapp

import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.data.IGroceryRepository
import com.example.groceryshopapp.ui.theme.BillingViewModel
import com.example.groceryshopapp.ui.theme.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: BillingViewModel
    private lateinit var fakeRepository: FakeGroceryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeGroceryRepository()
        viewModel = BillingViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addToCart should add item when stock is available`() = runTest {
        // Given
        val item = GroceryItem(id = "1", name = "Apple", price = 1.0, stock = 5)

        // When
        viewModel.addToCart(item)

        // Then
        assertEquals(1, viewModel.cart.value.size)
        assertEquals(1, viewModel.cart.value[0].quantity)
    }

    @Test
    fun `addToCart should not add item when stock is 0`() = runTest {
        // Given
        val item = GroceryItem(id = "1", name = "Apple", price = 1.0, stock = 0)

        // When
        viewModel.addToCart(item)

        // Then
        assertEquals(0, viewModel.cart.value.size)
    }

    @Test
    fun `grandTotal should calculate correctly`() = runTest {
        // Given
        val item1 = GroceryItem(id = "1", name = "Apple", price = 1.5, stock = 10)
        val item2 = GroceryItem(id = "2", name = "Milk", price = 2.0, stock = 10)

        // Start collecting grandTotal to keep the state active
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.grandTotal.collect {}
        }

        // When
        viewModel.addToCart(item1)
        viewModel.addToCart(item2)
        viewModel.addToCart(item1) // 2x Apple, 1x Milk

        // Then
        assertEquals(5.0, viewModel.grandTotal.value, 0.001)
    }

    @Test
    fun `updateQuantity should remove item if new quantity is 0`() = runTest {
        // Given
        val item = GroceryItem(id = "1", name = "Apple", price = 1.0, stock = 5)
        viewModel.addToCart(item)
        val cartItem = viewModel.cart.value[0]

        // When
        viewModel.updateQuantity(cartItem, -1)

        // Then
        assertEquals(0, viewModel.cart.value.size)
    }

    class FakeGroceryRepository : IGroceryRepository {
        override fun getGroceryItems(): Flow<List<GroceryItem>> = emptyFlow()
        override suspend fun addGroceryItem(item: GroceryItem): Result<Unit> = Result.success(Unit)
        override suspend fun updateGroceryItem(item: GroceryItem): Result<Unit> = Result.success(Unit)
        override suspend fun updateStock(itemId: String, newStock: Int): Result<Unit> = Result.success(Unit)
        override suspend fun deleteGroceryItem(itemId: String): Result<Unit> = Result.success(Unit)
    }
}

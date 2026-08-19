package com.example.groceryshopapp

import com.example.groceryshopapp.data.GroceryItem
import com.example.groceryshopapp.data.IGroceryRepository
import com.example.groceryshopapp.ui.theme.ShopViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ShopViewModel
    private lateinit var fakeRepository: FakeGroceryRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeGroceryRepository()
        viewModel = ShopViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filteredItems should update when search query changes`() = runTest {
        // Given
        val items = listOf(
            GroceryItem(id = "1", name = "Apple", category = "Fruits"),
            GroceryItem(id = "2", name = "Milk", category = "Dairy")
        )
        
        // Start collecting to keep the StateFlow active
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredItems.collect {}
        }

        fakeRepository.emit(items)

        // When
        viewModel.onSearchQueryChange("app")

        // Then
        val filtered = viewModel.filteredItems.value
        assertEquals(1, filtered.size)
        assertEquals("Apple", filtered[0].name)
    }

    @Test
    fun `filteredItems should be empty when no match`() = runTest {
        // Given
        val items = listOf(
            GroceryItem(id = "1", name = "Apple", category = "Fruits")
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredItems.collect {}
        }

        fakeRepository.emit(items)

        // When
        viewModel.onSearchQueryChange("xyz")

        // Then
        val filtered = viewModel.filteredItems.value
        assertEquals(0, filtered.size)
    }

    class FakeGroceryRepository : IGroceryRepository {
        private val _itemsFlow = MutableStateFlow<List<GroceryItem>>(emptyList())
        
        override fun getGroceryItems(): Flow<List<GroceryItem>> = _itemsFlow

        fun emit(items: List<GroceryItem>) {
            _itemsFlow.value = items
        }

        override suspend fun addGroceryItem(item: GroceryItem): Result<Unit> = Result.success(Unit)
        override suspend fun updateGroceryItem(item: GroceryItem): Result<Unit> = Result.success(Unit)
        override suspend fun updateStock(itemId: String, newStock: Int): Result<Unit> = Result.success(Unit)
        override suspend fun deleteGroceryItem(itemId: String): Result<Unit> = Result.success(Unit)
    }
}

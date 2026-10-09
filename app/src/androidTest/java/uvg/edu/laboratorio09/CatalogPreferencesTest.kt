package uvg.edu.laboratorio09

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uvg.edu.laboratorio09.data.local.catalogSortPreferenceKey
import uvg.edu.laboratorio09.data.local.storePreferencesDataStore
import uvg.edu.laboratorio09.model.CatalogSort
import uvg.edu.laboratorio09.viewmodel.StoreViewModel

@RunWith(AndroidJUnit4::class)
class CatalogPreferencesTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Before
    fun clearPreference() = runBlocking {
        application.storePreferencesDataStore.edit { it.clear() }
    }

    @After
    fun cleanUpPreference() = runBlocking {
        application.storePreferencesDataStore.edit { it.clear() }
    }

    @Test
    fun viewModelWritesCatalogSortToDataStore() = runBlocking {
        StoreViewModel(application).updateCatalogSort(CatalogSort.PRICE)

        val storedValue = withTimeout(5_000) {
            application.storePreferencesDataStore.data.first { preferences ->
                preferences[catalogSortPreferenceKey] == CatalogSort.PRICE.storedValue
            }[catalogSortPreferenceKey]
        }

        assertEquals("price", storedValue)
    }

    @Test
    fun newViewModelRestoresPersistedSortAndAppliesItToCatalog() = runBlocking {
        val firstViewModel = StoreViewModel(application)
        firstViewModel.updateCatalogSort(CatalogSort.NAME)
        withTimeout(5_000) {
            application.storePreferencesDataStore.data.first { preferences ->
                preferences[catalogSortPreferenceKey] == CatalogSort.NAME.storedValue
            }
        }

        val restoredState = withTimeout(5_000) {
            StoreViewModel(application).uiState.first { it.catalogSort == CatalogSort.NAME }
        }
        val names = restoredState.filteredProducts.map { it.name.lowercase() }

        assertEquals(CatalogSort.NAME, restoredState.catalogSort)
        assertTrue(names.zipWithNext().all { (first, second) -> first <= second })
    }
}

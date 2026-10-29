package uvg.edu.laboratorio09.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import uvg.edu.laboratorio09.domain.repository.CatalogPreferencesRepository
import uvg.edu.laboratorio09.model.CatalogSort

class DataStoreCatalogPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) : CatalogPreferencesRepository {
    override fun observeCatalogSort(): Flow<CatalogSort> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            CatalogSort.fromStoredValue(preferences[catalogSortPreferenceKey])
        }

    override suspend fun setCatalogSort(sort: CatalogSort): Result<Unit> = try {
        dataStore.edit { preferences ->
            preferences[catalogSortPreferenceKey] = sort.storedValue
        }
        Result.success(Unit)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}

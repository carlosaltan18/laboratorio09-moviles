package uvg.edu.laboratorio09.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val StorePreferencesName = "store_preferences"

val Context.storePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = StorePreferencesName
)

val catalogSortPreferenceKey = stringPreferencesKey("catalog_sort")

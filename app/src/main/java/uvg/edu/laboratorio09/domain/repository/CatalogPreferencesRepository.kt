package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow
import uvg.edu.laboratorio09.model.CatalogSort

interface CatalogPreferencesRepository {
    fun observeCatalogSort(): Flow<CatalogSort>

    suspend fun setCatalogSort(sort: CatalogSort): Result<Unit>
}

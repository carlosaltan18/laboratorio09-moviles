package uvg.edu.laboratorio09.domain.repository

import kotlinx.coroutines.flow.Flow
import uvg.edu.laboratorio09.model.Chocolatier

interface ChocolatierRepository {
    fun observeChocolatiers(): Flow<List<Chocolatier>>
}

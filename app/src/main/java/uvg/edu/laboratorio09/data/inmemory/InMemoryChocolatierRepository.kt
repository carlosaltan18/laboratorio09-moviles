package uvg.edu.laboratorio09.data.inmemory

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import uvg.edu.laboratorio09.domain.repository.ChocolatierRepository
import uvg.edu.laboratorio09.model.Chocolatier

class InMemoryChocolatierRepository : ChocolatierRepository {
    override fun observeChocolatiers(): Flow<List<Chocolatier>> = flowOf(chocolatiers)

    private companion object {
        val chocolatiers = listOf(
            Chocolatier(
                id = "chocolatier_01",
                name = "Cacao Maya",
                role = "Chocolatero artesanal",
                location = "Cobán, Alta Verapaz",
                description = "Taller dedicado a producir chocolates artesanales con cacao guatemalteco y especias locales."
            ),
            Chocolatier(
                id = "chocolatier_02",
                name = "Dulce Antigua",
                role = "Fabricante de chocolate",
                location = "Antigua Guatemala, Sacatepéquez",
                description = "Chocolatería especializada en combinar cacao nacional con café y otros sabores tradicionales."
            )
        )
    }
}

package uvg.edu.laboratorio09.data

import kotlin.random.Random
import uvg.edu.laboratorio09.model.Chocolate

object ChocolateCatalogFactory {
    private const val TotalProducts = 500
    private const val RandomSeed = 202610

    fun createCatalog(
        originalChocolates: List<Chocolate>,
        chocolatierIds: List<String>
    ): List<Chocolate> {
        require(chocolatierIds.isNotEmpty())
        require(originalChocolates.size <= TotalProducts)
        require(originalChocolates.map { it.id }.distinct().size == originalChocolates.size)

        val missingProducts = TotalProducts - originalChocolates.size
        if (missingProducts == 0) return originalChocolates

        val random = Random(RandomSeed)
        val cacaoTypes = listOf("Oscuro 70%", "Oscuro 85%", "Con leche", "Blanco")
        val flavors = listOf("Cardamomo", "Café", "Almendra", "Naranja", "Canela", "Macadamia")
        val presentations = listOf("Barra 60 g", "Barra 90 g", "Caja de bombones", "Tableta artesanal")
        val origins = listOf("Alta Verapaz", "Suchitepéquez", "Izabal", "Petén")

        val generatedChocolates = List(missingProducts) { index ->
            val number = index + 1
            val id = "generated-chocolate-${number.toString().padStart(3, '0')}"
            val cacaoType = cacaoTypes.random(random)
            val flavor = flavors.random(random)
            val presentation = presentations.random(random)
            val origin = origins.random(random)

            Chocolate(
                id = id,
                chocolatierId = chocolatierIds.random(random),
                name = "$cacaoType con $flavor",
                description = "$presentation elaborado con cacao de $origin.",
                priceCents = random.nextInt(from = 3000, until = 9001),
                stock = random.nextInt(from = 0, until = 13),
                imageUrl = "https://picsum.photos/seed/$id/400/400"
            )
        }

        return originalChocolates + generatedChocolates
    }
}

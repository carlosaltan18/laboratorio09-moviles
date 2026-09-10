package uvg.edu.laboratorio09.model

data class StoreUiState(
    val products: List<Chocolate>,
    val profiles: List<Chocolatier>,
    val favoriteProductIds: Set<String> = emptySet()
)

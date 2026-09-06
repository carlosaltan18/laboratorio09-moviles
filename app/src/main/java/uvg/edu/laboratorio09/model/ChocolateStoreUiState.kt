package uvg.edu.laboratorio09.model

data class ChocolateStoreUiState(
    val chocolates: List<Chocolate>,
    val chocolatiers: List<Chocolatier>,
    val favoriteChocolateIds: Set<String> = emptySet()
)

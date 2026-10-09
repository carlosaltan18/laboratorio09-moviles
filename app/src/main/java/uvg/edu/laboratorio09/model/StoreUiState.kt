package uvg.edu.laboratorio09.model

data class StoreUiState(
    val products: List<Chocolate>,
    val profiles: List<Chocolatier>,
    val favoriteProductIds: Set<String> = emptySet(),
    val query: String = "",
    val catalogSort: CatalogSort = CatalogSort.ORIGINAL,
    val orderLines: List<OrderLine> = emptyList(),
    val orderMessage: String? = null,
    val orderSubtotalsCents: Map<String, Int> = emptyMap(),
    val orderTotalCents: Int = 0
) {
    val filteredProducts: List<Chocolate>
        get() {
            val normalizedQuery = query.trim()
            val matchingProducts = if (normalizedQuery.isEmpty()) products else products.filter {
                it.name.contains(normalizedQuery, ignoreCase = true)
            }
            return sortCatalog(matchingProducts, catalogSort)
        }

    val totalOrderUnits: Int
        get() = orderLines.sumOf { it.quantity }
}

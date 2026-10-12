package uvg.edu.laboratorio09.model

enum class CatalogSort(val storedValue: String) {
    ORIGINAL("original"),
    NAME("name"),
    PRICE("price");

    companion object {
        fun fromStoredValue(value: String?): CatalogSort =
            entries.firstOrNull { it.storedValue == value } ?: ORIGINAL
    }
}

fun sortCatalog(
    products: List<Chocolate>,
    sort: CatalogSort
): List<Chocolate> = when (sort) {
    CatalogSort.ORIGINAL -> products
    CatalogSort.NAME -> products.sortedWith(
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    )
    CatalogSort.PRICE -> products.sortedWith(compareBy<Chocolate> { it.priceCents }.thenBy { it.id })
}

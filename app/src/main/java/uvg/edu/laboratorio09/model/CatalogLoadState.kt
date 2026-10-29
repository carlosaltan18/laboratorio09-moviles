package uvg.edu.laboratorio09.model

sealed interface CatalogLoadState {
    data object Loading : CatalogLoadState

    data class Content(
        val products: List<Chocolate>
    ) : CatalogLoadState

    data class Error(
        val message: String
    ) : CatalogLoadState
}

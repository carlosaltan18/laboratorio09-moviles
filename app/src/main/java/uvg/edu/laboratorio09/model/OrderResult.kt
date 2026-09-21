package uvg.edu.laboratorio09.model

sealed interface OrderResult {
    data class Success(val lines: List<OrderLine>, val message: String) : OrderResult
    data class Rejected(val reason: String) : OrderResult
}

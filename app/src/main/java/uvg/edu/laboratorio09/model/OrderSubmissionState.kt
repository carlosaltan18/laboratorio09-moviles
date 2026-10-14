package uvg.edu.laboratorio09.model

sealed interface OrderSubmissionState {
    data object Idle : OrderSubmissionState
    data object Submitting : OrderSubmissionState
    data class Success(val serverId: String) : OrderSubmissionState
    data class Error(val userMessage: String) : OrderSubmissionState
}

package uvg.edu.laboratorio09.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uvg.edu.laboratorio09.StoreApplication
import uvg.edu.laboratorio09.domain.repository.ProductRepository

class ProductDetailViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun onConcurrencyTest(productId: String, attempts: Int = 20) {
        if (_uiState.value.isTesting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            val results = List(attempts) {
                async { productRepository.reserveUnit(productId) }
            }.awaitAll()
            val successes = results.count { it.isSuccess }
            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = "Éxitos: $successes · Rechazos: ${attempts - successes}"
                )
            }
        }
    }

    fun clearTestResult() {
        _uiState.update { it.copy(testResult = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[
                    ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
                ] as StoreApplication
                ProductDetailViewModel(application.container.productRepository)
            }
        }
    }
}

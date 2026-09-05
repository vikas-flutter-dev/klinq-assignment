package com.example.klinq.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.klinq.data.model.Product
import com.example.klinq.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProductDetailsUiState {
    data object Loading : ProductDetailsUiState
    data class Success(val product: Product) : ProductDetailsUiState
    data class Error(val message: String) : ProductDetailsUiState
}

class ProductDetailsViewModel(
    private val repository: ProductRepository,
    private val productId: String,
    private val variantId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductDetailsUiState>(ProductDetailsUiState.Loading)
    val uiState: StateFlow<ProductDetailsUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    fun retry() = loadProduct()

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.value = ProductDetailsUiState.Loading
            repository.getProductDetails(productId, variantId)
                .onSuccess { _uiState.value = ProductDetailsUiState.Success(it) }
                .onFailure { _uiState.value = ProductDetailsUiState.Error(it.message ?: "Something went wrong") }
        }
    }

    class Factory(
        private val repository: ProductRepository,
        private val productId: String,
        private val variantId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProductDetailsViewModel::class.java))
            return ProductDetailsViewModel(repository, productId, variantId) as T
        }
    }
}

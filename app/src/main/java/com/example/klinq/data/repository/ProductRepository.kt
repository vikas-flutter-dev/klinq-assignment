package com.example.klinq.data.repository

import com.example.klinq.data.api.ProductApiService
import com.example.klinq.data.model.Product

class ProductRepository(private val api: ProductApiService) {
    suspend fun getProductDetails(productId: String, variantId: String): Result<Product> {
        return runCatching {
            val response = api.getProductDetails(productId, variantId)
            if (response.status != 200 || response.data == null) {
                error(response.message ?: "Unable to load product")
            }
            response.data
        }
    }
}

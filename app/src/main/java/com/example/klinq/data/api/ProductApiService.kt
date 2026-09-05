package com.example.klinq.data.api

import com.example.klinq.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    @GET("rest/V1/productdetails/{productId}/{variantId}")
    suspend fun getProductDetails(
        @Path("productId") productId: String,
        @Path("variantId") variantId: String,
        @Query("lang") lang: String = "en",
        @Query("store") store: String = "KWD"
    ): ProductResponse
}

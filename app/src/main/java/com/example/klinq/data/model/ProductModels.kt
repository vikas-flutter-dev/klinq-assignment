package com.example.klinq.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Product?
)

data class Product(
    @SerializedName("id") val id: String,
    @SerializedName("sku") val sku: String?,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: String?,
    @SerializedName("final_price") val finalPrice: String?,
    @SerializedName("brand_name") val brandName: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("web_url") val webUrl: String?,
    @SerializedName("is_new") val isNew: Int,
    @SerializedName("is_sale") val isSale: Int,
    @SerializedName("is_trending") val isTrending: Int,
    @SerializedName("is_best_seller") val isBestSeller: Int,
    @SerializedName("image") val image: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("short_description") val shortDescription: String?,
    @SerializedName("is_salable") val isSalable: Boolean,
    @SerializedName("remaining_qty") val remainingQty: Int?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("configurable_option") val configurableOptions: List<ConfigurableOption>?
)

data class ConfigurableOption(
    @SerializedName("attribute_id") val attributeId: Int?,
    @SerializedName("type") val type: String?,
    @SerializedName("attribute_code") val attributeCode: String?,
    @SerializedName("attributes") val attributes: List<ProductVariant>?
)

data class ProductVariant(
    @SerializedName("value") val value: String,
    @SerializedName("option_id") val optionId: String,
    @SerializedName("attribute_image_url") val attributeImageUrl: String?,
    @SerializedName("price") val price: String?,
    @SerializedName("images") val images: List<String>?,
    @SerializedName("color_code") val colorCode: String?,
    @SerializedName("swatch_url") val swatchUrl: String?
)

package com.example.klinq

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.klinq.data.api.RetrofitProvider
import com.example.klinq.data.model.Product
import com.example.klinq.data.model.ProductVariant
import com.example.klinq.data.repository.ProductRepository
import com.example.klinq.databinding.ActivityMainBinding
import com.example.klinq.ui.ColorVariantAdapter
import com.example.klinq.ui.ImagePagerAdapter
import com.example.klinq.ui.ProductDetailsUiState
import com.example.klinq.ui.ProductDetailsViewModel
import kotlinx.coroutines.launch
import java.util.Locale
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val imagePagerAdapter = ImagePagerAdapter()
    private lateinit var colorVariantAdapter: ColorVariantAdapter
    private var productInfoExpanded = true
    private var stockQty = 0
    private var isFavorite = false

    private val viewModel: ProductDetailsViewModel by viewModels {
        ProductDetailsViewModel.Factory(
            repository = ProductRepository(RetrofitProvider.api),
            productId = "6701",
            variantId = "253620"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi()
        observeState()
    }

    private fun setupUi() {
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val topBarHeight = resources.getDimensionPixelSize(R.dimen.top_bar_height)
        val topBarPaddingH = resources.getDimensionPixelSize(R.dimen.top_bar_padding_horizontal)
        val bottomBarPaddingH = resources.getDimensionPixelSize(R.dimen.bottom_bar_padding_horizontal)
        val bottomBarPaddingT = resources.getDimensionPixelSize(R.dimen.bottom_bar_padding_top)
        val bottomBarPaddingB = resources.getDimensionPixelSize(R.dimen.bottom_bar_padding_bottom)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            binding.topBar.updatePadding(
                left = topBarPaddingH,
                top = statusBarInset,
                right = topBarPaddingH,
                bottom = 0
            )
            binding.topBar.layoutParams = binding.topBar.layoutParams.apply {
                height = topBarHeight + statusBarInset
            }

            val scrollParams = binding.contentScroll.layoutParams as ViewGroup.MarginLayoutParams
            scrollParams.topMargin = topBarHeight + statusBarInset
            binding.contentScroll.layoutParams = scrollParams

            binding.bottomBar.updatePadding(
                left = bottomBarPaddingH,
                top = bottomBarPaddingT,
                right = bottomBarPaddingH,
                bottom = bottomBarPaddingB + navBarInset
            )

            insets
        }

        binding.bottomBar.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val bottomBarHeight = bottom - top
            if (bottomBarHeight > 0) {
                binding.contentScroll.updatePadding(bottom = bottomBarHeight + 16.dp())
            }
        }

        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteButton()
            val message = if (isFavorite) "Added to wishlist" else "Removed from wishlist"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        updateFavoriteButton()
        binding.shareButtonTop.setOnClickListener { shareProduct() }
        binding.bagButton.setOnClickListener { binding.contentScroll.smoothScrollTo(0, binding.bottomBar.top) }
        binding.shareButton.setOnClickListener { shareProduct() }
        binding.addToBagButton.setOnClickListener {
            Toast.makeText(this, "Product added to bag", Toast.LENGTH_SHORT).show()
        }
        binding.productInfoHeader.setOnClickListener { toggleProductInfo() }

        // The reference UI uses a square hero area. A fixed square pager prevents
        // layout stretching while Glide's fitCenter preserves each image's native ratio.
        val width = resources.displayMetrics.widthPixels
        binding.imagePager.layoutParams = binding.imagePager.layoutParams.apply { height = width }

        binding.imagePager.adapter = imagePagerAdapter
        binding.imagePager.offscreenPageLimit = 1
        binding.imagePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                renderPageDots(imagePagerAdapter.itemCount, position)
            }
        })

        colorVariantAdapter = ColorVariantAdapter { variant -> showVariant(variant) }
        binding.colorList.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorVariantAdapter
            itemAnimator = null
        }

        binding.minusButton.setOnClickListener {
            val qty = binding.quantityText.text.toString().toIntOrNull() ?: 1
            if (qty > 1) updateQuantity(qty - 1)
        }
        binding.plusButton.setOnClickListener {
            val qty = binding.quantityText.text.toString().toIntOrNull() ?: 1
            if (stockQty <= 0 || qty < stockQty) updateQuantity(qty + 1)
        }
        updateQuantity(1)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        ProductDetailsUiState.Loading -> showLoading(true)
                        is ProductDetailsUiState.Success -> {
                            showLoading(false)
                            renderProduct(state.product)
                        }
                        is ProductDetailsUiState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
        binding.contentScroll.isVisible = !loading
        binding.topBar.isVisible = !loading
        binding.bottomBar.isVisible = !loading
        binding.errorContainer.isVisible = false
    }

    private fun showError(message: String) {
        binding.contentScroll.isVisible = false
        binding.topBar.isVisible = true
        binding.bottomBar.isVisible = false
        binding.errorContainer.isVisible = true
        binding.errorText.text = message
        binding.retryButton.setOnClickListener {
            binding.errorContainer.isVisible = false
            viewModel.retry()
        }
    }

    private fun renderProduct(product: Product) {
        binding.errorContainer.isVisible = false
        binding.topBar.isVisible = true
        binding.bottomBar.isVisible = true

        val productName = product.name.orEmpty()
        binding.topTitle.text = productName
        binding.brandText.text = product.brandName.orEmpty().uppercase(Locale.getDefault())
        binding.productName.text = productName
        binding.skuText.text = "SKU: ${product.sku.orEmpty()}"
        binding.priceText.text = "${formatPrice(product.finalPrice ?: product.price)} KWD"
        stockQty = product.remainingQty ?: 0
        updateQuantity(binding.quantityText.text.toString().toIntOrNull() ?: 1)

        val selectedFileName = product.image?.substringAfterLast("/")
        val defaultVariant = product.configurableOptions
            ?.flatMap { it.attributes.orEmpty() }
            ?.firstOrNull { variant ->
                selectedFileName != null && variant.images.orEmpty().any {
                    it.substringAfterLast("/") == selectedFileName
                }
            }

        val initialImages = defaultVariant?.images.orEmpty()
            .ifEmpty { product.images.orEmpty() }
            .ifEmpty { product.image?.let(::listOf).orEmpty() }

        imagePagerAdapter.submitList(initialImages)
        binding.imagePager.setCurrentItem(0, false)
        renderPageDots(initialImages.size, 0)

        val colorVariants = product.configurableOptions
            ?.firstOrNull { it.attributeCode.equals("color", true) }
            ?.attributes
            .orEmpty()
        colorVariantAdapter.submitList(colorVariants)
        colorVariantAdapter.selectMatchingImage(product.image)
        binding.colorList.isVisible = colorVariants.isNotEmpty()
        binding.colorLabel.isVisible = colorVariants.isNotEmpty()

        val price = (product.finalPrice ?: product.price)?.toDoubleOrNull() ?: 0.0
        binding.installmentText.text = "or 4 interest-free payments\n${String.format(Locale.US, "%.2f", price / 4)} KWD"

        binding.descriptionText.text = renderHtmlDescription(product.description)
        binding.descriptionText.movementMethod = LinkMovementMethod.getInstance()
        setProductInfoExpanded(true)
    }

    private fun renderHtmlDescription(html: String?): CharSequence {
        if (html.isNullOrBlank()) return ""
        val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        return spanned.trimEnd()
    }

    private fun updateFavoriteButton() {
        val iconRes = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        binding.favoriteButton.setImageResource(iconRes)
    }

    private fun showVariant(variant: ProductVariant) {
        val variantImages = variant.images.orEmpty().distinct()
        if (variantImages.isEmpty()) return
        imagePagerAdapter.submitList(variantImages)
        binding.imagePager.setCurrentItem(0, false)
        renderPageDots(variantImages.size, 0)
        binding.priceText.text = "${formatPrice(variant.price)} KWD"
    }

    private fun renderPageDots(count: Int, selected: Int) {
        binding.pageDotsContainer.removeAllViews()
        if (count <= 1) return
        repeat(count) { index ->
            val dot = View(this).apply {
                layoutParams = ViewGroup.MarginLayoutParams(14.dp(), 14.dp()).apply {
                    leftMargin = 3.dp(); rightMargin = 3.dp()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ContextCompat.getColor(this@MainActivity, if (index == selected) R.color.black else R.color.border))
                }
            }
            binding.pageDotsContainer.addView(dot)
        }
    }

    private fun toggleProductInfo() {
        setProductInfoExpanded(!productInfoExpanded)
    }

    private fun setProductInfoExpanded(expanded: Boolean) {
        productInfoExpanded = expanded
        binding.descriptionText.isVisible = expanded
        binding.productInfoArrow.rotation = if (expanded) 180f else 0f
    }

    private fun shareProduct() {
        val product = (viewModel.uiState.value as? ProductDetailsUiState.Success)?.product
        val url = product?.webUrl.orEmpty()
        val text = buildString {
            append(product?.name.orEmpty())
            if (url.isNotBlank()) append("\n").append(url)
        }
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, "Share product"))
    }

    private fun formatPrice(value: String?): String {
        val amount = value?.toDoubleOrNull() ?: 0.0
        return String.format(Locale.US, "%.2f", amount)
    }

    private fun updateQuantity(newQty: Int) {
        val currentQty = newQty.coerceAtLeast(1)
        binding.quantityText.text = currentQty.toString()
        binding.minusButton.isEnabled = currentQty > 1
        binding.plusButton.isEnabled = stockQty <= 0 || currentQty < stockQty
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
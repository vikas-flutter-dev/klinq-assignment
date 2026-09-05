package com.example.klinq.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.klinq.R
import com.example.klinq.data.model.ProductVariant
import com.example.klinq.databinding.ItemColorVariantBinding

class ColorVariantAdapter(
    private val onClick: (ProductVariant) -> Unit
) : RecyclerView.Adapter<ColorVariantAdapter.ColorViewHolder>() {
    private val items = mutableListOf<ProductVariant>()
    private var selectedPosition = 0

    fun submitList(newItems: List<ProductVariant>) {
        items.clear()
        items.addAll(newItems)
        selectedPosition = 0
        notifyDataSetChanged()
    }

    fun select(position: Int) {
        val old = selectedPosition
        selectedPosition = position.coerceIn(0, (items.lastIndex).coerceAtLeast(0))
        notifyItemChanged(old)
        notifyItemChanged(selectedPosition)
    }


    fun selectMatchingImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank() || items.isEmpty()) return
        val fileName = imageUrl.substringAfterLast("/")
        val position = items.indexOfFirst { variant ->
            variant.images.orEmpty().any { it.substringAfterLast("/") == fileName }
        }
        if (position >= 0) select(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorVariantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position == selectedPosition)
        holder.itemView.setOnClickListener {
            select(position)
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class ColorViewHolder(private val binding: ItemColorVariantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductVariant, selected: Boolean) {
//            binding.colorName.text = item.value
            binding.colorCard.strokeWidth = if (selected) 3 else 1
//            binding.colorCard.setStrokeColorResource(
//                if (selected) R.color.klinq_black else R.color.klinq_border
//            )
            Glide.with(binding.colorImage)
                .load(item.swatchUrl ?: item.images?.firstOrNull())
                .error(R.drawable.bg_rounded_surface)
                .into(binding.colorImage)
        }
    }
}

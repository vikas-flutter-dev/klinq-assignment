package com.example.klinq.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.klinq.R
import com.example.klinq.databinding.ItemThumbnailBinding

class ImageThumbnailAdapter(
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ImageThumbnailAdapter.ThumbnailViewHolder>() {
    private val items = mutableListOf<String>()
    private var selectedPosition = 0

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems.distinct())
        selectedPosition = 0
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        val oldPosition = selectedPosition
        selectedPosition = position.coerceIn(0, (items.lastIndex).coerceAtLeast(0))
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val binding = ItemThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThumbnailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
        holder.itemView.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size

    class ThumbnailViewHolder(private val binding: ItemThumbnailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(url: String, selected: Boolean) {
            binding.thumbCard.strokeWidth = if (selected) 3 else 1
//            binding.thumbCard.setStrokeColorResource(
//                if (selected) R.color.klinq_black else R.color.klinq_border
//            )
            Glide.with(binding.thumbnail)
                .load(url)
                .error(R.drawable.bg_rounded_surface)
                .into(binding.thumbnail)
        }
    }
}

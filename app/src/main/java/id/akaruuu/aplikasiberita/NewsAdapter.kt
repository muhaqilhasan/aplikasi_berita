package id.akaruuu.aplikasiberita

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.akaruuu.aplikasiberita.databinding.ItemHeadlineBinding
import id.akaruuu.aplikasiberita.databinding.ItemNewsBinding

class NewsAdapter(
    private val onClick: (News) -> Unit,
    private val onSaveClick: (News) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var newsList = listOf<News>()
    private var isHeadlineMode = false // Flag untuk menentukan apakah ada headline

    // Konstanta tipe view
    private val TYPE_HEADLINE = 0
    private val TYPE_ITEM = 1

    fun setData(newList: List<News>, showHeadline: Boolean = false) {
        newsList = newList
        isHeadlineMode = showHeadline && newList.isNotEmpty()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        // Jika mode headline aktif DAN ini adalah item pertama (posisi 0)
        return if (isHeadlineMode && position == 0) TYPE_HEADLINE else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADLINE) {
            val binding = ItemHeadlineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            HeadlineViewHolder(binding)
        } else {
            val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ItemViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val news = newsList[position]

        if (holder is HeadlineViewHolder) {
            holder.bind(news)
        } else if (holder is ItemViewHolder) {
            holder.bind(news)
        }
    }

    override fun getItemCount() = newsList.size

    // --- ViewHolder untuk HEADLINE (Item Pertama) ---
    inner class HeadlineViewHolder(private val binding: ItemHeadlineBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(news: News) {
            binding.tvHeadlineTitle.text = news.title
            binding.tvHeadlineCategory.text = news.category
            binding.tvHeadlineTime.text = "${news.date} • ${news.time}"

            Glide.with(itemView.context)
                .load(news.image)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.imgHeadline)

            // Atur Icon Bookmark
            val iconRes = if (news.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
            binding.btnHeadlineBookmark.setImageResource(iconRes)

            binding.root.setOnClickListener { onClick(news) }
            binding.btnHeadlineBookmark.setOnClickListener {
                onSaveClick(news)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    // --- ViewHolder untuk LIST BIASA ---
    inner class ItemViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(news: News) {
            binding.tvTitle.text = news.title
            binding.tvCategory.text = news.category
            binding.tvTime.text = "${news.date} • ${news.time}"

            Glide.with(itemView.context)
                .load(news.image)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.imgNews)

            // Atur Icon Bookmark
            val iconRes = if (news.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border
            binding.btnBookmark.setImageResource(iconRes)
            // Hilangkan tint warna abu-abu default karena kita pakai drawable berwarna
            binding.btnBookmark.clearColorFilter()

            binding.root.setOnClickListener { onClick(news) }
            binding.btnBookmark.setOnClickListener {
                onSaveClick(news)
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
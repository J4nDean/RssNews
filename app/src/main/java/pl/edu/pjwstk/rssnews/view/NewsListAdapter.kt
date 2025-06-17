package pl.edu.pjwstk.rssnews.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import pl.edu.pjwstk.rssnews.R
import pl.edu.pjwstk.rssnews.model.NewsItem

class NewsListAdapter(
    private val onClick: (NewsItem) -> Unit,
    private val onFavoriteChanged: (NewsItem, Boolean) -> Unit
) : ListAdapter<NewsItem, NewsListAdapter.NewsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // KLUCZOWE: Wyszarzanie przeczytanych newsów
        if (item.isRead) {
            holder.itemView.alpha = 0.4f
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5")) // Szare tło
        } else {
            holder.itemView.alpha = 1.0f
            holder.itemView.setBackgroundColor(Color.WHITE) // Białe tło
        }

        val favIcon = holder.itemView.findViewById<ImageView>(R.id.favoriteIcon)
        favIcon.setImageResource(
            if (item.isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_outline
        )
        favIcon.setColorFilter(Color.BLACK)

        favIcon.setOnClickListener {
            onFavoriteChanged(item, !item.isFavorite)
        }

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: NewsItem) {
            val titleView = itemView.findViewById<TextView>(R.id.newsTitle)
            val dateView = itemView.findViewById<TextView>(R.id.newsDate)
            val descView = itemView.findViewById<TextView>(R.id.newsDescription)

            titleView.text = item.title
            dateView.text = item.pubDate
            descView.text = item.description

            // KLUCZOWE: Zmiana koloru tekstu dla przeczytanych
            if (item.isRead) {
                titleView.setTextColor(Color.GRAY)
                descView.setTextColor(Color.GRAY)
                dateView.setTextColor(Color.LTGRAY)
            } else {
                titleView.setTextColor(Color.BLACK)
                descView.setTextColor(Color.BLACK)
                dateView.setTextColor(Color.parseColor("#666666"))
            }

            val imageView = itemView.findViewById<ImageView>(R.id.newsImage)
            if (!item.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.color.graphite)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.color.graphite)
            }

            // Wyszarz również obrazek
            imageView.alpha = if (item.isRead) 0.5f else 1.0f
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(old: NewsItem, new: NewsItem) = old.link == new.link
        override fun areContentsTheSame(old: NewsItem, new: NewsItem) = old == new
    }
}

package pl.edu.pjwstk.rssnews.view

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

class NewsListAdapter(private val onClick: (NewsItem) -> Unit) :
    ListAdapter<NewsItem, NewsListAdapter.NewsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: NewsItem) {
            itemView.findViewById<TextView>(R.id.newsTitle).text = item.title
            itemView.findViewById<TextView>(R.id.newsDate).text = item.pubDate
            val imageView = itemView.findViewById<ImageView>(R.id.newsImage)
            if (!item.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(item.imageUrl)
                    .placeholder(R.color.graphite)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.color.graphite)
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NewsItem>() {
        override fun areItemsTheSame(old: NewsItem, new: NewsItem) = old.link == new.link
        override fun areContentsTheSame(old: NewsItem, new: NewsItem) = old == new
    }
}
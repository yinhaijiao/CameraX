package com.example.cameraapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(
    private val newsList: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewNewsTitle)
        private val summaryTextView: TextView = itemView.findViewById(R.id.textViewNewsSummary)
        private val sourceTextView: TextView = itemView.findViewById(R.id.textViewNewsSource)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewNewsTime)

        fun bind(newsItem: NewsItem) {
            titleTextView.text = newsItem.title
            summaryTextView.text = newsItem.summary
            sourceTextView.text = newsItem.source
            timeTextView.text = newsItem.publishTime

            itemView.setOnClickListener {
                onItemClick(newsItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size
}
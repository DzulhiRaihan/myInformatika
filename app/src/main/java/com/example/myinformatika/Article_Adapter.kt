package com.example.myinformatika

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ArticleAdapter(private val list: List<ArticleModel>) :
    RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.articleTitle)
        val date: TextView = itemView.findViewById(R.id.articleDate)
        val image: ImageView = itemView.findViewById(R.id.articleImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.article_item, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.title
        holder.date.text = item.date

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.pstilogo)
            .into(holder.image)

        // Intent ke Detail
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ArticleDetail::class.java).apply {
                putExtra("title", item.title)
                putExtra("date", item.date)
                putExtra("imageUrl", item.imageUrl)
                putExtra("content", item.content) // jika kamu punya konten
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = list.size
}

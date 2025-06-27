package com.example.myinformatika

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ArticleDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val contentTextView = findViewById<TextView>(R.id.contentTextView)
        val articleImageView = findViewById<ImageView>(R.id.articleImageView)


        // Data dari Intent
        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val content = intent.getStringExtra("content")
        val imageUrl = intent.getStringExtra("imageUrl")

        titleTextView.text = title
        dateTextView.text = date
        contentTextView.text = content

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.pstilogo)
            .into(articleImageView)
    }
}

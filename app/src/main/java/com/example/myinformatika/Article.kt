package com.example.myinformatika

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class Article : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter
    private val articleList = mutableListOf<ArticleModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Article)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleAdapter = ArticleAdapter(articleList)
        recyclerView.adapter = articleAdapter

        fetchArticlesFromFirebase()
    }

    private fun fetchArticlesFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val articlesRef = database.getReference("articles")

        articlesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                articleList.clear()
                for (articleSnapshot in snapshot.children) {
                    val article = articleSnapshot.getValue(ArticleModel::class.java)
                    if (article != null) {
                        articleList.add(article)
                    }
                }
                articleAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Gagal memuat data: ${error.message}")
            }
        })
    }
}

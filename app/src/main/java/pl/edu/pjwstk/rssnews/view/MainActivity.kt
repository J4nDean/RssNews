package pl.edu.pjwstk.rssnews.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import pl.edu.pjwstk.rssnews.R
import pl.edu.pjwstk.rssnews.viewmodel.NewsViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(true)

        adapter = NewsListAdapter { newsItem ->
            val intent = Intent(this, NewsDetailActivity::class.java)
            intent.putExtra("title", newsItem.title)
            intent.putExtra("description", newsItem.description)
            intent.putExtra("imageUrl", newsItem.imageUrl)
            intent.putExtra("pubDate", newsItem.pubDate)
            intent.putExtra("link", newsItem.link)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_rss)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        viewModel.newsList.observe(this, Observer { news ->
            adapter.submitList(news)
        })

        viewModel.loadNews()
    }
}
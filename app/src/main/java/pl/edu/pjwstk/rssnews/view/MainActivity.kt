package pl.edu.pjwstk.rssnews.view

import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import pl.edu.pjwstk.rssnews.R
import pl.edu.pjwstk.rssnews.viewmodel.NewsViewModel
import pl.edu.pjwstk.rssnews.model.NewsItem
import pl.edu.pjwstk.rssnews.repository.UserNewsRepository

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsListAdapter
    private var showingFavorites = false
    private var allNews: List<NewsItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        adapter = NewsListAdapter(
            onClick = { newsItem -> showNewsDetail(newsItem) },
            onFavoriteChanged = { newsItem, newState ->
                viewModel.toggleFavorite(newsItem, newState)
            }
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_rss)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        viewModel.newsList.observe(this, Observer { news ->
            allNews = news
            updateDisplayedList()
        })
        viewModel.loadNews()

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val favoriteIcon = findViewById<ImageView>(R.id.favoriteIcon)
        favoriteIcon.setOnClickListener {
            showingFavorites = !showingFavorites
            updateDisplayedList()
            favoriteIcon.setImageResource(
                if (showingFavorites) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_outline
            )
        }
    }

    private fun updateDisplayedList() {
        val displayList = if (showingFavorites) {
            allNews.filter { it.isFavorite }
        } else {
            allNews
        }
        adapter.submitList(displayList)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNews()
    }

    private fun showNewsDetail(newsItem: NewsItem) {
        Log.d("MainActivity", "Clicking news: ${newsItem.title}, isRead: ${newsItem.isRead}")

        if (!newsItem.isRead) {
            UserNewsRepository().markAsRead(newsItem)
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.loadNews()
            }, 500)
        }
        val options = arrayOf("Otwórz w aplikacji", "Otwórz w przeglądarce")
        AlertDialog.Builder(this)
            .setTitle("Jak chcesz otworzyć artykuł?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, WebViewActivity::class.java).apply {
                        putExtra("link", newsItem.link)
                    })
                    1 -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.link)))
                }
            }
            .show()
    }
}

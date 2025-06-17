package pl.edu.pjwstk.rssnews.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import com.squareup.picasso.Picasso
import pl.edu.pjwstk.rssnews.R

class NewsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.extras == null) {
            Toast.makeText(this, "Błąd ładowania artykułu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_news_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_name)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val bottomAppBar = findViewById<BottomAppBar>(R.id.bottom_app_bar)
        bottomAppBar.setNavigationOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val imageUrl = intent.getStringExtra("imageUrl")

        findViewById<TextView>(R.id.detailTitle).text = title
        findViewById<TextView>(R.id.detailContent).text = description

        val imageView = findViewById<ImageView>(R.id.detailImage)
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.color.graphite)
                .into(imageView)
        } else {
            imageView.setImageResource(R.color.graphite)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

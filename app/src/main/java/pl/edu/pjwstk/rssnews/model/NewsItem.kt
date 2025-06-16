package pl.edu.pjwstk.rssnews.model

data class NewsItem(
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val imageUrl: String?
)

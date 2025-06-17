package pl.edu.pjwstk.rssnews.repository

import pl.edu.pjwstk.rssnews.model.NewsItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.net.URL

class NewsRepository {
    fun fetchNews(): List<NewsItem> {
        val url = "https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_swiat.xml"
        val newsList = mutableListOf<NewsItem>()
        val input = URL(url).openStream()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(input, null)

        var eventType = parser.eventType
        var title = ""
        var description = ""
        var link = ""
        var pubDate = ""
        var imageUrl: String? = null
        var text = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tag == "item") {
                        title = ""
                        description = ""
                        link = ""
                        pubDate = ""
                        imageUrl = null
                    }
                    if (tag == "enclosure") {
                        imageUrl = parser.getAttributeValue(null, "url")
                    }
                }
                XmlPullParser.TEXT -> {
                    text = parser.text ?: ""
                }
                XmlPullParser.END_TAG -> {
                    when (tag) {
                        "title" -> if (title.isEmpty()) title = text
                        "description" -> if (description.isEmpty()) description = text
                        "link" -> if (link.isEmpty()) link = text
                        "pubDate" -> if (pubDate.isEmpty()) pubDate = text
                        "item" -> {
                            if (imageUrl == null) {
                                val imgRegex = Regex("src=['\"]([^'\"]+)['\"]")
                                val match = imgRegex.find(description)
                                imageUrl = match?.groups?.get(1)?.value
                            }
                            newsList.add(
                                NewsItem(
                                    title = title,
                                    description = description.replace(Regex("<.*?>"), ""),
                                    link = link,
                                    pubDate = pubDate,
                                    imageUrl = imageUrl
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return newsList
    }
}

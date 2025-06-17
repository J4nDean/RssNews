package pl.edu.pjwstk.rssnews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.edu.pjwstk.rssnews.model.NewsItem
import pl.edu.pjwstk.rssnews.repository.NewsRepository
import pl.edu.pjwstk.rssnews.repository.UserNewsRepository

class NewsViewModel : ViewModel() {
    private val repository = NewsRepository()
    private val userRepo = UserNewsRepository()
    private val _newsList = MutableLiveData<List<NewsItem>>()
    val newsList: LiveData<List<NewsItem>> = _newsList

    private var readLinks = emptySet<String>()
    private var favoriteLinks = emptySet<String>()
    private var registrations = mutableListOf<ListenerRegistration>()

    // Lokalne zmiany oczekujące na synchronizację
    private val pendingFavoriteChanges = mutableMapOf<String, Boolean>()

    init {
        loadNews()
        setupFirestoreListeners()
    }

    private fun setupFirestoreListeners() {
        registrations.add(
            userRepo.getReadLinksLive { links ->
                readLinks = links
                mergeData()
            }
        )
        registrations.add(
            userRepo.getFavoritesLinksLive { links ->
                favoriteLinks = links
                // Wyczyść pending changes które zostały zsynchronizowane
                pendingFavoriteChanges.keys.removeAll { link ->
                    val isFavoriteInFirestore = links.contains(link)
                    val pendingState = pendingFavoriteChanges[link]
                    isFavoriteInFirestore == pendingState
                }
                mergeData()
            }
        )
    }

    fun toggleFavorite(newsItem: NewsItem, newFavoriteState: Boolean) {
        // Zapisz lokalne zmiany
        pendingFavoriteChanges[newsItem.link] = newFavoriteState

        // Natychmiast zaktualizuj UI
        mergeData()

        // Synchronizuj z Firestore
        viewModelScope.launch(Dispatchers.IO) {
            if (newFavoriteState) {
                userRepo.addToFavorites(newsItem)
            } else {
                userRepo.removeFromFavorites(newsItem)
            }
        }
    }

    private fun normalizeLink(link: String): String {
        return link.trim().lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .split("?")[0]
            .split("#")[0]
    }

    private fun mergeData() {
        val currentNews = _newsList.value ?: return

        val updated = currentNews.map { item ->
            val normalizedItemLink = normalizeLink(item.link)

            val isRead = readLinks.any { readLink ->
                val normalizedReadLink = normalizeLink(readLink)
                item.link == readLink ||
                        normalizedItemLink == normalizedReadLink ||
                        item.link.contains(readLink) ||
                        readLink.contains(item.link)
            }

            // Sprawdź najpierw pending changes, potem Firestore
            val isFavorite = pendingFavoriteChanges[item.link] ?: favoriteLinks.any { favLink ->
                val normalizedFavLink = normalizeLink(favLink)
                item.link == favLink ||
                        normalizedItemLink == normalizedFavLink ||
                        item.link.contains(favLink) ||
                        favLink.contains(item.link)
            }

            item.copy(
                isRead = isRead,
                isFavorite = isFavorite
            )
        }

        _newsList.postValue(updated)
    }

    fun loadNews() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val news = repository.fetchNews()
                _newsList.postValue(news)
                mergeData()
            } catch (e: Exception) {
                // Obsłuż błąd w razie potrzeby
            }
        }
    }

    override fun onCleared() {
        registrations.forEach { it.remove() }
        super.onCleared()
    }
}

package pl.edu.pjwstk.rssnews.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.edu.pjwstk.rssnews.model.NewsItem
import pl.edu.pjwstk.rssnews.repository.NewsRepository

class NewsViewModel : ViewModel() {
    private val repository = NewsRepository()
    private val _newsList = MutableLiveData<List<NewsItem>>()
    val newsList: LiveData<List<NewsItem>> = _newsList

    fun loadNews() {
        viewModelScope.launch(Dispatchers.IO) {
            val news = repository.fetchNews()
            _newsList.postValue(news)
        }
    }
}
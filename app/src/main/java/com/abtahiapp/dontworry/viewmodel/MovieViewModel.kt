package com.abtahiapp.dontworry.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abtahiapp.dontworry.Movie
import com.abtahiapp.dontworry.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> get() = _movies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchMoviesByMood(mood: String?, apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val genreId = when (mood) {
                "Angry" -> 35 // Comedy
                "Very Sad", "Sad" -> 18 // Drama
                "Fine", "Very Fine" -> 28 // Action
                else -> null // If no mood, fetch popular movies
            }

            try {
                val movieResponse = withContext(Dispatchers.IO) {
                    if (genreId != null) {
                        RetrofitClient.movieInstance.getMoviesByGenre(apiKey, genreId)
                    } else {
                        RetrofitClient.movieInstance.getMovies(apiKey)
                    }
                }
                fetchTrailersForMovies(movieResponse.results, apiKey)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Failed to fetch movies: ${e.message}"
            }
        }
    }

    private suspend fun fetchTrailersForMovies(movies: List<Movie>, apiKey: String) {
        viewModelScope.launch {
            movies.forEach { movie ->
                try {
                    val trailerResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.movieInstance.getMovieTrailers(movie.id, apiKey)
                    }
                    val trailer = trailerResponse.results.find { it.type == "Trailer" }
                    trailer?.let {
                        movie.trailerUrl = it.key
                    }
                } catch (e: Exception) {
                }
            }
            _movies.postValue(movies)
        }
    }
}

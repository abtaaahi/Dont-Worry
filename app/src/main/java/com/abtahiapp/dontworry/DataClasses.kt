package com.abtahiapp.dontworry

import com.google.gson.annotations.SerializedName

data class Mood(
    val moodImage: Int,
    val dateTime: String,
    val details: String
)

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

data class Article(
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)

data class VideoResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(
    val videoId: String
)

data class Snippet(
    val title: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val high: Thumbnail
)

data class Thumbnail(
    val url: String
)

data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    val total_results: Int,
    val total_pages: Int
)

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("overview") val description: String,
    @SerializedName("poster_path") val thumbnailUrl: String,
    var trailerUrl: String? = null
)

data class TrailerResponse(
    val results: List<Trailer>
)

data class Trailer(
    val key: String,
    val type: String
)

data class WeatherResponse(
    val list: List<WeatherForecast>
)
data class WeatherForecast(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherDescription>,
    val dt_txt: String
)
data class Main(
    val temp: Double
)
data class WeatherDescription(
    val description: String,
    val icon: String
)
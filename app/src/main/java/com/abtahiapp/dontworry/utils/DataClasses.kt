package com.abtahiapp.dontworry.utils

import com.google.gson.annotations.SerializedName

data class Mood(
    val moodImage: Int,
    val dateTime: String,
    val details: String
)

data class GoogleCustomSearchResponse(
    val items: List<Item>
)

data class Item(
    val title: String,
    val snippet: String,
    val link: String,
    val pagemap: Pagemap?
)

data class Pagemap(
    val cse_image: List<CseImage>?
)

data class CseImage(
    val src: String?
)

data class Place(
    val name: String,
    val imageUrl: String
)

data class CustomSearchResponse(
    val items: List<SearchItem>
)

data class SearchItem(
    val title: String,
    val link: String,
    val pagemap: PageMapPlaces?
)

data class PageMapPlaces(
    val cse_thumbnail: List<ThumbnailPlaces>?
)

data class ThumbnailPlaces(
    val src: String
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

data class CurrentWeatherResponse(
    val weather: List<CurrentWeatherDescription>
)
data class CurrentWeatherDescription(
    val main: String
)

data class QuoteResponse(
    val quote: String,
    val author: String
)

data class HomeItem(
    val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val type: HomeItemType
)

enum class HomeItemType {
    VIDEO,
    ARTICLE,
    AUDIO,
    POST
}

data class Post(
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val postTime: String = "",
    val userId: String = "",
    var id: String = ""
)

sealed class HomeFeedItem {
    data class PostItem(val post: Post) : HomeFeedItem()
    data class HomeItemItem(val homeItem: HomeItem) : HomeFeedItem()
}

data class PersonalItem(
    val text: String,
    val timestamp: String,
    val voiceUrl: String
)

data class SentimentRequest(val text: String)

data class SentimentResponse(val sentiment: String)

//data class NewsResponse(
//    val status: String,
//    val totalResults: Int,
//    val articles: List<Article>
//)
//
//data class Article(
//    val author: String?,
//    val title: String,
//    val description: String?,
//    val url: String,
//    val urlToImage: String?,
//    val publishedAt: String,
//    val content: String?
//)

//data class Place(
//    val name: String,
//    val imageUrl: String,
//    val latitude: Double,
//    val longitude: Double
//)
//
//data class PlacesResponse(
//    val results: List<PlaceResult>
//)
//
//data class PlaceResult(
//    val name: String,
//    val geometry: Geometry,
//    val photos: List<Photo>?
//)
//
//data class Geometry(
//    val location: Location
//)
//
//data class Location(
//    val lat: Double,
//    val lng: Double
//)
//
//data class Photo(
//    val photo_reference: String
//)
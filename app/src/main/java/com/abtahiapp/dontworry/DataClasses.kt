package com.abtahiapp.dontworry

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

data class AudioResponse(
    val album: List<AudioItem>
)

data class AudioItem(
    val idAlbum: String,
    val strAlbum: String,
    val strAlbumThumb: String,
    val strArtist: String
)

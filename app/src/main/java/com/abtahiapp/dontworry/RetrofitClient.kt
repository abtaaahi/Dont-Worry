package com.abtahiapp.dontworry

import com.abtahiapp.dontworry.apiservice.MovieApiService
import com.abtahiapp.dontworry.apiservice.NewsApiService
import com.abtahiapp.dontworry.apiservice.WeatherApiService
import com.abtahiapp.dontworry.apiservice.YouTubeApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_NEWS = "https://newsapi.org/"
    private const val BASE_URL_YOUTUBE = "https://www.googleapis.com/"
    private const val BASE_URL_MOVIE = "https://api.themoviedb.org/3/"
    private const val BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/"
    //private const val BASE_URL_AUDIO = "https://theaudiodb.com/api/v1/json/2/"
    //private const val BASE_URL_SPOTIFY = "https://api.spotify.com/v1/"
    //private const val BASE_URL_DEEZER = "https://api.deezer.com/"

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: NewsApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_NEWS)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(NewsApiService::class.java)
    }

    val youtubeInstance: YouTubeApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_YOUTUBE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(YouTubeApiService::class.java)
    }

    val movieInstance: MovieApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_MOVIE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(MovieApiService::class.java)
    }

    val weatherInstance: WeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(WeatherApiService::class.java)
    }

//    val audioInstance: AudioApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL_DEEZER)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(AudioApiService::class.java)
//    }
}

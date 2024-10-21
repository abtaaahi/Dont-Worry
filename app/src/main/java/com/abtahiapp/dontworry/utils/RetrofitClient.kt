package com.abtahiapp.dontworry.utils

import com.abtahiapp.dontworry.BuildConfig
import com.abtahiapp.dontworry.apiservice.CurrentWeatherApiService
import com.abtahiapp.dontworry.apiservice.GoogleCustomSearchApiService
import com.abtahiapp.dontworry.apiservice.MovieApiService
import com.abtahiapp.dontworry.apiservice.PlacesApiService
import com.abtahiapp.dontworry.apiservice.QuotesApiService
import com.abtahiapp.dontworry.apiservice.RevAiService
import com.abtahiapp.dontworry.apiservice.TextBlobApiService
import com.abtahiapp.dontworry.apiservice.WeatherApiService
import com.abtahiapp.dontworry.apiservice.YouTubeApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Request

object RetrofitClient {
    private const val ACCESS_TOKEN = BuildConfig.REVAI_ACCESS_TOKEN

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: GoogleCustomSearchApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_GOOGLE_CUSTOM_SEARCH)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(GoogleCustomSearchApiService::class.java)
    }

    val youtubeInstance: YouTubeApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_GOOGLE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(YouTubeApiService::class.java)
    }

    val movieInstance: MovieApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_MOVIE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(MovieApiService::class.java)
    }

    val weatherInstance: WeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(WeatherApiService::class.java)
    }

    val currentWeatherInstance: CurrentWeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_CURRENT_WEATHER)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CurrentWeatherApiService::class.java)
    }

    val quotesInstance: QuotesApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_QUOTES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(QuotesApiService::class.java)
    }

    val customSearchInstance: PlacesApiService by lazy {
        val retrofit = Retrofit.Builder()
        .baseUrl(BaseUrls.BASE_URL_GOOGLE)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PlacesApiService::class.java)
    }

    private val authInterceptor = Interceptor { chain ->
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $ACCESS_TOKEN")
            .build()
        chain.proceed(newRequest)
    }

    private val client2 = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val revInstance: RevAiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrls.BASE_URL_REV)
            .client(client2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RevAiService::class.java)
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BaseUrls.BASE_URL_TEXT_BLOB)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun create(serviceClass: Class<TextBlobApiService>): TextBlobApiService {
        return retrofit.create(serviceClass)
    }
}

//    val instance: NewsApiService by lazy {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL_NEWS)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//        retrofit.create(NewsApiService::class.java)
//    }

//    val placesInstance: PlacesApiService by lazy {
//        val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL_GOOGLE)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        retrofit.create(PlacesApiService::class.java)
//    }

//    val audioInstance: AudioApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL_DEEZER)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(AudioApiService::class.java)
//    }
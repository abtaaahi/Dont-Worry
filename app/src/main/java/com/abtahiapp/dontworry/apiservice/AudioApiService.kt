package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.utils.VideoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AudioApiService {
    @GET("youtube/v3/search")
    fun getVideos(
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 10,
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("key") apiKey: String
    ): Call<VideoResponse>
}

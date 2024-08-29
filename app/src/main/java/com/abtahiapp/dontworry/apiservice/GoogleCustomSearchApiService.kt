package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.GoogleCustomSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleCustomSearchApiService {
    @GET("v1")
    fun getSearchResults(
        @Query("q") query: String,
        @Query("cx") customSearchEngineId: String,
        @Query("key") apiKey: String
    ): Call<GoogleCustomSearchResponse>
}
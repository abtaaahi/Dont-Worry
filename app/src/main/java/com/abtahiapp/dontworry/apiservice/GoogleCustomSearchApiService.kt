package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.utils.GoogleCustomSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleCustomSearchApiService {
    @GET("v1")
    suspend fun getSearchResults(
        @Query("q") query: String,
        @Query("cx") customSearchEngineId: String,
        @Query("key") apiKey: String
    ): GoogleCustomSearchResponse
}
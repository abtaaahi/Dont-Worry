package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.PlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String = "tourist_attraction",
        @Query("key") apiKey: String
    ): Call<PlacesResponse>
}

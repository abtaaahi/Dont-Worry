package com.abtahiapp.dontworry

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AudioApiService {
    @GET("searchalbum.php")
    fun searchAlbum(
        @Query("s") artistName: String
    ): Call<AudioResponse>
}
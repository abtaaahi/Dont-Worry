package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.MovieResponse
import com.abtahiapp.dontworry.TrailerResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    fun getMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Call<MovieResponse>

    @GET("discover/movie")
    fun getMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1
    ): Call<MovieResponse>

    @GET("movie/{movie_id}/videos")
    fun getMovieTrailers(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<TrailerResponse>
}


package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.utils.QuoteResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface QuotesApiService {
    @Headers("X-Api-Key: J5XFUU/Y+rXH1QOcA13VDQ==BRxrRXMNUGpGFzsU")
    @GET("quotes")
    fun getRandomQuote(@Query("category") category: String = ""): Call<List<QuoteResponse>>
}
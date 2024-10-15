package com.abtahiapp.dontworry.apiservice

import com.abtahiapp.dontworry.utils.SentimentRequest
import com.abtahiapp.dontworry.utils.SentimentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TextBlobApiService {
    @POST("/analyze")
    fun analyzeSentiment(@Body request: SentimentRequest): Call<SentimentResponse>
}
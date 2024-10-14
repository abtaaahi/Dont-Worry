package com.abtahiapp.dontworry.apiservice

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RevAiService {

    @Multipart
    @POST("speechtotext/v1/jobs")
    fun submitTranscriptionJob(
        @Part audio: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("speechtotext/v1/jobs/{job_id}/transcript")
    fun getTranscriptionResult(
        @Path("job_id") jobId: String
    ): Call<ResponseBody>
}
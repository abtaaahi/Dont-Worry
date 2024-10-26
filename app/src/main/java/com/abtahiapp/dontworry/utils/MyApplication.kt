package com.abtahiapp.dontworry.utils

import android.app.Application
import com.abtahiapp.dontworry.apiservice.TextBlobApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val socketManager = SocketManager.getInstance(this)
        socketManager.connectSocket()

        if (NetworkUtil.isOnline(this)) {
            wakeUpServer()
        }
    }

    private fun wakeUpServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val service = RetrofitClient.create(TextBlobApiService::class.java)
                service.analyzeSentiment(SentimentRequest("Test"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        val socketManager = SocketManager.getInstance(this)
        socketManager.disconnectSocket()
    }
}
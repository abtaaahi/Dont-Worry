package com.abtahiapp.dontworry.utils

import android.app.Application

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val socketManager = SocketManager.getInstance(this)
        socketManager.connectSocket()
    }

    override fun onTerminate() {
        super.onTerminate()

        val socketManager = SocketManager.getInstance(this)
        socketManager.disconnectSocket()
    }
}
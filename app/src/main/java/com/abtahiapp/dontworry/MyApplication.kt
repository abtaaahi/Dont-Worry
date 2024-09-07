package com.abtahiapp.dontworry

import android.app.Application
import com.abtahiapp.dontworry.SocketManager

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
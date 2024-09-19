package com.abtahiapp.dontworry.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class SocketManager private constructor(context: Context) {

    private lateinit var socket: Socket

    init {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            val userId = account?.id ?: "unknown"
            socket = IO.socket("https://dont-worry.onrender.com", IO.Options().apply {
                query = "userId=$userId"
            })
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun connectSocket() {
        socket.connect()
        Log.d("SocketManager", "Connected to the server.")
    }

    fun disconnectSocket() {
        socket.disconnect()
        Log.d("SocketManager", "Disconnected from the server.")
    }

    fun getSocket(): Socket {
        return socket
    }

    companion object {
        @Volatile
        private var INSTANCE: SocketManager? = null

        fun getInstance(context: Context): SocketManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager(context).also { INSTANCE = it }
            }
    }
}

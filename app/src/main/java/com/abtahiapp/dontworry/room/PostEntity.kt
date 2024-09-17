package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val postTime: String = "",
    val userId: String = ""
)

package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class AudioEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val thumbnailUrl: String
)
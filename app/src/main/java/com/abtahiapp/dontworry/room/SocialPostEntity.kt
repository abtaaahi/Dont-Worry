package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_posts")
data class SocialPostEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val userPhotoUrl: String?,
    val content: String,
    val postTime: String,
    val userId: String
)
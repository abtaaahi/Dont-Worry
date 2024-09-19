package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.abtahiapp.dontworry.utils.HomeItemType

@Entity(tableName = "home_items")
data class HomeItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val imageUrl: String,
    val type: HomeItemType
)

@Entity(tableName = "home_posts")
data class HomePostEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val userPhotoUrl: String,
    val content: String,
    val postTime: String
)
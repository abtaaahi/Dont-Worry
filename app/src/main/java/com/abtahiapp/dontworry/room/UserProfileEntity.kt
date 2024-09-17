package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_table")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val photoUrl: String = "",
    val music: String = "",
    val bookMovie: String = "",
    val likes: String = "",
    val dislikes: String = "",
    val trust: String = "",
    val water: String = "",
    val choices: String = "",
    val sleep: String = ""
)
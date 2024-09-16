package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_table")
data class MoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moodImage: Int,
    val dateTime: String,
    val details: String
){
    constructor(moodImage: Int, dateTime: String, details: String) : this(0, moodImage, dateTime, details)
}

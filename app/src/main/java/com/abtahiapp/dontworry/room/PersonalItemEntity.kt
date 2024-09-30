package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_items")
data class PersonalItemEntity(
    val text: String,
    val timestamp: String,
    val voiceUrl: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)
package com.abtahiapp.dontworry.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quote_table")
data class QuoteEntity(
    @PrimaryKey val id: Int = 0,
    val quote: String,
    val author: String
)
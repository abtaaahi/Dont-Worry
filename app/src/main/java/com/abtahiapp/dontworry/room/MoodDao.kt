package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MoodDao {
    @Insert
    suspend fun insertAll(moods: List<MoodEntity>)

    @Query("SELECT * FROM mood_table ORDER BY id DESC")
    suspend fun getAllMoods(): List<MoodEntity>

    @Query("DELETE FROM mood_table")
    suspend fun clearMoods()
}
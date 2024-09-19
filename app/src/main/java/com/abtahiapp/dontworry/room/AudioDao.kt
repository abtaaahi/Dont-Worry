package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AudioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<AudioEntity>)

    @Query("SELECT * FROM videos")
    suspend fun getAllVideos(): List<AudioEntity>

    @Query("DELETE FROM videos")
    suspend fun clearVideos()
}
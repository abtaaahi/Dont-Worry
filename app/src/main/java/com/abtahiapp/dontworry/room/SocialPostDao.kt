package com.abtahiapp.dontworry.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SocialPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<SocialPostEntity>)

    @Query("SELECT * FROM social_posts ORDER BY postTime DESC")
    fun getAllPosts(): LiveData<List<SocialPostEntity>>

    @Query("DELETE FROM social_posts")
    suspend fun deleteAllPosts()
}
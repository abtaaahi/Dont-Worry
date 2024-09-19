package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HomeItemDao {

    @Query("SELECT * FROM home_items")
    suspend fun getAllHomeItems(): List<HomeItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeItems(items: List<HomeItemEntity>)

    @Query("DELETE FROM home_items")
    suspend fun clearHomeItems()
}

@Dao
interface HomePostDao {

    @Query("SELECT * FROM home_posts ORDER BY postTime DESC")
    suspend fun getAllPosts(): List<HomePostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<HomePostEntity>)

    @Query("DELETE FROM home_posts")
    suspend fun clearPosts()
}

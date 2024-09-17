package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY postTime DESC")
    suspend fun getUserPosts(userId: String): List<PostEntity>

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)

    @Query("DELETE FROM posts WHERE userId = :userId")
    suspend fun clearUserPosts(userId: String)
}
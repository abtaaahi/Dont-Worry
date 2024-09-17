package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Query("SELECT * FROM user_profile_table WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Query("DELETE FROM user_profile_table")
    suspend fun clearUserProfile()
}

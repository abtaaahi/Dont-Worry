package com.abtahiapp.dontworry.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PersonalItemDao {

    @Insert
    suspend fun insert(personalItem: PersonalItemEntity)

    @Insert
    suspend fun insertAll(personalItems: List<PersonalItemEntity>)

    @Query("SELECT * FROM personal_items ORDER BY timestamp DESC")
    fun getAllItems(): List<PersonalItemEntity>

    @Insert
    suspend fun insertItem(item: PersonalItemEntity)

    @Delete
    suspend fun delete(personalItem: PersonalItemEntity)
}
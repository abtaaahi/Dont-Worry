package com.abtahiapp.dontworry.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SocialPostEntity::class], version = 1)
abstract class SocialPostDatabase : RoomDatabase() {

    abstract fun socialPostDao(): SocialPostDao

    companion object {
        @Volatile
        private var INSTANCE: SocialPostDatabase? = null

        fun getDatabase(context: Context): SocialPostDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SocialPostDatabase::class.java,
                    "social_post_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
package com.abtahiapp.dontworry.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [HomeItemEntity::class, HomePostEntity::class], version = 1)
abstract class HomeDatabase : RoomDatabase() {
    abstract fun homeItemDao(): HomeItemDao
    abstract fun homePostDao(): HomePostDao

    companion object {
        @Volatile private var INSTANCE: HomeDatabase? = null

        fun getDatabase(context: Context): HomeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HomeDatabase::class.java,
                    "home_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
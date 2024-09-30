package com.abtahiapp.dontworry.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PersonalItemEntity::class], version = 1)
abstract class PersonalSpaceDatabase : RoomDatabase() {
    abstract fun personalItemDao(): PersonalItemDao
}

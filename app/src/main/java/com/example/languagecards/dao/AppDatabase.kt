package com.example.languagecards.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WordCardEntity::class, WordCardFtsEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordCardDao(): WordCardDao
}
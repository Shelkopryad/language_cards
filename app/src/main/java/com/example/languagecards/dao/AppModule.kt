package com.example.languagecards.dao

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    private val MIGRATION_1_2 = object : Migration(1, 2) {
//        override fun migrate(db: SupportSQLiteDatabase) {
//            db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `word_cards_fts` USING FTS4(`french_word` TEXT, `russian_translation` TEXT, content=`word_cards`)")
//        }
//    }
//
//    private val MIGRATION_2_3 = object : Migration(2, 3) {
//        override fun migrate(db: SupportSQLiteDatabase) {
//            db.execSQL("DROP TABLE IF EXISTS `word_cards_fts`")
//
//            db.execSQL("""
//            CREATE VIRTUAL TABLE `word_cards_fts` USING FTS4(
//                `french_word` TEXT,
//                `russian_translation` TEXT,
//                content=`word_cards`,
//                tokenize=unicode61
//            )
//        """)
//        }
//    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context):
            AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
//            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun wordCardDao(appDatabase: AppDatabase): WordCardDao {
        return appDatabase.wordCardDao()
    }
}
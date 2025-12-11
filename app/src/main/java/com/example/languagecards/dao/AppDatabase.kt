package com.example.languagecards.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаём новую таблицу с обновлённой схемой
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS word_cards_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                article TEXT NOT NULL,
                foreign_word TEXT NOT NULL,
                russian_translation TEXT NOT NULL,
                gender INTEGER,
                language INTEGER NOT NULL DEFAULT 1
            )
        """)
        
        // Копируем данные из старой таблицы (french_word -> foreign_word, language = 1 для French)
        database.execSQL("""
            INSERT INTO word_cards_new (id, article, foreign_word, russian_translation, gender, language)
            SELECT id, article, french_word, russian_translation, gender, 1 FROM word_cards
        """)
        
        // Удаляем старую таблицу
        database.execSQL("DROP TABLE word_cards")
        
        // Переименовываем новую таблицу
        database.execSQL("ALTER TABLE word_cards_new RENAME TO word_cards")
        
        // Создаём индексы
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_word_cards_foreign_word_language ON word_cards (foreign_word, language)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_word_cards_russian_translation ON word_cards (russian_translation)")
        
        // Удаляем старую FTS таблицу
        database.execSQL("DROP TABLE IF EXISTS word_cards_fts")
        
        // Пересоздаём FTS таблицу с порядком колонок как ожидает Room
        // Room ожидает: columns=[russian_translation, foreign_word], options=[tokenize=unicode61, content=`word_cards`]
        database.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `word_cards_fts` 
            USING FTS4(`russian_translation`, `foreign_word`, content=`word_cards`, tokenize=unicode61)
        """)
        
        // Заполняем FTS таблицу
        database.execSQL("INSERT INTO word_cards_fts(word_cards_fts) VALUES('rebuild')")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Создаём таблицу для переводов
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS translations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                word_card_id INTEGER NOT NULL,
                translation TEXT NOT NULL,
                FOREIGN KEY(word_card_id) REFERENCES word_cards(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_translations_word_card_id ON translations (word_card_id)")
        
        // 2. Создаём новую таблицу word_cards с обновлённой схемой
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS word_cards_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                full_word TEXT NOT NULL,
                gender INTEGER,
                language INTEGER NOT NULL DEFAULT 1
            )
        """)
        
        // 3. Переносим данные: объединяем article и foreign_word в full_word
        database.execSQL("""
            INSERT INTO word_cards_new (id, full_word, gender, language)
            SELECT 
                id,
                CASE 
                    WHEN article != '' THEN article || ' ' || foreign_word
                    ELSE foreign_word
                END as full_word,
                gender,
                language
            FROM word_cards
        """)
        
        // 4. Переносим переводы в новую таблицу
        database.execSQL("""
            INSERT INTO translations (word_card_id, translation)
            SELECT id, russian_translation FROM word_cards
        """)
        
        // 5. Удаляем старую таблицу
        database.execSQL("DROP TABLE word_cards")
        
        // 6. Переименовываем новую таблицу
        database.execSQL("ALTER TABLE word_cards_new RENAME TO word_cards")
        
        // 7. Создаём индекс
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_word_cards_full_word_language ON word_cards (full_word, language)")
        
        // 8. Удаляем старую FTS таблицу
        database.execSQL("DROP TABLE IF EXISTS word_cards_fts")
        
        // 9. Создаём новую FTS таблицу для поиска по full_word
        database.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS word_cards_fts 
            USING FTS4(full_word, content=word_cards, tokenize=unicode61)
        """)
        
        // 10. Заполняем FTS таблицу
        database.execSQL("INSERT INTO word_cards_fts(word_cards_fts) VALUES('rebuild')")
    }
}

@Database(
    entities = [WordCardEntity::class, WordCardFtsEntity::class, TranslationEntity::class], 
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordCardDao(): WordCardDao
}
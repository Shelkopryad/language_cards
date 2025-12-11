package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

// FTS таблица для поиска по словам
// Порядок полей важен для Room миграции!
@Entity(tableName = "word_cards_fts")
@Fts4(contentEntity = WordCardEntity::class, tokenizer = "unicode61")
data class WordCardFtsEntity(
    @ColumnInfo(name = "full_word")
    val fullWord: String
)


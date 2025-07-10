package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

// FTS таблица для поиска по словам
@Entity(tableName = "word_cards_fts")
@Fts4(contentEntity = WordCardEntity::class, tokenizer = "unicode61") // Связываем с основной Entity
data class WordCardFtsEntity(
    // Важно: имена колонок в FTS-таблице должны совпадать
    // с именами колонок в contentEntity (WordCardEntity), по которым будет поиск.
    @ColumnInfo(name = "french_word")
    val frenchWord: String,

    @ColumnInfo(name = "russian_translation")
    val russianTranslation: String
)

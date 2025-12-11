package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "word_cards_fts")
@Fts4(contentEntity = WordCardEntity::class, tokenizer = "unicode61")
data class WordCardFtsEntity(
    @ColumnInfo(name = "full_word")
    val fullWord: String
)


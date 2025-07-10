package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object GenderType {
    const val FEMININE = 1
    const val MASCULINE = 2
}

@Entity(
    tableName = "word_cards",
    indices = [
        androidx.room.Index(value = ["french_word"], unique = true),
        androidx.room.Index(value = ["russian_translation"])
    ]
)
data class WordCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "article") val article: String,
    @ColumnInfo(name = "french_word") val frenchWord: String,
    @ColumnInfo(name = "russian_translation") val russianTranslation: String,
    @ColumnInfo(name = "gender") val gender: Int,
)


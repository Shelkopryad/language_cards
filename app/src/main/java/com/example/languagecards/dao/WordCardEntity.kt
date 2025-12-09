package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object GenderType {
    const val FEMININE = 1
    const val MASCULINE = 2
    const val NEUTER = 3  // Средний род для румынского
}

object LanguageType {
    const val FRENCH = 1
    const val ROMANIAN = 2
}

@Entity(
    tableName = "word_cards",
    indices = [
        androidx.room.Index(value = ["foreign_word", "language"], unique = true),
        androidx.room.Index(value = ["russian_translation"])
    ]
)
data class WordCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "article") val article: String,
    @ColumnInfo(name = "foreign_word") val foreignWord: String,
    @ColumnInfo(name = "russian_translation") val russianTranslation: String,
    @ColumnInfo(name = "gender") val gender: Int?, // null для не-существительных
    @ColumnInfo(name = "language") val language: Int = LanguageType.FRENCH,
)

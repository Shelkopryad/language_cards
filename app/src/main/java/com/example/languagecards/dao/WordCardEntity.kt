package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object GenderType {
    const val FEMININE = 1
    const val MASCULINE = 2
    const val NEUTER = 3
}

object LanguageType {
    const val FRENCH = 1
    const val ROMANIAN = 2
}

@Entity(
    tableName = "word_cards",
    indices = [
        androidx.room.Index(value = ["full_word", "language"], unique = true)
    ]
)
data class WordCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "full_word") val fullWord: String,
    @ColumnInfo(name = "gender") val gender: Int?,
    @ColumnInfo(name = "language") val language: Int = LanguageType.FRENCH,
)

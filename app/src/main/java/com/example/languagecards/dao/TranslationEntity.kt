package com.example.languagecards.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translations",
    foreignKeys = [
        ForeignKey(
            entity = WordCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["word_card_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["word_card_id"])]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "word_card_id") val wordCardId: Int,
    @ColumnInfo(name = "translation") val translation: String
)

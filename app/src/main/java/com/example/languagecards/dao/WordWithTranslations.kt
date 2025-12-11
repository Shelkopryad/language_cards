package com.example.languagecards.dao

import androidx.room.Embedded
import androidx.room.Relation

data class WordWithTranslations(
    @Embedded val word: WordCardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "word_card_id"
    )
    val translations: List<TranslationEntity>
)

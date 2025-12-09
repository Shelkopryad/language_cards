package com.example.languagecards.ui

import androidx.compose.ui.graphics.Color
import com.example.languagecards.dao.WordCardEntity

data class WordOption(
    val wordCard: WordCardEntity,
    val displayColor: Color
)

// Алиас для обратной совместимости
typealias FrenchOption = WordOption

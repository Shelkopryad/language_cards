package com.example.languagecards.ui

import androidx.compose.ui.graphics.Color
import com.example.languagecards.dao.WordCardEntity

data class FrenchOption(
    val wordCard: WordCardEntity, // Содержит французское слово, род и id
    val displayColor: Color
)

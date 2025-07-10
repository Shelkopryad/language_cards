package com.example.languagecards.ui

data class QuizQuestion(
    val russianWord: String,
    val frenchOptions: List<FrenchOption>,
    val correctAnswerId: Int // id правильного WordCardEntity
)

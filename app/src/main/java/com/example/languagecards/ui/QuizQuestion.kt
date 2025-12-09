package com.example.languagecards.ui

data class QuizQuestion(
    val russianWord: String,
    val wordOptions: List<WordOption>,
    val correctAnswerId: Int
) {
    // Для обратной совместимости
    val frenchOptions: List<FrenchOption> get() = wordOptions
}

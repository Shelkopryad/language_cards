package com.example.languagecards.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.WordCardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslationQuizViewModel @Inject constructor(
    private val wordCardDao: WordCardDao
) : ViewModel() {

    private val _currentQuestion = MutableStateFlow<QuizQuestion?>(null)
    val currentQuestion: StateFlow<QuizQuestion?> = _currentQuestion.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val feminineColor = Color(0xFFF8BBD0)
    private val masculineColor = Color(0xFFB3E5FC)
    private val defaultColor = Color.Transparent

    init {
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _isLoading.value = true
            _userMessage.value = null
            val allWords = wordCardDao.getAllWords().firstOrNull() // Получаем один раз список

            if (allWords.isNullOrEmpty() || allWords.size < 4) { // Нужно хотя бы 4 слова для 3 неправильных вариантов + 1 правильный
                _userMessage.value = "Недостаточно слов в базе данных для начала квиза."
                _currentQuestion.value = null
                _isLoading.value = false
                return@launch
            }

            val randomWord = allWords.random()
            val correctAnswerId = randomWord.id

            // Формируем неправильные варианты
            val incorrectOptions = allWords
                .filter { it.id != correctAnswerId } // Исключаем правильный ответ
                .shuffled() // Перемешиваем
                .take(3)    // Берем 3 неправильных варианта

            if (incorrectOptions.size < 3) {
                _userMessage.value = "Недостаточно слов для формирования вариантов ответа."
                // Можно показать только правильный ответ или меньше вариантов
                // Для простоты пока просто выведем сообщение
                _currentQuestion.value = null
                _isLoading.value = false
                return@launch
            }

            val allOptionsEntities = (incorrectOptions + randomWord).shuffled()

            val frenchOptions = allOptionsEntities.map { wordEntity ->
                val color = when (wordEntity.gender) {
                    GenderType.FEMININE -> feminineColor
                    GenderType.MASCULINE -> masculineColor
                    else -> defaultColor
                }
                FrenchOption(wordCard = wordEntity, displayColor = color)
            }

            _currentQuestion.value = QuizQuestion(
                russianWord = randomWord.russianTranslation,
                frenchOptions = frenchOptions,
                correctAnswerId = correctAnswerId
            )
            _isLoading.value = false
        }
    }

    fun checkAnswer(selectedOptionId: Int): Boolean {
        val question = _currentQuestion.value ?: return false
        val isCorrect = selectedOptionId == question.correctAnswerId
        _userMessage.value =
            if (isCorrect) "Правильно!" else "Неправильно. Попробуй еще раз или перейди к следующему."
        return isCorrect
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
package com.example.languagecards.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.WordCardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslationQuizViewModel @Inject constructor(
    private val wordCardDao: WordCardDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentQuestion = MutableStateFlow<QuizQuestion?>(null)
    val currentQuestion: StateFlow<QuizQuestion?> = _currentQuestion.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageType.FRENCH)

    private val feminineColor = Color(0xFFF8BBD0)
    private val masculineColor = Color(0xFFB3E5FC)
    private val neuterColor = Color(0xFFC8E6C9)
    private val defaultColor = Color(0xFFE0E0E0)

    init {
        viewModelScope.launch {
            // Ждём первое значение из DataStore перед загрузкой
            settingsRepository.selectedLanguage.first()
            loadNextQuestion()
        }
    }

    fun loadNextQuestion() {
        viewModelScope.launch {
            _isLoading.value = true
            _userMessage.value = null
            
            // Получаем актуальное значение языка из DataStore
            val language = settingsRepository.selectedLanguage.first()
            val allWords = wordCardDao.getRandomWords(limit = 10, language = language)

            if (allWords.size < 4) {
                _userMessage.value = "Недостаточно слов в базе данных для начала квиза."
                _currentQuestion.value = null
                _isLoading.value = false
                return@launch
            }

            val randomWord = allWords.random()
            val correctAnswerId = randomWord.id

            val incorrectOptions = allWords
                .filter { it.id != correctAnswerId }
                .shuffled()
                .take(3)

            if (incorrectOptions.size < 3) {
                _userMessage.value = "Недостаточно слов для формирования вариантов ответа."
                _currentQuestion.value = null
                _isLoading.value = false
                return@launch
            }

            val allOptionsEntities = (incorrectOptions + randomWord).shuffled()

            val wordOptions = allOptionsEntities.map { wordEntity ->
                val color = when (wordEntity.gender) {
                    GenderType.FEMININE -> feminineColor
                    GenderType.MASCULINE -> masculineColor
                    GenderType.NEUTER -> neuterColor
                    else -> defaultColor
                }
                WordOption(wordCard = wordEntity, displayColor = color)
            }

            _currentQuestion.value = QuizQuestion(
                russianWord = randomWord.russianTranslation,
                wordOptions = wordOptions,
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
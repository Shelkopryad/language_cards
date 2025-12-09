package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordCardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleQuizUiState(
    val currentWord: WordCardEntity? = null,
    val userAnswerArticle: String = "",
    val userAnswerTranslation: String = "",
    val isLoading: Boolean = true,
    val showResult: Boolean = false,
    val isCorrectArticle: Boolean? = null,
    val isCorrectTranslation: Boolean? = null,
    val feedbackMessage: String? = null,
    val hasArticle: Boolean = false
)

@HiltViewModel
class ArticleAndTranslationQuizViewModel @Inject constructor(
    private val wordCardDao: WordCardDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleQuizUiState())
    val uiState: StateFlow<ArticleQuizUiState> = _uiState.asStateFlow()

    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageType.FRENCH)

    init {
        viewModelScope.launch {
            // Ждём первое значение из DataStore перед загрузкой
            settingsRepository.selectedLanguage.first()
            loadNextWord()
        }
    }

    fun loadNextWord() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    showResult = false,
                    isCorrectArticle = null,
                    isCorrectTranslation = null,
                    userAnswerArticle = "",
                    userAnswerTranslation = "",
                    feedbackMessage = null
                )
            }
            
            // Получаем актуальное значение языка из DataStore
            val language = settingsRepository.selectedLanguage.first()
            val allWords = wordCardDao.getAllWords(language).firstOrNull()

            if (allWords.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentWord = null,
                        feedbackMessage = "Нет слов для квиза. Добавьте слова в словарь."
                    )
                }
                return@launch
            }

            // Выбираем любое случайное слово
            val randomWord = allWords.randomOrNull()

            if (randomWord == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentWord = null,
                        feedbackMessage = "Нет слов для квиза."
                    )
                }
                return@launch
            }

            // Определяем, есть ли у слова артикль
            val hasArticle = randomWord.article.isNotBlank() && randomWord.gender != null

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentWord = randomWord,
                    hasArticle = hasArticle
                )
            }
        }
    }

    fun onUserArticleChange(article: String) {
        _uiState.update { it.copy(userAnswerArticle = article, showResult = false) }
    }

    fun onUserTranslationChange(translation: String) {
        _uiState.update { it.copy(userAnswerTranslation = translation, showResult = false) }
    }

    fun checkAnswer() {
        val currentWord = _uiState.value.currentWord ?: return
        val hasArticle = _uiState.value.hasArticle
        val userAnswerTranslation = _uiState.value.userAnswerTranslation.trim()

        val correctTranslation = currentWord.russianTranslation.trim()
        val isTranslationCorrect = userAnswerTranslation.equals(correctTranslation, ignoreCase = true)

        val feedback: String
        val isArticleCorrect: Boolean?

        if (hasArticle) {
            // Слово с артиклем - проверяем и артикль, и перевод
            val userAnswerArticle = _uiState.value.userAnswerArticle.trim()
            val correctArticle = currentWord.article.trim()
            isArticleCorrect = userAnswerArticle.equals(correctArticle, ignoreCase = true)

            feedback = when {
                isArticleCorrect && isTranslationCorrect -> "Отлично! Всё верно!"
                isArticleCorrect -> "Артикль верный, но перевод нет. Правильный перевод: '$correctTranslation'"
                isTranslationCorrect -> "Перевод верный, но артикль нет. Правильный артикль: '$correctArticle'"
                else -> "Неверно. Правильный артикль: '$correctArticle', правильный перевод: '$correctTranslation'"
            }
        } else {
            // Слово без артикля - проверяем только перевод
            isArticleCorrect = null
            feedback = if (isTranslationCorrect) {
                "Отлично! Всё верно!"
            } else {
                "Неверно. Правильный перевод: '$correctTranslation'"
            }
        }

        _uiState.update {
            it.copy(
                showResult = true,
                isCorrectArticle = isArticleCorrect,
                isCorrectTranslation = isTranslationCorrect,
                feedbackMessage = feedback
            )
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
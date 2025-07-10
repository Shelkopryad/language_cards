package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordCardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleQuizUiState(
    val currentWord: WordCardEntity? = null,
    val userAnswerArticle: String = "",
    val userAnswerTranslation: String = "",
    val isLoading: Boolean = true,
    val showResult: Boolean = false, // Показать результат после проверки
    val isCorrectArticle: Boolean? = null, // null если еще не проверяли
    val isCorrectTranslation: Boolean? = null, // null если еще не проверяли
    val feedbackMessage: String? = null // Общее сообщение или подсказка
)

@HiltViewModel
class ArticleAndTranslationQuizViewModel @Inject constructor(
    private val wordCardDao: WordCardDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleQuizUiState())
    val uiState: StateFlow<ArticleQuizUiState> = _uiState.asStateFlow()

    init {
        loadNextWord()
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
            val allWords = wordCardDao.getAllWords().firstOrNull() // Получаем актуальный список

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

            // Выбираем случайное слово, у которого есть артикль
            // Это важно, так как квиз на артикли
            val wordWithOptions = allWords.filter { !it.article.isNullOrBlank() }.randomOrNull()

            if (wordWithOptions == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentWord = null,
                        feedbackMessage = "Нет слов с артиклями для этого квиза."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentWord = wordWithOptions
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
        val userAnswerArticle = _uiState.value.userAnswerArticle.trim()
        val userAnswerTranslation = _uiState.value.userAnswerTranslation.trim()

        // Сравнение артиклей без учета регистра
        val correctArticle = currentWord.article?.trim() ?: ""
        val isArticleCorrect = userAnswerArticle.equals(correctArticle, ignoreCase = true)

        // Сравнение переводов без учета регистра
        val correctTranslation = currentWord.russianTranslation.trim()
        val isTranslationCorrect =
            userAnswerTranslation.equals(correctTranslation, ignoreCase = true)

        val feedback: String
        if (isArticleCorrect && isTranslationCorrect) {
            feedback = "Отлично! Всё верно!"
        } else if (isArticleCorrect) {
            feedback = "Артикль верный, но перевод нет. Правильный перевод: '${correctTranslation}'"
        } else if (isTranslationCorrect) {
            feedback = "Перевод верный, но артикль нет. Правильный артикль: '${correctArticle}'"
        } else {
            feedback =
                "Неверно. Правильный артикль: '${correctArticle}', правильный перевод: '${correctTranslation}'"
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
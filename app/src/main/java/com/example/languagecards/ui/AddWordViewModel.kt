package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.WordCardEntity
import com.example.languagecards.dao.WordCardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWordUiState(
    val foreignWordInput: String = "",
    val derivedForeignWord: String = "",
    val russianTranslation: String = "",
    val article: String = "",
    val selectedGender: Int? = null,
    val isNoun: Boolean = true,
    val isSaving: Boolean = false,
    val userMessage: String? = null,
    val saveSuccess: Boolean = false,
    val editingWordId: Int? = null
)

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val wordCardDao: WordCardDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWordUiState())
    val uiState = _uiState.asStateFlow()

    // Глобальный выбор языка из настроек
    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageType.FRENCH)

    // Французские артикли
    private val frenchArticles = mapOf(
        "le" to GenderType.MASCULINE,
        "la" to GenderType.FEMININE,
        "l'" to null,
        "les" to null
    )

    // Румынские артикли
    private val romanianArticles = mapOf(
        "un" to GenderType.MASCULINE,
        "o" to GenderType.FEMININE,
    )

    private fun getCurrentArticlesMap(): Map<String, Int?> {
        return when (selectedLanguage.value) {
            LanguageType.ROMANIAN -> romanianArticles
            else -> frenchArticles
        }
    }

    fun onForeignWordInputChange(input: String) {
        val trimmedInput = input.trimStart()
        _uiState.update {
            it.copy(
                foreignWordInput = trimmedInput,
                saveSuccess = false,
                userMessage = null
            )
        }
        extractArticleAndWord(trimmedInput)
    }

    private fun extractArticleAndWord(input: String) {
        var currentArticle = _uiState.value.article
        var currentWord = input
        var preselectedGender: Int? = _uiState.value.selectedGender
        val articlesMap = getCurrentArticlesMap()

        var foundArticle = false
        for ((art, genderMarker) in articlesMap) {
            if (input.startsWith("$art ", ignoreCase = true) ||
                (art == "l'" && input.startsWith(art, ignoreCase = true) 
                    && input.length > art.length && input[art.length].isLetter())
            ) {
                currentArticle = input.substring(0, art.length)
                currentWord = input.substring(art.length).trimStart()
                if (genderMarker != null) {
                    preselectedGender = genderMarker
                }
                foundArticle = true
                break
            }
        }

        _uiState.update {
            it.copy(
                derivedForeignWord = currentWord,
                article = if (foundArticle) currentArticle else it.article,
                selectedGender = if (_uiState.value.isNoun) preselectedGender else null
            )
        }
    }

    fun onRussianTranslationChange(translation: String) {
        _uiState.update {
            it.copy(
                russianTranslation = translation.trimStart(),
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onArticleChange(articleInput: String) {
        val trimmedArticle = articleInput.trim()
        var newSelectedGender = _uiState.value.selectedGender
        val articlesMap = getCurrentArticlesMap()

        val articleKey = trimmedArticle.lowercase()
        if (articlesMap.containsKey(articleKey)) {
            articlesMap[articleKey]?.let { gender ->
                newSelectedGender = gender
            }
            if (articlesMap[articleKey] == null && _uiState.value.selectedGender != null) {
                // не меняем
            } else {
                newSelectedGender = articlesMap[articleKey] ?: _uiState.value.selectedGender
            }
        }

        _uiState.update {
            it.copy(
                article = trimmedArticle,
                selectedGender = if (_uiState.value.isNoun) newSelectedGender else null,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onIsNounChanged(isNoun: Boolean) {
        _uiState.update {
            it.copy(
                isNoun = isNoun,
                selectedGender = if (isNoun) it.selectedGender else null,
                article = if (isNoun) it.article else "",
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onGenderSelected(gender: Int) {
        if (!_uiState.value.isNoun) return

        var updatedArticle = _uiState.value.article
        val language = selectedLanguage.value

        if (updatedArticle.isBlank()) {
            updatedArticle = getDefaultArticleForGender(gender, language)
        } else if (shouldUpdateArticleForGender(updatedArticle, gender, language)) {
            updatedArticle = getDefaultArticleForGender(gender, language)
        }

        _uiState.update {
            it.copy(
                selectedGender = gender,
                article = updatedArticle,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    private fun getDefaultArticleForGender(gender: Int, language: Int): String {
        return when (language) {
            LanguageType.FRENCH -> {
                val wordStartsWithVowel = _uiState.value.derivedForeignWord.lowercase().firstOrNull()
                    ?.let { it in "aeiouyàâéèêëîïôùûü" } == true
                when {
                    wordStartsWithVowel -> "l'"
                    gender == GenderType.MASCULINE -> "le"
                    gender == GenderType.FEMININE -> "la"
                    else -> ""
                }
            }
            LanguageType.ROMANIAN -> when (gender) {
                GenderType.MASCULINE -> "un"
                GenderType.FEMININE -> "o"
                GenderType.NEUTER -> "un"
                else -> ""
            }
            else -> ""
        }
    }

    private fun shouldUpdateArticleForGender(article: String, gender: Int, language: Int): Boolean {
        return when (language) {
            LanguageType.FRENCH -> {
                (article.lowercase() == "le" && gender == GenderType.FEMININE) ||
                (article.lowercase() == "la" && gender == GenderType.MASCULINE)
            }
            LanguageType.ROMANIAN -> {
                (article.lowercase() == "un" && gender == GenderType.FEMININE) ||
                (article.lowercase() == "o" && gender == GenderType.MASCULINE)
            }
            else -> false
        }
    }

    fun loadWordForEditing(wordId: Int) {
        viewModelScope.launch {
            val word = wordCardDao.getWordById(wordId)
            if (word != null) {
                val isNoun = word.gender != null
                val foreignWordInput = if (word.article.isNotBlank()) {
                    "${word.article} ${word.foreignWord}"
                } else {
                    word.foreignWord
                }
                
                _uiState.update {
                    it.copy(
                        editingWordId = wordId,
                        foreignWordInput = foreignWordInput,
                        derivedForeignWord = word.foreignWord,
                        russianTranslation = word.russianTranslation,
                        article = word.article,
                        selectedGender = word.gender,
                        isNoun = isNoun,
                        saveSuccess = false,
                        userMessage = null
                    )
                }
            }
        }
    }

    fun saveWord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, userMessage = null, saveSuccess = false) }

            val currentState = _uiState.value
            val foreignWordToSave = currentState.derivedForeignWord.trim()
            val russianTranslationToSave = currentState.russianTranslation.trim()
            val articleToSave = currentState.article.trim()

            if (foreignWordToSave.isBlank()) {
                _uiState.update {
                    it.copy(isSaving = false, userMessage = "Слово не может быть пустым.")
                }
                return@launch
            }
            if (russianTranslationToSave.isBlank()) {
                _uiState.update {
                    it.copy(isSaving = false, userMessage = "Русский перевод не может быть пустым.")
                }
                return@launch
            }
            if (currentState.isNoun && currentState.selectedGender == null) {
                _uiState.update {
                    it.copy(isSaving = false, userMessage = "Необходимо выбрать род слова.")
                }
                return@launch
            }

            try {
                if (currentState.editingWordId != null) {
                    // Update existing word
                    val wordCard = WordCardEntity(
                        id = currentState.editingWordId,
                        foreignWord = foreignWordToSave.lowercase(),
                        russianTranslation = russianTranslationToSave.lowercase(),
                        article = articleToSave.lowercase(),
                        gender = if (currentState.isNoun) currentState.selectedGender else null,
                        language = selectedLanguage.value
                    )
                    wordCardDao.updateWord(wordCard)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = "Слово '${wordCard.foreignWord}' успешно обновлено!",
                            saveSuccess = true
                        )
                    }
                } else {
                    // Insert new word
                    val wordCard = WordCardEntity(
                        foreignWord = foreignWordToSave.lowercase(),
                        russianTranslation = russianTranslationToSave.lowercase(),
                        article = articleToSave.lowercase(),
                        gender = if (currentState.isNoun) currentState.selectedGender else null,
                        language = selectedLanguage.value
                    )
                    wordCardDao.insertWord(wordCard)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = "Слово '${wordCard.foreignWord}' успешно сохранено!",
                            saveSuccess = true,
                            foreignWordInput = "",
                            derivedForeignWord = "",
                            russianTranslation = "",
                            article = "",
                            selectedGender = null,
                            isNoun = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Ошибка сохранения: ${e.message ?: "Неизвестная ошибка"}"
                    )
                }
            }
        }
    }
}
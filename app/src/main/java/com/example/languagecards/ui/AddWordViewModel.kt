package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.TranslationEntity
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
    val fullWord: String = "",
    val translations: List<String> = listOf(""),
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

    fun onFullWordChange(word: String) {
        _uiState.update {
            it.copy(
                fullWord = word.trimStart(),
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onTranslationChange(index: Int, translation: String) {
        val updatedTranslations = _uiState.value.translations.toMutableList()
        if (index < updatedTranslations.size) {
            updatedTranslations[index] = translation.trimStart()
            _uiState.update {
                it.copy(
                    translations = updatedTranslations,
                    saveSuccess = false,
                    userMessage = null
                )
            }
        }
    }

    fun addTranslation() {
        _uiState.update {
            it.copy(
                translations = it.translations + "",
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun removeTranslation(index: Int) {
        if (_uiState.value.translations.size > 1) {
            val updatedTranslations = _uiState.value.translations.toMutableList()
            updatedTranslations.removeAt(index)
            _uiState.update {
                it.copy(
                    translations = updatedTranslations,
                    saveSuccess = false,
                    userMessage = null
                )
            }
        }
    }

    fun onIsNounChanged(isNoun: Boolean) {
        _uiState.update {
            it.copy(
                isNoun = isNoun,
                selectedGender = if (isNoun) it.selectedGender else null,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onGenderSelected(gender: Int) {
        if (!_uiState.value.isNoun) return

        _uiState.update {
            it.copy(
                selectedGender = gender,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun loadWordForEditing(wordId: Int) {
        viewModelScope.launch {
            val wordWithTranslations = wordCardDao.getWordWithTranslationsById(wordId)
            if (wordWithTranslations != null) {
                val word = wordWithTranslations.word
                val translations = wordWithTranslations.translations.map { it.translation }
                val isNoun = word.gender != null
                
                _uiState.update {
                    it.copy(
                        editingWordId = wordId,
                        fullWord = word.fullWord,
                        translations = if (translations.isNotEmpty()) translations else listOf(""),
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
            val fullWordToSave = currentState.fullWord.trim()
            val translationsToSave = currentState.translations
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (fullWordToSave.isBlank()) {
                _uiState.update {
                    it.copy(isSaving = false, userMessage = "Слово не может быть пустым.")
                }
                return@launch
            }
            if (translationsToSave.isEmpty()) {
                _uiState.update {
                    it.copy(isSaving = false, userMessage = "Необходимо указать хотя бы один перевод.")
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
                        fullWord = fullWordToSave.lowercase(),
                        gender = if (currentState.isNoun) currentState.selectedGender else null,
                        language = selectedLanguage.value
                    )
                    wordCardDao.updateWord(wordCard)
                    
                    // Delete old translations and insert new ones
                    wordCardDao.deleteTranslationsForWord(currentState.editingWordId)
                    translationsToSave.forEach { translation ->
                        wordCardDao.insertTranslation(
                            TranslationEntity(
                                wordCardId = currentState.editingWordId,
                                translation = translation.lowercase()
                            )
                        )
                    }
                    
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = "Слово '$fullWordToSave' успешно обновлено!",
                            saveSuccess = true
                        )
                    }
                } else {
                    // Insert new word
                    val wordCard = WordCardEntity(
                        fullWord = fullWordToSave.lowercase(),
                        gender = if (currentState.isNoun) currentState.selectedGender else null,
                        language = selectedLanguage.value
                    )
                    val wordId = wordCardDao.insertWord(wordCard)
                    
                    // Insert translations
                    translationsToSave.forEach { translation ->
                        wordCardDao.insertTranslation(
                            TranslationEntity(
                                wordCardId = wordId.toInt(),
                                translation = translation.lowercase()
                            )
                        )
                    }
                    
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            userMessage = "Слово '$fullWordToSave' успешно сохранено!",
                            saveSuccess = true,
                            fullWord = "",
                            translations = listOf(""),
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
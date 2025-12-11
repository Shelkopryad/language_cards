package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordWithTranslations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val words: List<WordWithTranslations> = emptyList(),
    val isLoading: Boolean = false,
    val selectedWordForMenu: WordWithTranslations? = null,
    val wordToDelete: WordWithTranslations? = null,
    val showDeleteConfirmDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordCardDao: WordCardDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Задержка 300мс перед поиском
                .flatMapLatest { query ->
                    val language = selectedLanguage.value
                    if (query.isBlank()) {
                        wordCardDao.getAllWordsWithTranslations(language)
                    } else {
                        wordCardDao.searchWordsWithTranslations(query.trim(), language)
                    }
                }.collect { words ->
                    _uiState.update { it.copy(words = words, isLoading = false) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onWordSelectedForMenu(word: WordWithTranslations) {
        _uiState.update { it.copy(selectedWordForMenu = word) }
    }

    fun onDismissMenu() {
        _uiState.update { it.copy(selectedWordForMenu = null) }
    }

    fun onWordSelectedForDelete(word: WordWithTranslations) {
        _uiState.update {
            it.copy(
                wordToDelete = word,
                showDeleteConfirmDialog = true
            )
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update {
            it.copy(
                wordToDelete = null,
                showDeleteConfirmDialog = false
            )
        }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.wordToDelete?.let { wordWithTranslations ->
                wordCardDao.deleteWord(wordWithTranslations.word)
                _uiState.update {
                    it.copy(
                        wordToDelete = null,
                        showDeleteConfirmDialog = false
                    )
                }
            }
        }
    }
}
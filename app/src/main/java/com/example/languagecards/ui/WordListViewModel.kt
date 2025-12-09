package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordCardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val words: List<WordCardEntity> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val wordToDelete: WordCardEntity? = null,
    val showDeleteConfirmDialog: Boolean = false,
    val selectedWordForMenu: WordCardEntity? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordCardDao: WordCardDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Глобальный выбор языка из настроек
    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageType.FRENCH)

    private val _uiState = MutableStateFlow(WordListUiState(isLoading = true))
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300), 
                settingsRepository.selectedLanguage
            ) { query, language ->
                Pair(query, language)
            }
                .flatMapLatest { (query, language) ->
                    val currentUiState = _uiState.value
                    if (query.isBlank()) {
                        wordCardDao.getAllWords(language).map { words ->
                            currentUiState.copy(
                                words = words,
                                isLoading = false,
                                searchQuery = query
                            )
                        }
                    } else {
                        val trimmedQuery = query.trim()
                        if (trimmedQuery.isEmpty()) {
                            wordCardDao.getAllWords(language).map { words ->
                                currentUiState.copy(
                                    words = words,
                                    isLoading = false,
                                    searchQuery = query
                                )
                            }
                        } else {
                            val ftsQuery = "$trimmedQuery*"
                            wordCardDao.searchWords(ftsQuery, language).map { words ->
                                currentUiState.copy(
                                    words = words,
                                    isLoading = false,
                                    searchQuery = query
                                )
                            }
                        }
                    }
                }
                .catch { throwable ->
                    emit(_uiState.value.copy(isLoading = false))
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onWordSelectedForDelete(wordCard: WordCardEntity) {
        _uiState.update { it.copy(wordToDelete = wordCard, showDeleteConfirmDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(wordToDelete = null, showDeleteConfirmDialog = false) }
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            _uiState.value.wordToDelete?.let { word ->
                wordCardDao.deleteWord(word)
                _uiState.update { it.copy(wordToDelete = null, showDeleteConfirmDialog = false) }
            }
        }
    }

    fun onWordSelectedForMenu(wordCard: WordCardEntity) {
        _uiState.update { it.copy(selectedWordForMenu = wordCard) }
    }

    fun onDismissMenu() {
        _uiState.update { it.copy(selectedWordForMenu = null) }
    }
}
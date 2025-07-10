package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordCardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val words: List<WordCardEntity> = emptyList(),
    val isLoading: Boolean = true, // Изначально true, пока не придут первые данные
    val searchQuery: String = "",
    val wordToDelete: WordCardEntity? = null, // Слово, выбранное для удаления
    val showDeleteConfirmDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordCardDao: WordCardDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(WordListUiState(isLoading = true))
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init {
        // Объединяем Flow для слов с Flow для поискового запроса
        // и обновляем _uiState
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .flatMapLatest { query ->
                    val currentUiState = _uiState.value // Сохраняем текущее состояние диалога
                    if (query.isBlank()) {
                        wordCardDao.getAllWords().map { words ->
                            currentUiState.copy(
                                words = words,
                                isLoading = false,
                                searchQuery = query
                            )
                        }
                    } else {
                        val trimmedQuery = query.trim()
                        if (trimmedQuery.isEmpty()) {
                            wordCardDao.getAllWords().map { words ->
                                currentUiState.copy(
                                    words = words,
                                    isLoading = false,
                                    searchQuery = query
                                )
                            }
                        } else {
                            val ftsQuery = "$trimmedQuery*"
                            wordCardDao.searchWords(ftsQuery).map { words ->
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
                    // Обработка ошибок
                    emit(_uiState.value.copy(isLoading = false /*, error = throwable.message */))
                }
                .collect { newState ->
                    _uiState.value = newState // Обновляем _uiState новым списком слов и searchQuery
                }
        }
    }


    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        // Сбрасываем состояние диалога при изменении поиска, если это необходимо
        // _uiState.update { it.copy(showDeleteConfirmDialog = false, wordToDelete = null) }
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
                // После удаления, список слов автоматически обновится благодаря Flow из DAO
                // Сбрасываем состояние диалога
                _uiState.update { it.copy(wordToDelete = null, showDeleteConfirmDialog = false) }
            }
        }
    }
}
package com.example.languagecards.ui

import android.util.Log
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.WordCardDao
import com.example.languagecards.dao.WordCardEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.text.isBlank
import kotlin.text.trim

data class WordListUiState(
    val words: List<WordCardEntity> = emptyList(),
    val isLoading: Boolean = true, // Изначально true, пока не придут первые данные
    val searchQuery: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordListViewModel @Inject constructor(
    wordCardDao: WordCardDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<WordListUiState> = _searchQuery
        .debounce(300) // Добавляем debounce, чтобы не делать запрос на каждое изменение символа
        .flatMapLatest { query -> // flatMapLatest для автоматической отмены предыдущего запроса при новом вводе
            // Показываем isLoading = true перед началом нового запроса, если это необходимо
            // Однако, stateIn с initialValue уже обрабатывает начальную загрузку.
            // Для последующих поисков можно не показывать глобальный isLoading,
            // т.к. список просто обновится.

            if (query.isBlank()) {
                wordCardDao.getAllWords().map { words ->
                    WordListUiState(words = words, isLoading = false, searchQuery = query)
                }
            } else {
                // Добавляем '*' для поиска по префиксу/вхождению.
                // SQLite FTS также поддерживает другие операторы (AND, OR, NEAR),
                // но для простого поиска по вхождению этого достаточно.
                // Убедись, что запрос не пустой после trim, чтобы избежать ошибок с FTS.
                val trimmedQuery = query.trim()
                if (trimmedQuery.isEmpty()) {
                    wordCardDao.getAllWords().map { words ->
                        WordListUiState(words = words, isLoading = false, searchQuery = query)
                    }
                } else {
                    val ftsQuery = "$trimmedQuery*"
                    Log.d("SearchDebug", "FTS Query: '$ftsQuery'")
                    wordCardDao.searchWords(ftsQuery).map { words ->
                        WordListUiState(words = words, isLoading = false, searchQuery = query)
                    }
                }
            }
        }
        .catch { throwable -> // Обработка ошибок, если запрос к базе данных не удался
            // Здесь можно залогировать ошибку или показать сообщение пользователю
            // Для примера, просто возвращаем пустое состояние с ошибкой (можно добавить поле error в UiState)
            emit(WordListUiState(isLoading = false, searchQuery = _searchQuery.value /*, error = throwable.message */))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Начать сбор, когда есть подписчики, остановить через 5с после последнего
            initialValue = WordListUiState(isLoading = true) // Начальное состояние, пока первый Flow не эмитит значение
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
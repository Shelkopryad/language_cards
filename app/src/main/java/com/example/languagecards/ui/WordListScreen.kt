package com.example.languagecards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.TranslationEntity
import com.example.languagecards.dao.WordCardEntity
import com.example.languagecards.dao.WordWithTranslations

@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
    onNavigateToAddWord: () -> Unit = {},
    onNavigateToEditWord: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    WordListContent(
        uiState = uiState,
        searchQuery = searchQuery,
        selectedLanguage = selectedLanguage,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onNavigateToAddWord = onNavigateToAddWord,
        onNavigateToEditWord = onNavigateToEditWord,
        onWordSelectedForMenu = viewModel::onWordSelectedForMenu,
        onDismissMenu = viewModel::onDismissMenu,
        onWordSelectedForDelete = viewModel::onWordSelectedForDelete,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDelete = viewModel::onConfirmDelete
    )
}

@Composable
fun WordListContent(
    uiState: WordListUiState,
    searchQuery: String,
    selectedLanguage: Int,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateToAddWord: () -> Unit,
    onNavigateToEditWord: (Int) -> Unit,
    onWordSelectedForMenu: (WordWithTranslations) -> Unit,
    onDismissMenu: () -> Unit,
    onWordSelectedForDelete: (WordWithTranslations) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    val feminineColor = Color(0xFFF8BBD0)
    val masculineColor = Color(0xFFB3E5FC)
    val neuterColor = Color(0xFFC8E6C9)
    val defaultRowColor = Color(0xFFE0E0E0)

    val languageLabel = when (selectedLanguage) {
        LanguageType.ROMANIAN -> "Română"
        else -> "Français"
    }

    if (uiState.showDeleteConfirmDialog && uiState.wordToDelete != null) {
        val translationsText = uiState.wordToDelete.translations.joinToString(", ") { it.translation }
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Удалить слово?") },
            text = {
                Text(
                    "Вы уверены, что хотите удалить слово \"${uiState.wordToDelete.word.fullWord} / $translationsText\"?"
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
            .exclude(androidx.compose.foundation.layout.WindowInsets.statusBars),
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddWord) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить слово")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Простой заголовок вместо TopAppBar
            Text(
                text = "Список слов ($languageLabel)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Поиск слов...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Очистить поиск")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                })
            )

            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isLoading && uiState.words.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.words.isEmpty() && searchQuery.isNotEmpty()) {
                    Text(
                        text = "По вашему запросу ничего не найдено.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else if (uiState.words.isEmpty() && searchQuery.isBlank()) {
                    Text(
                        text = "Слов пока нет. Нажмите '+' чтобы добавить.",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.words, key = { wordWithTranslations -> wordWithTranslations.word.id }) { wordWithTranslations ->
                            WordCardItem(
                                wordWithTranslations = wordWithTranslations,
                                feminineColor = feminineColor,
                                masculineColor = masculineColor,
                                neuterColor = neuterColor,
                                defaultColor = defaultRowColor,
                                onClick = {
                                    onWordSelectedForMenu(wordWithTranslations)
                                },
                                isMenuExpanded = uiState.selectedWordForMenu?.word?.id == wordWithTranslations.word.id,
                                onDismissMenu = onDismissMenu,
                                onEditClick = {
                                    onDismissMenu()
                                    onNavigateToEditWord(wordWithTranslations.word.id)
                                },
                                onDeleteClick = {
                                    onDismissMenu()
                                    onWordSelectedForDelete(wordWithTranslations)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordCardItem(
    wordWithTranslations: WordWithTranslations,
    feminineColor: Color,
    masculineColor: Color,
    neuterColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    isMenuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val backgroundColor = when (wordWithTranslations.word.gender) {
        GenderType.FEMININE -> feminineColor
        GenderType.MASCULINE -> masculineColor
        GenderType.NEUTER -> neuterColor
        else -> defaultColor
    }

    val translationsText = wordWithTranslations.translations.joinToString(", ") { it.translation }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = wordWithTranslations.word.fullWord,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = translationsText,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        androidx.compose.material3.DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = onDismissMenu
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Редактировать") },
                onClick = onEditClick
            )
            androidx.compose.material3.DropdownMenuItem(
                text = { Text("Удалить") },
                onClick = onDeleteClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WordListScreenPreview() {
    val sampleWords = listOf(
        WordWithTranslations(
            word = WordCardEntity(id = 1, fullWord = "le chat", gender = GenderType.MASCULINE, language = LanguageType.FRENCH),
            translations = listOf(TranslationEntity(id = 1, wordCardId = 1, translation = "кот"))
        ),
        WordWithTranslations(
            word = WordCardEntity(id = 2, fullWord = "la maison", gender = GenderType.FEMININE, language = LanguageType.FRENCH),
            translations = listOf(TranslationEntity(id = 2, wordCardId = 2, translation = "дом"), TranslationEntity(id = 3, wordCardId = 2, translation = "жилище"))
        ),
        WordWithTranslations(
            word = WordCardEntity(id = 3, fullWord = "bonjour", gender = null, language = LanguageType.FRENCH),
            translations = listOf(TranslationEntity(id = 4, wordCardId = 3, translation = "привет"), TranslationEntity(id = 5, wordCardId = 3, translation = "здравствуйте"))
        )
    )

    WordListContent(
        uiState = WordListUiState(words = sampleWords, isLoading = false),
        searchQuery = "",
        selectedLanguage = LanguageType.FRENCH,
        onSearchQueryChanged = {},
        onNavigateToAddWord = {},
        onNavigateToEditWord = {},
        onWordSelectedForMenu = {},
        onDismissMenu = {},
        onWordSelectedForDelete = {},
        onDismissDeleteDialog = {},
        onConfirmDelete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun WordListScreenEmptyPreview() {
    WordListContent(
        uiState = WordListUiState(words = emptyList(), isLoading = false),
        searchQuery = "",
        selectedLanguage = LanguageType.FRENCH,
        onSearchQueryChanged = {},
        onNavigateToAddWord = {},
        onNavigateToEditWord = {},
        onWordSelectedForMenu = {},
        onDismissMenu = {},
        onWordSelectedForDelete = {},
        onDismissDeleteDialog = {},
        onConfirmDelete = {}
    )
}
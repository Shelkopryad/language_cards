package com.example.languagecards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.WordCardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
    onNavigateToAddWord: () -> Unit = {},
    onNavigateToEditWord: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

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
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("Удалить слово?") },
            text = {
                Text(
                    "Вы уверены, что хотите удалить слово \"${uiState.wordToDelete?.foreignWord} / ${uiState.wordToDelete?.russianTranslation}\"?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDelete() }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список слов ($languageLabel)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Поиск слов...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
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
                        items(uiState.words, key = { word -> word.id }) { wordCard ->
                            WordCardItem(
                                wordCard = wordCard,
                                feminineColor = feminineColor,
                                masculineColor = masculineColor,
                                neuterColor = neuterColor,
                                defaultColor = defaultRowColor,
                                onClick = {
                                    viewModel.onWordSelectedForMenu(wordCard)
                                },
                                isMenuExpanded = uiState.selectedWordForMenu?.id == wordCard.id,
                                onDismissMenu = { viewModel.onDismissMenu() },
                                onEditClick = {
                                    viewModel.onDismissMenu()
                                    onNavigateToEditWord(wordCard.id)
                                },
                                onDeleteClick = {
                                    viewModel.onDismissMenu()
                                    viewModel.onWordSelectedForDelete(wordCard)
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
    wordCard: WordCardEntity,
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
    val backgroundColor = when (wordCard.gender) {
        GenderType.FEMININE -> feminineColor
        GenderType.MASCULINE -> masculineColor
        GenderType.NEUTER -> neuterColor
        else -> defaultColor
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val foreignDisplay = buildAnnotatedString {
                if (wordCard.article.isNotBlank()) {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                        append(wordCard.article)
                    }
                    append(" ")
                }
                withStyle(style = SpanStyle(fontSize = 18.sp)) {
                    append(wordCard.foreignWord)
                }
            }
            Text(
                text = foreignDisplay,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = wordCard.russianTranslation,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                modifier = Modifier.padding(start = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
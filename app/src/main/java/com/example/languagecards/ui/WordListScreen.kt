package com.example.languagecards.ui

import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.WordCardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel = hiltViewModel(),
    onNavigateToAddWord: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState() // Собираем searchQuery отдельно для TextField

    val focusManager = LocalFocusManager.current

    // Цвета для родов
    val feminineColor = Color(0xFFF8BBD0)
    val masculineColor = Color(0xFFB3E5FC)
    val defaultRowColor = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список слов") },
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
                    focusManager.clearFocus() // Скрыть клавиатуру при нажатии "Search"
                })
            )

            // Список слов
            Box(
                modifier = Modifier.weight(1f) // Занимает оставшееся место
            ) {
                if (uiState.isLoading && uiState.words.isEmpty()) { // Показываем загрузку только если список еще пуст
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
                                defaultColor = defaultRowColor
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
fun WordCardItem( // Эта функция остается такой же, как и раньше
    wordCard: WordCardEntity,
    feminineColor: Color,
    masculineColor: Color,
    defaultColor: Color
) {
    val backgroundColor = when (wordCard.gender) {
        GenderType.FEMININE -> feminineColor
        GenderType.MASCULINE -> masculineColor
        else -> defaultColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val frenchDisplay = buildAnnotatedString {
            if (!wordCard.article.isNullOrBlank()) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                    append(wordCard.article)
                }
                append(" ")
            }
            withStyle(style = SpanStyle(fontSize = 18.sp)) {
                append(wordCard.frenchWord)
            }
        }
        Text(
            text = frenchDisplay,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = wordCard.russianTranslation,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
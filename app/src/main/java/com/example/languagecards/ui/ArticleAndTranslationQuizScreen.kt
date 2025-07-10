package com.example.languagecards.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleAndTranslationQuizScreen(
    viewModel: ArticleAndTranslationQuizViewModel = hiltViewModel(),
    onQuizFinished: () -> Unit = {} // Если нужно действие по завершению (например, нет слов)
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val translationFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.currentWord) {
        // Когда слово меняется, сбрасываем фокус или устанавливаем на первое поле
        // Также можно очищать сообщение об обратной связи, если оно не очищается в ViewModel
        if (uiState.currentWord != null) {
            viewModel.clearFeedbackMessage() // Очищаем старое сообщение
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null && uiState.feedbackMessage.toString()
                .startsWith("Отлично!")
        ) {
            delay(1500) // Небольшая задержка перед загрузкой следующего слова
            viewModel.loadNextWord()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Квиз: Артикль и Перевод") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 50.dp))
            } else if (uiState.currentWord == null) {
                Text(
                    text = uiState.feedbackMessage ?: "Нет слов для этого квиза.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 50.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    // Возможно, перейти на другой экран или показать диалог добавления слов
                    onQuizFinished()
                }) {
                    Text("Вернуться")
                }
            } else {
                val wordInfo = uiState.currentWord!!

                Text(
                    text = "Введите артикль и перевод для слова:",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Строка для артикля и французского слова
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.userAnswerArticle,
                        onValueChange = { viewModel.onUserArticleChange(it) },
                        label = { Text("Артикль") },
                        modifier = Modifier
                            .weight(0.3f) // Меньше места для артикля
                            .padding(end = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { translationFocusRequester.requestFocus() }
                        ),
                        textStyle = TextStyle(fontSize = 18.sp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = if (uiState.showResult) (if (uiState.isCorrectArticle == true) Color.Green else Color.Red) else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (uiState.showResult) (if (uiState.isCorrectArticle == true) Color.Green else if (uiState.isCorrectArticle == false) Color.Red else MaterialTheme.colorScheme.outline) else MaterialTheme.colorScheme.outline,
                        )
                    )
                    Text(
                        text = wordInfo.frenchWord,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(0.7f)
                    )
                }

                // Поле для ввода перевода
                OutlinedTextField(
                    value = uiState.userAnswerTranslation,
                    onValueChange = { viewModel.onUserTranslationChange(it) },
                    label = { Text("Русский перевод") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(translationFocusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.checkAnswer()
                        }
                    ),
                    textStyle = TextStyle(fontSize = 18.sp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = if (uiState.showResult) (if (uiState.isCorrectTranslation == true) Color.Green else Color.Red) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (uiState.showResult) (if (uiState.isCorrectTranslation == true) Color.Green else if (uiState.isCorrectTranslation == false) Color.Red else MaterialTheme.colorScheme.outline) else MaterialTheme.colorScheme.outline,
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Сообщение с результатом
                if (uiState.showResult && uiState.feedbackMessage != null) {
                    Text(
                        text = uiState.feedbackMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isCorrectArticle == true && uiState.isCorrectTranslation == true) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f)) // Занимает оставшееся место

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.checkAnswer()
                        },
                        enabled = !uiState.isLoading && uiState.currentWord != null && !uiState.showResult, // Активна только до проверки
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text("Проверить")
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.loadNextWord()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(if (uiState.showResult && uiState.isCorrectArticle == true && uiState.isCorrectTranslation == true) "Далее" else "Пропустить")
                    }
                }
            }
        }
    }
}
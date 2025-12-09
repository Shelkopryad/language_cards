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
import com.example.languagecards.dao.LanguageType
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleAndTranslationQuizScreen(
    viewModel: ArticleAndTranslationQuizViewModel = hiltViewModel(),
    onQuizFinished: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val focusManager = LocalFocusManager.current
    val translationFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.currentWord) {
        if (uiState.currentWord != null) {
            viewModel.clearFeedbackMessage()
        }
    }

    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null && uiState.feedbackMessage.toString().startsWith("Отлично!")) {
            delay(1500)
            viewModel.loadNextWord()
        }
    }

    val languageLabel = when (selectedLanguage) {
        LanguageType.ROMANIAN -> "Română"
        else -> "Français"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Квиз: Артикль и Перевод ($languageLabel)") },
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
                println(uiState.toString())
                Text(
                    text = uiState.feedbackMessage ?: "Нет слов для этого квиза.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 50.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onQuizFinished() }) {
                    Text("Вернуться")
                }
            } else {
                val wordInfo = uiState.currentWord!!
                val hasArticle = uiState.hasArticle

                Text(
                    text = if (hasArticle) {
                        "Введите артикль и перевод для слова:"
                    } else {
                        "Введите перевод для слова:"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (hasArticle) {
                    // Показываем поле артикля и слово в одной строке
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = uiState.userAnswerArticle,
                            onValueChange = { viewModel.onUserArticleChange(it) },
                            label = { Text("Артикль") },
                            modifier = Modifier
                                .weight(0.3f)
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
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = if (uiState.showResult) (if (uiState.isCorrectArticle == true) Color.Green else Color.Red) else MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = if (uiState.showResult) (if (uiState.isCorrectArticle == true) Color.Green else if (uiState.isCorrectArticle == false) Color.Red else MaterialTheme.colorScheme.outline) else MaterialTheme.colorScheme.outline,
                            )
                        )
                        Text(
                            text = wordInfo.foreignWord,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                } else {
                    // Показываем только слово без поля артикля
                    Text(
                        text = wordInfo.foreignWord,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = if (uiState.showResult) (if (uiState.isCorrectTranslation == true) Color.Green else Color.Red) else MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = if (uiState.showResult) (if (uiState.isCorrectTranslation == true) Color.Green else if (uiState.isCorrectTranslation == false) Color.Red else MaterialTheme.colorScheme.outline) else MaterialTheme.colorScheme.outline,
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.showResult && uiState.feedbackMessage != null) {
                    val isSuccess = if (uiState.hasArticle) {
                        uiState.isCorrectArticle == true && uiState.isCorrectTranslation == true
                    } else {
                        uiState.isCorrectTranslation == true
                    }
                    Text(
                        text = uiState.feedbackMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.checkAnswer()
                        },
                        enabled = !uiState.isLoading && uiState.currentWord != null && !uiState.showResult,
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
                        val isSuccess = if (uiState.hasArticle) {
                            uiState.isCorrectArticle == true && uiState.isCorrectTranslation == true
                        } else {
                            uiState.isCorrectTranslation == true
                        }
                        Text(if (uiState.showResult && isSuccess) "Далее" else "Пропустить")
                    }
                }
            }
        }
    }
}
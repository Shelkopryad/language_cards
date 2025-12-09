package com.example.languagecards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.LanguageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    wordId: Int? = null,
    viewModel: AddWordViewModel = hiltViewModel(),
    onWordAddedSuccessfully: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()

    LaunchedEffect(wordId) {
        if (wordId != null) {
            viewModel.loadWordForEditing(wordId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onWordAddedSuccessfully()
        }
    }

    val languageLabel = when (selectedLanguage) {
        LanguageType.ROMANIAN -> "Română"
        else -> "Français"
    }

    val screenTitle = if (uiState.editingWordId != null) {
        "Редактировать слово ($languageLabel)"
    } else {
        "Добавить слово ($languageLabel)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Введите слово (можно с артиклем):",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.foreignWordInput,
                onValueChange = { viewModel.onForeignWordInputChange(it) },
                label = { 
                    Text(
                        if (selectedLanguage == LanguageType.FRENCH) 
                            "Французское слово" 
                        else 
                            "Румынское слово"
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.article,
                    onValueChange = { viewModel.onArticleChange(it) },
                    label = { Text("Артикль") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = uiState.isNoun
                )
                OutlinedTextField(
                    value = uiState.derivedForeignWord,
                    onValueChange = { },
                    label = { Text("Слово (без артикля)") },
                    modifier = Modifier.weight(2f),
                    readOnly = true,
                    colors = TextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Text(
                "Русский перевод:",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.russianTranslation,
                onValueChange = { viewModel.onRussianTranslationChange(it) },
                label = { Text("Русский перевод") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            // Чекбокс "Не существительное"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.onIsNounChanged(!uiState.isNoun) }
            ) {
                Checkbox(
                    checked = !uiState.isNoun,
                    onCheckedChange = { viewModel.onIsNounChanged(!it) }
                )
                Text(
                    text = "Не существительное",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Выбор рода (только для существительных)
            if (uiState.isNoun) {
                Text(
                    "Род слова:",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedGender == GenderType.MASCULINE,
                        onClick = { viewModel.onGenderSelected(GenderType.MASCULINE) }
                    )
                    Text(
                        text = "Муж.",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { viewModel.onGenderSelected(GenderType.MASCULINE) }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(
                        selected = uiState.selectedGender == GenderType.FEMININE,
                        onClick = { viewModel.onGenderSelected(GenderType.FEMININE) }
                    )
                    Text(
                        text = "Жен.",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable { viewModel.onGenderSelected(GenderType.FEMININE) }
                    )
                    // Средний род только для румынского
                    if (selectedLanguage == LanguageType.ROMANIAN) {
                        Spacer(modifier = Modifier.width(12.dp))
                        RadioButton(
                            selected = uiState.selectedGender == GenderType.NEUTER,
                            onClick = { viewModel.onGenderSelected(GenderType.NEUTER) }
                        )
                        Text(
                            text = "Ср.",
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { viewModel.onGenderSelected(GenderType.NEUTER) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            uiState.userMessage?.let { message ->
                Text(
                    text = message,
                    color = if (uiState.saveSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.saveWord() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (uiState.editingWordId != null) "Обновить слово" else "Сохранить слово")
                }
            }
        }
    }
}
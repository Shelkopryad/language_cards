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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    viewModel: AddWordViewModel = hiltViewModel(),
    onWordAddedSuccessfully: () -> Unit = {} // Callback для навигации или другого действия после успеха
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onWordAddedSuccessfully()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить новое слово") },
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
                "Введите французское слово (можно с артиклем):",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.frenchWordInput,
                onValueChange = { viewModel.onFrenchWordInputChange(it) },
                label = { Text("Французское слово") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences // Для l' в начале
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
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.derivedFrenchWord,
                    onValueChange = { /* Обычно это поле не редактируется напрямую */ },
                    label = { Text("Слово (без артикля)") },
                    modifier = Modifier.weight(2f),
                    readOnly = true, // Это поле выводится из frenchWordInput
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
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
                    text = "Мужской",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { viewModel.onGenderSelected(GenderType.MASCULINE) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = uiState.selectedGender == GenderType.FEMININE,
                    onClick = { viewModel.onGenderSelected(GenderType.FEMININE) }
                )
                Text(
                    text = "Женский",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { viewModel.onGenderSelected(GenderType.FEMININE) }
                )
            }

            Spacer(modifier = Modifier.weight(1f)) // Занимает оставшееся место

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
                    Text("Сохранить слово")
                }
            }
        }
    }
}
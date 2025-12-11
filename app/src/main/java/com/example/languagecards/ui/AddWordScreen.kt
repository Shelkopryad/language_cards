package com.example.languagecards.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.languagecards.R
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
        LanguageType.ROMANIAN -> stringResource(R.string.romanian_label)
        else -> stringResource(R.string.french_label)
    }

    val screenTitle = if (uiState.editingWordId != null) {
        stringResource(R.string.edit_word_title, languageLabel)
    } else {
        stringResource(R.string.add_word_title, languageLabel)
    }

    Scaffold(
        contentWindowInsets = androidx.compose.material3.ScaffoldDefaults.contentWindowInsets
            .exclude(androidx.compose.foundation.layout.WindowInsets.statusBars)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Простой заголовок вместо TopAppBar
            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                stringResource(R.string.enter_word_label),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.fullWord,
                onValueChange = { viewModel.onFullWordChange(it) },
                label = {
                    Text(
                        if (selectedLanguage == LanguageType.FRENCH)
                            stringResource(R.string.french_word_hint)
                        else
                            stringResource(R.string.romanian_word_hint)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Text(
                stringResource(R.string.russian_translations_label),
                style = MaterialTheme.typography.titleMedium
            )

            uiState.translations.forEachIndexed { index, translation ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = translation,
                        onValueChange = { viewModel.onTranslationChange(index, it) },
                        label = { Text(stringResource(R.string.translation_label, index + 1)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                    if (uiState.translations.size > 1) {
                        IconButton(
                            onClick = { viewModel.removeTranslation(index) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete_translation_content_desc)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.addTranslation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_translation_content_desc),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_another_translation))
            }

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
                    text = stringResource(R.string.not_a_noun_label),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Выбор рода (только для существительных)
            if (uiState.isNoun) {
                Text(
                    stringResource(R.string.gender_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = uiState.selectedGender == GenderType.MASCULINE,
                        onClick = { viewModel.onGenderSelected(GenderType.MASCULINE) }
                    )
                    Text(
                        text = stringResource(R.string.gender_masculine),
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
                        text = stringResource(R.string.gender_feminine),
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
                            text = stringResource(R.string.gender_neuter),
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
                    Text(
                        if (uiState.editingWordId != null) stringResource(R.string.update_word_button) else stringResource(
                            R.string.save_word_button
                        )
                    )
                }
            }
        }
    }
}
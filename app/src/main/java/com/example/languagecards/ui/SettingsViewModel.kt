package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.LanguageType
import com.example.languagecards.dao.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedLanguage = settingsRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LanguageType.FRENCH)

    fun setLanguage(language: Int) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language)
        }
    }
}

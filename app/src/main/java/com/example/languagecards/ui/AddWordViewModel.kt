package com.example.languagecards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.languagecards.dao.GenderType
import com.example.languagecards.dao.WordCardEntity
import com.example.languagecards.dao.WordCardDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWordUiState(
    val frenchWordInput: String = "", // Слово как вводит юзер, может содержать артикль вначале
    val derivedFrenchWord: String = "", // Слово без артикля
    val russianTranslation: String = "",
    val article: String = "",
    val selectedGender: Int? = null, // null, GenderType.MASCULINE, GenderType.FEMININE
    val isSaving: Boolean = false,
    val userMessage: String? = null,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class AddWordViewModel @Inject constructor(
    private val wordCardDao: WordCardDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWordUiState())
    val uiState = _uiState.asStateFlow()

    // Фокусируемся на определенных артиклях
    // 'les' добавлен, так как это определенный артикль множественного числа,
    // но он не поможет определить род единственного числа.
    private val definiteArticles = mapOf(
        "le" to GenderType.MASCULINE,
        "la" to GenderType.FEMININE,
        "l'" to null, // Род не определяем однозначно, пользователь должен выбрать
        "les" to null // Множественное число, род для единственного числа не определяем
    )

    fun onFrenchWordInputChange(input: String) {
        val trimmedInput = input.trimStart() // Убираем пробелы только в начале для парсинга артикля
        _uiState.update {
            it.copy(
                frenchWordInput = trimmedInput,
                saveSuccess = false,
                userMessage = null
            )
        }
        extractArticleAndWord(trimmedInput)
    }

    private fun extractArticleAndWord(input: String) {
        var currentArticle =
            _uiState.value.article // Сохраняем текущий артикль, если пользователь его уже ввел/изменил
        var currentWord = input
        var preselectedGender: Int? =
            _uiState.value.selectedGender // Сохраняем текущий выбор пользователя

        var foundArticle = false
        for ((art, genderMarker) in definiteArticles) {
            // Проверяем, начинается ли строка с артикля и за ним пробел, или это l' и за ним буква
            if (input.startsWith("$art ", ignoreCase = true) ||
                (art == "l'" && input.startsWith(
                    art,
                    ignoreCase = true
                ) && input.length > art.length && input[art.length].isLetter())
            ) {
                currentArticle = input.substring(0, art.length)
                currentWord =
                    input.substring(art.length).trimStart() // Убираем пробел после артикля
                if (genderMarker != null) { // Если артикль однозначно указывает на род
                    preselectedGender = genderMarker
                } else if (art == "l'") {
                    // Для l' не сбрасываем род, если он уже был выбран пользователем.
                    // Пользователь должен будет подтвердить или выбрать.
                }
                foundArticle = true
                break
            }
        }

        if (!foundArticle && currentWord.contains(" ")) {
            // Если артикль не распознан, но есть пробел, возможно, пользователь ввел что-то свое
            // В этом случае, не трогаем поле артикля, если он его уже редактировал,
            // и слово остается как есть (без автоматического отсечения)
            // Либо можно очистить currentArticle, если мы не хотим сохранять "неправильные" артикли
            // currentArticle = "" // Раскомментировать, если нужно очищать поле артикля при нераспознанном
        } else if (!foundArticle) {
            // Если артикль не найден и нет пробелов (одно слово), то артикля нет
            // Не меняем currentArticle, если пользователь его ввел вручную ранее
        }


        _uiState.update {
            it.copy(
                derivedFrenchWord = currentWord,
                article = if (foundArticle) currentArticle else it.article, // Обновляем артикль только если нашли
                selectedGender = preselectedGender
            )
        }
    }

    fun onRussianTranslationChange(translation: String) {
        _uiState.update {
            it.copy(
                russianTranslation = translation.trimStart(),
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onArticleChange(articleInput: String) {
        val trimmedArticle = articleInput.trim()
        var newSelectedGender = _uiState.value.selectedGender

        // Попробуем определить род по новому артиклю
        val articleKey = trimmedArticle.lowercase()
        if (definiteArticles.containsKey(articleKey)) {
            definiteArticles[articleKey]?.let { gender ->
                newSelectedGender = gender
            }
            // Если это l' или les, и род был null, не меняем его на null,
            // даем пользователю выбрать или оставляем предыдущий выбор.
            if (definiteArticles[articleKey] == null && _uiState.value.selectedGender != null) {
                // не меняем newSelectedGender
            } else {
                newSelectedGender = definiteArticles[articleKey] ?: _uiState.value.selectedGender
            }
        }

        _uiState.update {
            it.copy(
                article = trimmedArticle,
                selectedGender = newSelectedGender,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun onGenderSelected(gender: Int) {
        var updatedArticle = _uiState.value.article
        // Если артикль пуст или l', и пользователь выбрал род,
        // можно предложить стандартный артикль.
        // Но для l' это сложнее, так как зависит от следующей буквы слова.
        // Пока оставим артикль как есть, если он l'.
        // Пользователь должен сам корректно ввести l' если слово начинается с гласной.
        if (updatedArticle.isBlank() || updatedArticle.lowercase() !in listOf(
                "le",
                "la",
                "l'",
                "les"
            )
        ) {
            updatedArticle = when (gender) {
                GenderType.MASCULINE -> "le"
                GenderType.FEMININE -> "la"
                else -> ""
            }
            // Проверка на l' (упрощенная, без учета h muet)
            val wordStartsWithVowel = _uiState.value.derivedFrenchWord.lowercase().firstOrNull()
                ?.let { it in "aeiouyàâéèêëîïôùûü" } == true
            if (wordStartsWithVowel && (updatedArticle == "le" || updatedArticle == "la")) {
                updatedArticle = "l'"
            }
        } else if ((updatedArticle.lowercase() == "le" && gender == GenderType.FEMININE) ||
            (updatedArticle.lowercase() == "la" && gender == GenderType.MASCULINE)
        ) {
            // Если артикль противоречит выбранному роду (и это не l'), обновляем артикль
            updatedArticle = when (gender) {
                GenderType.MASCULINE -> "le"
                GenderType.FEMININE -> "la"
                else -> ""
            }
            val wordStartsWithVowel = _uiState.value.derivedFrenchWord.lowercase().firstOrNull()
                ?.let { it in "aeiouyàâéèêëîïôùûü" } == true
            if (wordStartsWithVowel && (updatedArticle == "le" || updatedArticle == "la")) {
                updatedArticle = "l'"
            }
        }


        _uiState.update {
            it.copy(
                selectedGender = gender,
                article = updatedArticle,
                saveSuccess = false,
                userMessage = null
            )
        }
    }

    fun saveWord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, userMessage = null, saveSuccess = false) }

            val currentState = _uiState.value
            val frenchWordToSave = currentState.derivedFrenchWord.trim()
            val russianTranslationToSave = currentState.russianTranslation.trim()
            // Используем артикль из state, который мог быть автоматически скорректирован
            // или введен пользователем.
            val articleToSave = currentState.article.trim()

            // Валидация
            if (frenchWordToSave.isBlank()) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Французское слово не может быть пустым."
                    )
                }
                return@launch
            }
            if (russianTranslationToSave.isBlank()) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Русский перевод не может быть пустым."
                    )
                }
                return@launch
            }
            if (currentState.selectedGender == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Необходимо выбрать род слова."
                    )
                }
                return@launch
            }
            // Проверка артикля: если выбран род, артикль не должен быть пустым,
            // если это не слово, которое используется без артикля (что маловероятно для французских существительных с родом).
            // Логика в onGenderSelected должна была предложить артикль.
            // Если артикль 'les', а выбран род единственного числа, это может быть несоответствие,
            // но мы сохраним как есть, предполагая, что пользователь знает, что делает,
            // или что 'les' используется как пример. Для простоты пока не добавляем сложную валидацию на 'les'.
            if (articleToSave.isBlank() && currentState.selectedGender != null) {
                // Можно добавить предупреждение, но onGenderSelected должен был это исправить.
                // Если все же пустой, можно попробовать его установить еще раз на основе рода.
                // Но лучше, чтобы предыдущие шаги это гарантировали.
                // Для простоты, если он пуст, а род выбран, это может быть ошибкой в логике выше.
                // _uiState.update { it.copy(isSaving = false, userMessage = "Артикль не может быть пустым при выбранном роде.") }
                // return@launch
            }


            val wordCard = WordCardEntity(
                frenchWord = frenchWordToSave.lowercase(),
                russianTranslation = russianTranslationToSave.lowercase(),
                article = articleToSave.lowercase(), // Сохраняем артикль из состояния
                gender = currentState.selectedGender // selectedGender гарантированно не null после проверки выше
            )

            try {
                // Предполагается, что у вас есть метод insertWord или аналогичный в WordCardDao
                wordCardDao.insertWord(wordCard)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Слово '${wordCard.frenchWord}' успешно сохранено!",
                        saveSuccess = true,
                        // Сброс полей для следующего ввода
                        frenchWordInput = "",
                        derivedFrenchWord = "",
                        russianTranslation = "",
                        article = "",
                        selectedGender = null
                    )
                }
            } catch (e: Exception) {
                // Обработка возможных ошибок при вставке в БД (например, нарушение unique constraint)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Ошибка сохранения: ${e.message ?: "Неизвестная ошибка"}"
                    )
                }
            }
        }
    }
}
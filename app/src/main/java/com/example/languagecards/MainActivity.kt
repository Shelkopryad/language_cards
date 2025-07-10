package com.example.languagecards

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.languagecards.ui.AddWordScreen
import com.example.languagecards.ui.ArticleAndTranslationQuizScreen
import com.example.languagecards.ui.TranslationQuizScreen
import com.example.languagecards.ui.WordListScreen
import com.example.testapp.ui.theme.LanguageCardsTheme
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        enableEdgeToEdge()
        setContent {
            LanguageCardsTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            actions = {
                                IconButton(
                                    onClick = {
                                        navController.navigate("translateQuiz")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Translate Quiz"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        navController.navigate("articleQuiz")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Create,
                                        contentDescription = "Article Quiz"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        navController.navigate("addWords")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add new word"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        navController.navigate("wordList")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.List,
                                        contentDescription = "Word List"
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "translateQuiz",
                        modifier = Modifier.padding(innerPadding),
                    ) {
                        composable("translateQuiz") {
                            TranslationQuizScreen()
                        }

                        composable("articleQuiz") {
                            ArticleAndTranslationQuizScreen()
                        }

                        composable("addWords") {
                            AddWordScreen()
                        }

                        composable("wordList") {
                            WordListScreen(
                                onNavigateToAddWord = { navController.navigate("addWords") }
                            )
                        }
                    }
                }
            }
        }
    }
}
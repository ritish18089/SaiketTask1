package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.AuthRepository
import com.example.data.AuthState
import com.example.data.QuizRepository
import com.example.ui.navigation.*
import com.example.ui.screens.*
import com.example.ui.theme.QuizMasterTheme
import com.example.ui.theme.ThemePreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        AuthRepository.init(this)
        QuizRepository.init(this)
        ThemePreferences.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemePreferences.themeMode.collectAsState()
            val isDarkTheme = when (themeMode) {
                2 -> true
                else -> false
            }
            
            val authState by AuthRepository.authState.collectAsState()
            
            QuizMasterTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()
                val quizViewModel: QuizViewModel = viewModel()
                
                LaunchedEffect(authState) {
                    if (authState == AuthState.AUTHENTICATED) {
                        navController.navigate(Home) {
                            popUpTo(0)
                        }
                    } else if (authState == AuthState.UNAUTHENTICATED) {
                        navController.navigate(Login) {
                            popUpTo(0)
                        }
                    }
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Splash,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                    ) {
                        composable<Splash> { SplashScreen() }
                        composable<Login> { LoginScreen(navController) }
                        composable<Signup> { SignupScreen(navController) }
                        composable<Home> { HomeScreen(navController) }
                        composable<Categories> { CategoriesScreen(navController) }
                        composable<Profile> { ProfileScreen(navController) }
                        composable<Subcategories> {
                            val args = it.toRoute<Subcategories>()
                            SubcategoriesScreen(navController, args.mainSectionId, args.title)
                        }
                        composable<PreQuiz> {
                            val args = it.toRoute<PreQuiz>()
                            PreQuizScreen(navController, args.subcategoryId, args.title)
                        }
                        composable<Quiz> {
                            val args = it.toRoute<Quiz>()
                            QuizScreen(navController, args.subcategoryId, quizViewModel)
                        }
                        composable<Practice> {
                            val args = it.toRoute<Practice>()
                            PracticeScreen(navController, args.subcategoryId, quizViewModel)
                        }
                        composable<Result> { ResultScreen(navController, quizViewModel) }
                        composable<ReviewAnswers> { ReviewAnswersScreen(navController, quizViewModel) }
                        composable<EditProfile> { EditProfileScreen(navController) }
                        composable<ChangePassword> { ChangePasswordScreen(navController) }
                        composable<Appearance> { AppearanceScreen(navController) }
                        composable<QuizHistory> { QuizHistoryScreen(navController) }
                    }
                }
            }
        }
    }
}

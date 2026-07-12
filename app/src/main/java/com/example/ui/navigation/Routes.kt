package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object Splash
@Serializable object Login
@Serializable object Signup
@Serializable object Home
@Serializable object Categories
@Serializable object Profile
@Serializable data class Subcategories(val mainSectionId: String, val title: String)
@Serializable data class PreQuiz(val subcategoryId: String, val title: String)
@Serializable data class Quiz(val subcategoryId: String, val title: String)
@Serializable data class Practice(val subcategoryId: String, val title: String)
@Serializable object Result
@Serializable object ReviewAnswers
@Serializable object EditProfile
@Serializable object ChangePassword
@Serializable object Appearance
@Serializable object QuizHistory

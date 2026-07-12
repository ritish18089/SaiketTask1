package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.navigation.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController, categoryId: String, viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsState()
    var showSubmitDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(categoryId) {
        viewModel.loadQuestions(categoryId)
        viewModel.startTimer()
    }
    
    LaunchedEffect(state.isSubmitted) {
        if (state.isSubmitted) {
            navController.navigate(Result) {
                popUpTo(Result) { inclusive = true }
            }
        }
    }
    
    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Quiz") },
            text = {
                val answered = state.selectedAnswers.size
                val unanswered = state.questions.size - answered
                Text("You have answered $answered out of ${state.questions.size} questions.\n$unanswered questions are unanswered.\nDo you want to submit?")
            },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    viewModel.submitQuiz()
                }) {
                    Text("Submit Quiz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Continue Quiz")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val mins = state.timeLeftSeconds / 60
                    val secs = state.timeLeftSeconds % 60
                    val currentScore = state.score
                    Text(String.format("%02d:%02d | Score: %d", mins, secs, currentScore), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showSubmitDialog = true }) {
                        Text("Submit", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.questions.isNotEmpty()) {
            val currentQuestion = state.questions[state.currentQuestionIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Question ${state.currentQuestionIndex + 1} of ${state.questions.size}", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(currentQuestion.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(24.dp))
                
                currentQuestion.options.forEach { option ->
                    val isSelected = state.selectedAnswers[currentQuestion.id] == option
                    val isCorrect = option == currentQuestion.correctAnswer
                    val showAnswer = state.isAnswerValidated
                    
                    val color = if (showAnswer) {
                        if (isCorrect) Color(0xFF4CAF50)
                        else if (isSelected) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    } else MaterialTheme.colorScheme.onSurface
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { 
                                if (!showAnswer) {
                                    viewModel.selectAnswer(currentQuestion.id, option)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyLarge, color = color)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { 
                            viewModel.goToPrevious() 
                        },
                        enabled = state.currentQuestionIndex > 0
                    ) {
                        Text("Previous")
                    }
                    
                    Button(
                        onClick = {
                            if (!state.isAnswerValidated) {
                                viewModel.validateCurrentAnswer()
                            } else {
                                if (state.currentQuestionIndex < state.questions.size - 1) {
                                    viewModel.goToNext()
                                } else {
                                    showSubmitDialog = true
                                }
                            }
                        }
                    ) {
                        Text(if (!state.isAnswerValidated) "Next" else if (state.currentQuestionIndex < state.questions.size - 1) "Continue" else "Finish")
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No questions available.")
            }
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(navController: NavController, categoryId: String, viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(categoryId) {
        viewModel.loadQuestions(categoryId)
    }
    
    BackHandler {
        showExitDialog = true
    }
    
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Practice Mode") },
            text = { Text("Are you sure you want to exit Practice Mode?") },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Practice Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { showExitDialog = true }) {
                        Text("Exit", fontWeight = FontWeight.Bold)
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
                
                if (state.isAnswerValidated) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val userAnswer = state.selectedAnswers[currentQuestion.id]
                            val isAnswerCorrect = userAnswer == currentQuestion.correctAnswer
                            
                            if (isAnswerCorrect) {
                                Text(
                                    text = "Correct! ✓",
                                    color = Color(0xFF4CAF50),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Incorrect! ✕",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Explanation:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currentQuestion.explanation)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.goToPrevious() },
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
                                    showExitDialog = true
                                }
                            }
                        }
                    ) {
                        Text(if (!state.isAnswerValidated) "Next" else if (state.currentQuestionIndex < state.questions.size - 1) "Continue" else "Finish")
                    }
                }
            }
        }
    }
}

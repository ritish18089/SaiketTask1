package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.navigation.Categories
import com.example.ui.navigation.Quiz
import com.example.ui.navigation.ReviewAnswers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, viewModel: QuizViewModel) {
    val state by viewModel.state.collectAsState()

    var correct = 0
    var wrong = 0
    state.questions.forEach { q ->
        val answer = state.selectedAnswers[q.id]
        if (answer != null) {
            if (answer == q.correctAnswer) correct++ else wrong++
        }
    }
    val unanswered = state.questions.size - (correct + wrong)
    val percentage = if (state.questions.isNotEmpty()) (correct.toFloat() / state.questions.size) * 100 else 0f
    val timeTakenSeconds = (30 * 60) - state.timeLeftSeconds
    val mins = timeTakenSeconds / 60
    val secs = timeTakenSeconds % 60

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Quiz Result", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (percentage >= 50f) "Pass" else "Fail",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (percentage >= 50f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Final Score: ${percentage.toInt()}%", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Questions: ${state.questions.size}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Correct Answers: $correct", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wrong Answers: $wrong", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unanswered: $unanswered", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(String.format("Time Taken: %02d:%02d", mins, secs), style = MaterialTheme.typography.bodyLarge)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { navController.navigate(ReviewAnswers) },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Review Answers")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    navController.navigate(Quiz(state.categoryId, "Retry Quiz")) {
                        popUpTo(Categories)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Retry Quiz")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    navController.navigate(Categories) {
                        popUpTo(0)
                    }
                }
            ) {
                Text("Back to Categories")
            }
        }
    }
}

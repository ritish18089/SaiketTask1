package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class QuizHistoryItem(
    val id: String,
    val subcategoryName: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val date: Date
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHistoryScreen(navController: NavController) {
    var history by remember { mutableStateOf<List<QuizHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val currentUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        if (currentUser != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                // Assuming history might be stored here
                db.collection("users").document(currentUser.uid).collection("quizHistory")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            val items = snapshot.documents.mapNotNull { doc ->
                                try {
                                    QuizHistoryItem(
                                        id = doc.id,
                                        subcategoryName = doc.getString("subcategoryName") ?: "Unknown",
                                        score = doc.getLong("score")?.toInt() ?: 0,
                                        totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0,
                                        percentage = doc.getLong("percentage")?.toInt() ?: 0,
                                        date = doc.getDate("timestamp") ?: Date()
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            history = items
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        // Fallback check to top-level collection if needed, but we'll just fail here
                        db.collection("quizHistory").whereEqualTo("userId", currentUser.uid)
                            .get()
                            .addOnSuccessListener { fallbackSnapshot ->
                                val items = fallbackSnapshot.documents.mapNotNull { doc ->
                                    try {
                                        QuizHistoryItem(
                                            id = doc.id,
                                            subcategoryName = doc.getString("subcategoryName") ?: "Unknown",
                                            score = doc.getLong("score")?.toInt() ?: 0,
                                            totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 0,
                                            percentage = doc.getLong("percentage")?.toInt() ?: 0,
                                            date = doc.getDate("timestamp") ?: Date()
                                        )
                                    } catch (ex: Exception) {
                                        null
                                    }
                                }
                                history = items.sortedByDescending { it.date }
                                isLoading = false
                            }
                            .addOnFailureListener {
                                errorMessage = "Failed to load history."
                                isLoading = false
                            }
                    }
            } catch (e: Exception) {
                errorMessage = "An error occurred."
                isLoading = false
            }
        } else {
            isLoading = false
            errorMessage = "Not logged in."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        } else if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No quiz history yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = item.subcategoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.percentage}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.percentage >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "${item.score} / ${item.totalQuestions} correct",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

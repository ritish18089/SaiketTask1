package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.navigation.Practice
import com.example.ui.navigation.Quiz

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreQuizScreen(navController: NavController, subcategoryId: String, title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            Text("Select Mode", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { navController.navigate(Quiz(subcategoryId, title)) },
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Quiz Mode", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Exactly 30 questions. 30:00 minutes timer.", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedButton(
                onClick = { navController.navigate(Practice(subcategoryId, title)) },
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Practice Mode", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Learn at your own pace.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

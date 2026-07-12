package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.AuthRepository
import com.example.data.DataImportRepository
import com.example.ui.navigation.*
import com.example.ui.theme.ThemePreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val userProfile by AuthRepository.currentUser.collectAsState()
    
    val displayName = userProfile?.fullName ?: "User Name"
    val displayEmail = userProfile?.email ?: "user@example.com"
    val displayAvatarInitial = if (displayName.isNotBlank()) displayName.take(1).uppercase() else "U"

    var totalQuizzes by remember { mutableStateOf(userProfile?.totalQuizzes?.toString() ?: "0") }
    var totalQuestions by remember { mutableStateOf(userProfile?.totalQuestions?.toString() ?: "0") }
    var averageScore by remember { mutableStateOf(userProfile?.averageScore?.toInt()?.toString() ?: "0") }
    var personalBest by remember { mutableStateOf(userProfile?.personalBest?.toString() ?: "0") }

    var importStatus by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val currentUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        if (currentUser != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val fetchedName = document.getString("fullName")
                            val fetchedEmail = document.getString("email") ?: currentUser.email
                            
                            // Update AuthRepository if it doesn't match
                            if (userProfile == null && fetchedName != null && fetchedEmail != null) {
                                AuthRepository.signup(fetchedName, fetchedEmail)
                            } else if (fetchedName != null && fetchedEmail != null && 
                                       (userProfile?.fullName != fetchedName || userProfile?.email != fetchedEmail)) {
                                AuthRepository.updateProfile(fetchedName, fetchedEmail)
                            }
                            
                            document.getLong("totalQuizzes")?.let { totalQuizzes = it.toString() }
                            document.getLong("totalQuestions")?.let { totalQuestions = it.toString() }
                            document.getDouble("averageScore")?.let { averageScore = it.toInt().toString() }
                            document.getLong("personalBest")?.let { personalBest = it.toString() }
                        }
                    }
            } catch (e: Exception) {
                // Ignore, keep fallbacks
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        AuthRepository.logout()
                        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) { }
                        ThemePreferences.resetTheme(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = { BottomNavBar(navController, "Profile") },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayAvatarInitial,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = displayEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Statistics Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        text = "Your Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            modifier = Modifier.weight(1f),
                            value = totalQuizzes,
                            label = "Quizzes"
                        )
                        StatItem(
                            modifier = Modifier.weight(1f),
                            value = totalQuestions,
                            label = "Questions"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatItem(
                            modifier = Modifier.weight(1f),
                            value = "${averageScore}%",
                            label = "Avg Score"
                        )
                        StatItem(
                            modifier = Modifier.weight(1f),
                            value = "${personalBest}%",
                            label = "Best Score"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Profile Options Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileOptionRow(
                        icon = Icons.Default.Person,
                        text = "Edit Profile",
                        onClick = { navController.navigate(EditProfile) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileOptionRow(
                        icon = Icons.Default.Lock,
                        text = "Change Password",
                        onClick = { navController.navigate(ChangePassword) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileOptionRow(
                        icon = Icons.Default.ColorLens,
                        text = "Appearance",
                        onClick = { navController.navigate(Appearance) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileOptionRow(
                        icon = Icons.Default.History,
                        text = "Quiz History",
                        onClick = { navController.navigate(QuizHistory) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ProfileOptionRow(
                        icon = Icons.Default.Logout,
                        text = "Logout",
                        onClick = { showLogoutDialog = true },
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Developer Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Developer Options",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (importStatus != null) {
                        Text(
                            text = importStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    Button(
                        onClick = {
                            isImporting = true
                            scope.launch {
                                DataImportRepository.importAllData(context) { status ->
                                    importStatus = status
                                }
                                isImporting = false
                            }
                        },
                        enabled = !isImporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp).padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        }
                        Text("Import Questions to Firestore")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatItem(modifier: Modifier = Modifier, value: String, label: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (!isDestructive) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Forward",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

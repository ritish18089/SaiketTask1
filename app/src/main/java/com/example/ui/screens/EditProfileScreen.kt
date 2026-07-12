package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val userState by AuthRepository.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        val currentUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        if (currentUser != null) {
            email = currentUser.email ?: ""
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val name = document.getString("fullName") ?: ""
                            fullName = name
                            initial = name.take(1).uppercase()
                            
                            // Also pre-fill email if it's stored in Firestore, or keep Auth email
                            val storedEmail = document.getString("email")
                            if (!storedEmail.isNullOrBlank()) {
                                email = storedEmail
                            }
                        }
                        isLoading = false
                    }
                    .addOnFailureListener {
                        // fallback
                        isLoading = false
                        errorMessage = "Failed to load profile."
                    }
            } catch (e: Exception) {
                isLoading = false
            }
        } else {
            // Fallback for AuthRepository local state if Firebase is not linked
            if (userState != null) {
                fullName = userState!!.fullName
                email = userState!!.email
                initial = fullName.take(1).uppercase()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (fullName.isNotBlank()) fullName.take(1).uppercase() else "U",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true, 
                    readOnly = false,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (successMessage != null) {
                    Text(successMessage!!, color = Color(0xFF4CAF50), modifier = Modifier.padding(bottom = 16.dp))
                }
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                }
                
                Button(
                    onClick = {
                        if (fullName.isBlank()) {
                            errorMessage = "Name cannot be empty."
                            return@Button
                        }
                        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        isSaving = true
                        successMessage = null
                        errorMessage = null
                        
                        // Fix for FATAL EXCEPTION: FirebaseAuth.getInstance() throws if not configured
                        val currentUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
                        
                        if (currentUser != null) {
                            val db = FirebaseFirestore.getInstance()
                            
                            val updates = mapOf(
                                "fullName" to fullName,
                                "email" to email
                            )
                            
                            db.collection("users").document(currentUser.uid)
                                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                                .addOnSuccessListener {
                                    isSaving = false
                                    successMessage = "Profile updated successfully!"
                                    AuthRepository.updateProfile(fullName, email)
                                    navController.popBackStack()
                                }
                                .addOnFailureListener { e ->
                                    isSaving = false
                                    errorMessage = "Failed to update profile: ${e.message}"
                                }
                        } else {
                            AuthRepository.updateProfile(fullName, email)
                            isSaving = false
                            successMessage = "Profile updated successfully!"
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}


package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val totalQuizzes: Int = 0,
    val totalQuestions: Int = 0,
    val averageScore: Float = 0f,
    val personalBest: Int = 0,
    val bestCategory: String = "N/A"
)

enum class AuthState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED
}

object AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState

    private var auth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    fun init(context: Context) {
        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()
            
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                fetchUserProfile(currentUser.uid, currentUser.email ?: "")
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Firebase not configured. Falling back to unauthenticated.")
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }

    private fun fetchUserProfile(uid: String, fallbackEmail: String) {
        db?.collection("users")?.document(uid)?.get()?.addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                if (profile != null) {
                    _currentUser.value = profile
                    _authState.value = AuthState.AUTHENTICATED
                } else {
                    fallbackProfile(uid, fallbackEmail)
                }
            } else {
                fallbackProfile(uid, fallbackEmail)
            }
        }?.addOnFailureListener {
            Log.e("AuthRepository", "fetchUserProfile failed, but user is logged in", it)
            fallbackProfile(uid, fallbackEmail)
        }
    }

    private fun fallbackProfile(uid: String, email: String) {
        // Keeps user logged in even if Firestore fetch fails
        _currentUser.value = UserProfile(uid = uid, email = email, fullName = email.substringBefore("@").replaceFirstChar { it.uppercase() })
        _authState.value = AuthState.AUTHENTICATED
    }

    fun login(email: String, password: String = "") {
        if (password.isEmpty()) return
        _authState.value = AuthState.LOADING
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth?.currentUser
                if (user != null) {
                    fetchUserProfile(user.uid, user.email ?: email)
                }
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
                Log.e("AuthRepository", "Login failed", task.exception)
            }
        }
    }

    fun signup(fullName: String, email: String, password: String = "") {
        if (password.isEmpty()) return
        _authState.value = AuthState.LOADING
        auth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth?.currentUser
                if (user != null) {
                    val profile = UserProfile(
                        uid = user.uid,
                        fullName = fullName,
                        email = email
                    )
                    db?.collection("users")?.document(user.uid)?.set(profile)?.addOnCompleteListener {
                        _currentUser.value = profile
                        _authState.value = AuthState.AUTHENTICATED
                    }
                }
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
                Log.e("AuthRepository", "Signup failed", task.exception)
            }
        }
    }

    fun logout() {
        auth?.signOut()
        _currentUser.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }

    fun updateProfile(fullName: String, email: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(fullName = fullName, email = email)
        _currentUser.value = updated
        
        db?.collection("users")?.document(current.uid)?.update(
            "fullName", fullName,
            "email", email
        )?.addOnFailureListener {
            Log.e("AuthRepository", "Failed to update profile", it)
        }
    }
}

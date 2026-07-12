package com.example.data

import android.content.Context
import android.util.Log
import com.example.models.Category
import com.example.models.Question
import com.example.models.Subcategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

object QuizRepository {
    var categories = listOf(
        Category(
            id = "aptitude",
            title = "Aptitude MCQ",
            subcategories = listOf(
                Subcategory("aptitude_number_system", "Number System"),
                Subcategory("aptitude_average", "Average"),
                Subcategory("aptitude_ratio_proportion", "Ratio & Proportion"),
                Subcategory("aptitude_calendars", "Calendars"),
                Subcategory("aptitude_blood_relation", "Blood Relation"),
                Subcategory("aptitude_coding_decoding", "Coding-Decoding"),
                Subcategory("aptitude_seating_arrangement", "Seating Arrangement"),
                Subcategory("aptitude_number_series", "Number Series"),
                Subcategory("aptitude_time_work", "Time & Work"),
                Subcategory("aptitude_problems_on_ages", "Problems on Ages")
            )
        ),
        Category(
            id = "technical",
            title = "Technical MCQ",
            subcategories = listOf(
                Subcategory("technical_database", "Database"),
                Subcategory("technical_computer_networks", "Computer Networks"),
                Subcategory("technical_operating_systems", "Operating Systems")
            )
        ),
        Category(
            id = "programming",
            title = "Programming MCQ",
            subcategories = listOf(
                Subcategory("programming_java", "Java"),
                Subcategory("programming_python", "Python"),
                Subcategory("programming_c", "C"),
                Subcategory("programming_react", "React"),
                Subcategory("programming_javascript", "JavaScript"),
                Subcategory("programming_typescript", "TypeScript")
            )
        ),
        Category(
            id = "gk",
            title = "General Knowledge (GK) MCQ",
            subcategories = listOf(
                Subcategory("gk_country", "Country"),
                Subcategory("gk_subjects", "Subjects"),
                Subcategory("gk_current_affairs", "Current Affairs")
            )
        )
    )

    private var allQuestions: List<Question> = emptyList()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var db: FirebaseFirestore? = null

    fun init(context: Context) {
        if (allQuestions.isNotEmpty()) return
        try {
            db = FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("QuizRepository", "Firebase not configured. Operating in local mode.")
        }

        context.assets.open("questions.json").use { inputStream ->
            val jsonString = InputStreamReader(inputStream).readText()
            allQuestions = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        }
        
        scope.launch {
            seedDatabase()
        }
    }

    private suspend fun seedDatabase() {
        val firestore = db ?: return
        try {
            // Seed Categories
            for (category in categories) {
                val catDoc = firestore.collection("categories").document(category.id).get().await()
                if (!catDoc.exists()) {
                    firestore.collection("categories").document(category.id).set(category).await()
                }
            }

            // Seed Questions
            // Check if seeded by looking at a sample question
            val sampleQ = allQuestions.firstOrNull()
            if (sampleQ != null) {
                val qDoc = firestore.collection("questions").document(sampleQ.id).get().await()
                if (!qDoc.exists()) {
                    // Start seeding
                    Log.d("QuizRepository", "Seeding questions to Firestore...")
                    allQuestions.chunked(100).forEach { batch ->
                        val batchWriter = firestore.batch()
                        batch.forEach { q ->
                            val docRef = firestore.collection("questions").document(q.id)
                            batchWriter.set(docRef, q)
                        }
                        batchWriter.commit().await()
                    }
                    Log.d("QuizRepository", "Finished seeding questions.")
                }
            }
        } catch (e: Exception) {
            Log.e("QuizRepository", "Error seeding database", e)
        }
    }

    suspend fun getQuestionsForCategory(categoryId: String): List<Question> {
        val firestore = db
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("questions")
                    .whereEqualTo("categoryId", categoryId)
                    .get()
                    .await()
                
                if (!snapshot.isEmpty) {
                    val questions = snapshot.toObjects(Question::class.java)
                    return questions.shuffled()
                }
            } catch (e: Exception) {
                Log.e("QuizRepository", "Error fetching questions from Firestore, falling back to local", e)
            }
        }
        // Fallback to local if Firestore fails or is empty
        return allQuestions.filter { it.categoryId == categoryId }.shuffled()
    }


    fun saveQuizResult(
        categoryId: String,
        scorePercentage: Int,
        correctAnswers: Int,
        wrongAnswers: Int,
        unansweredQuestions: Int,
        totalQuestions: Int
    ) {
        try {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val firestore = db
            if (user != null && firestore != null) {
                val uid = user.uid
                val result = hashMapOf(
                    "userId" to uid,
                    "categoryId" to categoryId,
                    "subcategoryId" to categoryId, // Since we use subcategoryId as categoryId in UI
                    "score" to scorePercentage,
                    "correctAnswers" to correctAnswers,
                    "wrongAnswers" to wrongAnswers,
                    "unansweredQuestions" to unansweredQuestions,
                    "totalQuestions" to totalQuestions,
                    "completedAt" to Timestamp.now()
                )
                
                firestore.collection("quizResults").add(result)
                    .addOnSuccessListener {
                        // Update user statistics
                        val userRef = firestore.collection("users").document(uid)
                        firestore.runTransaction { transaction ->
                            val snapshot = transaction.get(userRef)
                            
                            val currentTotalQuizzes = snapshot.getLong("totalQuizzes") ?: 0L
                            val newTotalQuizzes = currentTotalQuizzes + 1
                            
                            val currentTotalQuestions = snapshot.getLong("totalQuestions") ?: 0L
                            val newTotalQuestions = currentTotalQuestions + totalQuestions
                            
                            val currentAverageScore = snapshot.getDouble("averageScore") ?: 0.0
                            val newAverageScore = ((currentAverageScore * currentTotalQuizzes) + scorePercentage) / newTotalQuizzes
                            
                            val currentPersonalBest = snapshot.getLong("personalBest") ?: 0L
                            val newPersonalBest = maxOf(currentPersonalBest, scorePercentage.toLong())
                            
                            transaction.update(userRef, mapOf(
                                "totalQuizzes" to newTotalQuizzes,
                                "totalQuestions" to newTotalQuestions,
                                "averageScore" to newAverageScore,
                                "personalBest" to newPersonalBest
                            ))
                        }.addOnFailureListener { e ->
                            Log.e("QuizRepository", "Failed to update user stats", e)
                        }
                    }
                    .addOnFailureListener {
                        Log.w("QuizRepository", "Failed to save quiz result", it)
                    }
            }
        } catch (e: Exception) {
            Log.w("QuizRepository", "Error saving quiz result to Firebase", e)
        }
    }
}

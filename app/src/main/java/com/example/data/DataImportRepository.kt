package com.example.data

import android.content.Context
import android.util.Log
import com.example.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

object DataImportRepository {
    private val json = Json { ignoreUnknownKeys = true }

    data class ImportResult(
        val uploaded: Int = 0,
        val updated: Int = 0,
        val failures: Int = 0,
        val logs: List<String> = emptyList()
    )

    suspend fun importAllData(context: Context, onProgress: (String) -> Unit): ImportResult {
        var result = ImportResult()
        
        onProgress("Starting import process...")
        
        // Import Practice Questions
        onProgress("Importing practice questions...")
        val practiceResult = importJsonToFirestore(
            context, 
            "practice.json", 
            "practiceQuestions",
            onProgress
        )
        
        // Import Quiz Questions
        onProgress("Importing quiz questions...")
        val quizResult = importJsonToFirestore(
            context, 
            "questions.json", 
            "quizQuestions",
            onProgress
        )
        
        result = ImportResult(
            uploaded = practiceResult.uploaded + quizResult.uploaded,
            updated = practiceResult.updated + quizResult.updated,
            failures = practiceResult.failures + quizResult.failures,
            logs = practiceResult.logs + quizResult.logs
        )
        
        onProgress("Import Complete!")
        onProgress("Total Uploaded: ${result.uploaded}")
        onProgress("Total Updated: ${result.updated}")
        onProgress("Total Failures: ${result.failures}")
        
        return result
    }

    private suspend fun importJsonToFirestore(
        context: Context,
        fileName: String,
        collectionName: String,
        onProgress: (String) -> Unit
    ): ImportResult {
        var uploaded = 0
        var updated = 0
        var failures = 0
        val logs = mutableListOf<String>()
        val db = FirebaseFirestore.getInstance()

        try {
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val jsonString = reader.readText()
            val questions = json.decodeFromString<List<Question>>(jsonString)
            
            onProgress("Found ${questions.size} questions in $fileName")

            questions.forEachIndexed { index, question ->
                if (index % 10 == 0) {
                    onProgress("Processing $collectionName: $index / ${questions.size}")
                }

                // Validation
                val validationError = validateQuestion(question)
                if (validationError != null) {
                    failures++
                    val logMsg = "Validation failed for ${question.id}: $validationError"
                    logs.add(logMsg)
                    Log.e("DataImport", logMsg)
                    return@forEachIndexed
                }

                try {
                    val docRef = db.collection(collectionName).document(question.id)
                    val docSnapshot = docRef.get().await()
                    
                    if (docSnapshot.exists()) {
                        updated++
                    } else {
                        uploaded++
                    }
                    
                    docRef.set(question).await()
                } catch (e: Exception) {
                    failures++
                    val logMsg = "Failed to upload ${question.id}: ${e.message}"
                    logs.add(logMsg)
                    Log.e("DataImport", logMsg)
                }
            }
        } catch (e: Exception) {
            val logMsg = "Critical error reading $fileName: ${e.message}"
            logs.add(logMsg)
            Log.e("DataImport", logMsg)
            failures++
        }

        return ImportResult(uploaded, updated, failures, logs)
    }

    private fun validateQuestion(q: Question): String? {
        if (q.id.isBlank()) return "ID is empty"
        if (q.categoryId.isBlank()) return "Category ID is empty"
        if (q.text.isBlank()) return "Question text is empty"
        if (q.options.size != 4) return "Options count is ${q.options.size}, expected 4"
        if (q.correctAnswer !in q.options) return "Correct answer not in options"
        return null
    }
}

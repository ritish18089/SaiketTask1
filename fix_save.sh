sed -i '/fun saveQuizResult/,/^}/c\
    fun saveQuizResult(categoryId: String, score: Int, totalQuestions: Int) {\
        try {\
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()\
            val user = auth.currentUser\
            val firestore = db\
            if (user != null && firestore != null) {\
                val result = hashMapOf(\
                    "userId" to user.uid,\
                    "categoryId" to categoryId,\
                    "subcategoryId" to categoryId, // Since we use subcategoryId as categoryId in UI\
                    "score" to score,\
                    "totalQuestions" to totalQuestions,\
                    "completedAt" to System.currentTimeMillis()\
                )\
                firestore.collection("quizResults").add(result).addOnFailureListener {\
                    android.util.Log.w("QuizRepository", "Failed to save quiz result", it)\
                }\
            }\
        } catch (e: Exception) {\
            android.util.Log.w("QuizRepository", "Firebase not configured. Quiz result not saved online.")\
        }\
    }' app/src/main/java/com/example/data/QuizRepository.kt

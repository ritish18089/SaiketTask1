cat << 'INNER_EOF' >> app/src/main/java/com/example/data/QuizRepository.kt

    fun saveQuizResult(categoryId: String, score: Int, totalQuestions: Int) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val user = auth.currentUser
        val firestore = db
        if (user != null && firestore != null) {
            val result = hashMapOf(
                "userId" to user.uid,
                "categoryId" to categoryId,
                "subcategoryId" to categoryId, // Since we use subcategoryId as categoryId in UI
                "score" to score,
                "totalQuestions" to totalQuestions,
                "completedAt" to System.currentTimeMillis()
            )
            firestore.collection("quizResults").add(result).addOnFailureListener {
                android.util.Log.e("QuizRepository", "Failed to save quiz result", it)
            }
        }
    }
}
INNER_EOF
sed -i 's/^}//g' app/src/main/java/com/example/data/QuizRepository.kt

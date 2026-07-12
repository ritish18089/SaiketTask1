package com.example.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QuizRepository
import com.example.models.Question
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizState(
    val categoryId: String = "",
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val timeLeftSeconds: Int = 30 * 60,
    val isSubmitted: Boolean = false,
    val isLoading: Boolean = true,
    val isAnswerValidated: Boolean = false,
    val score: Int = 0
)

class QuizViewModel : ViewModel() {
    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()
    private var timerJob: Job? = null

    fun loadQuestions(categoryId: String) {
        viewModelScope.launch {
            _state.value = QuizState(categoryId = categoryId, isLoading = true)
            // Log matching finding
            val questions = QuizRepository.getQuestionsForCategory(categoryId)
            Log.d("QuizMaster", "Selected category ID: \$categoryId")
            Log.d("QuizMaster", "Repository query ID: \$categoryId")
            Log.d("QuizMaster", "Matching questions found: \${questions.size}")
            Log.d("QuizMaster", "Final questions loaded: \${questions.size}")
            
            _state.value = _state.value.copy(
                questions = questions,
                isLoading = false,
                selectedAnswers = emptyMap(),
                score = 0,
                isAnswerValidated = false,
                currentQuestionIndex = 0
            )
        }
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (_state.value.timeLeftSeconds > 0 && !_state.value.isSubmitted) {
                delay(1000)
                _state.value = _state.value.copy(timeLeftSeconds = _state.value.timeLeftSeconds - 1)
                if (_state.value.timeLeftSeconds <= 0) {
                    submitQuiz()
                }
            }
        }
    }

    fun selectAnswer(questionId: String, answer: String) {
        if (_state.value.isSubmitted || _state.value.isAnswerValidated) return
        val currentAnswers = _state.value.selectedAnswers.toMutableMap()
        currentAnswers[questionId] = answer
        _state.value = _state.value.copy(selectedAnswers = currentAnswers)
    }

    fun validateCurrentAnswer() {
        if (_state.value.isAnswerValidated) return
        
        val currentQuestion = _state.value.questions[_state.value.currentQuestionIndex]
        val selectedAnswer = _state.value.selectedAnswers[currentQuestion.id]
        
        var newScore = _state.value.score
        if (selectedAnswer == currentQuestion.correctAnswer) {
            newScore++
        }
        
        _state.value = _state.value.copy(
            isAnswerValidated = true,
            score = newScore
        )
    }

    fun goToNext() {
        if (_state.value.currentQuestionIndex < _state.value.questions.size - 1) {
            _state.value = _state.value.copy(
                currentQuestionIndex = _state.value.currentQuestionIndex + 1,
                isAnswerValidated = false
            )
        }
    }

    fun goToPrevious() {
        if (_state.value.currentQuestionIndex > 0) {
            val prevIndex = _state.value.currentQuestionIndex - 1
            val prevQuestion = _state.value.questions[prevIndex]
            val hasAnswer = _state.value.selectedAnswers.containsKey(prevQuestion.id)
            
            _state.value = _state.value.copy(
                currentQuestionIndex = prevIndex,
                isAnswerValidated = hasAnswer
            )
        }
    }

    fun jumpToQuestion(index: Int) {
        if (index in _state.value.questions.indices) {
            val targetQuestion = _state.value.questions[index]
            val hasAnswer = _state.value.selectedAnswers.containsKey(targetQuestion.id)
            
            _state.value = _state.value.copy(
                currentQuestionIndex = index,
                isAnswerValidated = hasAnswer
            )
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        
        val questions = _state.value.questions
        val selectedAnswers = _state.value.selectedAnswers
        
        var correct = 0
        var wrong = 0
        questions.forEach { q ->
            val answer = selectedAnswers[q.id]
            if (answer != null) {
                if (answer == q.correctAnswer) correct++ else wrong++
            }
        }
        
        val totalQuestions = questions.size
        val unanswered = totalQuestions - (correct + wrong)
        val scorePercentage = if (totalQuestions > 0) (correct.toFloat() / totalQuestions * 100).toInt() else 0
        
        QuizRepository.saveQuizResult(
            categoryId = _state.value.categoryId,
            scorePercentage = scorePercentage,
            correctAnswers = correct,
            wrongAnswers = wrong,
            unansweredQuestions = unanswered,
            totalQuestions = totalQuestions
        )

        _state.value = _state.value.copy(isSubmitted = true)
    }
}

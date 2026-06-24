package com.example.data

import androidx.room.*

@Entity(tableName = "pro_exams")
data class ProExam(
    @PrimaryKey val examId: String,
    val title: String,
    val description: String,
    val subject: String,
    val assignedBatches: String, // Comma separated batch IDs
    val startTimestamp: Long,
    val endTimestamp: Long,
    val durationMinutes: Int,
    val totalMarks: Int,
    val negativeMarking: Float = 0f,
    val marksPerQuestion: Float = 1f,
    val isStrictMode: Boolean = true,
    val maxLeavesAllowed: Int = 3,
    val status: String = "Published"
)

@Entity(tableName = "pro_questions")
data class ProQuestion(
    @PrimaryKey val questionId: String,
    val examId: String,
    val questionText: String,
    val explanation: String? = null,
    val difficulty: String = "Medium"
)

@Entity(tableName = "pro_question_options")
data class ProQuestionOption(
    @PrimaryKey val optionId: String,
    val questionId: String,
    val optionText: String,
    val isCorrect: Boolean
)

@Entity(tableName = "pro_exam_attempts")
data class ProExamAttempt(
    @PrimaryKey val attemptId: String,
    val userId: String,
    val examId: String,
    val startTime: Long,
    var submitTime: Long? = null,
    var status: String = "in_progress", // in_progress, submitted, auto_submitted
    var violations: Int = 0,
    var isSynced: Boolean = false
)

enum class ProExamStatus {
    Draft,
    Scheduled,
    Live,
    Completed
}

@Entity(tableName = "pro_attempt_answers")
data class ProAttemptAnswer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attemptId: String,
    val questionId: String,
    val selectedOptionId: String?,
    val timestamp: Long = System.currentTimeMillis()
)

fun ProExam.getStatus(): ProExamStatus {
    val now = System.currentTimeMillis()
    return when {
        now < startTimestamp -> ProExamStatus.Scheduled
        now in startTimestamp..endTimestamp -> ProExamStatus.Live
        else -> ProExamStatus.Completed
    }
}

@Entity(tableName = "pro_exam_drafts")
data class ProExamDraft(
    @PrimaryKey val draftId: String,
    val title: String,
    val description: String,
    val subject: String,
    val assignedBatches: String = "",
    val durationMinutes: Int = 60,
    val totalMarks: Int = 0,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "pro_question_drafts")
data class ProQuestionDraft(
    @PrimaryKey val questionId: String,
    val draftId: String,
    val questionText: String,
    val explanation: String? = null
)

@Entity(tableName = "pro_option_drafts")
data class ProQuestionOptionDraft(
    @PrimaryKey val optionId: String,
    val questionId: String,
    val optionText: String,
    val isCorrect: Boolean
)

@Dao
interface ProExamDao {
    @Query("SELECT * FROM pro_exams WHERE status = 'Published'")
    suspend fun getAllPublishedExams(): List<ProExam>

    @Query("SELECT * FROM pro_exams WHERE examId = :examId")
    suspend fun getExamById(examId: String): ProExam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ProExam)

    @Query("SELECT * FROM pro_questions WHERE examId = :examId")
    suspend fun getQuestionsForExam(examId: String): List<ProQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: ProQuestion)

    @Query("SELECT * FROM pro_question_options WHERE questionId = :questionId")
    suspend fun getOptionsForQuestion(questionId: String): List<ProQuestionOption>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOption(option: ProQuestionOption)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ProExamAttempt)

    @Update
    suspend fun updateAttempt(attempt: ProExamAttempt)

    @Query("SELECT * FROM pro_exam_attempts WHERE userId = :userId AND examId = :examId")
    suspend fun getAttempt(userId: String, examId: String): ProExamAttempt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAnswer(answer: ProAttemptAnswer)

    @Query("SELECT * FROM pro_attempt_answers WHERE attemptId = :attemptId")
    suspend fun getAnswersForAttempt(attemptId: String): List<ProAttemptAnswer>

    // Draft Management
    @Query("SELECT * FROM pro_exam_drafts ORDER BY lastModified DESC")
    suspend fun getAllDrafts(): List<ProExamDraft>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: ProExamDraft)

    @Query("DELETE FROM pro_exam_drafts WHERE draftId = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Query("SELECT * FROM pro_question_drafts WHERE draftId = :draftId")
    suspend fun getDraftQuestions(draftId: String): List<ProQuestionDraft>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftQuestion(question: ProQuestionDraft)

    @Query("SELECT * FROM pro_option_drafts WHERE questionId = :questionId")
    suspend fun getDraftOptions(questionId: String): List<ProQuestionOptionDraft>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftOption(option: ProQuestionOptionDraft)
}

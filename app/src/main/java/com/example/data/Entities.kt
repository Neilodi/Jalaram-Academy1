package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String, // e.g. ADM-001, STU-1234
    val name: String,
    val role: String, // Admin, Teacher, Student
    val mobile: String,
    val parentMobile: String? = null,
    val batch: String? = null, // e.g. Class 10-A, JEE Mains
    val subjects: String? = null, // Comma separated
    val pin: String,
    val status: String, // Pending, Active, Suspended
    val suspensionDurationDays: Int = 0,
    val suspensionStartDate: Long = 0,
    val avatar: String = "U",
    val isBiometricEnabled: Boolean = false
) : java.io.Serializable

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Int,
    val rating: Double,
    val videoUrl: String,
    val category: String
) : java.io.Serializable

@Entity(tableName = "batches")
data class AcademyBatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val teacherId: String? = null
) : java.io.Serializable

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey val id: String, // e.g. EXM-101
    val title: String,
    val batch: String,
    val durationMinutes: Int,
    val questionsJson: String // Format: Q1||Opt1~~Opt2~~Opt3~~Opt4||CorrectIndex###Q2||Opt1~~Opt2~~Opt3~~Opt4||CorrectIndex
) : java.io.Serializable

@Entity(tableName = "results")
data class ResultRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examId: String,
    val examTitle: String,
    val studentId: String,
    val studentName: String,
    val score: Int,
    val totalQuestions: Int,
    val dateString: String,
    val isAbsent: Boolean = false
) : java.io.Serializable

data class GradeEntryInput(
    val studentId: String,
    val studentName: String,
    val score: Int,
    val isAbsent: Boolean
)

@Entity(tableName = "notifications")
data class SystemNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val dateString: String,
    val type: String // Info, Warning, Success
) : java.io.Serializable

data class Question(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int
) {
    companion object {
        fun serializeList(questions: List<Question>): String {
            return questions.joinToString("###") { q ->
                "${q.id}||${q.questionText}||${q.options.joinToString("~~")}||${q.correctIndex}"
            }
        }

        fun deserializeList(serialized: String): List<Question> {
            if (serialized.isEmpty()) return emptyList()
            return serialized.split("###").mapNotNull { part ->
                try {
                    val segments = part.split("||")
                    if (segments.size >= 4) {
                        Question(
                            id = segments[0].toInt(),
                            questionText = segments[1],
                            options = segments[2].split("~~"),
                            correctIndex = segments[3].toInt()
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,       // e.g. "ADM-001", "STU-101"
    val senderName: String,     // e.g. "Ravi Patel"
    val senderRole: String,     // "Admin", "Teacher", "Student"
    val channelType: String,    // "Announcement" or "Subject"
    val channelSubject: String, // "All" (if Announcement), or "Physics", "Maths", etc.
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable

@Entity(tableName = "exam_schedules")
data class ExamSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,        // e.g. "Physics", "Maths", "Chemistry", "Biology", "English"
    val batch: String,          // e.g. "Grade 10-A", "JEE Mains"
    val examDate: String,       // e.g. "2026-06-30" (YYYY-MM-DD)
    val timeSlot: String,       // e.g. "09:00 AM - 12:00 PM", "02:00 PM - 05:00 PM"
    val examinerName: String,   // e.g. "Dr. Shalini Vyas"
    val location: String,       // e.g. "Main Examination Hall", "Room 303"
    val durationMinutes: Int = 180,
    val isAlertEnabled: Boolean = false // user's visual setting for automated local reminder
) : java.io.Serializable

@Entity(tableName = "live_classes")
data class LiveClassSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val batch: String,
    val topic: String,
    val jitsiLink: String,
    val startedBy: String,
    val isLive: Boolean = true,
    val startTime: Long = System.currentTimeMillis()
) : java.io.Serializable

@Entity(tableName = "lecture_materials")
data class LectureMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val topic: String,
    val materialType: String, // "PDF Notes", "Formula Sheet", "Video Lecture", "Assignment", "Slides"
    val fileUrl: String,
    val uploadDate: String,
    val uploadedBy: String,
    val fileSize: String,
    val description: String
) : java.io.Serializable

@Entity(tableName = "course_enrollments")
data class CourseEnrollment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val batchId: Int
) : java.io.Serializable

@Entity(tableName = "assignment_deadlines")
data class AssignmentDeadline(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val deadlineDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = false
) : java.io.Serializable



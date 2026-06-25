package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Course::class,
        AcademyBatch::class,
        Exam::class,
        ResultRecord::class,
        SystemNotification::class,
        ChatMessage::class,
        ExamSchedule::class,
        LiveClassSession::class,
        LectureMaterial::class,
        CourseEnrollment::class,
        AssignmentDeadline::class,
        ProExam::class,
        ProQuestion::class,
        ProQuestionOption::class,
        ProExamAttempt::class,
        ProAttemptAnswer::class,
        ProExamDraft::class,
        ProQuestionDraft::class,
        ProQuestionOptionDraft::class,
        AttendanceRecord::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun batchDao(): AcademyBatchDao
    abstract fun examDao(): ExamDao
    abstract fun resultDao(): ResultDao
    abstract fun notificationDao(): NotificationDao
    abstract fun chatDao(): ChatDao
    abstract fun examScheduleDao(): ExamScheduleDao
    abstract fun liveClassDao(): LiveClassDao
    abstract fun lectureMaterialDao(): LectureMaterialDao
    abstract fun enrollmentDao(): CourseEnrollmentDao
    abstract fun deadlineDao(): AssignmentDeadlineDao
    abstract fun proExamDao(): ProExamDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jalaram_academy_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE userId = :id OR mobile = :id LIMIT 1")
    suspend fun findUserByIdOrMobile(id: String): User?

    @Query("SELECT * FROM users WHERE (userId = :id OR mobile = :id) AND role = :role LIMIT 1")
    suspend fun findUserByIdOrMobileAndRole(id: String, role: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateUserStatus(userId: String, status: String)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id DESC")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY id DESC")
    suspend fun getAllCourses(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: Int)
}

@Dao
interface AcademyBatchDao {
    @Query("SELECT * FROM batches")
    fun getAllBatchesFlow(): Flow<List<AcademyBatch>>

    @Query("SELECT * FROM batches")
    suspend fun getAllBatches(): List<AcademyBatch>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: AcademyBatch)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatch(id: Int)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams")
    fun getAllExamsFlow(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE batch = :batch")
    fun getExamsForBatchFlow(batch: String): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :examId")
    suspend fun deleteExam(examId: String)
}

@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY id DESC")
    fun getAllResultsFlow(): Flow<List<ResultRecord>>

    @Query("SELECT * FROM results WHERE studentId = :studentId ORDER BY id DESC")
    fun getResultsForStudentFlow(studentId: String): Flow<List<ResultRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultRecord)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY id DESC")
    fun getAllNotificationsFlow(): Flow<List<SystemNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SystemNotification)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE channelType = :channelType AND channelSubject = :channelSubject ORDER BY timestamp ASC")
    fun getMessagesForChannelFlow(channelType: String, channelSubject: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface ExamScheduleDao {
    @Query("SELECT * FROM exam_schedules ORDER BY examDate ASC")
    fun getAllSchedulesFlow(): Flow<List<ExamSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ExamSchedule): Long

    @Query("UPDATE exam_schedules SET isAlertEnabled = :enabled WHERE id = :id")
    suspend fun updateAlertEnabled(id: Int, enabled: Boolean)

    @Query("DELETE FROM exam_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: Int)

    @Query("DELETE FROM exam_schedules")
    suspend fun clearAllSchedules()
}

@Dao
interface LiveClassDao {
    @Query("SELECT * FROM live_classes WHERE isLive = 1 ORDER BY startTime DESC")
    fun getActiveLiveClassesFlow(): Flow<List<LiveClassSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(session: LiveClassSession): Long

    @Query("UPDATE live_classes SET isLive = 0 WHERE id = :id")
    suspend fun stopLiveClass(id: Int)

    @Query("DELETE FROM live_classes WHERE id = :id")
    suspend fun deleteLiveClass(id: Int)

    @Query("DELETE FROM live_classes")
    suspend fun clearAllLiveClasses()
}

@Dao
interface LectureMaterialDao {
    @Query("SELECT * FROM lecture_materials ORDER BY id DESC")
    fun getAllMaterialsFlow(): Flow<List<LectureMaterial>>

    @Query("SELECT * FROM lecture_materials WHERE subject = :subject ORDER BY id DESC")
    fun getMaterialsBySubjectFlow(subject: String): Flow<List<LectureMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: LectureMaterial): Long

    @Query("DELETE FROM lecture_materials WHERE id = :id")
    suspend fun deleteMaterial(id: Int)

    @Query("DELETE FROM lecture_materials")
    suspend fun clearAllMaterials()
}



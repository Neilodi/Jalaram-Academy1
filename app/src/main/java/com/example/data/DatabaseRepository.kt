package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseRepository(private val db: AppDatabase) {
    val usersFlow: Flow<List<User>> = db.userDao().getAllUsersFlow()
    val coursesFlow: Flow<List<Course>> = db.courseDao().getAllCoursesFlow()
    val batchesFlow: Flow<List<AcademyBatch>> = db.batchDao().getAllBatchesFlow()
    val examsFlow: Flow<List<Exam>> = db.examDao().getAllExamsFlow()
    val resultsFlow: Flow<List<ResultRecord>> = db.resultDao().getAllResultsFlow()
    val notificationsFlow: Flow<List<SystemNotification>> = db.notificationDao().getAllNotificationsFlow()

    suspend fun findUserByIdOrMobile(id: String): User? = db.userDao().findUserByIdOrMobile(id)

    suspend fun findUserByIdOrMobileAndRole(id: String, role: String): User? =
        db.userDao().findUserByIdOrMobileAndRole(id, role)

    suspend fun insertUser(user: User) = db.userDao().insertUser(user)

    suspend fun updateUser(user: User) = db.userDao().updateUser(user)

    suspend fun updateUserStatus(userId: String, status: String) = db.userDao().updateUserStatus(userId, status)

    suspend fun deleteUser(userId: String) = db.userDao().deleteUser(userId)

    suspend fun insertCourse(course: Course) = db.courseDao().insertCourse(course)

    suspend fun deleteCourse(id: Int) = db.courseDao().deleteCourse(id)

    suspend fun insertBatch(batch: AcademyBatch) = db.batchDao().insertBatch(batch)

    suspend fun deleteBatch(id: Int) = db.batchDao().deleteBatch(id)

    suspend fun getAllUsers(): List<User> = db.userDao().getAllUsers()

    suspend fun getAllBatches(): List<AcademyBatch> = db.batchDao().getAllBatches()

    suspend fun insertExam(exam: Exam) = db.examDao().insertExam(exam)

    suspend fun deleteExam(examId: String) = db.examDao().deleteExam(examId)

    suspend fun insertResult(result: ResultRecord) = db.resultDao().insertResult(result)

    fun getResultsForStudentFlow(studentId: String): Flow<List<ResultRecord>> =
        db.resultDao().getResultsForStudentFlow(studentId)

    suspend fun insertNotification(notification: SystemNotification) =
        db.notificationDao().insertNotification(notification)

    suspend fun deleteNotification(id: Int) = db.notificationDao().deleteNotification(id)

    val allMessagesFlow: Flow<List<ChatMessage>> = db.chatDao().getAllMessagesFlow()

    fun getMessagesForChannelFlow(channelType: String, channelSubject: String): Flow<List<ChatMessage>> =
        db.chatDao().getMessagesForChannelFlow(channelType, channelSubject)

    suspend fun insertChatMessage(message: ChatMessage) = db.chatDao().insertMessage(message)

    suspend fun clearAllChatMessages() = db.chatDao().clearAllMessages()

    val allSchedulesFlow: Flow<List<ExamSchedule>> = db.examScheduleDao().getAllSchedulesFlow()

    suspend fun insertExamSchedule(schedule: ExamSchedule): Long = db.examScheduleDao().insertSchedule(schedule)

    suspend fun updateScheduleAlertEnabled(id: Int, enabled: Boolean) = db.examScheduleDao().updateAlertEnabled(id, enabled)

    suspend fun deleteExamSchedule(id: Int) = db.examScheduleDao().deleteSchedule(id)

    suspend fun clearAllExamSchedules() = db.examScheduleDao().clearAllSchedules()

    val activeLiveClassesFlow: Flow<List<LiveClassSession>> = db.liveClassDao().getActiveLiveClassesFlow()

    suspend fun insertLiveClass(session: LiveClassSession): Long = db.liveClassDao().insertLiveClass(session)

    suspend fun stopLiveClass(id: Int) = db.liveClassDao().stopLiveClass(id)

    suspend fun deleteLiveClass(id: Int) = db.liveClassDao().deleteLiveClass(id)

    suspend fun clearAllLiveClasses() = db.liveClassDao().clearAllLiveClasses()

    val lectureMaterialsFlow: Flow<List<LectureMaterial>> = db.lectureMaterialDao().getAllMaterialsFlow()

    fun getEnrollmentsFlow(): Flow<List<CourseEnrollment>> = db.enrollmentDao().getAllEnrollmentsFlow()

    suspend fun insertEnrollment(enrollment: CourseEnrollment) = db.enrollmentDao().insertEnrollment(enrollment)

    suspend fun deleteEnrollment(studentId: String, batchId: Int) = db.enrollmentDao().deleteEnrollment(studentId, batchId)

    val deadlinesFlow: Flow<List<AssignmentDeadline>> = db.deadlineDao().getAllDeadlinesFlow()

    suspend fun insertDeadline(deadline: AssignmentDeadline) = db.deadlineDao().insertDeadline(deadline)

    suspend fun deleteDeadline(id: Int) = db.deadlineDao().deleteDeadline(id)

    fun getLectureMaterialsBySubjectFlow(subject: String): Flow<List<LectureMaterial>> =
        db.lectureMaterialDao().getMaterialsBySubjectFlow(subject)

    suspend fun insertLectureMaterial(material: LectureMaterial): Long =
        db.lectureMaterialDao().insertMaterial(material)

    suspend fun deleteLectureMaterial(id: Int) =
        db.lectureMaterialDao().deleteMaterial(id)

    suspend fun clearAllLectureMaterials() =
        db.lectureMaterialDao().clearAllMaterials()

    suspend fun seedDatabaseIfNeeded() {
        val allUsers = db.userDao().getAllUsers()
        if (allUsers.isNotEmpty()) return

        // Seed Users
        val admin = User(
            userId = "ADM-001",
            name = "System Administrator",
            role = "Admin",
            mobile = "9999999999",
            pin = "1234",
            status = "Active",
            avatar = "A"
        )
        val student = User(
            userId = "STU-101",
            name = "Ravi Patel",
            role = "Student",
            mobile = "9900881122",
            parentMobile = "9844221100",
            batch = "JEE Mains",
            pin = "1234",
            status = "Active",
            avatar = "R"
        )
        val teacher = User(
            userId = "TCH-201",
            name = "Dr. Shalini Vyas",
            role = "Teacher",
            mobile = "9876543210",
            subjects = "Physics, Maths",
            pin = "1234",
            status = "Active",
            avatar = "S"
        )
        val head = User(
            userId = "HED-001",
            name = "Head Controller",
            role = "Head",
            mobile = "8888888888",
            pin = "1234",
            status = "Active",
            avatar = "H"
        )
        db.userDao().insertUser(admin)
        db.userDao().insertUser(student)
        db.userDao().insertUser(teacher)
        db.userDao().insertUser(head)

        // Seed Batches
        db.batchDao().insertBatch(AcademyBatch(name = "Class 10-A", teacherId = "TCH-201"))
        db.batchDao().insertBatch(AcademyBatch(name = "Class 12-B"))
        db.batchDao().insertBatch(AcademyBatch(name = "NEET Droppers"))
        db.batchDao().insertBatch(AcademyBatch(name = "JEE Mains", teacherId = "TCH-201"))

        // Seed Courses
        db.courseDao().insertCourse(
            Course(
                title = "Physics Masterclass",
                description = "Complete syllabus coverage for JEE Advanced and National Olympiads.",
                price = 4999,
                rating = 4.8,
                videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                category = "Physics"
            )
        )
        db.courseDao().insertCourse(
            Course(
                title = "Organic Chemistry",
                description = "Master reaction mechanisms, synthesis, and aromatic compounds.",
                price = 3499,
                rating = 4.5,
                videoUrl = "https://www.w3schools.com/html/movie.mp4",
                category = "Chemistry"
            )
        )
        db.courseDao().insertCourse(
            Course(
                title = "Calculus Essentials",
                description = "Differential and integral calculus from modern graphical concepts.",
                price = 2999,
                rating = 4.7,
                videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                category = "Maths"
            )
        )

        // Seed Exams
        val physicsQuestions = listOf(
            Question(1, "What is the dimensional formula of universal gravitational constant G?", listOf("M⁻¹ L³ T⁻²", "M¹ L² T⁻¹", "M⁻² L³ T⁻²", "M⁻¹ L² T⁻³"), 0),
            Question(2, "A body starts from rest and moves with uniform acceleration. If it covers distance s in time t, what is the acceleration?", listOf("2s/t²", "s/t²", "s / 2t²", "2s/t"), 0),
            Question(3, "Which of the following is a scalar quantity?", listOf("Velocity", "Acceleration", "Work", "Force"), 2)
        )
        val chemistryQuestions = listOf(
            Question(1, "Which element is the fundamental building block of organic chemistry?", listOf("Hydrogen", "Carbon", "Oxygen", "Nitrogen"), 1),
            Question(2, "What is the pH of pure water at 25°C?", listOf("5", "7", "9", "14"), 1),
            Question(3, "Which gas is known as Laughing Gas?", listOf("Nitric oxide", "Nitrous oxide", "Nitrogen pentoxide", "Nitrogen dioxide"), 1)
        )

        db.examDao().insertExam(
            Exam(
                id = "EXM-101",
                title = "Physics Diagnostic MCQ",
                batch = "JEE Mains",
                durationMinutes = 30,
                questionsJson = Question.serializeList(physicsQuestions)
            )
        )
        db.examDao().insertExam(
            Exam(
                id = "EXM-102",
                title = "Chemistry Organic Quiz",
                batch = "JEE Mains",
                durationMinutes = 15,
                questionsJson = Question.serializeList(chemistryQuestions)
            )
        )

        // Seed Notifications
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = sdf.format(Date())

        db.notificationDao().insertNotification(
            SystemNotification(
                title = "Exam Schedule Released",
                content = "Final Board and and mock entrance examinations start from March 15th. Check datesheets under Exams.",
                dateString = currentDate,
                type = "Info"
            )
        )
        db.notificationDao().insertNotification(
            SystemNotification(
                title = "Fee Submission Quarter 4",
                content = "Last date of submission of Academy Quarter 4 Tuition & Lab fee is February 28th.",
                dateString = currentDate,
                type = "Warning"
            )
        )
        db.notificationDao().insertNotification(
            SystemNotification(
                title = "Live Lectures Notice",
                content = "Physics class with Dr. Shalini Vyas is scheduled via the Live Classroom portals.",
                dateString = currentDate,
                type = "Success"
            )
        )

        // Seed Chat Messages
        val now = System.currentTimeMillis()
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "ADM-001",
                senderName = "System Administrator",
                senderRole = "Admin",
                channelType = "Announcement",
                channelSubject = "All",
                messageText = "Welcome to the Jalaram Academy central announcement board! All general updates, holiday schedules, and official notices will be published in this secure space.",
                timestamp = now - 3600000 * 6 // 6 hours ago
            )
        )
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "TCH-201",
                senderName = "Dr. Shalini Vyas",
                senderRole = "Teacher",
                channelType = "Announcement",
                channelSubject = "All",
                messageText = "Hello everyone, please note that the results for the recent Physics Diagnostic MCQ have been published under the Performance tab. Great job, everyone!",
                timestamp = now - 3600000 * 3 // 3 hours ago
            )
        )

        // Subject Chats: Physics
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "TCH-201",
                senderName = "Dr. Shalini Vyas",
                senderRole = "Teacher",
                channelType = "Subject",
                channelSubject = "Physics",
                messageText = "Welcome to the Physics discussion forum! For tonight's homework, please review the kinematic equations. We will solve circular motion problems in tomorrow's live class.",
                timestamp = now - 3600000 * 5 // 5 hours ago
            )
        )
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "STU-101",
                senderName = "Ravi Patel",
                senderRole = "Student",
                channelType = "Subject",
                channelSubject = "Physics",
                messageText = "Understood ma'am. Should we also attempt the numerical exercise on page 42 of the reference textbook?",
                timestamp = now - 3600000 * 4 // 4 hours ago
            )
        )
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "TCH-201",
                senderName = "Dr. Shalini Vyas",
                senderRole = "Teacher",
                channelType = "Subject",
                channelSubject = "Physics",
                messageText = "Yes, Ravi! Solve questions 1 to 5. They will serve as excellent practice for your conceptual understandings before the lecture.",
                timestamp = now - 3600000 * 2 // 2 hours ago
            )
        )

        // Subject Chats: Maths
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "TCH-201",
                senderName = "Dr. Shalini Vyas",
                senderRole = "Teacher",
                channelType = "Subject",
                channelSubject = "Maths",
                messageText = "To all students in JEE Mains: I have uploaded the Calculus Essentials notes. Please download the PDF and check the solved problems.",
                timestamp = now - 3600000 * 4 // 4 hours ago
            )
        )
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "STU-101",
                senderName = "Ravi Patel",
                senderRole = "Student",
                channelType = "Subject",
                channelSubject = "Maths",
                messageText = "Thank you so much! The step-by-step limits diagrams in the notes are extremely clear and helpful.",
                timestamp = now - 3600000 * 3 // 3 hours ago
            )
        )

        // Subject Chats: Chemistry
        db.chatDao().insertMessage(
            ChatMessage(
                senderId = "ADM-001",
                senderName = "System Administrator",
                senderRole = "Admin",
                channelType = "Subject",
                channelSubject = "Chemistry",
                messageText = "Chemistry discussion group created. Only assigned students, Chemistry teachers, and administrators have secure access here.",
                timestamp = now - 3600000 * 5 // 5 hours ago
            )
        )

        // Seed Exam Schedules
        db.examScheduleDao().insertSchedule(
            ExamSchedule(
                subject = "Physics",
                batch = "Grade 10-A",
                examDate = "2026-06-25",
                timeSlot = "09:00 AM - 12:00 PM",
                examinerName = "Dr. Shalini Vyas",
                location = "Main Examination Hall (A)",
                durationMinutes = 180,
                isAlertEnabled = true
            )
        )
        db.examScheduleDao().insertSchedule(
            ExamSchedule(
                subject = "Maths",
                batch = "JEE Mains",
                examDate = "2026-06-27",
                timeSlot = "02:00 PM - 05:00 PM",
                examinerName = "Dr. Shalini Vyas",
                location = "Main Examination Hall (B)",
                durationMinutes = 180,
                isAlertEnabled = false
            )
        )
        db.examScheduleDao().insertSchedule(
            ExamSchedule(
                subject = "Chemistry",
                batch = "Grade 10-A",
                examDate = "2026-06-29",
                timeSlot = "09:00 AM - 12:00 PM",
                examinerName = "System Administrator",
                location = "Chemistry Lab 1",
                durationMinutes = 180,
                isAlertEnabled = false
            )
        )

        // Seed Lecture Materials & Course Notes
        db.lectureMaterialDao().insertMaterial(
            LectureMaterial(
                title = "Kinematics & Projectile Motion Revision Sheet",
                subject = "Physics",
                topic = "Kinematics",
                materialType = "PDF Notes",
                fileUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                uploadDate = "2026-06-21",
                uploadedBy = "Dr. Shalini Vyas",
                fileSize = "1.8 MB",
                description = "All core kinematics equations, projectile trajectory derivations, and multi-concept practice questions for final revision."
            )
        )
        db.lectureMaterialDao().insertMaterial(
            LectureMaterial(
                title = "Definite & Indefinite Integrals Cheat Sheet",
                subject = "Maths",
                topic = "Calculus",
                materialType = "Formula Sheet",
                fileUrl = "https://www.w3schools.com/html/movie.mp4",
                uploadDate = "2026-06-22",
                uploadedBy = "Dr. Shalini Vyas",
                fileSize = "840 KB",
                description = "Comprehensive standard integrals, integration by parts, substitution techniques, and trigonometric reduction formulas."
            )
        )
        db.lectureMaterialDao().insertMaterial(
            LectureMaterial(
                title = "Periodic Trends & Chemical Bonding Lecture Slides",
                subject = "Chemistry",
                topic = "Inorganic Chemistry",
                materialType = "Slides",
                fileUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                uploadDate = "2026-06-23",
                uploadedBy = "System Administrator",
                fileSize = "4.2 MB",
                description = "Class slides covering atomic radius variations, ionization potential, electron gain enthalpy, and VSEPR theory shapes."
            )
        )
        db.lectureMaterialDao().insertMaterial(
            LectureMaterial(
                title = "Cell Structure & Organelles Microscopic Guide",
                subject = "Biology",
                topic = "Cytology",
                materialType = "PDF Notes",
                fileUrl = "https://www.w3schools.com/html/movie.mp4",
                uploadDate = "2026-06-20",
                uploadedBy = "Dr. Shalini Vyas",
                fileSize = "3.1 MB",
                description = "Handwritten revision notes summarizing differences between animal and plant cells, plastid functions, and ribosomes."
            )
        )
    }
}

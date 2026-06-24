package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.PhoneAuthProvider
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ErpViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = DatabaseRepository(database)
    private val authRepository = AuthRepository()
    private val sessionManager = SessionManager(application)

    // Domain lists
    val usersList = repository.usersFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val coursesList = repository.coursesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val batchesList = repository.batchesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val examsList = repository.examsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val resultsList = repository.resultsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notificationsList = repository.notificationsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val chatMessagesList = repository.allMessagesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val examSchedulesList = repository.allSchedulesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeLiveClasses = repository.activeLiveClassesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lectureMaterialsList = repository.lectureMaterialsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deadlinesList = repository.deadlinesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val enrollmentsList = repository.getEnrollmentsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication States
    private val _realCurrentUser = MutableStateFlow<User?>(null)

    private val _headDeviceCount = MutableStateFlow(0)
    val headDeviceCount: StateFlow<Int> = _headDeviceCount.asStateFlow()

    private val _headDeviceLimit = MutableStateFlow(3)
    val headDeviceLimit: StateFlow<Int> = _headDeviceLimit.asStateFlow()

    fun setHeadDeviceCount(count: Int) {
        _headDeviceCount.value = count.coerceAtLeast(0)
    }

    fun setHeadDeviceLimit(limit: Int) {
        _headDeviceLimit.value = limit.coerceAtLeast(1)
    }

    val currentUser: StateFlow<User?> = _realCurrentUser.asStateFlow()

    private val _tempUser = MutableStateFlow<User?>(null)
    val tempUser: StateFlow<User?> = _tempUser.asStateFlow()

    private val _generatedOtp = MutableStateFlow<Int?>(null)
    val generatedOtp: StateFlow<Int?> = _generatedOtp.asStateFlow()

    private val _showOtpVerification = MutableStateFlow(false)
    val showOtpVerification: StateFlow<Boolean> = _showOtpVerification.asStateFlow()

    // Navigation and Tab states
    private val _currentTab = MutableStateFlow("dashboard")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Live Classroom States
    private val _liveClassActive = MutableStateFlow(false)
    val liveClassActive: StateFlow<Boolean> = _liveClassActive.asStateFlow()

    private val _currentLiveSession = MutableStateFlow<LiveClassSession?>(null)
    val currentLiveSession: StateFlow<LiveClassSession?> = _currentLiveSession.asStateFlow()

    // Examination Engine States
    private val _activeExam = MutableStateFlow<Exam?>(null)
    val activeExam: StateFlow<Exam?> = _activeExam.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // QuestId -> OptionIdx
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _examTimeRemaining = MutableStateFlow(0) // seconds remaining
    val examTimeRemaining: StateFlow<Int> = _examTimeRemaining.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private var examJob: Job? = null

    private val prefs = application.getSharedPreferences("erp_prefs", android.content.Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
            
            // Device-bound persistent session check
            if (sessionManager.isLoggedIn() && sessionManager.isDeviceBound()) {
                val savedUser = sessionManager.getUserSession()
                if (savedUser != null && savedUser.status != "Suspended") {
                    _realCurrentUser.value = savedUser
                    if (savedUser.role == "Head") {
                        _currentTab.value = "head_panel"
                    } else {
                        _currentTab.value = "dashboard"
                    }
                }
            }
        }
    }

    // 1. Authentication Controls
    private var otpAttempts = 0

    fun login(userId: String, role: String, pin: String, onBiometricRequested: (com.example.data.User) -> Unit, onOtpRequested: (Int) -> Unit, onError: (String) -> Unit) {
        if (userId.isBlank()) {
            onError("User ID / Mobile cannot be blank")
            return
        }
        if (pin.isBlank()) {
            onError("PIN/Password cannot be blank")
            return
        }
        viewModelScope.launch {
            val user = repository.findUserByIdOrMobileAndRole(userId, role)
            if (user == null) {
                onError("User details not found or role does not match.")
                return@launch
            }
            if (user.pin != pin) {
                onError("Incorrect PIN/Password.")
                return@launch
            }
            when (user.status) {
                "Pending" -> {
                    onError("Account is pending Administrator approval.")
                }
                "Suspended" -> {
                    onError("Account is suspended. Please contact Administration.")
                }
                else -> {
                    _tempUser.value = user
                    if (user.isBiometricEnabled) {
                        onBiometricRequested(user)
                    } else {
                        otpAttempts = 0
                        val code = (1000..9999).random()
                        _generatedOtp.value = code
                        _showOtpVerification.value = true
                        onOtpRequested(code)
                    }
                }
            }
        }
    }

    fun proceedToOtp(onOtpRequested: (Int) -> Unit) {
        otpAttempts = 0
        val code = (1000..9999).random()
        _generatedOtp.value = code
        _showOtpVerification.value = true
        onOtpRequested(code)
    }

    fun completeLoginWithoutOtp() {
        val loggedInUser = _tempUser.value
        _realCurrentUser.value = loggedInUser
        loggedInUser?.let {
            prefs.edit().putString("logged_in_user_id", it.userId).apply()
        }
        if (loggedInUser?.role == "Head") {
            _headDeviceCount.value = (_headDeviceCount.value + 1).coerceAtMost(5)
            _currentTab.value = "head_panel"
        } else {
            _currentTab.value = "dashboard"
        }
        _tempUser.value = null
        _generatedOtp.value = null
        _showOtpVerification.value = false
    }

    fun verifyOtp(otpInput: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val expected = _generatedOtp.value?.toString() ?: ""

        if (otpInput == expected) {
            val loggedInUser = _tempUser.value
            _realCurrentUser.value = loggedInUser
            loggedInUser?.let {
                prefs.edit().putString("logged_in_user_id", it.userId).apply()
            }
            if (loggedInUser?.role == "Head") {
                _headDeviceCount.value = (_headDeviceCount.value + 1).coerceAtMost(5)
                _currentTab.value = "head_panel"
            } else {
                _currentTab.value = "dashboard"
            }
            _tempUser.value = null
            _generatedOtp.value = null
            _showOtpVerification.value = false
            onSuccess()
        } else {
            otpAttempts++
            if (otpAttempts >= 3) {
                cancelOtpFlow()
                onError("Maximum failed attempts reached. Access blocked.")
            } else {
                onError("Invalid security verification code. Try again.")
            }
        }
    }

    fun cancelOtpFlow() {
        _tempUser.value = null
        _generatedOtp.value = null
        _showOtpVerification.value = false
    }

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            val user = authRepository.signInWithEmail(email, pass)
            if (user != null) {
                sessionManager.saveUserSession(user)
                _realCurrentUser.value = user
                _currentTab.value = if (user.role == "Head") "head_panel" else "dashboard"
            }
        }
    }

    fun startPhoneAuth(mobile: String, activity: Activity) {
        authRepository.sendOtp(mobile, activity, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification
                credential.smsCode?.let { verifyOtpFirebase(it) }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Handle error
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                _verificationId.value = id
                _showOtpVerification.value = true
            }
        })
    }

    fun verifyOtpFirebase(otp: String) {
        val id = _verificationId.value ?: return
        viewModelScope.launch {
            val user = authRepository.signInWithOtp(id, otp)
            if (user != null) {
                sessionManager.saveUserSession(user)
                _realCurrentUser.value = user
                _currentTab.value = if (user.role == "Head") "head_panel" else "dashboard"
                _showOtpVerification.value = false
            }
        }
    }

    fun logout() {
        authRepository.signOut()
        sessionManager.logout()
        _realCurrentUser.value = null
        _currentTab.value = "dashboard"
        stopLiveClass()
        cancelExam()
    }

    // 2. User registration
    fun registerNewUser(
        name: String,
        role: String,
        mobile: String,
        parentMobile: String?,
        batch: String?,
        subjects: String?,
        pin: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.trim().length < 3) {
            onError("Please enter a valid Full Name (min 3 chars)")
            return
        }
        
        val isStudent = role == "Student"
        val mobileClean = mobile.trim()
        
        if (isStudent) {
            // Student personal number is optional. If provided, must be valid 10 digits
            if (mobileClean.isNotEmpty()) {
                if (mobileClean.length != 10 || !mobileClean.all { it.isDigit() }) {
                    onError("Invalid Student personal mobile number (requires 10 digits)")
                    return
                }
            }
            
            // Parent numbers are required, could be multiple comma-separated
            if (parentMobile.isNullOrBlank()) {
                onError("Please provide at least one Parent Contact Number")
                return
            } else {
                val parents = parentMobile.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (parents.isEmpty()) {
                    onError("Please provide at least one Parent Contact Number")
                    return
                }
                parents.forEach { p ->
                    if (p.length != 10 || !p.all { it.isDigit() }) {
                        onError("Each Parent mobile number must be exactly 10 digits: $p")
                        return
                    }
                }
            }
        } else {
            // Teacher / Admin: mobile is required and must be 10 digits
            if (mobileClean.length != 10 || !mobileClean.all { it.isDigit() }) {
                onError("Invalid mobile number (requires 10 digits)")
                return
            }
        }

        viewModelScope.launch {
            if (mobileClean.isNotEmpty()) {
                val existing = repository.findUserByIdOrMobile(mobileClean)
                if (existing != null) {
                    if (existing.status == "Pending") {
                        // Pre-added/invited user completing registration - activate and auto-login!
                        val updatedUser = existing.copy(
                            name = name,
                            role = role,
                            pin = pin,
                            status = "Active",
                            parentMobile = if (role == "Student") parentMobile else null,
                            batch = if (role == "Student") batch else null,
                            subjects = if (role == "Teacher") subjects else null
                        )
                        repository.insertUser(updatedUser)
                        _realCurrentUser.value = updatedUser
                        onSuccess(updatedUser.userId)
                        return@launch
                    } else {
                        onError("Mobile number already in use by student or parent.")
                        return@launch
                    }
                }
            }

            val prefix = when (role) {
                "Student" -> "STU"
                "Teacher" -> "TCH"
                else -> "ADM"
            }
            val randomId = (1000..9999).random()
            val newUserId = "$prefix-$randomId"

            val newUser = User(
                userId = newUserId,
                name = name,
                role = role,
                mobile = mobileClean,
                parentMobile = if (role == "Student") parentMobile else null,
                batch = if (role == "Student") batch else null,
                subjects = if (role == "Teacher") subjects else null,
                pin = pin,
                status = "Pending"
            )

            repository.insertUser(newUser)
            onSuccess(newUserId)
        }
    }

    fun addNewSubjectCourse(subject: String) {
        viewModelScope.launch {
            val title = "$subject Masterclass"
            val category = subject.trim()
            val newCourse = Course(
                title = title,
                description = "Comprehensive study material and video lectures on $category.",
                price = 2499,
                rating = 4.8,
                videoUrl = "https://www.w3schools.com/html/mov_bbb.mp4",
                category = category
            )
            repository.insertCourse(newCourse)
            createNotification(
                title = "New Course Added",
                content = "A new subject domain '$category' has been registered in the course library.",
                type = "System"
            )
        }
    }

    fun suspendUser(userId: String, days: Int) {
        viewModelScope.launch {
            val user = repository.findUserByIdOrMobile(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    status = "Suspended",
                    suspensionDurationDays = days,
                    suspensionStartDate = System.currentTimeMillis()
                )
                repository.updateUser(updatedUser)
            }
        }
    }

    // Gradebook batch save entry method
    fun saveGradebook(
        title: String,
        batch: String,
        maxScore: Int,
        grades: List<GradeEntryInput>
    ) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            grades.forEach { grade ->
                val result = ResultRecord(
                    examId = "GB-" + (1000..9999).random(),
                    examTitle = title,
                    studentId = grade.studentId,
                    studentName = grade.studentName,
                    score = if (grade.isAbsent) 0 else grade.score,
                    totalQuestions = maxScore,
                    dateString = dateStr,
                    isAbsent = grade.isAbsent
                )
                repository.insertResult(result)
            }
            createNotification(
                title = "Grades Published",
                content = "Grades for '$title' in batch '$batch' have been published.",
                type = "Success"
            )
        }
    }

    // 3. Admin / Course & User Controls
    fun approveUser(userId: String) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, "Active")
            createNotification("Registration Approved", "User $userId registration is approved. Welcome!", "Success")
        }
    }

    fun approveUserWithEdits(
        oldUserId: String,
        newUserId: String,
        name: String,
        role: String,
        mobile: String,
        parentMobile: String?,
        batch: String?,
        subjects: String?
    ) {
        viewModelScope.launch {
            val user = repository.findUserByIdOrMobile(oldUserId)
            if (user != null) {
                if (oldUserId != newUserId) {
                    val existing = repository.findUserByIdOrMobile(newUserId)
                    if (existing != null) {
                        createNotification(
                            title = "Approval Error",
                            content = "User ID '$newUserId' is already taken.",
                            type = "Error"
                        )
                        return@launch
                    }
                    repository.deleteUser(oldUserId)
                }
                val updatedUser = user.copy(
                    userId = newUserId,
                    name = name,
                    role = role,
                    mobile = mobile,
                    parentMobile = if (role == "Student") parentMobile else null,
                    batch = if (role == "Student") batch else null,
                    subjects = if (role == "Teacher") subjects else null,
                    status = "Active"
                )
                repository.insertUser(updatedUser)
                createNotification(
                    title = "Registration Approved",
                    content = "User '$newUserId' registration approved as $role.",
                    type = "Success"
                )
            }
        }
    }

    fun rejectUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun addNewUserByAdmin(
        name: String,
        role: String,
        mobile: String,
        parentMobile: String?,
        batch: String?,
        subjects: String?,
        pin: String,
        status: String
    ) {
        viewModelScope.launch {
            val mobileClean = mobile.trim()
            if (mobileClean.isNotEmpty()) {
                val existing = repository.findUserByIdOrMobile(mobileClean)
                if (existing != null) {
                    createNotification(
                        title = "Registration Error",
                        content = "User with mobile $mobileClean already exists.",
                        type = "Error"
                    )
                    return@launch
                }
            }

            val prefix = when (role) {
                "Student" -> "STU"
                "Teacher" -> "TCH"
                else -> "ADM"
            }
            val randomId = (1000..9999).random()
            val newUserId = "$prefix-$randomId"

            val newUser = User(
                userId = newUserId,
                name = name,
                role = role,
                mobile = mobileClean,
                parentMobile = if (role == "Student") parentMobile else null,
                batch = if (role == "Student") batch else null,
                subjects = if (role == "Teacher") subjects else null,
                pin = pin,
                status = status
            )

            repository.insertUser(newUser)
            createNotification(
                title = "User Added",
                content = "New user '$newUserId' ($role) added by Admin with status: $status.",
                type = "Success"
            )
        }
    }

    fun editUserByAdmin(userId: String, name: String, mobile: String, parentMobile: String?, batch: String?, subjects: String?, isBiometricEnabled: Boolean = false) {
        viewModelScope.launch {
            val user = repository.findUserByIdOrMobile(userId)
            if (user != null) {
                val updated = user.copy(name = name, mobile = mobile, parentMobile = parentMobile, batch = batch, subjects = subjects, isBiometricEnabled = isBiometricEnabled)
                repository.updateUser(updated)
            }
        }
    }

    fun sendChatMessage(senderId: String, senderName: String, senderRole: String, channelType: String, channelSubject: String, text: String) {
        viewModelScope.launch {
            val message = ChatMessage(
                senderId = senderId,
                senderName = senderName,
                senderRole = senderRole,
                channelType = channelType,
                channelSubject = channelSubject,
                messageText = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertChatMessage(message)
        }
    }

    fun createNotification(title: String, content: String, type: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            repository.insertNotification(SystemNotification(title = title, content = content, dateString = dateStr, type = type))
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun createBatch(name: String, teacherId: String? = null) {
        viewModelScope.launch {
            repository.insertBatch(AcademyBatch(name = name, teacherId = teacherId))
            createNotification(
                title = "New Batch Assembled",
                content = "Batch '$name' has been added to the system directory.",
                type = "System"
            )
        }
    }

    fun editBatch(batchId: Int, newName: String, teacherId: String?, selectedStudentIds: List<String>) {
        viewModelScope.launch {
            val existingBatches = repository.getAllBatches()
            val batchToEdit = existingBatches.find { it.id == batchId } ?: return@launch
            val oldName = batchToEdit.name

            // Update the batch
            val updatedBatch = batchToEdit.copy(name = newName, teacherId = teacherId)
            repository.insertBatch(updatedBatch)

            // Update students
            val allUsers = repository.getAllUsers()
            allUsers.forEach { user ->
                if (user.role == "Student") {
                    val isNowSelected = user.userId in selectedStudentIds
                    val wasInThisBatch = user.batch == oldName

                    if (isNowSelected && user.batch != newName) {
                        repository.insertUser(user.copy(batch = newName))
                    } else if (!isNowSelected && wasInThisBatch) {
                        repository.insertUser(user.copy(batch = null))
                    }
                }
            }
            createNotification(
                title = "Batch Updated",
                content = "Batch '$oldName' updated to '$newName' with coordinator and student assignment changes.",
                type = "System"
            )
        }
    }

    fun deleteBatch(id: Int) {
        viewModelScope.launch {
            repository.deleteBatch(id)
        }
    }

    fun publishCourse(title: String, description: String, price: Int, videoUrl: String, category: String) {
        viewModelScope.launch {
            val course = Course(
                title = title,
                description = description,
                price = price,
                rating = 4.5,
                videoUrl = videoUrl,
                category = category
            )
            repository.insertCourse(course)
            createNotification("New Course Published", "Course '$title' is now available in library.", "Info")
        }
    }

    fun deleteCourse(id: Int) {
        viewModelScope.launch {
            repository.deleteCourse(id)
        }
    }

    fun uploadLectureMaterial(
        title: String,
        subject: String,
        topic: String,
        materialType: String,
        fileUrl: String,
        uploadedBy: String,
        fileSize: String,
        description: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val material = LectureMaterial(
                title = title.trim(),
                subject = subject,
                topic = topic.trim().ifEmpty { "General Revision" },
                materialType = materialType,
                fileUrl = fileUrl.trim().ifEmpty { "https://www.w3schools.com/html/mov_bbb.mp4" },
                uploadDate = dateStr,
                uploadedBy = uploadedBy,
                fileSize = fileSize.trim().ifEmpty { "1.2 MB" },
                description = description.trim()
            )
            repository.insertLectureMaterial(material)
            createNotification(
                title = "📚 NEW MATERIAL UPLOADED: $subject",
                content = "New material '$title' ($materialType) added for $subject by $uploadedBy.",
                type = "Success"
            )
        }
    }

    fun deleteLectureMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteLectureMaterial(id)
        }
    }

    fun createExam(title: String, batch: String, durationMin: Int, questions: List<Question>) {
        viewModelScope.launch {
            val examId = "EXM-${(100..999).random()}"
            val exam = Exam(
                id = examId,
                title = title,
                batch = batch,
                durationMinutes = durationMin,
                questionsJson = Question.serializeList(questions)
            )
            repository.insertExam(exam)
            createNotification("New Exam Posted", "Exam '$title' is scheduled for batch '$batch'.", "Warning")
        }
    }

    fun deleteExam(id: String) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    // 4. Live Classroom Toggle
    fun startLiveClass() {
        _liveClassActive.value = true
    }

    fun joinLiveClass(session: LiveClassSession) {
        _currentLiveSession.value = session
        _liveClassActive.value = true
    }

    fun createLiveClass(subject: String, batch: String, topic: String, startedBy: String) {
        viewModelScope.launch {
            val cleanSubject = subject.trim()
            val cleanBatch = batch.trim()
            val cleanTopic = topic.trim()
            val cleanSubjectForUrl = cleanSubject.replace(Regex("[^a-zA-Z0-9]"), "")
            val cleanBatchForUrl = cleanBatch.replace(Regex("[^a-zA-Z0-9]"), "")
            val uniqueRoom = "Jalaram_${cleanSubjectForUrl}_${cleanBatchForUrl}_${(100000..999999).random()}"
            val jitsiUrl = "https://meet.jit.si/$uniqueRoom"

            val session = LiveClassSession(
                subject = cleanSubject,
                batch = cleanBatch,
                topic = cleanTopic,
                jitsiLink = jitsiUrl,
                startedBy = startedBy,
                isLive = true
            )
            
            val sessionId = repository.insertLiveClass(session)
            val savedSession = session.copy(id = sessionId.toInt())
            
            _currentLiveSession.value = savedSession
            _liveClassActive.value = true

            // Send notification for students of that particular subject & class (batch)
            createNotification(
                title = "🚨 LIVE CLASS STARTED: $cleanSubject",
                content = "High-fidelity live lecture started for $cleanBatch. Topic: '$cleanTopic' by $startedBy. Connect now to join the free limitless video meeting!",
                type = "Success"
            )
        }
    }

    fun stopLiveClass() {
        val current = _currentLiveSession.value
        if (current != null) {
            viewModelScope.launch {
                repository.stopLiveClass(current.id)
                _currentLiveSession.value = null
                _liveClassActive.value = false
            }
        } else {
            _liveClassActive.value = false
        }
    }

    fun endLiveClassSession(id: Int) {
        viewModelScope.launch {
            repository.stopLiveClass(id)
        }
    }

    // 5. Examinations Core Engines
    fun startExam(exam: Exam) {
        _activeExam.value = exam
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _examTimeRemaining.value = exam.durationMinutes * 60

        examJob?.cancel()
        examJob = viewModelScope.launch {
            while (_examTimeRemaining.value > 0) {
                delay(1000)
                _examTimeRemaining.value -= 1
            }
            submitExam()
        }
    }

    fun selectOptionForCurrentQuestion(optionIndex: Int) {
        val qIdx = _currentQuestionIndex.value
        val answers = _selectedAnswers.value.toMutableMap()
        answers[qIdx] = optionIndex
        _selectedAnswers.value = answers
    }

    fun nextQuestion() {
        val size = Question.deserializeList(_activeExam.value?.questionsJson ?: "").size
        if (_currentQuestionIndex.value < size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun cancelExam() {
        examJob?.cancel()
        _activeExam.value = null
        _selectedAnswers.value = emptyMap()
        _currentQuestionIndex.value = 0
    }

    fun submitExam() {
        examJob?.cancel()
        val exam = _activeExam.value ?: return
        val questions = Question.deserializeList(exam.questionsJson)
        val student = currentUser.value ?: return

        var correctCount = 0
        questions.forEachIndexed { index, q ->
            val selected = _selectedAnswers.value[index]
            if (selected == q.correctIndex) {
                correctCount++
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())

        val resultRecord = ResultRecord(
            examId = exam.id,
            examTitle = exam.title,
            studentId = student.userId,
            studentName = student.name,
            score = correctCount,
            totalQuestions = questions.size,
            dateString = dateStr
        )

        viewModelScope.launch {
            repository.insertResult(resultRecord)
            createNotification("Exam Submitted", "${student.name} completed exam ${exam.title}.", "Success")
            _activeExam.value = null
            _selectedAnswers.value = emptyMap()
            _currentTab.value = "results"
        }
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun scheduleExamSubject(
        subject: String,
        batch: String,
        examDate: String,
        timeSlot: String,
        examinerName: String,
        location: String,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val schedule = ExamSchedule(
                subject = subject,
                batch = batch,
                examDate = examDate,
                timeSlot = timeSlot,
                examinerName = examinerName,
                location = location,
                durationMinutes = durationMinutes
            )
            repository.insertExamSchedule(schedule)
            
            // Automated notification for students assigned to that subject/batch!
            val notificationTitle = "📅 New Exam Scheduled: $subject"
            val notificationContent = "Assessment scheduled for $batch on $examDate during $timeSlot ($location). Invigilator: $examinerName."
            createNotification(notificationTitle, notificationContent, "Info")
        }
    }

    fun toggleScheduleAlert(id: Int, currentAlert: Boolean) {
        viewModelScope.launch {
            repository.updateScheduleAlertEnabled(id, !currentAlert)
        }
    }

    fun deleteSchedule(id: Int) {
        viewModelScope.launch {
            repository.deleteExamSchedule(id)
        }
    }

    // Pro Examination Engine States
    private val _proExams = MutableStateFlow<List<ProExam>>(emptyList())
    val proExams: StateFlow<List<ProExam>> = _proExams.asStateFlow()

    private val _activeProExam = MutableStateFlow<ProExam?>(null)
    val activeProExam: StateFlow<ProExam?> = _activeProExam.asStateFlow()

    private val _currentProQuestions = MutableStateFlow<List<ProQuestion>>(emptyList())
    val currentProQuestions: StateFlow<List<ProQuestion>> = _currentProQuestions.asStateFlow()

    private val _currentProOptions = MutableStateFlow<Map<String, List<ProQuestionOption>>>(emptyMap())
    val currentProOptions: StateFlow<Map<String, List<ProQuestionOption>>> = _currentProOptions.asStateFlow()

    private val _proAnswers = MutableStateFlow<Map<String, String?>>(emptyMap()) // QuestId -> OptionId
    val proAnswers: StateFlow<Map<String, String?>> = _proAnswers.asStateFlow()

    private val _proViolations = MutableStateFlow(0)
    val proViolations: StateFlow<Int> = _proViolations.asStateFlow()

    private val _proExamTimer = MutableStateFlow(0) // seconds
    val proExamTimer: StateFlow<Int> = _proExamTimer.asStateFlow()

    private var proExamJob: Job? = null

    fun loadProExams() {
        val user = _realCurrentUser.value ?: return
        viewModelScope.launch {
            val allExams = repository.getAllPublishedExams()
            // Visibility logic: enrolled in batch AND subject
            val userBatch = user.batch ?: ""
            val userSubjects = user.subjects?.split(",")?.map { it.trim() } ?: emptyList()
            
            val visibleExams = allExams.filter { exam ->
                val examBatches = exam.assignedBatches.split(",").map { it.trim() }
                (userBatch in examBatches) && (exam.subject in userSubjects)
            }
            _proExams.value = visibleExams
        }
    }

    fun startProExam(exam: ProExam) {
        val user = _realCurrentUser.value ?: return
        viewModelScope.launch {
            val questions = repository.getQuestionsForExam(exam.examId)
            val optionsMap = mutableMapOf<String, List<ProQuestionOption>>()
            questions.forEach { q ->
                optionsMap[q.questionId] = repository.getOptionsForQuestion(q.questionId)
            }
            
            var attempt = repository.getProAttempt(user.userId, exam.examId)
            if (attempt == null) {
                attempt = ProExamAttempt(
                    attemptId = "ATT-${System.currentTimeMillis()}",
                    userId = user.userId,
                    examId = exam.examId,
                    startTime = System.currentTimeMillis()
                )
                repository.insertProAttempt(attempt)
            } else if (attempt.status != "in_progress") {
                // Already submitted
                return@launch
            }

            // Restore answers
            val savedAnswers = repository.getAnswersForAttempt(attempt.attemptId)
            val answersMap = savedAnswers.associateBy({ it.questionId }, { it.selectedOptionId }).toMutableMap()
            // Ensure all questions have an entry
            questions.forEach { q ->
                if (!answersMap.containsKey(q.questionId)) {
                    answersMap[q.questionId] = null
                }
            }

            _activeProExam.value = exam
            _currentProQuestions.value = questions
            _currentProOptions.value = optionsMap
            _proAnswers.value = answersMap
            _proViolations.value = attempt.violations
            _proExamTimer.value = exam.durationMinutes * 60
            
            startProExamTimer()
        }
    }

    private fun startProExamTimer() {
        proExamJob?.cancel()
        proExamJob = viewModelScope.launch {
            while (_proExamTimer.value > 0) {
                delay(1000)
                _proExamTimer.value -= 1
            }
            submitProExam(isAutoSubmit = true)
        }
    }

    fun saveProAnswer(questionId: String, optionId: String?) {
        viewModelScope.launch {
            val user = _realCurrentUser.value ?: return@launch
            val activeExam = _activeProExam.value ?: return@launch
            val attempt = repository.getProAttempt(user.userId, activeExam.examId) ?: return@launch
            
            val updatedAnswers = _proAnswers.value.toMutableMap()
            updatedAnswers[questionId] = optionId
            _proAnswers.value = updatedAnswers
            
            repository.saveProAnswer(ProAttemptAnswer(
                attemptId = attempt.attemptId,
                questionId = questionId,
                selectedOptionId = optionId
            ))
        }
    }

    fun logProViolation(type: String) {
        val user = _realCurrentUser.value ?: return
        val activeExam = _activeProExam.value ?: return
        if (activeExam.isStrictMode) {
            viewModelScope.launch {
                val attempt = repository.getProAttempt(user.userId, activeExam.examId) ?: return@launch
                attempt.violations += 1
                repository.updateProAttempt(attempt)
                _proViolations.value = attempt.violations
                
                if (attempt.violations >= activeExam.maxLeavesAllowed) {
                    submitProExam(isAutoSubmit = true)
                }
            }
        }
    }

    fun submitProExam(isAutoSubmit: Boolean = false) {
        val user = _realCurrentUser.value ?: return
        val activeExam = _activeProExam.value ?: return
        viewModelScope.launch {
            val attempt = repository.getProAttempt(user.userId, activeExam.examId) ?: return@launch
            attempt.status = if (isAutoSubmit) "auto_submitted" else "submitted"
            attempt.submitTime = System.currentTimeMillis()
            repository.updateProAttempt(attempt)
            
            // Clear local active state
            proExamJob?.cancel()
            _activeProExam.value = null
            _currentProQuestions.value = emptyList()
            _proAnswers.value = emptyMap()
            _proExamTimer.value = 0
            _currentTab.value = "dashboard" 
        }
    }

    // Teacher Draft Management
    private val _examDrafts = MutableStateFlow<List<ProExamDraft>>(emptyList())
    val examDrafts: StateFlow<List<ProExamDraft>> = _examDrafts.asStateFlow()

    fun loadDrafts() {
        viewModelScope.launch {
            _examDrafts.value = repository.getAllDrafts()
        }
    }

    fun saveExamAsDraft(title: String, subject: String, questions: List<Pair<String, List<Pair<String, Boolean>>>>) {
        viewModelScope.launch {
            val draftId = "DRAFT-${System.currentTimeMillis()}"
            val draft = ProExamDraft(
                draftId = draftId,
                title = title,
                description = "Locally saved draft",
                subject = subject,
                totalMarks = questions.size
            )
            repository.insertDraft(draft)
            
            questions.forEachIndexed { qIdx, qData ->
                val qId = "DQ-${draftId}-$qIdx"
                repository.insertDraftQuestion(ProQuestionDraft(qId, draftId, qData.first))
                qData.second.forEachIndexed { oIdx, oData ->
                    repository.insertDraftOption(ProQuestionOptionDraft("DO-$qId-$oIdx", qId, oData.first, oData.second))
                }
            }
            loadDrafts()
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            repository.deleteDraft(draftId)
            loadDrafts()
        }
    }

    fun publishDraft(draftId: String, startTime: Long, durationMinutes: Int, assignedBatches: String) {
        viewModelScope.launch {
            val draft = repository.getAllDrafts().find { it.draftId == draftId } ?: return@launch
            val questions = repository.getDraftQuestions(draftId)
            
            val newExam = ProExam(
                examId = "PRO-${System.currentTimeMillis()}",
                title = draft.title,
                description = draft.description,
                subject = draft.subject,
                assignedBatches = assignedBatches,
                startTimestamp = startTime,
                endTimestamp = startTime + (durationMinutes * 60 * 1000L),
                durationMinutes = durationMinutes,
                totalMarks = draft.totalMarks,
                status = "Published",
                isStrictMode = true
            )
            
            repository.insertProExam(newExam)
            
            // Transfer questions
            questions.forEach { dq ->
                val nq = ProQuestion(dq.questionId, newExam.examId, dq.questionText, dq.explanation)
                repository.insertProQuestion(nq)
                val options = repository.getDraftOptions(dq.questionId)
                options.forEach { opt ->
                    repository.insertProOption(ProQuestionOption(opt.optionId, nq.questionId, opt.optionText, opt.isCorrect))
                }
            }
            
            // Delete draft after publishing
            repository.deleteDraft(draftId)
            
            // Notify students via a system message in the chat
            repository.insertChatMessage(ChatMessage(
                senderId = "SYSTEM",
                senderName = "System Alert",
                senderRole = "Admin",
                messageText = "New Exam Scheduled: ${newExam.title} for ${newExam.subject} on ${SimpleDateFormat("dd MMM").format(Date(startTime))}",
                timestamp = System.currentTimeMillis(),
                channelType = "Batch",
                channelSubject = assignedBatches.split(",").firstOrNull()?.trim() ?: "General"
            ))
            
            loadDrafts()
            loadProExams()
        }
    }

    fun canEditOrDeleteExam(exam: ProExam): Boolean {
        // Administrative edits are forbidden once an exam is Live or Completed
        return exam.getStatus() == ProExamStatus.Scheduled
    }
}

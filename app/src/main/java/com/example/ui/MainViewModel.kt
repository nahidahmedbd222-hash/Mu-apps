package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    SPLASH,
    LOGIN,
    STUDENT_HOME, // Home, Notices, Bus, Results, Contacts
    ADMIN_HOME,
    MODERATOR_HOME,
    CSV_IMPORT,
    MANAGE_PERMISSIONS,
    ADD_EDIT_NOTICE,
    ADD_EDIT_EVENT,
    ADD_EDIT_BUS,
    NOTICE_DETAIL,
    EVENT_DETAIL
}

enum class StudentTab {
    DASHBOARD,
    NOTICES,
    BUS_SCHEDULE,
    NOTIFICATIONS
}

data class InAppNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val content: String,
    val type: String, // "Notice", "Event", "Emergency"
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(
        database.userDao(),
        database.noticeDao(),
        database.busScheduleDao(),
        database.eventDao(),
        database.resultDao()
    )

    // Current Screen Navigation
    var currentScreen by mutableStateOf(Screen.SPLASH)
        private set

    // Current student bottom navigation tab
    var currentStudentTab by mutableStateOf(StudentTab.DASHBOARD)

    // Active session details
    var currentUser by mutableStateOf<User?>(null)
        private set

    var rememberMe by mutableStateOf(false)
    var isAuthenticating by mutableStateOf(false)
    var loginErrorMessage by mutableStateOf<String?>(null)

    // Selection Details
    var selectedNotice by mutableStateOf<Notice?>(null)
    var selectedEvent by mutableStateOf<VarsityEvent?>(null)

    // Dynamic Lists (Observed from Room)
    val noticesList = repository.allNotices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val busSchedulesList = repository.allBusSchedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val eventsList = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val resultsList = repository.allResults.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val usersList = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Filters
    var noticeSearchQuery by mutableStateOf("")
    var noticeSelectedCategory by mutableStateOf("All")

    var busSearchQuery by mutableStateOf("")

    var eventSearchQuery by mutableStateOf("")
    var eventFilterUpcoming by mutableStateOf(true) // true = upcoming, false = past

    // Push Notifications Log (in-app display)
    private val _notifications = MutableStateFlow<List<InAppNotification>>(
        listOf(
            InAppNotification(
                title = "Welcome to MetroUni Portal!",
                content = "Access your academic notices, class schedules, bus timings, and result trackers seamlessly from your mobile device.",
                type = "General"
            )
        )
    )
    val notifications = _notifications.asStateFlow()

    // Heads up banners for push notification emulation
    var headsUpNotification by mutableStateOf<InAppNotification?>(null)

    // Admin Dashboard/CSV Statistics page states
    var csvProgressState by mutableStateOf(false)
    var csvImportReport by mutableStateOf<Pair<Int, List<String>>?>(null) // Count, Error strings

    // Internet Connectivity simulation
    var isOnline by mutableStateOf(true)
    var isCheckingConnection by mutableStateOf(false)

    // Pull to Refresh Emulations
    var isRefreshing by mutableStateOf(false)

    init {
        // Run Splash timeout, then auto check session
        viewModelScope.launch {
            delay(2200) // Beautiful cinematic entrance
            checkSavedSessionAndNavigate()
        }
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
    }

    private fun checkSavedSessionAndNavigate() {
        // Simulating persistent auto-login check
        navigateTo(Screen.LOGIN)
    }

    fun login(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            isAuthenticating = true
            loginErrorMessage = null
            delay(1000) // Human-like interactive delay

            val user = repository.authenticateUser(emailInput.trim(), passwordInput)
            isAuthenticating = false

            if (user != null) {
                currentUser = user
                when (user.role) {
                    "ADMIN" -> navigateTo(Screen.ADMIN_HOME)
                    "MODERATOR" -> navigateTo(Screen.MODERATOR_HOME)
                    else -> {
                        currentStudentTab = StudentTab.DASHBOARD
                        navigateTo(Screen.STUDENT_HOME)
                    }
                }
            } else {
                loginErrorMessage = "Invalid credentials. Please verify your email and password."
            }
        }
    }

    fun logout() {
        currentUser = null
        loginErrorMessage = null
        navigateTo(Screen.LOGIN)
    }

    fun simulatePullToRefresh() {
        viewModelScope.launch {
            isRefreshing = true
            delay(1500)
            isRefreshing = false
        }
    }

    fun triggerConnectionRetry() {
        viewModelScope.launch {
            isCheckingConnection = true
            delay(1200)
            isOnline = true
            isCheckingConnection = false
        }
    }

    fun toggleOnlineStatus() {
        isOnline = !isOnline
    }

    // --- NOTICE BROADCASTING (WITH NOTIFICATION TRIGGER) ---
    fun broadcastNotice(title: String, content: String, category: String, pdfUrl: String?, pdfFileName: String?, sendNotification: Boolean) {
        viewModelScope.launch {
            val notice = Notice(
                title = title,
                content = content,
                category = category,
                pdfUrl = pdfUrl,
                pdfFileName = pdfFileName
            )
            repository.insertNotice(notice)

            if (sendNotification) {
                val notif = InAppNotification(
                    title = "Notice: $title",
                    content = content.take(120) + if (content.length > 120) "..." else "",
                    type = if (category == "Emergency") "Emergency" else "Notice"
                )
                // Add to list
                _notifications.update { listOf(notif) + it }
                // Trigger visual slider banner toast
                headsUpNotification = notif
                delay(5000)
                if (headsUpNotification == notif) {
                    headsUpNotification = null
                }
            }
        }
    }

    fun deleteNotice(notice: Notice) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
            if (selectedNotice == notice) {
                selectedNotice = null
            }
        }
    }

    // --- EVENTS BROADCASTING ---
    fun addEvent(title: String, description: String, date: Long, imageUrl: String?, pdfUrl: String?) {
        viewModelScope.launch {
            val event = VarsityEvent(
                title = title,
                description = description,
                date = date,
                imageUrl = imageUrl ?: "https://picsum.photos/seed/${title.hashCode()}/800/400",
                pdfUrl = pdfUrl
            )
            repository.insertEvent(event)

            // Dynamic event alert
            val notif = InAppNotification(
                title = "New Event: $title",
                content = description.take(100) + "...",
                type = "Event"
            )
            _notifications.update { listOf(notif) + it }
            headsUpNotification = notif
            delay(5000)
            if (headsUpNotification == notif) {
                headsUpNotification = null
            }
        }
    }

    fun deleteEvent(event: VarsityEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            if (selectedEvent == event) {
                selectedEvent = null
            }
        }
    }

    // --- BUS OPERATIONS ---
    fun addBusSchedule(route: String, busNumber: String, timeText: String, frequencyText: String, pdfUrl: String?) {
        viewModelScope.launch {
            val sched = BusSchedule(
                route = route,
                busNumber = busNumber,
                timeText = timeText,
                frequencyText = frequencyText,
                pdfUrl = pdfUrl
            )
            repository.insertBusSchedule(sched)
        }
    }

    fun deleteBusSchedule(schedule: BusSchedule) {
        viewModelScope.launch {
            repository.deleteBusSchedule(schedule)
        }
    }

    // --- RESULTS OPERATIONS ---
    fun publishResult(semester: String, department: String, pdfUrl: String?, pdfFileName: String?) {
        viewModelScope.launch {
            val result = AcademicResult(
                semester = semester,
                department = department,
                pdfUrl = pdfUrl,
                pdfFileName = pdfFileName
            )
            repository.insertResult(result)

            val notif = InAppNotification(
                title = "Results Out: $semester",
                content = "Academic results folder has been updated for $department department. Download instructions published.",
                type = "Notice"
            )
            _notifications.update { listOf(notif) + it }
            headsUpNotification = notif
            delay(5000)
            if (headsUpNotification == notif) {
                headsUpNotification = null
            }
        }
    }

    fun deleteResult(result: AcademicResult) {
        viewModelScope.launch {
            repository.deleteResult(result)
        }
    }

    // --- USER MANAGEMENT & MODERATORS DYNAMIC PERMISSIONS ---
    fun createModeratorAccount(username: String, email: String, department: String, permissions: Map<String, Boolean>) {
        viewModelScope.launch {
            // Convert map to stringified JSON matching database seed
            val noticesPerm = permissions["notices"] == true
            val eventsPerm = permissions["events"] == true
            val busPerm = permissions["bus"] == true
            val resultsPerm = permissions["results"] == true
            
            val permJson = """{"notices":$noticesPerm,"events":$eventsPerm,"bus":$busPerm,"results":$resultsPerm}"""
            val user = User(
                studentId = "MOD-${System.currentTimeMillis() % 10000}",
                username = username,
                password = "password123", // Default placeholder
                email = email,
                department = department,
                role = "MODERATOR",
                permissionsJson = permJson
            )
            repository.insertUser(user)
        }
    }

    fun updateModeratorPermissions(user: User, notices: Boolean, events: Boolean, bus: Boolean, results: Boolean) {
        viewModelScope.launch {
            val updatedJson = """{"notices":$notices,"events":$events,"bus":$bus,"results":$results}"""
            val updatedUser = user.copy(permissionsJson = updatedJson)
            repository.updateUser(updatedUser)
            
            // If the updated user is currently logged in, update local UI state as well
            if (currentUser?.id == user.id) {
                currentUser = updatedUser
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun hasModeratorPermission(featureKey: String): Boolean {
        val user = currentUser ?: return false
        if (user.role == "ADMIN") return true
        if (user.role != "MODERATOR") return false

        // Parse local permissions JSON
        // E.g. {"notices":true,"events":true,"bus":false,"results":true}
        val json = user.permissionsJson
        return when (featureKey) {
            "notices" -> json.contains("\"notices\":true")
            "events" -> json.contains("\"events\":true")
            "bus" -> json.contains("\"bus\":true")
            "results" -> json.contains("\"results\":true")
            else -> false
        }
    }

    // --- CSV BULK IMPORT CONTROLS ---
    fun importUsersFromCsv(csvContent: String) {
        viewModelScope.launch {
            csvProgressState = true
            csvImportReport = null
            delay(2000) // Emulating secure bulk account generation and Firebase Auth sync
            val report = repository.parseAndImportUsersCsv(csvContent)
            csvImportReport = report
            csvProgressState = false
        }
    }

    fun clearCsvReport() {
        csvImportReport = null
    }

    // --- MANUAL BANNER DISMISSAL ---
    fun clearHeadsUpNotification() {
        headsUpNotification = null
    }
}

package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userDao: UserDao,
    private val noticeDao: NoticeDao,
    private val busScheduleDao: BusScheduleDao,
    private val eventDao: EventDao,
    private val resultDao: ResultDao
) {
    // --- USER FLOWS & OPERATIONS ---
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun authenticateUser(email: String, password: String): User? {
        return userDao.getUserByEmailAndPassword(email, password)
    }

    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun insertUsers(users: List<User>) {
        userDao.insertUsers(users)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    // --- NOTICE FLOWS & OPERATIONS ---
    val allNotices: Flow<List<Notice>> = noticeDao.getAllNotices()

    suspend fun insertNotice(notice: Notice): Long {
        return noticeDao.insertNotice(notice)
    }

    suspend fun updateNotice(notice: Notice) {
        noticeDao.updateNotice(notice)
    }

    suspend fun deleteNotice(notice: Notice) {
        noticeDao.deleteNotice(notice)
    }

    // --- BUS SCHEDULE FLOWS & OPERATIONS ---
    val allBusSchedules: Flow<List<BusSchedule>> = busScheduleDao.getAllBusSchedules()

    suspend fun insertBusSchedule(schedule: BusSchedule): Long {
        return busScheduleDao.insertBusSchedule(schedule)
    }

    suspend fun updateBusSchedule(schedule: BusSchedule) {
        busScheduleDao.updateBusSchedule(schedule)
    }

    suspend fun deleteBusSchedule(schedule: BusSchedule) {
        busScheduleDao.deleteBusSchedule(schedule)
    }

    // --- EVENTS FLOWS & OPERATIONS ---
    val allEvents: Flow<List<VarsityEvent>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: VarsityEvent): Long {
        return eventDao.insertEvent(event)
    }

    suspend fun updateEvent(event: VarsityEvent) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: VarsityEvent) {
        eventDao.deleteEvent(event)
    }

    // --- RESULTS FLOWS & OPERATIONS ---
    val allResults: Flow<List<AcademicResult>> = resultDao.getAllResults()

    suspend fun insertResult(result: AcademicResult): Long {
        return resultDao.insertResult(result)
    }

    suspend fun updateResult(result: AcademicResult) {
        resultDao.updateResult(result)
    }

    suspend fun deleteResult(result: AcademicResult) {
        resultDao.deleteResult(result)
    }

    // --- CSV PARSING UTILITY ---
    /**
     * Parses CSV data representing users.
     * Expects commas: student_id,username,password,email,department,role
     * E.g. 221-115-001,john123,pass123,john@gmail.com,CSE,student
     */
    suspend fun parseAndImportUsersCsv(csvText: String): Pair<Int, List<String>> {
        val lines = csvText.lines()
        val importedUsers = mutableListOf<User>()
        val errorsList = mutableListOf<String>()
        var count = 0

        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            // Skip header if matches student_id pattern
            if (index == 0 && trimmedLine.contains("student_id", ignoreCase = true)) {
                continue
            }

            // Split with handling for basic quotes or simply comma-split
            val parts = trimmedLine.split(",")
            if (parts.size >= 6) {
                val studentId = parts[0].trim()
                val username = parts[1].trim()
                val password = parts[2].trim()
                val email = parts[3].trim()
                val department = parts[4].trim()
                val roleStr = parts[5].trim().uppercase()

                val finalRole = when {
                    roleStr == "ADMIN" -> "ADMIN"
                    roleStr == "MODERATOR" -> "MODERATOR"
                    else -> "STUDENT"
                }

                // Check for duplicate in database or in lists
                val existing = userDao.getUserByEmail(email) ?: userDao.getUserByStudentId(studentId)
                if (existing != null) {
                    errorsList.add("Row ${index + 1}: Account with email $email or Student ID $studentId already exists.")
                } else {
                    val defaultPermissionsStr = when (finalRole) {
                        "ADMIN" -> """{"notices":true,"events":true,"bus":true,"results":true}"""
                        "MODERATOR" -> """{"notices":true,"events":true,"bus":false,"results":true}"""
                        else -> """{"notices":false,"events":false,"bus":false,"results":false}"""
                    }

                    importedUsers.add(
                        User(
                            studentId = studentId,
                            username = username,
                            password = password,
                            email = email,
                            department = department,
                            role = finalRole,
                            permissionsJson = defaultPermissionsStr
                        )
                    )
                    count++
                }
            } else {
                errorsList.add("Row ${index + 1}: Invalid format. Expected 6 comma-separated values.")
            }
        }

        if (importedUsers.isNotEmpty()) {
            userDao.insertUsers(importedUsers)
        }

        return Pair(count, errorsList)
    }
}

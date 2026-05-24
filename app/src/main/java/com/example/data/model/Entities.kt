package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: String,
    val username: String,
    val password: String,
    val email: String,
    val department: String,
    val role: String, // "ADMIN", "MODERATOR", "STUDENT"
    val permissionsJson: String, // Stringified JSON e.g. {"notices":true,"events":true,"bus":false,"results":true}
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String, // "Academic", "Exam", "Emergency", "General"
    val pdfUrl: String? = null,
    val pdfFileName: String? = null,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "bus_schedules")
data class BusSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val route: String,
    val busNumber: String,
    val pdfUrl: String? = null,
    val timeText: String,
    val frequencyText: String
)

@Entity(tableName = "events")
data class VarsityEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: Long,
    val imageUrl: String? = null, // Path or local URI or image reference
    val pdfUrl: String? = null
)

@Entity(tableName = "results")
data class AcademicResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val semester: String,
    val department: String,
    val pdfUrl: String? = null,
    val pdfFileName: String? = null,
    val date: Long = System.currentTimeMillis()
)

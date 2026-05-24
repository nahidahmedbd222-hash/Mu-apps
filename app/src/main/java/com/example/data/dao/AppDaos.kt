package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE studentId = :studentId LIMIT 1")
    suspend fun getUserByStudentId(studentId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice): Long

    @Update
    suspend fun updateNotice(notice: Notice)

    @Delete
    suspend fun deleteNotice(notice: Notice)
}

@Dao
interface BusScheduleDao {
    @Query("SELECT * FROM bus_schedules ORDER BY route ASC")
    fun getAllBusSchedules(): Flow<List<BusSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusSchedule(schedule: BusSchedule): Long

    @Update
    suspend fun updateBusSchedule(schedule: BusSchedule)

    @Delete
    suspend fun deleteBusSchedule(schedule: BusSchedule)
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<VarsityEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: VarsityEvent): Long

    @Update
    suspend fun updateEvent(event: VarsityEvent)

    @Delete
    suspend fun deleteEvent(event: VarsityEvent)
}

@Dao
interface ResultDao {
    @Query("SELECT * FROM results ORDER BY date DESC")
    fun getAllResults(): Flow<List<AcademicResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: AcademicResult): Long

    @Update
    suspend fun updateResult(result: AcademicResult)

    @Delete
    suspend fun deleteResult(result: AcademicResult)
}

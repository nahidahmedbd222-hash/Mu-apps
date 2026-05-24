package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Notice::class,
        BusSchedule::class,
        VarsityEvent::class,
        AcademicResult::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noticeDao(): NoticeDao
    abstract fun busScheduleDao(): BusScheduleDao
    abstract fun eventDao(): EventDao
    abstract fun resultDao(): ResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metropolitan_university_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        database.userDao(),
                        database.noticeDao(),
                        database.busScheduleDao(),
                        database.eventDao(),
                        database.resultDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(
            userDao: UserDao,
            noticeDao: NoticeDao,
            busDao: BusScheduleDao,
            eventDao: EventDao,
            resultDao: ResultDao
        ) {
            // Seed Users
            userDao.insertUser(
                User(
                    studentId = "ADMIN-001",
                    username = "nahidahmed",
                    password = "password123",
                    email = "nahidahmed@metrouni.edu.bd",
                    department = "CSE",
                    role = "ADMIN",
                    permissionsJson = """{"notices":true,"events":true,"bus":true,"results":true}"""
                )
            )
            userDao.insertUser(
                User(
                    studentId = "ADMIN-002",
                    username = "admin",
                    password = "admin",
                    email = "admin@metrouni.edu.bd",
                    department = "Admin Office",
                    role = "ADMIN",
                    permissionsJson = """{"notices":true,"events":true,"bus":true,"results":true}"""
                )
            )
            userDao.insertUser(
                User(
                    studentId = "MOD-001",
                    username = "academic_mod",
                    password = "password123",
                    email = "moderator@metrouni.edu.bd",
                    department = "Academic Affairs",
                    role = "MODERATOR",
                    permissionsJson = """{"notices":true,"events":false,"bus":false,"results":true}""" // limited permissions
                )
            )
            userDao.insertUser(
                User(
                    studentId = "221-115-001",
                    username = "john123",
                    password = "pass123",
                    email = "john@gmail.com",
                    department = "CSE",
                    role = "STUDENT",
                    permissionsJson = """{"notices":false,"events":false,"bus":false,"results":false}"""
                )
            )

            // Seed Notices
            noticeDao.insertNotice(
                Notice(
                    title = "Spring Semester 2026 Registration Notice",
                    content = "All students of Metropolitan University are hereby notified that the course registration for Spring 2026 will start from June 1st, 2026. Please clear all outstanding dues before registering. Contact your department coordinator for details.",
                    category = "Academic",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    pdfFileName = "Spring_202 registration_schedule.pdf"
                )
            )
            noticeDao.insertNotice(
                Notice(
                    title = "[EMERGENCY] Heavy Rainfall - Campus Closed for Tomorrow",
                    content = "Due to warning of extreme torrential rains and flooding in low-lying areas, all physical classes of Metropolitan University will remain suspended tomorrow (May 25th, 2026). Classes will be conducted online as per current schedules.",
                    category = "Emergency",
                    pdfUrl = null,
                    pdfFileName = null
                )
            )
            noticeDao.insertNotice(
                Notice(
                    title = "Midterm Examination Routine - CSE Department",
                    content = "The CSE department midterm examination schedule for Spring 2026 has been published. Exams will commence from June 15th, 2026.",
                    category = "Exam",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    pdfFileName = "CSE_Midterm_Routine_Spring2026.pdf"
                )
            )
            noticeDao.insertNotice(
                Notice(
                    title = "Inter-University Cricket Tournament Selection Trails",
                    content = "Selection and practice trails for Metro University Cricket team will be held next Monday at University Playground. Interested players report to the sports director.",
                    category = "General",
                    pdfUrl = null,
                    pdfFileName = null
                )
            )

            // Seed Bus Schedules
            busDao.insertBusSchedule(
                BusSchedule(
                    route = "Court Point to Alampur Camups (Direct)",
                    busNumber = "Metro-01",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    timeText = "AM Trips: 08:00 AM, 09:30 AM | PM Trips: 02:00 PM, 05:00 PM",
                    frequencyText = "Daily (Sunday to Thursday)"
                )
            )
            busDao.insertBusSchedule(
                BusSchedule(
                    route = "Shibgonj via Ambarkhana to Campus",
                    busNumber = "Metro-04",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    timeText = "AM Trips: 07:45 AM, 09:15 AM | PM Trips: 01:45 PM, 04:30 PM",
                    frequencyText = "Daily (Sunday to Thursday)"
                )
            )
            busDao.insertBusSchedule(
                BusSchedule(
                    route = "Zindabazar to Alampur Campus",
                    busNumber = "Metro-08",
                    pdfUrl = "",
                    timeText = "AM Trips: 08:15 AM | PM Trips: 02:15 PM, 05:15 PM",
                    frequencyText = "Daily (Sunday to Thursday)"
                )
            )

            // Seed Events
            val now = System.currentTimeMillis()
            eventDao.insertEvent(
                VarsityEvent(
                    title = "National Varsity Coding Carnival 2026",
                    description = "Metropolitan University is proud to host the CSE department's biggest event of the year: National Coding Carnival. Participate in competitive hacking, app showcase, and design sprints. Cash pool of BDT 100K!",
                    date = now + (3 * 24 * 60 * 60 * 1000L), // 3 days in future
                    imageUrl = "https://picsum.photos/seed/coding/800/400",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                )
            )
            eventDao.insertEvent(
                VarsityEvent(
                    title = "MU Robotics & Innovation Fair",
                    description = "Observe cutting-edge hardware, robotic automation and machine learning solutions developed by senior students. Keynote addresses from industry leading tech CTOs.",
                    date = now + (10 * 24 * 60 * 60 * 1000L), // 10 days in future
                    imageUrl = "https://picsum.photos/seed/robotics/800/400",
                    pdfUrl = null
                )
            )
            eventDao.insertEvent(
                VarsityEvent(
                    title = "International Conference on Sustainable Development",
                    description = "Collaborative academic exchange focusing on energy solutions, green environment, and scalable tech. Research papers from 15 global universities will be presented.",
                    date = now - (5 * 24 * 60 * 60 * 1000L), // 5 days in past
                    imageUrl = "https://picsum.photos/seed/conf/800/400",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                )
            )

            // Seed Academic Results
            resultDao.insertResult(
                AcademicResult(
                    semester = "Fall 2025 Semester Final Results",
                    department = "Computer Science & Engineering",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    pdfFileName = "CSE_Fall2025_Results.pdf"
                )
            )
            resultDao.insertResult(
                AcademicResult(
                    semester = "Fall 2025 Semester Final Results",
                    department = "Business Administration",
                    pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    pdfFileName = "BBA_Fall2025_Results.pdf"
                )
            )
        }
    }
}

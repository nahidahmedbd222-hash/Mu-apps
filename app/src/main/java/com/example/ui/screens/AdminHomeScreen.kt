package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(viewModel: MainViewModel) {
    val users by viewModel.usersList.collectAsState()
    val notices by viewModel.noticesList.collectAsState()
    val events by viewModel.eventsList.collectAsState()
    val schedules by viewModel.busSchedulesList.collectAsState()
    val results by viewModel.resultsList.collectAsState()

    var activeMenuIndex by remember { mutableIntStateOf(0) } // 0=Stats/Main, 1=CSV Import, 2=Moderators, 3=Publish Notices/Events
    val menuTitles = listOf("Stats & Overview", "CSV Upload Panel", "Manage Moderator Perms", "Upload Notices & Events")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Master Admin Console", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Role: Master Root Admin", fontSize = 11.sp, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("admin_logout")
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Interactive Bottom Navigation for Admin Screens
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeMenuIndex == 0,
                    onClick = { activeMenuIndex = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Overview") },
                    label = { Text("Overview", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_nav_overview")
                )
                NavigationBarItem(
                    selected = activeMenuIndex == 1,
                    onClick = { activeMenuIndex = 1 },
                    icon = { Icon(Icons.Default.UploadFile, contentDescription = "CSV Import") },
                    label = { Text("CSV Import", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_nav_csv")
                )
                NavigationBarItem(
                    selected = activeMenuIndex == 2,
                    onClick = { activeMenuIndex = 2 },
                    icon = { Icon(Icons.Default.LockPerson, contentDescription = "Moderators") },
                    label = { Text("Perms", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_nav_mods")
                )
                NavigationBarItem(
                    selected = activeMenuIndex == 3,
                    onClick = { activeMenuIndex = 3 },
                    icon = { Icon(Icons.Default.Publish, contentDescription = "Publish") },
                    label = { Text("Publish", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("admin_nav_publish")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeMenuIndex) {
                0 -> AdminOverviewTab(users, notices, events, schedules, results)
                1 -> AdminCsvImportTab(viewModel)
                2 -> AdminModeratorsTab(viewModel, users)
                3 -> AdminPublishTab(viewModel)
            }
        }
    }
}

// --- TAB 0: OVERVIEW & STATS CARD ---
@Composable
fun AdminOverviewTab(
    users: List<User>,
    notices: List<Notice>,
    events: List<VarsityEvent>,
    schedules: List<BusSchedule>,
    results: List<AcademicResult>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "UNIVERSITY STATS ANALYTICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        // Statistics blocks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Users",
                value = users.size.toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.primary
            )
            StatCard(
                title = "Announcements",
                value = notices.size.toString(),
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Live Events",
                value = events.size.toString(),
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f),
                tint = MaterialTheme.colorScheme.tertiary
            )
            StatCard(
                title = "Bus Schedules",
                value = schedules.size.toString(),
                icon = Icons.Default.DirectionsBus,
                modifier = Modifier.weight(1f),
                tint = Color(0xFF4CAF50)
            )
        }

        // Active Admin Accounts overview
        Text(
            text = "SYSTEM REGISTRY USER LIST",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                users.forEachIndexed { index, user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = user.username + " (" + user.role + ")",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user.email + " | ID: " + user.studentId,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (user.role) {
                                        "ADMIN" -> MaterialTheme.colorScheme.errorContainer
                                        "MODERATOR" -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = user.role,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (user.role) {
                                    "ADMIN" -> MaterialTheme.colorScheme.error
                                    "MODERATOR" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                    if (index < users.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: Any,
    modifier: Modifier = Modifier,
    tint: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = tint.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (icon is androidx.compose.ui.graphics.vector.ImageVector) {
                    Icon(imageVector = icon, contentDescription = title, tint = tint)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

// --- TAB 1: CSV ACCOUNT IMPORT LOADER ---
@Composable
fun AdminCsvImportTab(viewModel: MainViewModel) {
    var csvText by remember { mutableStateOf("") }
    val report = viewModel.csvImportReport
    val context = LocalContext.current

    val sampleCsv = """student_id,username,password,email,department,role
221-115-002,johnny456,pass456,johnny@gmail.com,CSE,student
221-115-003,selina789,pass789,selina@gmail.com,BBA,student
221-115-004,alex901,pass901,alex@gmail.com,EEE,student"""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FIREBASE AUTH SYNC MODULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Bulk Student Account Provisioning",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Master Admin can upload/paste student records in CSV format to automatically provision Firebase accounts and Firestore nodes concurrently.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PASTE CSV CONTENT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Button to load mock templates
            Button(
                onClick = {
                    csvText = sampleCsv
                    Toast.makeText(context, "Populated sample CSV record successfully.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("LOAD SAMPLE TEMPLATE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // TextBox CSV editor
        OutlinedTextField(
            value = csvText,
            onValueChange = { csvText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("csv_paste_input_field"),
            placeholder = { Text("student_id,username,password,email,department,role\n...", fontSize = 12.sp) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
        )

        Button(
            onClick = {
                if (csvText.isBlank()) {
                    Toast.makeText(context, "Please paste CSV logs to parse first.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.importUsersFromCsv(csvText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("admin_csv_import_submit"),
            enabled = !viewModel.csvProgressState
        ) {
            if (viewModel.csvProgressState) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Provisioning Accounts (Firebase Simulation)...")
            } else {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("BULK GENERATE & IMPORT USERS", fontWeight = FontWeight.Bold)
            }
        }

        // Report Visual logs
        if (report != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("csv_import_report_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IMPORT EXECUTION REPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { viewModel.clearCsvReport() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF4CAF50), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Accounts Created Successfully: " + report.first,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    if (report.second.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Validation Errors & Warnings:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        report.second.forEach { errorMsg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                Text(text = errorMsg, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 2: MODERATORS LOCKS & PERMISSIONS DYNAMIC MODULES ---
@Composable
fun AdminModeratorsTab(viewModel: MainViewModel, users: List<User>) {
    val moderators = remember(users) {
        users.filter { it.role == "MODERATOR" }
    }
    
    var showAddModDialog by remember { mutableStateOf(false) }
    var selectedModForPerms by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DYNAMIC ROLE CONSTRAINTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Assign Moderator Permissions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = { showAddModDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .size(36.dp)
                    .testTag("admin_add_mod_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mod", tint = Color.White)
            }
        }

        if (moderators.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No moderators added. Tap '+' above to add moderator credentials.",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moderators) { mod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedModForPerms = mod },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = mod.username,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "ID: " + mod.studentId + " | " + mod.department,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    Text(
                                        text = "Edit Locks",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Current Module Access Allowed:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PermissionBadge(label = "Notices", allowed = mod.permissionsJson.contains("\"notices\":true"))
                                PermissionBadge(label = "Events", allowed = mod.permissionsJson.contains("\"events\":true"))
                                PermissionBadge(label = "Bus", allowed = mod.permissionsJson.contains("\"bus\":true"))
                                PermissionBadge(label = "Results", allowed = mod.permissionsJson.contains("\"results\":true"))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal adding moderators dialog
    if (showAddModDialog) {
        var modUsername by remember { mutableStateOf("") }
        var modEmail by remember { mutableStateOf("") }
        var modDept by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddModDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (modUsername.isBlank() || modEmail.isBlank() || modDept.isBlank()) {
                        // Validate empty
                    } else {
                        viewModel.createModeratorAccount(modUsername, modEmail, modDept, emptyMap())
                        showAddModDialog = false
                    }
                }) {
                    Text("ADD MODERATOR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddModDialog = false }) {
                    Text("CANCEL")
                }
            },
            title = { Text("Generate Moderator Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = modUsername,
                        onValueChange = { modUsername = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_mod_username")
                    )
                    OutlinedTextField(
                        value = modEmail,
                        onValueChange = { modEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_mod_email")
                    )
                    OutlinedTextField(
                        value = modDept,
                        onValueChange = { modDept = it },
                        label = { Text("Department Context") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_mod_dept")
                    )
                }
            }
        )
    }

    // Modal managing active permissions checkbox overlay
    if (selectedModForPerms != null) {
        val mod = selectedModForPerms!!
        var noticesApprove by remember { mutableStateOf(mod.permissionsJson.contains("\"notices\":true")) }
        var eventsApprove by remember { mutableStateOf(mod.permissionsJson.contains("\"events\":true")) }
        var busApprove by remember { mutableStateOf(mod.permissionsJson.contains("\"bus\":true")) }
        var resultsApprove by remember { mutableStateOf(mod.permissionsJson.contains("\"results\":true")) }

        AlertDialog(
            onDismissRequest = { selectedModForPerms = null },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateModeratorPermissions(mod, noticesApprove, eventsApprove, busApprove, resultsApprove)
                    selectedModForPerms = null
                }) {
                    Text("SAVE CHANGES")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedModForPerms = null }) {
                    Text("CANCEL")
                }
            },
            title = { Text("Module Permissions Lock: " + mod.username) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Grant or restrict write capabilities for various app sections. In Firestore, standard rule sets will adjust automatically.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { noticesApprove = !noticesApprove }) {
                        Checkbox(checked = noticesApprove, onCheckedChange = { noticesApprove = it }, modifier = Modifier.testTag("mod_perm_notices"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Notices Write Privilege", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { eventsApprove = !eventsApprove }) {
                        Checkbox(checked = eventsApprove, onCheckedChange = { eventsApprove = it }, modifier = Modifier.testTag("mod_perm_events"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Events Write Privilege", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { busApprove = !busApprove }) {
                        Checkbox(checked = busApprove, onCheckedChange = { busApprove = it }, modifier = Modifier.testTag("mod_perm_bus"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bus Transit Write Privilege", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { resultsApprove = !resultsApprove }) {
                        Checkbox(checked = resultsApprove, onCheckedChange = { resultsApprove = it }, modifier = Modifier.testTag("mod_perm_results"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Academic Results Upload", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        )
    }
}

@Composable
fun PermissionBadge(label: String, allowed: Boolean) {
    Box(
        modifier = Modifier
            .background(
                color = if (allowed) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFEA4335).copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 0.5.dp,
                color = if (allowed) Color(0xFF4CAF50) else Color(0xFFEA4335),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label + ": " + if (allowed) "ON" else "OFF",
            color = if (allowed) Color(0xFF2E7D32) else Color(0xFFC62828),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- TAB 3: BROADCAST PUBLISH CRUD PANEL ---
@Composable
fun AdminPublishTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    var uploadSelectionIndex by remember { mutableIntStateOf(0) } // 0=Notice, 1=Event, 2=Bus, 3=Result

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SELECT CONTENT MODULE TO BIND",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )

        // Custom Chips Segmented Select Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Post Notice", "Add Event", "Add Bus Route", "Publish Result").forEachIndexed { idx, title ->
                val selected = uploadSelectionIndex == idx
                FilterChip(
                    selected = selected,
                    onClick = { uploadSelectionIndex = idx },
                    label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_publish_chip_$idx")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(4.dp))

        when (uploadSelectionIndex) {
            0 -> AdminPublishNoticeForm(viewModel)
            1 -> AdminPublishEventForm(viewModel)
            2 -> AdminPublishBusForm(viewModel)
            3 -> AdminPublishResultForm(viewModel)
        }
    }
}

@Composable
fun AdminPublishNoticeForm(viewModel: MainViewModel) {
    val context = LocalContext.current
    var noteTitle by remember { mutableStateOf("") }
    var noteCategory by remember { mutableStateOf("Academic") }
    var noteContent by remember { mutableStateOf("") }
    var notePdfUrl by remember { mutableStateOf("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf") }
    var notePdfName by remember { mutableStateOf("Academic_Schedule_2026.pdf") }
    var triggerNotificationAlert by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = noteTitle,
            onValueChange = { noteTitle = it },
            label = { Text("Notice Headline") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_notice_title")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Category: ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            listOf("Academic", "Exam", "Emergency", "General").forEach { cat ->
                val selected = noteCategory == cat
                FilterChip(
                    selected = selected,
                    onClick = { noteCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    modifier = Modifier.testTag("add_notice_cat_$cat")
                )
            }
        }

        OutlinedTextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            label = { Text("Notice Full Content") },
            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_notice_content"),
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )

        OutlinedTextField(
            value = notePdfUrl,
            onValueChange = { notePdfUrl = it },
            label = { Text("Attachment PDF url Link (W3 school mock)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_notice_pdfurl")
        )

        OutlinedTextField(
            value = notePdfName,
            onValueChange = { notePdfName = it },
            label = { Text("Attachment Display File Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_notice_pdfname")
        )

        // Broadcast notifications hook
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = triggerNotificationAlert,
                onCheckedChange = { triggerNotificationAlert = it },
                modifier = Modifier.testTag("add_notice_notif_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Broadcast Push Notification to Student alerts inbox", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (noteTitle.isBlank() || noteContent.isBlank()) {
                    Toast.makeText(context, "Please enter Notice Title and content.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.broadcastNotice(
                        noteTitle,
                        noteContent,
                        noteCategory,
                        notePdfUrl.ifBlank { null },
                        notePdfName.ifBlank { null },
                        triggerNotificationAlert
                    )
                    Toast.makeText(context, "Announcement posted dynamically, alerts triggers successfully emitted.", Toast.LENGTH_SHORT).show()
                    noteTitle = ""
                    noteContent = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_notice_submit_button")
        ) {
            Text("BROADCAST ANNOUNCEMENT NOW", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AdminPublishEventForm(viewModel: MainViewModel) {
    val context = LocalContext.current
    var eTitle by remember { mutableStateOf("") }
    var eDesc by remember { mutableStateOf("") }
    var ePdfUrl by remember { mutableStateOf("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = eTitle,
            onValueChange = { eTitle = it },
            label = { Text("Event Title Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_event_title")
        )

        OutlinedTextField(
            value = eDesc,
            onValueChange = { eDesc = it },
            label = { Text("Event Overview Description") },
            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("add_event_desc")
        )

        OutlinedTextField(
            value = ePdfUrl,
            onValueChange = { ePdfUrl = it },
            label = { Text("Event Guidelines Brochure Link (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_event_pdfurl")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (eTitle.isBlank() || eDesc.isBlank()) {
                    Toast.makeText(context, "Please enter Event name and descriptions.", Toast.LENGTH_SHORT).show()
                } else {
                    val dateVal = System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000L) // Set in future
                    viewModel.addEvent(eTitle, eDesc, dateVal, null, ePdfUrl.ifBlank { null })
                    Toast.makeText(context, "Event scheduled successfully, student lists populated.", Toast.LENGTH_SHORT).show()
                    eTitle = ""
                    eDesc = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_event_submit_button")
        ) {
            Text("SCHEDULE UNIVERSITY EVENT", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AdminPublishBusForm(viewModel: MainViewModel) {
    val context = LocalContext.current
    var bRoute by remember { mutableStateOf("") }
    var bNumber by remember { mutableStateOf("") }
    var bTimes by remember { mutableStateOf("Trips: 08:30 AM, 02:45 PM") }
    var bFreq by remember { mutableStateOf("Daily (Weekly Schedule)") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = bRoute,
            onValueChange = { bRoute = it },
            label = { Text("Route (e.g. Alampur to campus)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_bus_route")
        )

        OutlinedTextField(
            value = bNumber,
            onValueChange = { bNumber = it },
            label = { Text("Bus Identifier Number (e.g. Metro-09)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_bus_number")
        )

        OutlinedTextField(
            value = bTimes,
            onValueChange = { bTimes = it },
            label = { Text("Schedule Timings text") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_bus_times")
        )

        OutlinedTextField(
            value = bFreq,
            onValueChange = { bFreq = it },
            label = { Text("Transit days frequency") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_bus_freq")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (bRoute.isBlank() || bNumber.isBlank()) {
                    Toast.makeText(context, "Please enter Route name and Bus code.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addBusSchedule(
                        bRoute,
                        bNumber,
                        bTimes,
                        bFreq,
                        "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
                    )
                    Toast.makeText(context, "Bus schedule uploaded into local transit files.", Toast.LENGTH_SHORT).show()
                    bRoute = ""
                    bNumber = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_bus_submit_button")
        ) {
            Text("COMMIT BUS TRANSIT PATH", fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AdminPublishResultForm(viewModel: MainViewModel) {
    val context = LocalContext.current
    var resSem by remember { mutableStateOf("") }
    var resDept by remember { mutableStateOf("CSE") }
    var resPdfUrl by remember { mutableStateOf("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf") }
    var resFileName by remember { mutableStateOf("CSE_Semester_Grades.pdf") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = resSem,
            onValueChange = { resSem = it },
            label = { Text("Semester Description (e.g., Spring 2026 Primary Final)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_result_semester")
        )

        OutlinedTextField(
            value = resDept,
            onValueChange = { resDept = it },
            label = { Text("Department Context (BBA, CSE, EEE)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_result_department")
        )

        OutlinedTextField(
            value = resPdfUrl,
            onValueChange = { resPdfUrl = it },
            label = { Text("Result Grades Booklet PDF URL Link") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_result_pdfurl")
        )

        OutlinedTextField(
            value = resFileName,
            onValueChange = { resFileName = it },
            label = { Text("Grades PDF File Name display") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("add_result_filename")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (resSem.isBlank() || resDept.isBlank()) {
                    Toast.makeText(context, "Please enter Semester and Department folders.", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.publishResult(resSem, resDept, resPdfUrl, resFileName)
                    Toast.makeText(context, "Exam grade catalogs successfully matched.", Toast.LENGTH_SHORT).show()
                    resSem = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_result_submit_button")
        ) {
            Text("PUBLISH SEMESTER GRADES BOOKLET", fontWeight = FontWeight.ExtraBold)
        }
    }
}

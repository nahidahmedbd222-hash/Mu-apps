package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.example.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorHomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedWriteTab by remember { mutableIntStateOf(0) } // 0=Overview, 1=Manage modules
    
    // Check local permissions
    val noticesAllowed = viewModel.hasModeratorPermission("notices")
    val eventsAllowed = viewModel.hasModeratorPermission("events")
    val busAllowed = viewModel.hasModeratorPermission("bus")
    val resultsAllowed = viewModel.hasModeratorPermission("results")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Moderator Console", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Dept: " + (viewModel.currentUser?.department ?: "Academic Affairs"), fontSize = 11.sp, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("moderator_logout")
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
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedWriteTab == 0,
                    onClick = { selectedWriteTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Console Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("mod_nav_home")
                )
                NavigationBarItem(
                    selected = selectedWriteTab == 1,
                    onClick = { selectedWriteTab = 1 },
                    icon = { Icon(Icons.Default.Publish, contentDescription = "Publish Actions") },
                    label = { Text("Publish Content", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("mod_nav_publish")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedWriteTab == 0) {
                // MODERATOR HOME: Welcome message + local permissions review
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OFFICIAL RESPONSIBILITY PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Salam, " + (viewModel.currentUser?.username ?: "Academic Mod"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "Ensure academic integrity when posting. Notices will display immediately to students in realtime. Your active permission schema is assigned by the Master Admin.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                Text(
                    text = "YOUR SYSTEM PRIVILEGES MAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Render dynamic chips for notifications/privilege lists
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureCheckRow(title = "Upload notices & Announcements", allowed = noticesAllowed)
                    FeatureCheckRow(title = "Post and schedule varsity events", allowed = eventsAllowed)
                    FeatureCheckRow(title = "Upload transit schedules", allowed = busAllowed)
                    FeatureCheckRow(title = "Publish grade booklets", allowed = resultsAllowed)
                }

            } else {
                // MODERATOR PUBLISH ACTION FLOW: Dynamically check locks
                Text(
                    text = "CHOOSE PUBLISH TASK",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                var actionIndex by remember { mutableIntStateOf(0) }
                val allowedTabs = remember {
                    mutableListOf<String>().apply {
                        if (noticesAllowed) add("Post Announcement")
                        if (eventsAllowed) add("Add Event")
                        if (busAllowed) add("Add Transit Route")
                        if (resultsAllowed) add("Upload Result Grades")
                    }
                }

                if (allowedTabs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All Write Portals Locked", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text(
                                "Your administrator account has not been assigned any active write permissions. Please contact the Master Admin office.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    // Render tab rows
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allowedTabs.forEachIndexed { idx, title ->
                            val selected = actionIndex == idx
                            FilterChip(
                                selected = selected,
                                onClick = { actionIndex = idx },
                                label = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("mod_publish_chip_$idx")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Execute mapped screen matching active permissions index
                    val activeChoiceTitle = allowedTabs.getOrNull(actionIndex) ?: ""
                    when {
                        activeChoiceTitle.contains("Announcement") -> AdminPublishNoticeForm(viewModel)
                        activeChoiceTitle.contains("Event") -> AdminPublishEventForm(viewModel)
                        activeChoiceTitle.contains("Route") -> AdminPublishBusForm(viewModel)
                        activeChoiceTitle.contains("Result") -> AdminPublishResultForm(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCheckRow(title: String, allowed: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (allowed) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (allowed) Color(0xFF4CAF50) else Color(0xFFEA4335),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Box(
                modifier = Modifier
                    .background(
                        color = if (allowed) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFEA4335).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (allowed) "ALLOWED" else "RESTRICTED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (allowed) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}

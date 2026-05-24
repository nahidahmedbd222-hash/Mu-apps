package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarsityEventsScreen(viewModel: MainViewModel) {
    val events by viewModel.eventsList.collectAsState()
    val context = LocalContext.current
    var activeTabsUpcoming by remember { mutableStateOf(true) } // true=upcoming, false=past
    val now = System.currentTimeMillis()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("University Events", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.STUDENT_HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented toggle header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { activeTabsUpcoming = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("upcoming_events_tab"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTabsUpcoming) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeTabsUpcoming) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Upcoming", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { activeTabsUpcoming = false },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("past_events_tab"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!activeTabsUpcoming) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (!activeTabsUpcoming) Color.White else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Past Events", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Events List Filtered by Date
            val filteredEvents = remember(events, activeTabsUpcoming) {
                events.filter { event ->
                    if (activeTabsUpcoming) event.date >= now else event.date < now
                }
            }

            if (filteredEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (activeTabsUpcoming) "No Upcoming Events Scheduled" else "No Historic Events Logged",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredEvents) { event ->
                        EventItemCard(event, viewModel.isOnline) {
                            viewModel.selectedEvent = event
                        }
                    }
                }
            }
        }
    }

    // Modal dialog details overlay
    if (viewModel.selectedEvent != null) {
        val event = viewModel.selectedEvent!!
        AlertDialog(
            onDismissRequest = { viewModel.selectedEvent = null },
            confirmButton = {
                TextButton(onClick = { viewModel.selectedEvent = null }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (!event.pdfUrl.isNullOrEmpty()) {
                    TextButton(onClick = {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.pdfUrl))
                        context.startActivity(webIntent)
                    }) {
                        Text("DOWNLOAD BROCHURE", fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                ) {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = "Cover Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date(event.date)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!event.pdfUrl.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF File", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Attached Syllabus Guidelines", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Click brochure down below to view or print details.", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun EventItemCard(event: VarsityEvent, isOnline: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("event_item_card_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "cover Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(event.date)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = event.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = event.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!event.pdfUrl.isNullOrEmpty()) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF link",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Guide", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "View Event Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(viewModel: MainViewModel) {
    val results by viewModel.resultsList.collectAsState()
    val context = LocalContext.current
    var activeOptionWeb by remember { mutableStateOf(false) } // true=webview options, false=PDF catalogs

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Results Finder", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.STUDENT_HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Service Selector Toggle
            TabRow(selectedTabIndex = if (activeOptionWeb) 0 else 1) {
                Tab(
                    selected = activeOptionWeb,
                    onClick = { activeOptionWeb = true },
                    text = { Text("WebView Portal", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("result_tab_webview")
                )
                Tab(
                    selected = !activeOptionWeb,
                    onClick = { activeOptionWeb = false },
                    text = { Text("Result PDFs Folder", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("result_tab_pdf")
                )
            }

            if (activeOptionWeb) {
                // Option 1: WebView result portal mockup
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web Portal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Open MU Student Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "This matches Option 1: Open university result lookup portal from the app with modern responsive browser components.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "University Server Address",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "https://metrouni.edu.bd/result-portal/",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://metrouni.edu.bd/"))
                            context.startActivity(webIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("open_results_portal_web_button")
                    ) {
                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open Portal")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LAUNCH WEB PORTAL NOW", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Option 2: Uploaded Result PDFs list
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ImportContacts,
                                contentDescription = "None",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Published Results Found", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(results) { res ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("result_pdf_item_card_${res.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF file",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = res.department,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = res.semester,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Uploaded: " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(res.date)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            if (!res.pdfUrl.isNullOrEmpty()) {
                                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(res.pdfUrl))
                                                context.startActivity(webIntent)
                                            } else {
                                                Toast.makeText(context, "No dynamic PDF link assigned for this result row.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("result_download_pdf_button_${res.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = "Download results PDF",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact University", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.STUDENT_HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
            // Header University Brand Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Metropolitan University",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Alampur, Sylhet-3100, Bangladesh",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Text(
                text = "COMMUNICATION CHANNELS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Communications click items
            ContactItemCard(
                title = "Official Website Portal",
                subTitle = "metrouni.edu.bd",
                icon = Icons.Default.Language,
                tint = MaterialTheme.colorScheme.primary
            ) {
                triggerUrlLauncher(context, "https://metrouni.edu.bd")
            }

            ContactItemCard(
                title = "Official Facebook Page",
                subTitle = "facebook.com/MetropolitanUniversity",
                icon = Icons.Default.ThumbUp,
                tint = Color(0xFF1877F2) // Facebook Blue
            ) {
                triggerUrlLauncher(context, "https://www.facebook.com/MetropolitanUniversity")
            }

            ContactItemCard(
                title = "Academic Facebook Group",
                subTitle = "facebook.com/groups/metrouni",
                icon = Icons.Default.Group,
                tint = Color(0xFF00C7E2)
            ) {
                triggerUrlLauncher(context, "https://www.facebook.com/groups/metrouni")
            }

            ContactItemCard(
                title = "Telegram Broadcast Channel",
                subTitle = "t.me/metrouni",
                icon = Icons.Default.Send,
                tint = Color(0xFF0088CC) // Telegram Blue
            ) {
                triggerUrlLauncher(context, "https://telegram.org")
            }

            ContactItemCard(
                title = "Registrar Direct Hotline",
                subTitle = "+8801713301001",
                icon = Icons.Default.Phone,
                tint = Color(0xFF4CAF50) // Dial Green
            ) {
                triggerPhoneDialer(context, "+8801713301001")
            }

            ContactItemCard(
                title = "Academic Affairs Email Support",
                subTitle = "info@metrouni.edu.bd",
                icon = Icons.Default.Email,
                tint = Color(0xFFE91E63) // Email Rose tint
            ) {
                triggerEmailLauncher(context, "info@metrouni.edu.bd")
            }

            ContactItemCard(
                title = "Google Maps Location Map",
                subTitle = "View Metropolitan University on map",
                icon = Icons.Default.Place,
                tint = Color(0xFFEA4335) // Maps Red
            ) {
                triggerUrlLauncher(context, "https://maps.google.com/?q=Metropolitan+University+Sylhet+Alampur")
            }
        }
    }
}

@Composable
fun ContactItemCard(
    title: String,
    subTitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("contact_item_card_${title.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = tint.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (icon is ImageVector) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subTitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Launcher helper codes matching url_launcher functionality in Kotlin
fun triggerUrlLauncher(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open browser link context.", Toast.LENGTH_SHORT).show()
    }
}

fun triggerPhoneDialer(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot invoke dialing components.", Toast.LENGTH_SHORT).show()
    }
}

fun triggerEmailLauncher(context: Context, emailAddress: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")
            putExtra(Intent.EXTRA_SUBJECT, "Metropolitan University Portal Support Inquiry")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No email client installed on emulator.", Toast.LENGTH_SHORT).show()
    }
}

package com.tee.dosecare.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tee.dosecare.data.local.DoseLog
import com.tee.dosecare.data.local.DoseStatus
import com.tee.dosecare.data.local.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()
    val todayDoseLogs by viewModel.todayDoseLogs.collectAsState()
    val allDoseLogs by viewModel.allDoseLogs.collectAsState()

    // Compute streak
    val streak = viewModel.currentStreak.collectAsState().value

    // Group logs by date
    val groupedLogs = remember(allDoseLogs) {
        allDoseLogs
            .sortedByDescending { it.scheduledTime }
            .groupBy { log ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(log.scheduledTime))
            }
    }

    val takenTotal = allDoseLogs.count { it.status == DoseStatus.TAKEN }
    val totalLogs = allDoseLogs.size
    val adherencePercent = if (totalLogs > 0) (takenTotal * 100 / totalLogs) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dose History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (allDoseLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No history yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Your dose history will appear here\nonce you start taking medications",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // Stats row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Streak card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔥", fontSize = 20.sp)
                                Text(
                                    "$streak",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Day Streak",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Adherence card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF52B788).copy(alpha = 0.15f)
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📊", fontSize = 20.sp)
                                Text(
                                    "$adherencePercent%",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF52B788)
                                )
                                Text(
                                    "Adherence",
                                    fontSize = 11.sp,
                                    color = Color(0xFF52B788)
                                )
                            }
                        }

                        // Total doses card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("💊", fontSize = 20.sp)
                                Text(
                                    "$takenTotal",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Taken Total",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // History by date
                groupedLogs.forEach { (dateKey, logs) ->
                    item {
                        val displayDate = formatHistoryDate(dateKey)
                        Text(
                            text = displayDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(logs) { log ->
                        val medication = medications.find { it.id == log.medicationId }
                        HistoryLogCard(log = log, medication = medication)
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun HistoryLogCard(log: DoseLog, medication: Medication?) {
    val statusColor = when (log.status) {
        DoseStatus.TAKEN -> Color(0xFF52B788)
        DoseStatus.SKIPPED -> Color(0xFFFF6B35)
        DoseStatus.MISSED -> Color(0xFFE63946)
        else -> MaterialTheme.colorScheme.outline
    }

    val statusIcon = when (log.status) {
        DoseStatus.TAKEN -> "✅"
        DoseStatus.SKIPPED -> "⏭"
        DoseStatus.MISSED -> "❌"
        else -> "⏳"
    }

    val statusLabel = when (log.status) {
        DoseStatus.TAKEN -> "Taken"
        DoseStatus.SKIPPED -> "Skipped"
        DoseStatus.MISSED -> "Missed"
        else -> "Pending"
    }

    val scheduledTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(log.scheduledTime))

    val takenTime = log.takenTime?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(statusIcon, fontSize = 16.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    medication?.name ?: "Unknown Medication",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    buildString {
                        append("${medication?.dosage ?: ""} ${medication?.unit ?: ""}")
                        append(" • Scheduled $scheduledTime")
                        if (takenTime != null) append(" • Taken $takenTime")
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }

            // Status badge
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = statusColor.copy(alpha = 0.12f)
                )
            ) {
                Text(
                    statusLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun formatHistoryDate(dateKey: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(dateKey) ?: return dateKey
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { time = date }

        when {
            isSameDay(today, cal) -> "Today"
            isYesterday(today, cal) -> "Yesterday"
            else -> SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        dateKey
    }
}

private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(today: Calendar, other: Calendar): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    return isSameDay(yesterday, other)
}
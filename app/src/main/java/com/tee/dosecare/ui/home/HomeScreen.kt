package com.tee.dosecare.ui.home

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
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
fun HomeScreen(
    onAddMedication: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()
    val todayDoseLogs by viewModel.todayDoseLogs.collectAsState()

    val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning ☀☀\uFE0F️"
        hour < 17 -> "Good Afternoon 🌤️\uD83C\uDF24\uFE0F"
        else -> "Good Evening 🌙\uD83C\uDF19"
    }

    val takenCount = todayDoseLogs.count { it.status == DoseStatus.TAKEN }
    val totalCount = todayDoseLogs.size
    val pendingCount = todayDoseLogs.count { it.status == DoseStatus.PENDING }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(today, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                },
                actions = {
                    IconButton(onClick = onViewHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedication,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Med", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
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
                StatsRow(taken = takenCount, total = totalCount, pending = pendingCount)
            }

            // Adherence card
            item {
                AdherenceCard(taken = takenCount, total = totalCount)
            }

            // Quick tip
            if (pendingCount > 0) {
                item {
                    QuickReminderBanner(pendingCount = pendingCount)
                }
            }

            // Section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Medications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "${medications.size} active",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (medications.isEmpty()) {
                item { EmptyMedicationsCard(onAdd = onAddMedication) }
            } else {
                items(medications, key = { it.id }) { medication ->
                    val doseLog = todayDoseLogs.find { it.medicationId == medication.id }
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        MedicationCard(
                            medication = medication,
                            doseLog = doseLog,
                            onTaken = { log -> viewModel.markDose(log.id, DoseStatus.TAKEN) },
                            onSkipped = { log -> viewModel.markDose(log.id, DoseStatus.SKIPPED) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StatsRow(taken: Int, total: Int, pending: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$taken",
            label = "Taken",
            color = Color(0xFF52B788),
            icon = Icons.Filled.Check
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$pending",
            label = "Pending",
            color = Color(0xFFFFB703),
            icon = Icons.Filled.Schedule
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$total",
            label = "Total",
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Filled.MedicalServices
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun QuickReminderBanner(pendingCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("⏰", fontSize = 20.sp)
            Column {
                Text("Reminder", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF856404))
                Text(
                    "You have $pendingCount dose${if (pendingCount > 1) "s" else ""} pending today",
                    fontSize = 12.sp,
                    color = Color(0xFF856404)
                )
            }
        }
    }
}

@Composable
fun AdherenceCard(taken: Int, total: Int) {
    val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Adherence", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "$taken of $total doses",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(180.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$percentage%",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    doseLog: DoseLog?,
    onTaken: (DoseLog) -> Unit,
    onSkipped: (DoseLog) -> Unit
) {
    val statusColor = when (doseLog?.status) {
        DoseStatus.TAKEN -> Color(0xFF52B788)
        DoseStatus.SKIPPED -> Color(0xFFFF6B35)
        DoseStatus.MISSED -> Color(0xFFE63946)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    val statusLabel = when (doseLog?.status) {
        DoseStatus.TAKEN -> "✅ Taken"
        DoseStatus.SKIPPED -> "⏭ Skipped"
        DoseStatus.MISSED -> "❌ Missed"
        DoseStatus.PENDING -> "⏳ Pending"
        else -> "Not scheduled"
    }

    val cardColor = when (doseLog?.status) {
        DoseStatus.TAKEN -> Color(0xFFE8F5E9)
        DoseStatus.SKIPPED -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color dot + icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "${medication.dosage} ${medication.unit}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        medication.frequency,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status badge
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f))
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

            // Time info
            val times = medication.times.removeSurrounding("[", "]")
                .split(",").map { it.trim().removeSurrounding("\"") }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        times.joinToString(", "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                if (doseLog != null && doseLog.status == DoseStatus.PENDING) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onSkipped(doseLog) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE63946))
                        ) {
                            Text("Skip", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = { onTaken(doseLog) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF52B788))
                        ) {
                            Text("Taken", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMedicationsCard(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(40.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💊", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No Medications Yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Start by adding your first medication\nto track your daily doses",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Medication")
            }
        }
    }
}
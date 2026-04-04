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
fun HomeScreen(
    onAddMedication: () -> Unit = {},
    onViewHistory: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: MedicationViewModel = hiltViewModel()
) {
    val medications by viewModel.medications.collectAsState()
    val todayDoseLogs by viewModel.todayDoseLogs.collectAsState()

    val today = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
    val takenCount = todayDoseLogs.count { it.status == DoseStatus.TAKEN }
    val totalCount = todayDoseLogs.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DoseCare 💊", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(today, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedication,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Medication", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Progress card
            item {
                AdherenceCard(taken = takenCount, total = totalCount)
            }

            // Today's doses header
            item {
                Text(
                    text = "Today's Medications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (medications.isEmpty()) {
                item {
                    EmptyMedicationsCard(onAdd = onAddMedication)
                }
            } else {
                items(medications) { medication ->
                    val doseLog = todayDoseLogs.find { it.medicationId == medication.id }
                    MedicationCard(
                        medication = medication,
                        doseLog = doseLog,
                        onTaken = { log -> viewModel.markDose(log.id, DoseStatus.TAKEN) },
                        onSkipped = { log -> viewModel.markDose(log.id, DoseStatus.SKIPPED) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun AdherenceCard(taken: Int, total: Int) {
    val progress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Progress",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$taken / $total doses taken",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
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
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${medication.dosage} ${medication.unit} • ${medication.frequency}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (doseLog != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = doseLog.status,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (doseLog != null && doseLog.status == DoseStatus.PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onSkipped(doseLog) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE5E5))
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Skip", tint = Color(0xFFE63946), modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onTaken(doseLog) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Taken", tint = Color(0xFF52B788), modifier = Modifier.size(18.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("No medications yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Tap the + button to add your first medication",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAdd) { Text("Add Medication") }
        }
    }
}
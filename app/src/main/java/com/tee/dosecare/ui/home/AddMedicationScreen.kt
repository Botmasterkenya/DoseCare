package com.tee.dosecare.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tee.dosecare.data.local.Medication
import com.tee.dosecare.utils.Resource
import androidx.compose.ui.platform.LocalContext
// Add at the top of the composable:

// Then update the Button onClick:




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("mg") }
    var frequency by remember { mutableStateOf("Daily") }
    var time1 by remember { mutableStateOf("08:00") }
    var notes by remember { mutableStateOf("") }

    val operationState by viewModel.operationState.collectAsState()

    LaunchedEffect(operationState) {
        if (operationState is Resource.Success) {
            viewModel.resetOperationState()
            onNavigateBack()
        }
    }

    val units = listOf("mg", "ml", "tablets", "capsules", "drops")
    val frequencies = listOf("Daily", "Twice daily", "Three times daily", "Weekly", "As needed")

    var unitExpanded by remember { mutableStateOf(false) }
    var frequencyExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // Validation
    val isTimeValid = remember(time1) {
        val parts = time1.split(":")
        parts.size == 2 && parts[0].toIntOrNull()?.let { it in 0..23 } == true
                && parts[1].toIntOrNull()?.let { it in 0..59 } == true
    }
    val isSaveEnabled = name.isNotBlank() && dosage.isNotBlank() && isTimeValid
            && operationState !is Resource.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Medication", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Medication Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Medication Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = name.isBlank()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage *") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = dosage.isBlank()
                )

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { unit = option; unitExpanded = false }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = frequencyExpanded,
                onExpandedChange = { frequencyExpanded = it }
            ) {
                OutlinedTextField(
                    value = frequency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frequency") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = frequencyExpanded,
                    onDismissRequest = { frequencyExpanded = false }
                ) {
                    frequencies.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { frequency = option; frequencyExpanded = false }
                        )
                    }
                }
            }

            Text("Reminder Time", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = time1,
                onValueChange = { time1 = it },
                label = { Text("Time (HH:MM) *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("08:00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !isTimeValid,
                supportingText = {
                    if (!isTimeValid) Text("Enter a valid time e.g. 08:00 or 14:30")
                }
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            val errorMessage = (operationState as? Resource.Error)?.message
            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val medication = Medication(
                        name = name.trim(),
                        dosage = dosage.trim(),
                        unit = unit,
                        frequency = frequency,
                        times = "[\"$time1\"]",
                        startDate = System.currentTimeMillis(),
                        notes = notes.trim()
                    )
                    viewModel.addMedication(medication, context)
                },
                enabled = isSaveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (operationState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Medication", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ModalProgramarHorario(
    fechaInicio: LocalDate,
    fechaFin: LocalDate,
    horaInicio: String,
    horaFin: String,
    formatoFechaApp: DateTimeFormatter,
    esPeriodoValido: Boolean,
    onAbrirCalendarioInicio: () -> Unit,
    onAbrirTimePickerInicio: () -> Unit,
    onAbrirCalendarioFin: () -> Unit,
    onAbrirTimePickerFin: () -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Horario", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Configura el inicio y término del evento:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inicio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onAbrirCalendarioInicio,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(fechaInicio.format(formatoFechaApp))
                        }
                        OutlinedButton(
                            onClick = onAbrirTimePickerInicio,
                            modifier = Modifier.weight(0.7f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(horaInicio)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Término", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onAbrirCalendarioFin,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(fechaFin.format(formatoFechaApp))
                        }
                        OutlinedButton(
                            onClick = onAbrirTimePickerFin,
                            modifier = Modifier.weight(0.7f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(horaFin)
                        }
                    }
                }

                if (!esPeriodoValido) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "La fecha de término debe ser posterior al inicio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar, enabled = esPeriodoValido) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

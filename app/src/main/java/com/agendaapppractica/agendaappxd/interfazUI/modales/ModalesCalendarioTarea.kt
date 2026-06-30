package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.agendaapppractica.agendaappxd.model.FeriadoChile
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalCalendarioInicio(
    fechaInicio: LocalDate,
    feriados: List<FeriadoChile>,
    onFechaSeleccionada: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fecha de Inicio") },
        text = {
            CalendarioChile(
                fechaSeleccionada = fechaInicio,
                tareas = emptyList(),
                feriados = feriados,
                onFechaSeleccionada = onFechaSeleccionada
            )
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Aceptar") } }
    )
}

@Composable
fun ModalCalendarioFin(
    fechaFin: LocalDate,
    feriados: List<FeriadoChile>,
    onFechaSeleccionada: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fecha de Término") },
        text = {
            CalendarioChile(
                fechaSeleccionada = fechaFin,
                tareas = emptyList(),
                feriados = feriados,
                onFechaSeleccionada = onFechaSeleccionada
            )
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Aceptar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalTimePickerInicio(
    onHoraSeleccionada: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = 12, initialMinute = 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", state.hour, state.minute)
                onHoraSeleccionada(horaFormateada)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Hora de Inicio") },
        text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalTimePickerFin(
    onHoraSeleccionada: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = 13, initialMinute = 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val horaFormateada = String.format(Locale.getDefault(), "%02d:%02d", state.hour, state.minute)
                onHoraSeleccionada(horaFormateada)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Hora de Término") },
        text = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TimePicker(state = state) } }
    )
}
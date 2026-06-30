package com.agendaapppractica.agendaappxd.interfazUI.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ComponenteRecordatorios(
    avisosSeleccionados: Set<Long>,
    onAvisosCambio: (Set<Long>) -> Unit
) {
    var mostrarModalAvisos by remember { mutableStateOf(false) }

    val opcionesAvisos = listOf(
        "Al empezar" to 0L,
        "15 min antes" to 15L,
        "30 min antes" to 30L,
        "1 hora antes" to 60L,
        "2 horas antes" to 120L,
        "1 día antes" to 1440L,
        "2 días antes" to 2880L
    )

    val textoResumen = remember(avisosSeleccionados) {
        if (avisosSeleccionados.isEmpty()) {
            "Sin recordatorios"
        } else {
            "${avisosSeleccionados.size} seleccionados"
        }
    }

    OutlinedTextField(
        value = textoResumen,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = { Text("Configurar Avisos") },
        leadingIcon = { Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { mostrarModalAvisos = true },
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLeadingIconColor = MaterialTheme.colorScheme.primary
        )
    )

    if (mostrarModalAvisos) {
        AlertDialog(
            onDismissRequest = { mostrarModalAvisos = false },
            title = { Text("Programar Avisos", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opcionesAvisos.forEach { opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nuevoSet = if (avisosSeleccionados.contains(opcion.second)) {
                                        avisosSeleccionados - opcion.second
                                    } else {
                                        avisosSeleccionados + opcion.second
                                    }
                                    onAvisosCambio(nuevoSet)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = avisosSeleccionados.contains(opcion.second),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opcion.first, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { mostrarModalAvisos = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModalProgramarAvisos(
    avisosTemporales: Set<Long>,
    onCambiarAvisos: (Set<Long>) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    val mapaOpciones = remember {
        linkedMapOf(
            0L to "Al empezar",
            15L to "15 min antes",
            30L to "30 min antes",
            60L to "1 hora antes",
            120L to "2 horas antes",
            1440L to "1 día antes",
            2880L to "2 días antes"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Avisos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona cuándo deseas recibir alertas automáticas:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mapaOpciones.forEach { (minutos: Long, etiqueta: String) ->
                        val seleccionado = avisosTemporales.contains(minutos)
                        FilterChip(
                            selected = seleccionado,
                            onClick = {
                                val nuevoSet = if (seleccionado) {
                                    avisosTemporales - minutos
                                } else {
                                    avisosTemporales + minutos
                                }
                                onCambiarAvisos(nuevoSet)
                            },
                            label = { Text(etiqueta) }
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        confirmButton = {
            Button(onClick = onConfirmar) { Text("Aceptar") }
        }
    )
}
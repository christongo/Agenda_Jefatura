package com.agendaapppractica.agendaappxd.interfazUI.modales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModalProgramarAvisos(
    avisosTemporales: Set<Long>,
    onCambiarAvisos: (Set<Long>) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    var valorPersonalizado by remember { mutableStateOf("") }
    var tipoUnidadPersonalizada by remember { mutableStateOf(0) }
    val opcionesUnidades = listOf("Min", "Horas", "Días")
    var expandirMenuUnidades by remember { mutableStateOf(false) }

    val mapaPredeterminado = remember {
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

    val opcionesTotales = remember(avisosTemporales) {
        val mapaCompleto = LinkedHashMap(mapaPredeterminado)
        avisosTemporales.forEach { minutos ->
            if (!mapaCompleto.containsKey(minutos)) {
                val etiquetaVisual = when {
                    minutos % 1440L == 0L -> "${minutos / 1440L} días antes"
                    minutos % 60L == 0L -> "${minutos / 60L} horas antes"
                    else -> "$minutos min antes"
                }
                mapaCompleto[minutos] = etiquetaVisual
            }
        }
        mapaCompleto.entries.sortedBy { it.key }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Avisos", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Selecciona cuándo deseas recibir alertas automáticas:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    opcionesTotales.forEach { (minutos, etiqueta) ->
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

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    "Aviso personalizado:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = valorPersonalizado,
                        onValueChange = { input ->
                            valorPersonalizado = input.filter { it.isDigit() }
                        },
                        placeholder = { Text("Ej: 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Box {
                        OutlinedButton(
                            onClick = { expandirMenuUnidades = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(opcionesUnidades[tipoUnidadPersonalizada])
                        }
                        DropdownMenu(
                            expanded = expandirMenuUnidades,
                            onDismissRequest = { expandirMenuUnidades = false }
                        ) {
                            opcionesUnidades.forEachIndexed { indice, unidad ->
                                DropdownMenuItem(
                                    text = { Text(unidad) },
                                    onClick = {
                                        tipoUnidadPersonalizada = indice
                                        expandirMenuUnidades = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val cantidad = valorPersonalizado.toLongOrNull()
                            if (cantidad != null && cantidad > 0L) {
                                val minutosCalculados = when (tipoUnidadPersonalizada) {
                                    1 -> cantidad * 60L
                                    2 -> cantidad * 1440L
                                    else -> cantidad
                                }
                                onCambiarAvisos(avisosTemporales + minutosCalculados)
                                valorPersonalizado = ""
                            }
                        },
                        enabled = valorPersonalizado.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir aviso manual")
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

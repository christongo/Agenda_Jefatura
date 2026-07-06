package com.agendaapppractica.agendaappxd.interfazUI.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ComponenteRecordatorios(
    avisosSeleccionados: Set<Long>,
    onAvisosCambio: (Set<Long>) -> Unit
) {
    var mostrarModalAvisos by remember { mutableStateOf(false) }

    var valorPersonalizado by remember { mutableStateOf("") }
    var tipoUnidadPersonalizada by remember { mutableStateOf(0) }
    val opcionesUnidades = listOf("Min", "Horas", "Días")
    var expandirMenuUnidades by remember { mutableStateOf(false) }

    val opcionesPredeterminadas = listOf(
        0L to "Al empezar",
        15L to "15 min antes",
        30L to "30 min antes",
        60L to "1 hora antes",
        120L to "2 horas antes",
        1440L to "1 día antes",
        2880L to "2 días antes"
    )

    val opcionesAvisosTotales = remember(avisosSeleccionados) {
        val mapaFijo = opcionesPredeterminadas.toMap()
        val listaCompleta = opcionesPredeterminadas.toMutableList()

        avisosSeleccionados.forEach { minutos ->
            if (!mapaFijo.containsKey(minutos)) {
                val textoVisual = when {
                    minutos % 1440L == 0L -> "${minutos / 1440L} días antes (Personalizado)"
                    minutos % 60L == 0L -> "${minutos / 60L} horas antes (Personalizado)"
                    else -> "$minutos min antes (Personalizado)"
                }
                listaCompleta.add(minutos to textoVisual)
            }
        }
        listaCompleta.sortedBy { it.first }
    }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    opcionesAvisosTotales.forEach { (minutos, texto) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val nuevoSet = if (avisosSeleccionados.contains(minutos)) {
                                        avisosSeleccionados - minutos
                                    } else {
                                        avisosSeleccionados + minutos
                                    }
                                    onAvisosCambio(nuevoSet)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = avisosSeleccionados.contains(minutos),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(texto, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Text("Añadir aviso personalizado:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)

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
                            OutlinedButton(onClick = { expandirMenuUnidades = true }) {
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
                                val cantidadLong = valorPersonalizado.toLongOrNull()
                                if (cantidadLong != null && cantidadLong > 0L) {
                                    val minutosCalculados = when (tipoUnidadPersonalizada) {
                                        1 -> cantidadLong * 60L
                                        2 -> cantidadLong * 1440L
                                        else -> cantidadLong
                                    }
                                    onAvisosCambio(avisosSeleccionados + minutosCalculados)
                                    valorPersonalizado = ""
                                }
                            },
                            enabled = valorPersonalizado.isNotBlank(),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir aviso")
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
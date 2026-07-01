package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun DialogoPersonalizacion(
    modoOscuroActivo: Boolean,
    onModoOscuroCambiado: (Boolean) -> Unit,
    colorTemaActual: String,
    onColorTemaCambiChanged: (String) -> Unit,
    tipoTexturaActual: String,
    onTipoTexturaCambiado: (String) -> Unit,
    grosorLineaActual: Float,
    onGrosorLineaCambiado: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personalización", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tema Oscuro", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = modoOscuroActivo, onCheckedChange = onModoOscuroCambiado)
                }

                HorizontalDivider()

                Text("Color del Sistema", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                val coloresLocales = listOf(
                    "Morado" to Color(0xFF9C27B0),
                    "Azul" to Color(0xFF2196F3),
                    "Verde" to Color(0xFF4CAF50),
                    "Rojo" to Color(0xFFE53935),
                    "Naranja" to Color(0xFFFF9800),
                    "Rosa" to Color(0xFFE91E63),
                    "Cian" to Color(0xFF00BCD4)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    coloresLocales.take(4).forEach { (nombre, colorReal) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorReal)
                                .clickable { onColorTemaCambiChanged(nombre) }
                        ) {
                            if (colorTemaActual == nombre) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.align(Alignment.Center).size(18.dp))
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("Textura de las Notas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                val opcionesTextura = listOf("Ninguno", "Líneas")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    opcionesTextura.forEach { textura ->
                        FilterChip(
                            selected = tipoTexturaActual == textura,
                            onClick = { onTipoTexturaCambiado(textura) },
                            label = { Text(textura, fontSize = 12.sp) }
                        )
                    }
                }

                if (tipoTexturaActual == "Líneas") {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grosor de línea", style = MaterialTheme.typography.bodyMedium)
                            Text("${grosorLineaActual.roundToInt()} px", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = grosorLineaActual,
                            onValueChange = onGrosorLineaCambiado,
                            valueRange = 1f..6f,
                            steps = 4
                        )
                    }
                }

                Text("Vista previa del papel:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                val lineaColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (tipoTexturaActual == "Líneas") {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var y = 35.dp.toPx()
                                while (y < size.height) {
                                    drawLine(
                                        color = lineaColor,
                                        start = Offset(12.dp.toPx(), y),
                                        end = Offset(size.width - 12.dp.toPx(), y),
                                        strokeWidth = grosorLineaActual
                                    )
                                    y += 24.dp.toPx()
                                }
                            }
                        }
                        Text(
                            text = "Comienza a escribir aquí...",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 14.dp).padding(top = 45.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Aceptar") }
        }
    )
}
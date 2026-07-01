package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.BuildConfig
import com.agendaapppractica.agendaappxd.networkData.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun TarjetaActualizacion() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var buscando by remember { mutableStateOf(false) }

    var mostrarDialogo by remember { mutableStateOf(false) }
    var infoActualizacion by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SystemUpdate, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Buscar actualizaciones", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("Verificar manualmente", style = MaterialTheme.typography.bodySmall)
            }
            if (buscando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            buscando = true
                            val resultado = UpdateManager.verificarActualizacion()
                            buscando = false

                            if (resultado != null) {
                                infoActualizacion = resultado
                                mostrarDialogo = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Ya estás en la última versión (v${BuildConfig.VERSION_NAME})",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text("Buscar")
                }
            }
        }
    }

    if (mostrarDialogo && infoActualizacion != null) {
        DialogoActualizacion(
            versionNueva = infoActualizacion!!.versionName,
            onDescargar = {
                mostrarDialogo = false
                Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                UpdateManager.descargarEInstalarApk(
                    context = context,
                    urlApk = infoActualizacion!!.urlApk,
                    nombreVersion = infoActualizacion!!.versionName
                )
            },
            onDismiss = { mostrarDialogo = false }
        )
    }
}
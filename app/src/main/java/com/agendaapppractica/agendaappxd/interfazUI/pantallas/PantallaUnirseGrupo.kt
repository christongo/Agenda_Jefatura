package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaUnirseGrupo(
    onVolver: () -> Unit
) {
    val firestore = remember { FirestoreManager() }
    var codigoGroup by remember { mutableStateOf("") }

    var cargando by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val esCodigoValido = codigoGroup.trim().length == 6

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Unirse con Código", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onVolver, enabled = !cargando) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingresa al Espacio",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Digita el código numérico de 6 dígitos que te facilitó el administrador de la comunidad para enviar tu solicitud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = codigoGroup,
                onValueChange = { input ->
                    if (input.length <= 6) {
                        codigoGroup = input.filter { it.isDigit() }
                    }
                },
                label = { Text("Código de Acceso (6 números)") },
                placeholder = { Text("Ej: 548312") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando,
                supportingText = {
                    Text(
                        text = "${codigoGroup.length} / 6 dígitos",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (esCodigoValido) {
                        cargando = true

                        firestore.unirseAGrupo(codigoGroup.trim()) { exitoso, mensaje ->
                            cargando = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = mensaje,
                                    duration = if (exitoso) SnackbarDuration.Short else SnackbarDuration.Long
                                )
                                if (exitoso) {
                                    onVolver()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = esCodigoValido && !cargando,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Enviar Solicitud de Ingreso", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
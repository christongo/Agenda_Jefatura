package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.layout.*
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

    // Estados para controlar la respuesta de la base de datos
    var cargando by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Grupos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
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

            // Icono de cabecera estético
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unirse a un Grupo",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pídele el código de invitación al administrador del grupo e ingrésalo abajo para sincronizar tus eventos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto enfocado en el código
            OutlinedTextField(
                value = codigoGroup,
                onValueChange = { codigoGroup = it },
                label = { Text("Código del Grupo") },
                placeholder = { Text("Ej: 123456") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (codigoGroup.isNotBlank()) {
                        cargando = true

                        // 🔥 CORREGIDO: Ahora validamos el resultado real que devuelve Firebase
                        firestore.unirseAGrupo(codigoGroup) { exitoso ->
                            cargando = false
                            if (exitoso) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "¡Solicitud enviada al administrador!",
                                        duration = SnackbarDuration.Short
                                    )
                                    onVolver() // Volvemos de forma segura solo si se envió con éxito
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "El código ingresado no existe. Verifica e intenta de nuevo.",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = codigoGroup.isNotBlank() && !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Unirse al grupo", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
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

    var cargando by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val esCodigoValido = codigoGroup.trim().length >= 10

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Grupos") },
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
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unirse a un Grupo",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Copia el código de acceso largo desde los detalles del grupo e ingrésalo abajo para enviar tu solicitud.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = codigoGroup,
                onValueChange = { input ->
                    codigoGroup = input.filter { it.isLetterOrDigit() }
                },
                label = { Text("Código de Acceso del Grupo") },
                placeholder = { Text("Ej: ycUYMjdkO2QK37I30gav") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !cargando,
                supportingText = {
                    Text(
                        text = "Caracteres introducidos: ${codigoGroup.length}",
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

                        firestore.unirseAGrupo(codigoGroup.trim()) { exitoso ->
                            cargando = false
                            if (exitoso) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "¡Solicitud enviada al administrador!",
                                        duration = SnackbarDuration.Short
                                    )
                                    onVolver()
                                }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "El código no existe o ya eres miembro de este grupo.",
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
                enabled = esCodigoValido && !cargando
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
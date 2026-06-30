package com.agendaapppractica.agendaappxd.interfazUI.componentes

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.agendaapppractica.agendaappxd.networkData.FirestoreManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarDetalleGrupo(
    nombreGrupo: String,
    pestañaSeleccionada: Int,
    esAdminOCreador: Boolean,
    esCreador: Boolean,
    grupoId: String,
    onVolver: () -> Unit,
    onRefrescar: () -> Unit,
    onEditarInfoClick: () -> Unit,
    onEliminarGrupoClick: () -> Unit
) {
    var mostrarMenuTopBar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val firestore = remember { FirestoreManager() }

    TopAppBar(
        title = { Text(nombreGrupo) },
        navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
        actions = {
            if (pestañaSeleccionada == 0) {
                IconButton(onClick = onRefrescar) { Icon(Icons.Default.Refresh, null) }
            }
            if (pestañaSeleccionada == 0 && esAdminOCreador) {
                IconButton(onClick = onEditarInfoClick) { Icon(Icons.Default.Edit, null) }
            }
            Box {
                IconButton(onClick = { mostrarMenuTopBar = true }) { Icon(Icons.Default.MoreVert, null) }
                DropdownMenu(expanded = mostrarMenuTopBar, onDismissRequest = { mostrarMenuTopBar = false }) {
                    if (esCreador) {
                        DropdownMenuItem(
                            text = { Text("Eliminar Grupo", color = MaterialTheme.colorScheme.error) },
                            onClick = { mostrarMenuTopBar = false; onEliminarGrupoClick() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Salir del Grupo") },
                            onClick = {
                                mostrarMenuTopBar = false
                                firestore.salirDelGrupo(grupoId)
                                Toast.makeText(context, "Has salido del grupo", Toast.LENGTH_SHORT).show()
                                onVolver()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                        )
                    }
                }
            }
        }
    )
}
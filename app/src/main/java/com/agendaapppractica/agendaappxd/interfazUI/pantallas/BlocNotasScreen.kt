package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agendaapppractica.agendaappxd.datos.modelos.Nota
import com.agendaapppractica.agendaappxd.model.NotaManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BlocNotasScreen() {
    val context = LocalContext.current
    val notaManager = remember { NotaManager(context) }

    var listaNotas by remember { mutableStateOf(notaManager.obtenerNotas()) }
    var txtTitulo by remember { mutableStateOf("") }
    var txtContenido by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mi Bloc de Notas",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = txtTitulo,
                    onValueChange = { txtTitulo = it },
                    label = { Text("Título de la nota") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = txtContenido,
                    onValueChange = { txtContenido = it },
                    label = { Text("Escribe algo...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        if (txtTitulo.isNotBlank() || txtContenido.isNotBlank()) {
                            notaManager.guardarNota(txtTitulo, txtContenido)
                            listaNotas = notaManager.obtenerNotas()
                            txtTitulo = ""
                            txtContenido = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Nota")
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listaNotas) { nota ->
                val sdf = SimpleDateFormat("HH:mm - dd MMM yyyy", Locale.getDefault())
                val fechaYHoraStr = sdf.format(Date(nota.fechaCreacion))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nota.titulo.ifBlank { "Sin Título" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nota.contenido,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Creado: $fechaYHoraStr",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = {
                            notaManager.eliminarNota(nota.id)
                            listaNotas = notaManager.obtenerNotas()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
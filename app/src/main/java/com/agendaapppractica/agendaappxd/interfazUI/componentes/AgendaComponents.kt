package com.agendaapppractica.agendaappxd.interfazUI.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agendaapppractica.agendaappxd.model.Tarea

@Composable
fun TareaItem(tarea: Tarea) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = tarea.titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (tarea.descripcion.isNotEmpty()) {

                Text(
                    text = tarea.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Fecha: ${tarea.fecha}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun FormularioTarea(
    onGuardar: (String, String) -> Unit
) {

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Nueva Tarea",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {

                    if (titulo.isNotBlank()) {

                        onGuardar(titulo, descripcion)

                        titulo = ""
                        descripcion = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {

                Text("Guardar")
            }
        }
    }
}
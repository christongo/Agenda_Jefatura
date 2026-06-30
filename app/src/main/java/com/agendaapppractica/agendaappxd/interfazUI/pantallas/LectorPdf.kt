package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LectorPdf(pdfUriString: String, navController: NavController) {
    val context = LocalContext.current
    val archivoUri = Uri.parse(pdfUriString)
    val archivo = File(archivoUri.path ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(archivo.name.ifEmpty { "Visualizador PDF" }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (archivo.exists()) {
                            val uriParaCompartir = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                archivo
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uriParaCompartir)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir Documento"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir o Imprimir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {

            if (archivo.exists()) {
                Text(
                    text = "Documento cargado correctamente.\nUsa el botón superior para compartirlo, enviarlo por correo o mandarlo a imprimir.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                Text(text = "Error: El archivo no existe o fue eliminado.", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
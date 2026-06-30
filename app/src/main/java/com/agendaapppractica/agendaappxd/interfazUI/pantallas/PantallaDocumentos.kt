package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDocumentos(navController: NavController) {
    val context = LocalContext.current
    var listaPdfs by remember { mutableStateOf<List<File>>(emptyList()) }

    val actualizarLista = {
        val directorio = context.getExternalFilesDir(null)
        val archivos = directorio?.listFiles { file -> file.extension.lowercase() == "pdf" }?.toList()
        listaPdfs = archivos ?: emptyList()
    }

    LaunchedEffect(Unit) {
        actualizarLista()
    }

    val opcionesEscaner = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultForm(RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }

    val escaner = remember { GmsDocumentScanning.getClient(opcionesEscaner) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { resultado ->
        if (resultado.resultCode == AppCompatActivity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(resultado.data)
            scanResult?.pdf?.let { pdf ->
                val directorioDestino = context.getExternalFilesDir(null)
                val archivoDestino = File(directorioDestino, "Scanner_${System.currentTimeMillis()}.pdf")

                context.contentResolver.openInputStream(pdf.uri)?.use { input ->
                    archivoDestino.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                Toast.makeText(context, "Documento guardado con éxito", Toast.LENGTH_SHORT).show()
                actualizarLista()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Documentos PDF") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val actividad = context as? AppCompatActivity
                    if (actividad != null) {
                        escaner.getStartScanIntent(actividad)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Error: El contexto no es válido.", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = { Icon(Icons.Default.DocumentScanner, null) },
                text = { Text("Escanear") }
            )
        }
    ) { paddingValues ->
        if (listaPdfs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No tienes ningún documento escaneado.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(listaPdfs) { archivo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val uriString = Uri.fromFile(archivo).toString()
                            val encodedUri = URLEncoder.encode(uriString, "UTF-8")
                            navController.navigate("lector_pdf/$encodedUri")
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = archivo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "${(archivo.length() / 1024)} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
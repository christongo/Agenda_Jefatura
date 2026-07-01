package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

private fun Context.findActivity(): ComponentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is ComponentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDocumentos(navController: NavController) {
    val context = LocalContext.current
    var listaPdfs by remember { mutableStateOf<List<File>>(emptyList()) }

    var archivoParaVer by remember { mutableStateOf<File?>(null) }

    var ultimoArchivoVisto by remember { mutableStateOf<File?>(null) }

    var seccionSeleccionada by remember { mutableStateOf(0) }

    var archivoParaRenombrar by remember { mutableStateOf<File?>(null) }
    var nuevoNombreTexto by remember { mutableStateOf("") }
    var mostrarDialogRenombrar by remember { mutableStateOf(false) }

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
            .setResultFormats(RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
    }

    val escaner = remember { GmsDocumentScanning.getClient(opcionesEscaner) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
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
            Column {
                TopAppBar(
                    title = { Text("Mis Documentos Corporativos") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )

                TabRow(selectedTabIndex = seccionSeleccionada) {
                    Tab(
                        selected = seccionSeleccionada == 0,
                        onClick = { seccionSeleccionada = 0 },
                        text = { Text("Todos los PDFs") },
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                    )
                    Tab(
                        selected = seccionSeleccionada == 1,
                        onClick = { seccionSeleccionada = 1 },
                        text = { Text("Último Visto") },
                        icon = { Icon(Icons.Default.History, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val actividad = context.findActivity()
                    if (actividad != null) {
                        escaner.getStartScanIntent(actividad)
                            .addOnSuccessListener { intentSender ->
                                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Error: El contexto no contiene una Activity válida.", Toast.LENGTH_SHORT).show()
                    }
                },
                icon = { Icon(Icons.Default.DocumentScanner, null) },
                text = { Text("Escanear") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (seccionSeleccionada == 0) {
                // ==========================================
                if (listaPdfs.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Tip: Pulsa los tres puntos (⋮) al lado de cualquier documento para cambiar su nombre de forma rápida.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (listaPdfs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No tienes ningún documento escaneado.", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(listaPdfs) { archivo ->
                            var mostrarMenuOpciones by remember { mutableStateOf(false) }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        archivoParaVer = archivo
                                        ultimoArchivoVisto = archivo // Recordamos que este fue el último leído
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = archivo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text(text = "${(archivo.length() / 1024)} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Box {
                                        IconButton(onClick = { mostrarMenuOpciones = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                        }
                                        DropdownMenu(expanded = mostrarMenuOpciones, onDismissRequest = { mostrarMenuOpciones = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Cambiar nombre") },
                                                onClick = {
                                                    mostrarMenuOpciones = false
                                                    archivoParaRenombrar = archivo
                                                    nuevoNombreTexto = archivo.nameWithoutExtension
                                                    mostrarDialogRenombrar = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                val archivoActualEfectivo = ultimoArchivoVisto

                if (archivoActualEfectivo == null || !archivoActualEfectivo.exists()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Aún no has abierto ningún documento en esta sesión corporativa.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Retomar última lectura:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { archivoParaVer = archivoActualEfectivo },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            border = PaddingValues(1.dp).let { androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) }
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = archivoActualEfectivo.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Toca para abrir de inmediato en la última página", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    archivoParaVer?.let { archivo ->
        VisorPdfInternoDialog(archivo = archivo, onDismiss = { archivoParaVer = null })
    }

    if (mostrarDialogRenombrar && archivoParaRenombrar != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogRenombrar = false },
            title = { Text("Renombrar Documento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Introduce el nuevo nombre para este archivo corporativo:", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = nuevoNombreTexto,
                        onValueChange = { nuevoNombreTexto = it },
                        singleLine = true,
                        label = { Text("Nombre del archivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val archivoOriginal = archivoParaRenombrar!!
                        if (nuevoNombreTexto.isNotBlank()) {
                            val nuevoNombreConExtension = if (nuevoNombreTexto.lowercase().endsWith(".pdf")) nuevoNombreTexto else "$nuevoNombreTexto.pdf"
                            val destino = File(archivoOriginal.parentFile, nuevoNombreConExtension)

                            if (archivoOriginal.renameTo(destino)) {
                                Toast.makeText(context, "Nombre modificado", Toast.LENGTH_SHORT).show()
                                // Si renombramos el que estaba marcado como último visto, actualizamos la referencia
                                if (ultimoArchivoVisto == archivoOriginal) {
                                    ultimoArchivoVisto = destino
                                }
                                actualizarLista()
                            } else {
                                Toast.makeText(context, "No se pudo cambiar el nombre", Toast.LENGTH_SHORT).show()
                            }
                        }
                        mostrarDialogRenombrar = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogRenombrar = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun VisorPdfInternoDialog(archivo: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var indicePagina by remember { mutableStateOf(0) }
    var totalPaginas by remember { mutableStateOf(0) }
    var bitmapActual by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(archivo, indicePagina) {
        runCatching {
            val parcelFileDescriptor = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(parcelFileDescriptor)
            totalPaginas = renderer.pageCount

            if (totalPaginas > 0) {
                val pagina = renderer.openPage(indicePagina)
                val bitmap = Bitmap.createBitmap(pagina.width * 2, pagina.height * 2, Bitmap.Config.ARGB_8888)
                pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmapActual = bitmap
                pagina.close()
            }
            renderer.close()
            parcelFileDescriptor.close()
        }.onFailure {
            Toast.makeText(context, "Error abriendo páginas del documento", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(archivo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text("Pág. ${indicePagina + 1} de $totalPaginas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    if (bitmapActual != null) {
                        AndroidView(
                            factory = { ctx -> ImageView(ctx).apply { adjustViewBounds = true; scaleType = ImageView.ScaleType.FIT_CENTER } },
                            update = { imageView -> imageView.setImageBitmap(bitmapActual) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { if (indicePagina > 0) indicePagina-- }, enabled = indicePagina > 0, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Anterior")
                    }
                    Button(onClick = { if (indicePagina < totalPaginas - 1) indicePagina++ }, enabled = indicePagina < totalPaginas - 1, shape = RoundedCornerShape(8.dp)) {
                        Text("Siguiente")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
                    }
                }
            }
        }
    }
}
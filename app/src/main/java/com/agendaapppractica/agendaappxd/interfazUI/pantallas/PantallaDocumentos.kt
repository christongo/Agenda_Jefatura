package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

// === FUNCIÓN NATIVA DE COMPARTIR ===

private fun compartirPdf(context: Context, archivo: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir Documento vía:"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir archivo", Toast.LENGTH_SHORT).show()
    }
}

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

    val prefs = remember { context.getSharedPreferences("DocumentosTutorialPrefs", Context.MODE_PRIVATE) }
    var mostrarTutorial by remember { mutableStateOf(false) }

    val actualizarLista = {
        val directorio = context.getExternalFilesDir(null)
        val archivos = directorio?.listFiles { file -> file.extension.lowercase() == "pdf" }?.toList()
        listaPdfs = archivos ?: emptyList()
    }

    LaunchedEffect(Unit) {
        val yaVisto = prefs.getBoolean("ocultar_tutorial_documentos", false)
        if (!yaVisto) {
            mostrarTutorial = true
        }
        actualizarLista()
    }

    val opcionesEscaner = remember {
        GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setResultFormats(RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
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
                    },
                    actions = {
                        IconButton(onClick = { mostrarTutorial = true }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Ver guía de documentos",
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                                        ultimoArchivoVisto = archivo
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = archivo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                        Text(text = "${(archivo.length() / 1024)} KB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Box {
                                        IconButton(onClick = { mostrarMenuOpciones = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                        }
                                        DropdownMenu(expanded = mostrarMenuOpciones, onDismissRequest = { mostrarMenuOpciones = false }) {
                                            DropdownMenuItem(
                                                text = { Text("Compartir documento pdf") },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    mostrarMenuOpciones = false
                                                    compartirPdf(context, archivo)
                                                }
                                            )
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = archivoActualEfectivo.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
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

    if (mostrarTutorial) {
        DialogoTutorialDocumentos(
            onDismiss = { mostrarTutorial = false },
            onNoMostrarMas = {
                prefs.edit().putBoolean("ocultar_tutorial_documentos", true).apply()
                mostrarTutorial = false
                Toast.makeText(context, "Asistente de documentos desactivado", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// === COMPONENTE DEL TUTORIAL POR PASOS ===

@Composable
fun DialogoTutorialDocumentos(
    onDismiss: () -> Unit,
    onNoMostrarMas: () -> Unit
) {
    var pasoActual by remember { mutableStateOf(1) }
    val totalPasos = 3
    val progresoAnimado = pasoActual.toFloat() / totalPasos.toFloat()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (pasoActual) {
                            1 -> Icons.Default.DocumentScanner
                            2 -> Icons.Default.Share
                            else -> Icons.Default.History
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (pasoActual) {
                        1 -> "Digitalización ML Kit"
                        2 -> "Flujo de Distribución"
                        else -> "Persistencia de Lectura"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progresoAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sección $pasoActual de $totalPasos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 130.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    when (pasoActual) {
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Escáner de Actas Físicas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Botón Flotante Inferior: Presione 'Escanear' para activar la cámara del dispositivo con detección automática de bordes.\n" +
                                        "* Importación de Galería: El módulo le permite procesar imágenes capturadas con anterioridad y estructurarlas en un PDF consolidado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        2 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Opciones e Intercambio Nativos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Menú Desplegable (⋮): Permite modificar el título de sus archivos guardados de forma inmediata.\n" +
                                        "* Compartir Documento PDF: Utilice la integración nativa del sistema para distribuir el reporte directamente mediante canales corporativos como WhatsApp.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        3 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Pestaña de Historial",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "* Sección Último Visto: Registra el último archivo abierto dentro del módulo.\n" +
                                        "* Retorno Rápido: Pulsar la tarjeta central le permite reanudar su lectura en el visor interno optimizado sin necesidad de buscar el archivo nuevamente.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNoMostrarMas) {
                        Text(
                            text = "No volver a mostrar",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }

                    Button(
                        onClick = {
                            if (pasoActual < totalPasos) {
                                pasoActual++
                            } else {
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (pasoActual < totalPasos) "Siguiente" else "Comenzar",
                            fontWeight = FontWeight.Bold
                        )
                        if (pasoActual < totalPasos) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisorPdfInternoDialog(archivo: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var paginaActual by remember { mutableStateOf(0) }
    var totalPaginas by remember { mutableStateOf(0) }
    var bitmapPagina by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(archivo, paginaActual) {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            totalPaginas = pdfRenderer.pageCount

            if (totalPaginas > 0) {
                val pagina = pdfRenderer.openPage(paginaActual)

                val bitmap = Bitmap.createBitmap(pagina.width * 2, pagina.height * 2, Bitmap.Config.ARGB_8888)
                pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmapPagina = bitmap

                pagina.close()
            }
            pdfRenderer.close()
            parcelFileDescriptor.close()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al procesar renderizado de página", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Permite pantalla completa ocupando los márgenes correctos
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar Visor")
                    }
                    Text(
                        text = archivo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                    Text(
                        text = "${paginaActual + 1} / $totalPaginas",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapPagina != null) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    adjustViewBounds = true
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                }
                            },
                            update = { imageView ->
                                imageView.setImageBitmap(bitmapPagina)
                            }
                        )
                    } else {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (paginaActual > 0) paginaActual-- },
                        enabled = paginaActual > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anterior")
                    }

                    TextButton(
                        onClick = { if (paginaActual < totalPaginas - 1) paginaActual++ },
                        enabled = paginaActual < totalPaginas - 1
                    ) {
                        Text("Siguiente")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    }
}
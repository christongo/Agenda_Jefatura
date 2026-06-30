package com.agendaapppractica.agendaappxd.interfazUI.pantallas

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.agendaapppractica.agendaappxd.networkData.UpdateManager
import com.agendaapppractica.agendaappxd.ui.theme.AgendaappxdTheme
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenedorApp(
    sharedPref: SharedPreferences,
    darkTheme: Boolean,
    colorTema: String,
    tipoTextura: String,
    grosorLinea: Float,
    onDarkThemeChange: (Boolean) -> Unit,
    onColorTemaChange: (String) -> Unit,
    onTipoTexturaChange: (String) -> Unit,
    onGrosorLineaChange: (Float) -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var infoActualizacion by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }

    // 🚀 SISTEMA DE ACTUALIZACIÓN: Comprobar al arrancar el menú principal
    LaunchedEffect(Unit) {
        infoActualizacion = UpdateManager.verificarActualizacion()
    }

    // Alerta de actualización disponible In-App
    if (infoActualizacion != null) {
        AlertDialog(
            onDismissRequest = { infoActualizacion = null },
            title = { Text("Actualización disponible") },
            text = { Text("Hay una nueva versión disponible (v${infoActualizacion?.versionName}). ¿Deseas descargarla e instalarla ahora?") },
            confirmButton = {
                Button(
                    onClick = {
                        infoActualizacion?.let { info ->
                            UpdateManager.descargarEInstalarApk(
                                context = context,
                                urlApk = info.urlApk,
                                nombreVersion = info.versionName
                            )
                        }
                        infoActualizacion = null
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { infoActualizacion = null }) {
                    Text("Más tarde")
                }
            }
        )
    }

    AgendaappxdTheme(darkTheme = darkTheme, colorTema = colorTema) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    PantallaAjustes(
                        modoOscuroActivo = darkTheme,
                        onModoOscuroCambiado = { nuevoModo ->
                            onDarkThemeChange(nuevoModo)
                            sharedPref.edit().putBoolean("darkMode", nuevoModo).apply()
                        },
                        colorTemaActual = colorTema,
                        onColorTemaCambiado = { nuevoColor ->
                            onColorTemaChange(nuevoColor)
                            sharedPref.edit().putString("colorTema", nuevoColor).apply()
                        },
                        tipoTexturaActual = tipoTextura,
                        onTipoTexturaCambiado = { nuevaTextura ->
                            onTipoTexturaChange(nuevaTextura)
                            sharedPref.edit().putString("tipoTextura", nuevaTextura).apply()
                        },
                        grosorLineaActual = grosorLinea,
                        onGrosorLineaCambiado = { nuevoGrosor ->
                            onGrosorLineaChange(nuevoGrosor)
                            sharedPref.edit().putFloat("grosorLinea", nuevoGrosor).apply()
                        },
                        onCerrarSesion = {
                            onCerrarSesionClick()
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            // 🛠️ Agregamos las pantallas de documentos/lector al modo pantalla completa si no deseas barras superior/inferior en ellas
            val modoPantallaCompleta = currentRoute == "bloc_notas" ||
                    currentRoute?.startsWith("crear_nota/") == true ||
                    currentRoute == "documentos" ||
                    currentRoute?.startsWith("lector_pdf/") == true

            Scaffold(
                topBar = {
                    if (!modoPantallaCompleta) {
                        CenterAlignedTopAppBar(
                            title = { Text("Agenda") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, null)
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (!modoPantallaCompleta) {
                        NavigationBar {
                            val itemsNavegacion = listOf(
                                Triple("inicio", Icons.Default.Home, "Inicio"),
                                Triple("calendario", Icons.Default.CalendarMonth, "Calendario"),
                                Triple("eventos", Icons.Default.Event, "Eventos"),
                                Triple("grupos", Icons.Default.Group, "Grupos")
                            )

                            itemsNavegacion.forEach { (ruta, icono, etiqueta) ->
                                val esRutaGrupo = ruta == "grupos" && (currentRoute == "grupos" || currentRoute == "mis_grupos" || currentRoute == "unirse_grupo" || currentRoute == "crear_grupo")

                                NavigationBarItem(
                                    selected = currentRoute == ruta || esRutaGrupo,
                                    onClick = {
                                        if (currentRoute != ruta) {
                                            navController.navigate(ruta) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(icono, null) },
                                    label = { Text(etiqueta) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "inicio",
                    modifier = if (modoPantallaCompleta) Modifier else Modifier.padding(innerPadding)
                ) {
                    composable("inicio") { PantallaInicio(navController = navController) }
                    composable("calendario") { PantallaAgenda() }
                    composable("eventos") { PantallaEventos() }
                    composable("grupos") {
                        PantallaGrupos(
                            onUnirseGrupoClick = { navController.navigate("unirse_grupo") },
                            onMisGruposClick = { navController.navigate("mis_grupos") }
                        )
                    }
                    composable("mis_grupos") { PantallaGruposLista(onVolver = { navController.popBackStack() }) }
                    composable("unirse_grupo") { PantallaUnirseGrupo(onVolver = { navController.popBackStack() }) }
                    composable("bloc_notas") {
                        PantallaBlocNotas(
                            navController = navController,
                            onAbrirAjustes = { scope.launch { drawerState.open() } }
                        )
                    }
                    composable(
                        route = "crear_nota/{notaId}",
                        arguments = listOf(navArgument("notaId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val notaId = backStackEntry.arguments?.getString("notaId")
                        PantallaCrearNota(
                            navController = navController,
                            notaId = notaId,
                            tipoTextura = tipoTextura,
                            grosorLinea = grosorLinea
                        )
                    }

                    // 📸 🆕 NUEVO: Ruta para el listado de documentos y escáner nativo
                    composable("documentos") {
                        PantallaDocumentos(navController = navController)
                    }

                    // 📄 🆕 NUEVO: Ruta para visualizar el PDF
                    composable(
                        route = "lector_pdf/{pdfUri}",
                        arguments = listOf(navArgument("pdfUri") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedUri = backStackEntry.arguments?.getString("pdfUri") ?: ""
                        val decodedUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.name())
                        LectorPdf(pdfUriString = decodedUri, navController = navController)
                    }
                }
            }
        }
    }
}
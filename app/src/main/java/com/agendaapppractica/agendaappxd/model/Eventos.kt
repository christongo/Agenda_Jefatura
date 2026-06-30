package com.agendaapppractica.agendaappxd.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Eventos(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Calendario : Eventos(
        "calendario",
        "Calendario",
        Icons.Default.DateRange
    )

    object ListaEventos : Eventos(
        "eventos",
        "Eventos",
        Icons.Default.List
    )
}
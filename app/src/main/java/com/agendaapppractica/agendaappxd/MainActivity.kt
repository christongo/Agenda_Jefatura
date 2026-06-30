package com.agendaapppractica.agendaappxd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
// 🛠️ ESTE ES EL IMPORT QUE SOLUCIONA LOS DOS ERRORES DE COMPILACIÓN:
import com.agendaapppractica.agendaappxd.networkData.AutenticacionManager
import com.agendaapppractica.agendaappxd.interfazUI.pantallas.NavegacionPrincipal
import com.agendaapppractica.agendaappxd.networkData.NotificationHelper

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.crearCanal(this)

        // Limpieza obligatoria del estado del invitado local al arrancar de cero
        AutenticacionManager.sesionComoInvitadoLocal = false

        // Petición limpia de permisos de notificaciones para Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)

        setContent {
            // Se le hereda el SharedPreferences al enrutador modular en pantallas
            NavegacionPrincipal(sharedPref = sharedPref)
        }
    }
}
package com.agendaapppractica.agendaappxd.networkData

import android.content.Context
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

object LectorQRManager {


    fun iniciarEscaneoFuncional(context: Context, onResultadoExitoso: (String) -> Unit) {
        val scanner = GmsBarcodeScanning.getClient(context)

        Toast.makeText(context, "Abriendo cámara segura...", Toast.LENGTH_SHORT).show()

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val codigoTexto = barcode.rawValue
                if (!codigoTexto.isNullOrEmpty()) {
                    onResultadoExitoso(codigoTexto)
                } else {
                    Toast.makeText(context, "Código QR vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCanceledListener {
                Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al escanear: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}
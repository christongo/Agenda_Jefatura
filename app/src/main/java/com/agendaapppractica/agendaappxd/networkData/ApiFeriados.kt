package com.agendaapppractica.agendaappxd.networkData

import com.agendaapppractica.agendaappxd.model.FeriadoChile
import retrofit2.http.GET

data class RespuestaFeriados(
    val status: String,
    val data: List<FeriadoChile>
)

interface ApiFeriados {
    @GET("holidays.json") // Trae automáticamente todos los feriados del año actual
    suspend fun obtenerFeriados(): RespuestaFeriados
}
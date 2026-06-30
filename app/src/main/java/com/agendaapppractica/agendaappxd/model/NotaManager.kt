package com.agendaapppractica.agendaappxd.model

import android.content.Context
import com.agendaapppractica.agendaappxd.datos.modelos.Nota
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NotaManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("bloc_notas_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun obtenerNotas(): List<Nota> {
        val json = sharedPreferences.getString("lista_notas", null) ?: return emptyList()
        val type = object : TypeToken<List<Nota>>() {}.type
        val lista: List<Nota> = gson.fromJson(json, type)
        return lista.sortedByDescending { it.fechaCreacion }
    }

    fun guardarNota(titulo: String, contenido: String) {
        val notasActuales = obtenerNotas().toMutableList()
        val nuevaNota = Nota(titulo = titulo, contenido = contenido)
        notasActuales.add(nuevaNota)

        val json = gson.toJson(notasActuales)
        sharedPreferences.edit().putString("lista_notas", json).apply()
    }

    fun eliminarNota(id: String) {
        val notasActuales = obtenerNotas().filter { it.id != id }
        val json = gson.toJson(notasActuales)
        sharedPreferences.edit().putString("lista_notas", json).apply()
    }
}
package com.example.cuidadoyvirtudapp.domain

import java.util.Calendar

/**
 * Lógica pura (sin Android) de la agenda de recordatorios.
 * Importante: los avisos son recordatorios programados por un funcionario (horas fijas), NO una predicción de la IA.
 */
object Agenda {
    /** clave -> (ícono, nombre) */
    val TIPOS = linkedMapOf("remedio" to ("💊" to "Remedio"), "cita" to ("🩺" to "Cita"), "llamada" to ("📞" to "Llamada"), "otro" to ("📝" to "Otro"))

    private val HORA = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    fun horaValida(h: String) = HORA.matches(h)

    fun texto(hora24Hora: Int, minuto: Int) = "%02d:%02d".format(hora24Hora, minuto)

    /** Texto del aviso que se lee/ve la persona mayor. */
    fun textoAviso(tipo: String, titulo: String): String = when (tipo) {
        "remedio" -> "Es hora de su remedio: $titulo"
        "cita" -> "Recuerde su cita: $titulo"
        "llamada" -> "Es hora de su llamada: $titulo"
        else -> "Recordatorio: $titulo"
    }

    /** Próxima vez (en milisegundos) en que debe sonar un aviso, o null si ya pasó (evento de una sola vez). */
    fun proximaMillis(hora: String, repeticion: String, fecha: String?, ahora: Calendar): Long? {
        require(horaValida(hora)) { "Hora inválida: $hora" }
        val (h, m) = hora.split(":").map { it.toInt() }
        val c = ahora.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, h); c.set(Calendar.MINUTE, m); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        if (repeticion == "una_vez") {
            val p = fecha?.split("-")?.map { it.toInt() } ?: return null
            c.set(p[0], p[1] - 1, p[2], h, m, 0); c.set(Calendar.MILLISECOND, 0)
            return if (c.timeInMillis > ahora.timeInMillis) c.timeInMillis else null
        }
        if (c.timeInMillis <= ahora.timeInMillis) c.add(Calendar.DAY_OF_YEAR, 1)
        return c.timeInMillis
    }

    /** ¿Ahora estamos dentro de los primeros [ventanaMin] minutos de la hora del aviso? */
    fun esDebido(hora: String, ahora: Calendar, ventanaMin: Int = 3): Boolean {
        if (!horaValida(hora)) return false
        val (h, m) = hora.split(":").map { it.toInt() }
        val diff = (ahora.get(Calendar.HOUR_OF_DAY) * 60 + ahora.get(Calendar.MINUTE)) - (h * 60 + m)
        return diff in 0 until ventanaMin
    }
}

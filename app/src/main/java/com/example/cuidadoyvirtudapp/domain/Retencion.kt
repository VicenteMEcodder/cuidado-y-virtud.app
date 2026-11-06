package com.example.cuidadoyvirtudapp.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Retención limitada de lo que la persona decide contar con sus palabras: se borra sola a los 15 días.
 * Lógica pura (sin Android) para poder probarla.
 */
object Retencion {
    const val DIAS = 15
    private const val DIA_MS = 24L * 60 * 60 * 1000

    private fun fmt() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    /** Fecha y hora (ISO) antes de la cual una nota ya venció. */
    fun corte(ahora: Date, dias: Int = DIAS): String = fmt().format(Date(ahora.time - dias * DIA_MS))

    /** ¿Esta nota (con su fecha ISO) ya cumplió el plazo? Las fechas ISO se comparan como texto. */
    fun vencida(ts: String, ahora: Date, dias: Int = DIAS): Boolean = ts < corte(ahora, dias)

    /** Fecha (ISO) en que se borrará una nota creada en [ts]. */
    fun venceEl(ts: String, dias: Int = DIAS): String = fmt().format(Date(fmt().parse(ts)!!.time + dias * DIA_MS))
}

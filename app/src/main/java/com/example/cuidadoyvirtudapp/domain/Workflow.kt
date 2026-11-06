package com.example.cuidadoyvirtudapp.domain

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.example.cuidadoyvirtudapp.data.database.Db
import org.json.JSONArray
import org.json.JSONObject

/**
 * Flujo con decisión humana obligatoria.
 *  1. Máquina de estados: GENERADA -> PENDIENTE_REVISION -> (APROBADA | DESCARTADA | EN_ESPERA) -> EJECUTADA.
 *     No existe PENDIENTE_REVISION -> EJECUTADA.
 *  2. Una decisión exige quién decide, acción válida y justificación (>= 10 caracteres).
 *  3. Trigger de SQLite: no se inserta una derivación si la alerta no está APROBADA.
 *  4. La IA (Detector) solo crea alertas en PENDIENTE_REVISION; no tiene función de envío.
 *  5. Bitácora de cada hecho. Los "envíos" son SIMULADOS.
 */
class BloqueoHumano(msg: String) : Exception(msg)

object Workflow {
    val ACCIONES = mapOf(
        "descartar" to "DESCARTADA", "esperar" to "EN_ESPERA", "llamar" to "APROBADA",
        "visita" to "APROBADA", "derivar" to "APROBADA",
    )
    val ETIQUETA_ACCION = mapOf(
        "descartar" to "Descartar (falso positivo)", "esperar" to "Esperar y reevaluar",
        "llamar" to "Llamar primero a la persona", "visita" to "Visita presencial", "derivar" to "Derivar a red de apoyo",
    )
    val TRANSICIONES = mapOf(
        "GENERADA" to setOf("PENDIENTE_REVISION"),
        "PENDIENTE_REVISION" to setOf("APROBADA", "DESCARTADA", "EN_ESPERA"),
        "EN_ESPERA" to setOf("APROBADA", "DESCARTADA", "EN_ESPERA"),
        "APROBADA" to setOf("EJECUTADA"),
        "DESCARTADA" to emptySet(),
        "EJECUTADA" to emptySet(),
    )
    val ABIERTAS = listOf("PENDIENTE_REVISION", "EN_ESPERA")
    const val MIN_JUSTIFICACION = 10

    /** Pura (testeable sin Android). */
    fun transicionPermitida(desde: String, hacia: String): Boolean = hacia in (TRANSICIONES[desde] ?: emptySet())

    /** Pura (testeable sin Android): valida lo que exige una decisión humana. */
    fun validarDecision(reviewer: String, accion: String, justificacion: String) {
        if (accion !in ACCIONES) throw IllegalArgumentException("Acción no válida")
        if (reviewer.isBlank()) throw BloqueoHumano("Falta la identidad de quien decide")
        if (justificacion.trim().length < MIN_JUSTIFICACION)
            throw BloqueoHumano("Ninguna acción se envía sin su decisión y justificación explícita (mínimo $MIN_JUSTIFICACION caracteres).")
    }

    /** Pura: toda decisión humana (incluida aprobar una presentación entre personas) exige justificación. */
    fun exigirJustificacion(justificacion: String) {
        if (justificacion.trim().length < MIN_JUSTIFICACION)
            throw BloqueoHumano("Escriba el motivo de su decisión (mínimo $MIN_JUSTIFICACION caracteres).")
    }

    fun mensajeMinimo(alias: String) = "Sugerimos comunicarse hoy con $alias. Gracias por acompañarle."

    fun bitacora(db: SQLiteDatabase, actor: String, evento: String, alertaId: Long? = null, personaId: String? = null, detalle: String = "") {
        db.insert("bitacora", null, ContentValues().apply {
            put("ts", Db.ahora()); put("actor", actor); put("evento", evento)
            if (alertaId != null) put("alerta_id", alertaId)
            if (personaId != null) put("persona_id", personaId)
            put("detalle", detalle)
        })
    }

    private fun estadoDe(db: SQLiteDatabase, id: Long): String =
        db.rawQuery("SELECT estado FROM alertas WHERE id=?", arrayOf(id.toString())).use {
            if (it.moveToFirst()) it.getString(0) else throw NoSuchElementException("alerta inexistente")
        }

    private fun transicionar(db: SQLiteDatabase, id: Long, nuevo: String) {
        val actual = estadoDe(db, id)
        if (!transicionPermitida(actual, nuevo)) throw BloqueoHumano("Transición no permitida: $actual -> $nuevo")
        db.execSQL("UPDATE alertas SET estado=? WHERE id=?", arrayOf(nuevo, id))
    }

    fun senalesAJson(s: List<Senal>): String = JSONArray().apply {
        s.forEach { put(JSONObject().put("clave", it.clave).put("titulo", it.titulo).put("z", it.z ?: JSONObject.NULL).put("texto", it.texto).put("fuerza", it.fuerza)) }
    }.toString()

    fun senalesDeJson(j: String): List<Senal> {
        val a = JSONArray(j)
        return (0 until a.length()).map {
            val o = a.getJSONObject(it)
            Senal(o.getString("clave"), o.getString("titulo"), if (o.isNull("z")) null else o.getDouble("z"), o.getString("texto"), o.getString("fuerza"))
        }
    }

    /** Lo único que puede hacer la IA: crear una alerta en revisión (si no hay otra abierta para la persona). */
    fun crearAlerta(db: SQLiteDatabase, a: AlertaCalc, origen: String): Long? {
        val abierta = db.rawQuery("SELECT id FROM alertas WHERE persona_id=? AND estado IN ('PENDIENTE_REVISION','EN_ESPERA')", arrayOf(a.personaId)).use { it.moveToFirst() }
        if (abierta) return null
        val id = db.insert("alertas", null, ContentValues().apply {
            put("persona_id", a.personaId); put("creada", Db.ahora()); put("origen", origen); put("nivel", a.nivel)
            put("score", a.score); put("senales", senalesAJson(a.senales)); put("explicacion", a.explicacion); put("estado", "GENERADA")
        })
        transicionar(db, id, "PENDIENTE_REVISION")
        bitacora(db, "IA (detector)", "Alerta generada y enviada a revisión humana", id, a.personaId, "Nivel ${a.nivel} · origen $origen")
        return id
    }

    class Resultado(val estado: String, val derivacionDestino: String?, val derivacionMensaje: String?)

    fun decidir(
        db: SQLiteDatabase, alertaId: Long, reviewer: String, accion: String, justificacion: String,
        alias: String, comparte: Boolean, excepcion: String = "", destino: String = "Red de apoyo",
    ): Resultado {
        validarDecision(reviewer, accion, justificacion)
        val estado = estadoDe(db, alertaId)
        if (estado !in ABIERTAS) throw BloqueoHumano("La alerta ya está $estado")
        if (accion == "derivar" && !comparte && excepcion.trim().length < MIN_JUSTIFICACION)
            throw BloqueoHumano("La persona no autorizó compartir su información: documente un motivo excepcional.")
        val pid = db.rawQuery("SELECT persona_id FROM alertas WHERE id=?", arrayOf(alertaId.toString())).use { it.moveToFirst(); it.getString(0) }

        db.beginTransaction()
        try {
            val decisionId = db.insert("decisiones", null, ContentValues().apply {
                put("alerta_id", alertaId); put("reviewer_id", reviewer.trim()); put("accion", accion)
                put("justificacion", justificacion.trim()); put("ts", Db.ahora())
            })
            val nuevo = ACCIONES.getValue(accion)
            transicionar(db, alertaId, nuevo)
            bitacora(db, reviewer.trim(), "Decisión: ${ETIQUETA_ACCION.getValue(accion)}", alertaId, pid, justificacion.trim())
            var msg: String? = null
            if (nuevo == "APROBADA") {
                if (accion == "derivar") {
                    msg = mensajeMinimo(alias)
                    try {
                        db.insert("derivaciones", null, ContentValues().apply {
                            put("alerta_id", alertaId); put("decision_id", decisionId); put("destino", destino); put("mensaje", msg); put("ts", Db.ahora())
                        }).also { if (it == -1L) throw BloqueoHumano("No se pudo registrar la derivación") }
                    } catch (e: SQLiteException) {
                        throw BloqueoHumano(e.message ?: "BLOQUEADO")
                    }
                }
                transicionar(db, alertaId, "EJECUTADA")
                bitacora(db, "Sistema", "Acción registrada para ejecución humana", alertaId, pid, ETIQUETA_ACCION.getValue(accion))
            }
            db.setTransactionSuccessful()
            return Resultado(if (nuevo == "APROBADA") "EJECUTADA" else nuevo, if (msg != null) destino else null, msg)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Simula que el sistema/IA intenta derivar SIN decisión humana. Debe ser bloqueado siempre mientras la alerta
     * no esté APROBADA (lo frena el trigger de la base de datos). Se usa en la demo.
     */
    fun intentarEnvioDirecto(db: SQLiteDatabase, alertaId: Long): String {
        val pid = db.rawQuery("SELECT persona_id FROM alertas WHERE id=?", arrayOf(alertaId.toString())).use {
            if (it.moveToFirst()) it.getString(0) else throw NoSuchElementException("alerta inexistente")
        }
        try {
            db.execSQL("INSERT INTO derivaciones(alerta_id, decision_id, destino, mensaje, ts) VALUES (?,0,?,?,?)",
                arrayOf(alertaId, "Red de apoyo", "intento sin aprobación", Db.ahora()))
        } catch (e: SQLiteException) {
            val motivo = e.message?.substringAfter("BLOQUEADO:", "")?.substringBefore(" (code")?.trim()
            val texto = "BLOQUEADO: " + (motivo?.takeIf { it.isNotEmpty() } ?: "la alerta no tiene aprobación humana")
            bitacora(db, "IA (intento automático)", "ENVÍO BLOQUEADO: no hay decisión humana", alertaId, pid, texto)
            throw BloqueoHumano(texto)
        }
        return "La alerta ya tenía aprobación humana"
    }
}

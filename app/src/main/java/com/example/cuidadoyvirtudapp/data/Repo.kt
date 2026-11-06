package com.example.cuidadoyvirtudapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.cuidadoyvirtudapp.domain.AlertaCalc
import com.example.cuidadoyvirtudapp.domain.BloqueoHumano
import com.example.cuidadoyvirtudapp.domain.Datos
import com.example.cuidadoyvirtudapp.domain.Detector
import com.example.cuidadoyvirtudapp.domain.Retencion
import com.example.cuidadoyvirtudapp.domain.Senal
import com.example.cuidadoyvirtudapp.domain.Workflow

data class PersonaPub(val id: String, val alias: String, val iniciales: String, val tramo: String, val zona: String)
data class ConsentVista(val vigente: Boolean, val comparte: Boolean, val avisarFamilia: Boolean, val socializar: Boolean = false)
data class Nota(val ts: String, val tipo: String, val categoria: String, val texto: String, val vence: String = "")
data class Impacto(
    val personas: Int, val visibles: Int, val enCola: Int, val revisar: Int, val observar: Int, val sinNovedad: Int,
    val decisiones: Int, val descartadas: Int, val bloqueados: Int, val derivaciones: Int, val propuestas: Int, val notas: Int,
)
data class Encuentro(val id: Long, val a: PersonaPub, val b: PersonaPub, val ts: String, val estado: String)
data class AlertaVista(
    val id: Long, val nivel: String, val estado: String, val origen: String, val creada: String, val explicacion: String,
    val senales: List<Senal>, val score: Double, val persona: PersonaPub, val consent: ConsentVista, val spark: List<Double?>,
)
data class Grafico(val titulo: String, val serie: List<Double?>, val base: Double)
data class BitacoraItem(val ts: String, val actor: String, val evento: String, val detalle: String, val personaId: String?)
data class Detalle(val alerta: AlertaVista, val grafico: Grafico, val bitacora: List<BitacoraItem>, val notas: List<Nota> = emptyList())
data class Cola(val fecha: String, val revisar: Int, val observar: Int, val sinNovedad: Int, val alertas: List<AlertaVista>, val noVisibles: Int)
data class PersonaAdmin(val persona: PersonaPub, val consentimiento: String, val comparte: Boolean)
data class Clasif(val tp: Int, val fp: Int, val fn: Int, val tn: Int) {
    val recall get() = if (tp + fn == 0) 0.0 else tp.toDouble() / (tp + fn)
    val precision get() = if (tp + fp == 0) 0.0 else tp.toDouble() / (tp + fp)
}
data class GrupoEq(val nombre: String, val n: Int, val alertadas: Int, val sinRiesgo: Int, val falsosPos: Int) {
    val tasaAlertas get() = if (n == 0) 0.0 else alertadas.toDouble() / n
    val tasaFp get() = if (sinRiesgo == 0) 0.0 else falsosPos.toDouble() / sinRiesgo
}
data class Equidad(
    val revisarObservar: Clasif, val soloRevisar: Clasif, val reservado: Pair<Int, Int>, val conectividad: Pair<Int, Int>,
    val porAtributo: Map<String, List<GrupoEq>>,
)
data class DemoPersona(val id: String, val saludo: String, val medCronica: Boolean)

/** Fachada de datos para la UI. Todo local (SQLite); datos 100 % sintéticos. */
object Repo {
    const val REVISOR = "Camila Valenzuela (cuidadora territorial)"
    private lateinit var helper: Db
    private lateinit var db: SQLiteDatabase
    lateinit var datos: Datos
        private set
    var resultados: Map<String, AlertaCalc> = emptyMap()
        private set

    /** Sube cada vez que cambia algo, para que las pantallas se recarguen. */
    var version by mutableIntStateOf(0)

    fun iniciar(ctx: Context) {
        helper = Db(ctx.applicationContext)
        db = helper.writableDatabase
        recargar()
        val vacio = db.rawQuery("SELECT COUNT(*) FROM alertas", null).use { it.moveToFirst(); it.getInt(0) == 0 }
        if (vacio) sembrarAlertas()
        asegurarPermisoDemo()
        purgarNotasVencidas()
        version++
    }

    private fun recargar() {
        datos = Db.cargarDatos(db)
        resultados = Detector.evaluarCohorte(datos)
    }

    private fun sembrarAlertas() {
        db.beginTransaction()
        try {
            for ((pid, r) in resultados) if (r.nivel != "Sin novedad" && puedeProcesar(pid)) Workflow.crearAlerta(db, r, "detector")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun reiniciarDemo() {
        helper.reiniciarDemo(db)
        recargar(); sembrarAlertas(); asegurarPermisoDemo(); version++
    }

    // ---------------- consentimiento ----------------
    private fun consent(pid: String): ConsentVista =
        db.rawQuery("SELECT comparte, avisar_familia, pausado, socializar FROM consentimientos WHERE persona_id=?", arrayOf(pid)).use {
            it.moveToFirst(); ConsentVista(it.getInt(2) == 0, it.getInt(0) == 1, it.getInt(1) == 1, it.getInt(3) == 1)
        }

    private fun puedeProcesar(pid: String): Boolean = consent(pid).let { it.vigente && it.comparte }

    fun leerConsentimiento(pid: String): ConsentVista = consent(pid)

    fun guardarConsentimiento(pid: String, comparte: Boolean, avisarFamilia: Boolean, pausado: Boolean, socializar: Boolean) {
        db.update("consentimientos", ContentValues().apply {
            put("comparte", if (comparte) 1 else 0); put("avisar_familia", if (avisarFamilia) 1 else 0)
            put("pausado", if (pausado) 1 else 0); put("socializar", if (socializar) 1 else 0); put("ts", Db.ahora())
        }, "persona_id=?", arrayOf(pid))
        Workflow.bitacora(db, "Persona mayor", "Consentimiento actualizado", null, pid, "comparte=$comparte, familia=$avisarFamilia, pausado=$pausado, conocer_personas=$socializar")
        version++
    }

    // ---------------- notas que la persona decide anotar ----------------
    /**
     * Anota en la ficha lo que la persona QUISO contar (o una opción codificada). El audio nunca se guarda.
     * La IA (Detector) NO lee este texto: es solo para que el funcionario entienda mejor el contexto.
     */
    fun guardarNota(pid: String, tipo: String, categoria: String, texto: String) {
        db.insert("notas_ficha", null, ContentValues().apply {
            put("persona_id", pid); put("ts", Db.ahora()); put("tipo", tipo); put("categoria", categoria); put("texto", texto)
        })
        // La bitácora guarda solo la categoría, no el relato.
        Workflow.bitacora(db, "Persona mayor", "Anotó en su ficha lo que quiso contar", null, pid, "categoría: $categoria")
        version++
    }

    fun notasDe(pid: String): List<Nota> {
        purgarNotasVencidas()
        val l = ArrayList<Nota>()
        db.rawQuery("SELECT ts, tipo, categoria, texto FROM notas_ficha WHERE persona_id=? ORDER BY id DESC LIMIT 5", arrayOf(pid))
            .use { while (it.moveToNext()) l += Nota(it.getString(0), it.getString(1), it.getString(2), it.getString(3), Retencion.venceEl(it.getString(0))) }
        return l
    }

    /** Retención limitada: lo que la persona contó con sus palabras se borra solo a los 15 días. */
    fun purgarNotasVencidas(): Int {
        val n = db.delete("notas_ficha", "ts < ?", arrayOf(Retencion.corte(java.util.Date())))
        if (n > 0) { Workflow.bitacora(db, "Sistema", "Borrado automático de notas (${Retencion.DIAS} días)", null, null, "$n nota(s) vencida(s)"); version++ }
        return n
    }

    fun contarNotas(pid: String): Int =
        db.rawQuery("SELECT COUNT(*) FROM notas_ficha WHERE persona_id=?", arrayOf(pid)).use { it.moveToFirst(); it.getInt(0) }

    /** Derecho de la persona a borrar lo que contó, cuando quiera. */
    fun borrarMisNotas(pid: String): Int {
        val n = db.delete("notas_ficha", "persona_id=?", arrayOf(pid))
        Workflow.bitacora(db, "Persona mayor", "Borró lo que había contado", null, pid, "$n nota(s) eliminada(s)")
        version++
        return n
    }

    /** Cifras reales de esta sesión de demostración (datos sintéticos). */
    fun impacto(): Impacto {
        fun cuenta(sql: String) = db.rawQuery(sql, null).use { it.moveToFirst(); it.getInt(0) }
        val c = cola()
        return Impacto(
            datos.personas.size, datos.personas.keys.count { puedeProcesar(it) }, c.alertas.size, c.revisar, c.observar, c.sinNovedad,
            cuenta("SELECT COUNT(*) FROM decisiones"), cuenta("SELECT COUNT(*) FROM decisiones WHERE accion='descartar'"),
            cuenta("SELECT COUNT(*) FROM bitacora WHERE evento LIKE 'ENVÍO BLOQUEADO%'"), cuenta("SELECT COUNT(*) FROM derivaciones"),
            cuenta("SELECT COUNT(*) FROM encuentros WHERE estado='PROPUESTA'"), cuenta("SELECT COUNT(*) FROM notas_ficha"),
        )
    }

    // ---------------- compañía entre personas (siempre con decisión humana) ----------------
    private fun sinPropuestaAbierta(a: String, b: String): Boolean =
        db.rawQuery("SELECT COUNT(*) FROM encuentros WHERE estado='PROPUESTA' AND ((persona_a=? AND persona_b=?) OR (persona_a=? AND persona_b=?))",
            arrayOf(a, b, b, a)).use { it.moveToFirst(); it.getInt(0) == 0 }

    /**
     * Otra persona del programa que HOY también dijo que no habló con nadie y que aceptó conocer a otras personas.
     * Solo se muestra su nombre de pila si ella dio ese consentimiento. Nunca se comparten teléfonos ni direcciones.
     */
    fun candidatoSocial(pid: String): PersonaPub? {
        if (!consent(pid).let { it.vigente && it.socializar }) return null
        val hoy = datos.fechas.size - 1
        val candidatos = datos.personas.values.filter { p ->
            p.id != pid && datos.series.getValue(p.id).contacto[hoy] == 0.0 &&
                consent(p.id).let { it.vigente && it.socializar } && sinPropuestaAbierta(pid, p.id)
        }
        return com.example.cuidadoyvirtudapp.domain.Social.elegirCandidato(datos.personas.getValue(pid), candidatos)?.let { pub(it.id) }
    }

    /** La persona dijo que SÍ le gustaría conocerla. Esto no las conecta: queda como propuesta para que un funcionario decida. */
    fun proponerEncuentro(a: String, b: String) {
        db.insert("encuentros", null, ContentValues().apply { put("persona_a", a); put("persona_b", b); put("ts", Db.ahora()); put("estado", "PROPUESTA") })
        Workflow.bitacora(db, "Persona mayor", "Quiere conocer a otra persona del programa", null, a, "propuesta con ${datos.personas.getValue(b).alias}")
        version++
    }

    fun propuestas(): List<Encuentro> {
        val l = ArrayList<Encuentro>()
        db.rawQuery("SELECT id, persona_a, persona_b, ts, estado FROM encuentros WHERE estado='PROPUESTA' ORDER BY id", null).use {
            while (it.moveToNext()) l += Encuentro(it.getLong(0), pub(it.getString(1)), pub(it.getString(2)), it.getString(3), it.getString(4))
        }
        return l
    }

    /** Lanza BloqueoHumano si falta la justificación. Aprobar = el funcionario consulta a ambas y organiza la presentación. */
    fun decidirEncuentro(id: Long, aprobar: Boolean, justificacion: String) {
        Workflow.exigirJustificacion(justificacion)
        val (a, b) = db.rawQuery("SELECT persona_a, persona_b FROM encuentros WHERE id=?", arrayOf(id.toString())).use { it.moveToFirst(); it.getString(0) to it.getString(1) }
        db.update("encuentros", ContentValues().apply {
            put("estado", if (aprobar) "APROBADO" else "RECHAZADO"); put("reviewer", REVISOR); put("justificacion", justificacion.trim()); put("decidido_ts", Db.ahora())
        }, "id=?", arrayOf(id.toString()))
        Workflow.bitacora(db, REVISOR, if (aprobar) "Presentación aprobada: consultar a ambas personas y presentarlas" else "Presentación no aprobada por ahora", null, a, justificacion.trim())
        Workflow.bitacora(db, REVISOR, "Decisión sobre presentación (persona sugerida)", null, b, justificacion.trim())
        version++
    }

    /** Demo: la persona de demostración ya tiene precargado su permiso de conocer gente, salvo que lo haya cambiado ella. */
    private fun asegurarPermisoDemo() {
        val p = personaDemo().id
        val cambio = db.rawQuery("SELECT COUNT(*) FROM bitacora WHERE persona_id=? AND evento='Consentimiento actualizado'", arrayOf(p)).use { it.moveToFirst(); it.getInt(0) > 0 }
        if (!cambio) db.execSQL("UPDATE consentimientos SET socializar=1 WHERE persona_id=?", arrayOf(p))
    }

    // ---------------- persona mayor ----------------
    private fun pub(pid: String): PersonaPub = datos.personas.getValue(pid).let {
        PersonaPub(pid, it.alias, it.alias.take(2).uppercase(), it.tramo, it.zona)
    }

    /** Persona de demostración: hombre, estable, vigente, comparte y con medicación crónica. (Selección solo para la demo.) */
    fun personaDemo(): DemoPersona {
        val estables = db.rawQuery("SELECT persona_id FROM etiquetas WHERE escenario='estable'", null).use { c ->
            val l = ArrayList<String>(); while (c.moveToNext()) l += c.getString(0); l.toSet()
        }
        val p = datos.personas.values.firstOrNull { it.id in estables && it.sexo == "hombre" && it.consentInicial == "vigente" && it.comparteInicial && it.medCronica }
            ?: datos.personas.values.first()
        val hon = if (p.sexo == "hombre") "don" else "doña" // solo para el saludo de la interfaz
        return DemoPersona(p.id, "$hon ${p.alias}", p.medCronica)
    }

    /** Guarda SOLO respuestas codificadas (nunca audio ni texto). Una sola respuesta no dispara alerta por sí sola. */
    fun registrarCheckin(pid: String, canal: String, animo: String?, contacto: String?, actividades: String?, medicacion: String?) {
        db.insert("checkins_vivo", null, ContentValues().apply {
            put("persona_id", pid); put("ts", Db.ahora()); put("canal", canal)
            put("animo", animo); put("contacto", contacto); put("actividades", actividades); put("medicacion", medicacion)
        })
        Db.aplicarCheckin(datos, pid, animo, contacto, actividades, medicacion)
        resultados = Detector.evaluarCohorte(datos)
        val r = resultados.getValue(pid)
        db.beginTransaction()
        try {
            if (r.nivel != "Sin novedad" && puedeProcesar(pid)) Workflow.crearAlerta(db, r, "checkin")
            Workflow.bitacora(db, "Persona mayor", "Check-in registrado", null, pid, "canal=$canal")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        version++
    }

    /** Pedido de ayuda iniciado POR LA PERSONA: alerta prioritaria para un humano. No llama ni avisa a nadie por sí solo. */
    fun sos(pid: String) {
        val senal = Senal("sos", "Pidió ayuda", null, "La persona presionó «Necesito ayuda ahora» y confirmó.", "fuerte")
        val expl = "La persona pidió ayuda por sí misma. Conviene comunicarse con ella cuanto antes."
        db.beginTransaction()
        try {
            val abierta = db.rawQuery("SELECT id FROM alertas WHERE persona_id=? AND estado IN ('PENDIENTE_REVISION','EN_ESPERA')", arrayOf(pid))
                .use { if (it.moveToFirst()) it.getLong(0) else null }
            if (abierta != null) {
                db.update("alertas", ContentValues().apply {
                    put("nivel", "Revisar"); put("origen", "sos"); put("explicacion", expl); put("senales", Workflow.senalesAJson(listOf(senal)))
                }, "id=?", arrayOf(abierta.toString()))
                Workflow.bitacora(db, "Persona mayor", "SOS: alerta existente escalada a Revisar", abierta, pid)
            } else {
                val id = Workflow.crearAlerta(db, AlertaCalc(pid, "Revisar", 99.0, listOf(senal), expl, false), "sos")
                Workflow.bitacora(db, "Persona mayor", "SOS solicitado por la persona", id, pid)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        version++
    }

    // ---------------- cuidador ----------------
    private fun vista(c: android.database.Cursor): AlertaVista {
        val pid = c.getString(c.getColumnIndexOrThrow("persona_id"))
        return AlertaVista(
            c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("nivel")), c.getString(c.getColumnIndexOrThrow("estado")),
            c.getString(c.getColumnIndexOrThrow("origen")), c.getString(c.getColumnIndexOrThrow("creada")), c.getString(c.getColumnIndexOrThrow("explicacion")),
            Workflow.senalesDeJson(c.getString(c.getColumnIndexOrThrow("senales"))), c.getDouble(c.getColumnIndexOrThrow("score")),
            pub(pid), consent(pid), Detector.serieBienestar(datos, pid, 30),
        )
    }

    fun cola(): Cola {
        val l = ArrayList<AlertaVista>()
        db.rawQuery("SELECT * FROM alertas WHERE estado IN ('PENDIENTE_REVISION','EN_ESPERA')", null).use { while (it.moveToNext()) l += vista(it) }
        val alertas = l.filter { puedeProcesar(it.persona.id) }
            .sortedWith(compareBy<AlertaVista> { if (it.nivel == "Revisar") 0 else 1 }.thenBy { if (it.origen == "sos") 0 else 1 }.thenByDescending { it.score })
        val visibles = datos.personas.keys.filter { puedeProcesar(it) }
        val conAlerta = alertas.map { it.persona.id }.toSet()
        return Cola(datos.hoy(), alertas.count { it.nivel == "Revisar" }, alertas.count { it.nivel == "Observar" },
            visibles.count { it !in conAlerta }, alertas, datos.personas.size - visibles.size)
    }

    fun detalle(id: Long): Detalle? {
        val a = db.rawQuery("SELECT * FROM alertas WHERE id=?", arrayOf(id.toString())).use { if (it.moveToFirst()) vista(it) else null } ?: return null
        val principal = a.senales.firstOrNull { it.clave in Detector.SENALES }?.clave
        val g = if (principal != null) {
            val (serie, base) = Detector.serieSenal(datos, a.persona.id, principal)
            Grafico(Detector.SENALES.getValue(principal).first, serie, base)
        } else Grafico("Bienestar general", a.spark, Detector.mediaBase(datos, a.persona.id))
        val bit = ArrayList<BitacoraItem>()
        db.rawQuery("SELECT ts, actor, evento, detalle, persona_id FROM bitacora WHERE alerta_id=? OR persona_id=? ORDER BY id DESC LIMIT 12",
            arrayOf(id.toString(), a.persona.id)).use { while (it.moveToNext()) bit += BitacoraItem(it.getString(0), it.getString(1), it.getString(2), it.getString(3) ?: "", it.getString(4)) }
        return Detalle(a, g, bit, notasDe(a.persona.id))
    }

    /** Lanza BloqueoHumano si falta la decisión/justificación. */
    fun decidir(alertaId: Long, accion: String, justificacion: String, excepcion: String): Workflow.Resultado {
        val (pid, _) = db.rawQuery("SELECT persona_id FROM alertas WHERE id=?", arrayOf(alertaId.toString())).use { it.moveToFirst(); it.getString(0) to 0 }
        val res = Workflow.decidir(db, alertaId, REVISOR, accion, justificacion, datos.personas.getValue(pid).alias, consent(pid).comparte, excepcion)
        version++
        return res
    }

    /** Demo del bloqueo: devuelve el mensaje de bloqueo (debe ocurrir siempre en alertas sin aprobación). */
    fun intentoEnvioSinAprobacion(alertaId: Long): String {
        return try { Workflow.intentarEnvioDirecto(db, alertaId) } catch (e: BloqueoHumano) { version++; e.message ?: "BLOQUEADO" }
    }

    fun bitacora(limit: Int = 60): List<BitacoraItem> {
        val l = ArrayList<BitacoraItem>()
        db.rawQuery("SELECT ts, actor, evento, detalle, persona_id FROM bitacora ORDER BY id DESC LIMIT ?", arrayOf(limit.toString()))
            .use { while (it.moveToNext()) l += BitacoraItem(it.getString(0), it.getString(1), it.getString(2), it.getString(3) ?: "", it.getString(4)) }
        return l
    }

    // ---------------- administrador (sin datos de salud) ----------------
    fun personasAdmin(): List<PersonaAdmin> = datos.personas.keys.map {
        val c = consent(it); PersonaAdmin(pub(it), if (c.vigente) "vigente" else "pausado", c.comparte)
    }

    // ---------------- equidad y transparencia ----------------
    fun equidad(): Equidad {
        val et = HashMap<String, Pair<String, Boolean>>()
        db.rawQuery("SELECT persona_id, escenario, riesgo FROM etiquetas", null).use { while (it.moveToNext()) et[it.getString(0)] = it.getString(1) to (it.getInt(2) == 1) }
        fun clasif(niveles: Set<String>): Clasif {
            var tp = 0; var fp = 0; var fn = 0; var tn = 0
            for ((pid, r) in resultados) {
                val real = et.getValue(pid).second; val pred = r.nivel in niveles
                if (real && pred) tp++ else if (!real && pred) fp++ else if (real && !pred) fn++ else tn++
            }
            return Clasif(tp, fp, fn, tn)
        }
        fun trampa(esc: String): Pair<Int, Int> {
            val ids = et.filter { it.value.first == esc }.keys
            return ids.count { resultados.getValue(it).nivel != "Sin novedad" } to ids.size
        }
        fun attr(f: (com.example.cuidadoyvirtudapp.domain.Persona) -> String): List<GrupoEq> =
            datos.personas.values.groupBy(f).map { (nombre, ps) ->
                val sinRiesgo = ps.filter { !et.getValue(it.id).second }
                GrupoEq(nombre, ps.size, ps.count { resultados.getValue(it.id).nivel != "Sin novedad" }, sinRiesgo.size,
                    sinRiesgo.count { resultados.getValue(it.id).nivel != "Sin novedad" })
            }.sortedBy { it.nombre }
        return Equidad(clasif(setOf("Revisar", "Observar")), clasif(setOf("Revisar")), trampa("reservado_estable"), trampa("baja_conectividad"),
            mapOf("Por zona" to attr { it.zona }, "Por sexo" to attr { it.sexo }, "Por tramo etario" to attr { it.tramo }))
    }
}

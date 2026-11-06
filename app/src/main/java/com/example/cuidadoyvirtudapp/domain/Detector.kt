package com.example.cuidadoyvirtudapp.domain

import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Detector de cambios de rutina — Tu Cuidado (port a Kotlin del detector de Python).
 *
 * La IA NO decide ni diagnostica: PRIORIZA y EXPLICA cambios respecto de la normalidad de cada persona.
 * Capa 1: línea base individual (z-score por señal, semana reciente vs historia propia).
 * Capa 2: Isolation Forest sobre el vector de cambios de la cohorte (escala casos con señales moderadas).
 * NO usa edad, sexo, zona ni ingreso como variables.
 */

data class Persona(
    val id: String, val alias: String, val tramo: String, val zona: String, val sexo: String,
    val medCronica: Boolean, val consentInicial: String, val comparteInicial: Boolean,
)

class Series(n: Int) {
    val contacto = DoubleArray(n) { Double.NaN }
    val actividades = DoubleArray(n) { Double.NaN }
    val animo = DoubleArray(n) { Double.NaN }
    val medicacion = DoubleArray(n) { Double.NaN }
    val respondio = DoubleArray(n) { Double.NaN }
    fun por(clave: String): DoubleArray = when (clave) {
        "contacto" -> contacto; "actividades" -> actividades; "animo" -> animo; else -> medicacion
    }
}

class Datos(
    val fechas: List<String>,
    val personas: Map<String, Persona>,
    val series: Map<String, Series>,
    val controles: Map<String, List<Pair<String, Boolean>>>,
) {
    fun hoy(): String = fechas.last()
}

data class Det(
    val base: Double = 0.0, val reciente: Double = 0.0, val nRec: Int = 0, val kRec: Double? = null,
    val noAplica: Boolean = false, val insuficiente: Boolean = false,
)

data class Senal(val clave: String, val titulo: String, val z: Double?, val texto: String, val fuerza: String)

data class AlertaCalc(
    val personaId: String, val nivel: String, val score: Double, val senales: List<Senal>,
    val explicacion: String, val ifAtipico: Boolean,
)

object Detector {
    const val VENTANA = 7
    const val EXCLUIR_BASE = 14
    const val Z_FUERTE = -3.0
    const val Z_MODERADA = -2.0
    const val Z_DEBIL = -1.5

    val SENALES = linkedMapOf(
        "contacto" to ("Aislamiento atípico" to true),
        "actividades" to ("Actividades diarias" to false),
        "animo" to ("Ánimo" to false),
        "medicacion" to ("Medicación" to true),
    )

    val MAP_ANIMO = mapOf("bien" to 1.0, "regular" to 0.5, "mal" to 0.0)
    val MAP_ACT = mapOf("completo" to 1.0, "parcial" to 0.5, "no" to 0.0)
    val MAP_CONT = mapOf("si" to 1.0, "no" to 0.0)
    val MAP_MED = mapOf("tomo" to 1.0, "no_tomo" to 0.0)

    private fun limpio(a: DoubleArray, desde: Int, hasta: Int): List<Double> =
        (desde until hasta).map { a[it] }.filter { !it.isNaN() }

    private fun z(base: List<Double>, rec: List<Double>, bin: Boolean): Pair<Double, Det> {
        if (base.size < 10 || rec.size < 3) return 0.0 to Det(nRec = rec.size, insuficiente = true)
        val m0 = base.average()
        val m1 = rec.average()
        val sd = if (bin) {
            val p0 = m0.coerceIn(0.05, 0.95); sqrt(p0 * (1 - p0))
        } else {
            val v = base.map { (it - m0) * (it - m0) }.average(); max(sqrt(v), 0.2)
        }
        val zz = (m1 - m0) / (sd / sqrt(rec.size.toDouble()))
        return zz to Det(m0, m1, rec.size, if (bin) rec.sum() else null)
    }

    class Analisis(
        val z: Map<String, Double>, val det: Map<String, Det>, val silencio: Boolean,
        val controlPerdido: Boolean, val respRec: Double, val respBase: Double,
    )

    fun analizar(d: Datos, pid: String): Analisis {
        val s = d.series.getValue(pid)
        val n = d.fechas.size
        val cronico = d.personas.getValue(pid).medCronica
        val zs = LinkedHashMap<String, Double>()
        val dets = LinkedHashMap<String, Det>()
        for ((k, cfg) in SENALES) {
            if (k == "medicacion" && !cronico) { zs[k] = 0.0; dets[k] = Det(noAplica = true); continue }
            val (zz, dd) = z(limpio(s.por(k), 0, n - EXCLUIR_BASE), limpio(s.por(k), n - VENTANA, n), cfg.second)
            zs[k] = zz; dets[k] = dd
        }
        val respBase = limpio(s.respondio, 0, n - EXCLUIR_BASE).average()
        val respRec = limpio(s.respondio, n - VENTANA, n).average()
        val silencio = respBase >= 0.8 && respRec <= 0.45
        val corte = d.fechas[max(0, n - 1 - 30)]
        val control = d.controles[pid].orEmpty().any { (f, ok) -> f >= corte && !ok }
        return Analisis(zs, dets, silencio, control, respRec, respBase)
    }

    private fun pct(x: Double) = "%.0f".format(x * 100)

    private fun texto(k: String, d: Det): String = when (k) {
        "contacto" -> "Conversó con alguien solo ${d.kRec!!.toInt()} de los últimos ${d.nRec} días con respuesta, distinto de lo habitual en esta persona (antes ~${pct(d.base)} % de los días)."
        "actividades" -> "Le ha costado más hacer sus actividades de siempre esta semana (promedio ${pct(d.reciente)} %, antes ${pct(d.base)} %)."
        "animo" -> "Su ánimo estuvo más bajo que lo habitual esta semana (promedio ${pct(d.reciente)} %, antes ${pct(d.base)} %)."
        else -> "Dijo no haber tomado sus remedios en ${d.nRec - d.kRec!!.toInt()} de los últimos ${d.nRec} días (antes tomaba ~${pct(d.base)} % de los días)."
    }

    fun construirAlerta(pid: String, a: Analisis, ifAtipico: Boolean): AlertaCalc {
        val z = a.z
        val fuertes = z.filter { it.value <= Z_FUERTE }.keys
        val moderadas = z.filter { it.value > Z_FUERTE && it.value <= Z_MODERADA }.keys
        val debiles = z.filter { it.value > Z_MODERADA && it.value <= Z_DEBIL }.keys
        val nivel = when {
            fuertes.isNotEmpty() || moderadas.size >= 2 -> "Revisar"
            moderadas.isNotEmpty() || (ifAtipico && debiles.isNotEmpty()) -> "Observar"
            else -> "Sin novedad"
        }
        val orden = z.filter { it.value <= Z_DEBIL }.keys.sortedBy { z.getValue(it) }
        val senales = ArrayList<Senal>()
        if (nivel != "Sin novedad") {
            for (k in orden) {
                val zz = z.getValue(k)
                senales += Senal(k, SENALES.getValue(k).first, Math.round(zz * 100) / 100.0, texto(k, a.det.getValue(k)),
                    if (zz <= Z_FUERTE) "fuerte" else if (zz <= Z_MODERADA) "moderada" else "leve")
            }
            if (a.silencio) senales += Senal("silencio", "Sin respuesta", null,
                "No contestó el check-in casi toda la semana, aunque normalmente lo hace.", "leve")
            if (a.controlPerdido) senales += Senal("control", "Control de salud", null,
                "Faltó a un control de salud en el último mes (registro institucional).", "leve")
        }
        val score = z.values.filter { it < 0 }.sumOf { -it }
        val expl = if (nivel == "Sin novedad") "Sin cambios relevantes respecto de su rutina habitual."
        else senales.take(2).joinToString(" ") { it.texto }
        return AlertaCalc(pid, nivel, Math.round(score * 100) / 100.0, senales, expl, ifAtipico)
    }

    fun evaluarCohorte(d: Datos, contaminacion: Double = 0.2, semilla: Long = 0): Map<String, AlertaCalc> {
        val pids = d.personas.keys.toList()
        val an = pids.associateWith { analizar(d, it) }
        val x = pids.map { p ->
            val a = an.getValue(p)
            (SENALES.keys.map { a.z.getValue(it) } + (a.respRec - a.respBase)).toDoubleArray()
        }.toTypedArray()
        val bosque = IsolationForest(200, semilla).also { it.fit(x) }
        val scores = x.map { bosque.score(it) }
        val k = (contaminacion * pids.size).toInt()
        val atipicos = scores.withIndex().sortedByDescending { it.value }.take(k).map { it.index }.toSet()
        return pids.withIndex().associate { (i, p) -> p to construirAlerta(p, an.getValue(p), i in atipicos) }
    }

    /** Índice diario 0-1 (promedio de señales disponibles), suavizado 3 días. Solo para graficar. */
    fun serieBienestar(d: Datos, pid: String, dias: Int = 30): List<Double?> {
        val s = d.series.getValue(pid)
        val n = d.fechas.size
        val diario = DoubleArray(n) { i ->
            val v = listOf(s.contacto[i], s.actividades[i], s.animo[i], s.medicacion[i]).filter { !it.isNaN() }
            if (v.isEmpty()) Double.NaN else v.average()
        }
        return (n - dias until n).map { i ->
            val w = (max(0, i - 2)..i).map { diario[it] }.filter { !it.isNaN() }
            if (w.isEmpty()) null else Math.round(w.average() * 1000) / 1000.0
        }
    }

    fun mediaBase(d: Datos, pid: String): Double {
        val s = d.series.getValue(pid); val n = d.fechas.size
        val v = (0 until n - EXCLUIR_BASE).flatMap { i -> listOf(s.contacto[i], s.actividades[i], s.animo[i], s.medicacion[i]) }.filter { !it.isNaN() }
        return if (v.isEmpty()) 0.0 else Math.round(v.average() * 1000) / 1000.0
    }

    /** Serie de UNA señal (suavizada 5 días) y su media de línea base. Muestra el cambio que motivó la alerta. */
    fun serieSenal(d: Datos, pid: String, clave: String, dias: Int = 30): Pair<List<Double?>, Double> {
        val v = d.series.getValue(pid).por(clave); val n = d.fechas.size
        val serie = (n - dias until n).map { i ->
            val w = (max(0, i - 4)..i).map { v[it] }.filter { !it.isNaN() }
            if (w.isEmpty()) null else Math.round(w.average() * 1000) / 1000.0
        }
        val base = limpio(v, 0, n - EXCLUIR_BASE)
        return serie to (if (base.isEmpty()) 0.0 else Math.round(base.average() * 1000) / 1000.0)
    }
}

/** Isolation Forest mínimo (Liu et al.), semilla fija para reproducibilidad. */
class IsolationForest(private val nArboles: Int = 200, semilla: Long = 0) {
    private class Nodo(val f: Int = -1, val corte: Double = 0.0, val izq: Nodo? = null, val der: Nodo? = null, val tam: Int = 0)

    private val rnd = java.util.Random(semilla)
    private var arboles: List<Nodo> = emptyList()
    private var psi = 0

    fun fit(x: Array<DoubleArray>) {
        psi = minOf(256, x.size)
        val limite = ceil(log2(psi.toDouble())).toInt()
        arboles = List(nArboles) {
            val idx = x.indices.toList().shuffled(rnd).take(psi)
            construir(x, idx, 0, limite)
        }
    }

    private fun construir(x: Array<DoubleArray>, idx: List<Int>, prof: Int, limite: Int): Nodo {
        if (prof >= limite || idx.size <= 1) return Nodo(tam = idx.size)
        val f = rnd.nextInt(x[0].size)
        val vals = idx.map { x[it][f] }
        val mn = vals.min(); val mx = vals.max()
        if (mn == mx) return Nodo(tam = idx.size)
        val corte = mn + rnd.nextDouble() * (mx - mn)
        val (izq, der) = idx.partition { x[it][f] < corte }
        return Nodo(f, corte, construir(x, izq, prof + 1, limite), construir(x, der, prof + 1, limite))
    }

    private fun c(n: Int): Double = if (n > 2) 2 * (ln(n - 1.0) + 0.5772156649) - 2.0 * (n - 1) / n else if (n == 2) 1.0 else 0.0

    private fun camino(v: DoubleArray, nodo: Nodo, prof: Int): Double =
        if (nodo.izq == null || nodo.der == null) prof + c(nodo.tam)
        else camino(v, if (v[nodo.f] < nodo.corte) nodo.izq else nodo.der, prof + 1)

    fun score(v: DoubleArray): Double {
        val e = arboles.map { camino(v, it, 0) }.average()
        return 2.0.pow(-e / c(psi))
    }
}

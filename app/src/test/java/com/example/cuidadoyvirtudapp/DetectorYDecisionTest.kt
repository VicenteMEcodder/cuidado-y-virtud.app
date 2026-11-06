package com.example.cuidadoyvirtudapp

import com.example.cuidadoyvirtudapp.domain.AlertaCalc
import com.example.cuidadoyvirtudapp.domain.BloqueoHumano
import com.example.cuidadoyvirtudapp.domain.Datos
import com.example.cuidadoyvirtudapp.domain.Detector
import com.example.cuidadoyvirtudapp.domain.Persona
import com.example.cuidadoyvirtudapp.domain.Series
import com.example.cuidadoyvirtudapp.domain.Workflow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

/** Pruebas JVM (sin Android): detector sobre los mismos CSV sintéticos que usa la app, y garantías de decisión humana. */
class DetectorYDecisionTest {
    companion object {
        private fun csv(nombre: String) = File("src/main/assets/$nombre").readLines(Charsets.UTF_8).drop(1).filter { it.isNotBlank() }.map { it.split(",") }
        lateinit var datos: Datos
        lateinit var res: Map<String, AlertaCalc>
        lateinit var etiquetas: Map<String, Pair<String, Boolean>>

        @BeforeClass @JvmStatic fun cargar() {
            val personas = csv("personas.csv").associate { Persona(it[0], it[1], it[2], it[3], it[4], it[5] == "True", it[6], it[7] == "True").let { p -> p.id to p } }
            val filas = csv("checkins.csv")
            val fechas = filas.map { it[1] }.distinct().sorted()
            val idx = fechas.withIndex().associate { it.value to it.index }
            val series = personas.keys.associateWith { Series(fechas.size) }
            for (c in filas) {
                val s = series.getValue(c[0]); val i = idx.getValue(c[1])
                s.respondio[i] = if (c[2] == "True") 1.0 else 0.0
                if (c[2] == "True") {
                    Detector.MAP_ANIMO[c[4]]?.let { s.animo[i] = it }; Detector.MAP_CONT[c[5]]?.let { s.contacto[i] = it }
                    Detector.MAP_ACT[c[6]]?.let { s.actividades[i] = it }; Detector.MAP_MED[c[7]]?.let { s.medicacion[i] = it }
                }
            }
            val controles = HashMap<String, MutableList<Pair<String, Boolean>>>()
            csv("controles_salud.csv").forEach { controles.getOrPut(it[0]) { ArrayList() } += (it[1] to (it[2] == "True")) }
            datos = Datos(fechas, personas, series, controles)
            res = Detector.evaluarCohorte(datos)
            etiquetas = csv("etiquetas_simuladas.csv").associate { it[0] to (it[1] to (it[2] == "True")) }
        }
    }

    private fun alertada(pid: String) = res.getValue(pid).nivel != "Sin novedad"

    @Test fun detectaLaMayoriaDeLosCambiosDeRiesgoSimulados() {
        val riesgo = etiquetas.filter { it.value.second }.keys
        val tp = riesgo.count { alertada(it) }
        println("Sensibilidad Revisar+Observar: $tp/${riesgo.size}")
        assertTrue("sensibilidad insuficiente: $tp/${riesgo.size}", tp >= 0.8 * riesgo.size)
    }

    @Test fun lasFalsasAlarmasSonAcotadas() {
        val sinRiesgo = etiquetas.filter { !it.value.second }.keys
        val fp = sinRiesgo.count { alertada(it) }
        println("Falsas alarmas: $fp/${sinRiesgo.size}")
        assertTrue("demasiadas falsas alarmas: $fp/${sinRiesgo.size}", fp <= 0.3 * sinRiesgo.size)
    }

    @Test fun lasPersonasConMalaConexionNoGenerabanAlertaPorSiSolas() {
        val ids = etiquetas.filter { it.value.first == "baja_conectividad" }.keys
        val n = ids.count { alertada(it) }
        println("Baja conectividad con alerta: $n/${ids.size}")
        assertTrue("la mala conexión no debe disparar alertas: $n/${ids.size}", n <= 1)
    }

    @Test fun noUsaGruposDemograficosComoVariables() {
        // Si cambiamos edad/sexo/zona de todas las personas, el resultado debe ser idéntico (el modelo solo mira señales propias).
        val alterado = Datos(datos.fechas, datos.personas.mapValues { it.value.copy(tramo = "85+", zona = "rural", sexo = "hombre") }, datos.series, datos.controles)
        val r2 = Detector.evaluarCohorte(alterado)
        assertEquals(res.mapValues { it.value.nivel }, r2.mapValues { it.value.nivel })
    }

    @Test fun elResultadoEsReproducible() {
        assertEquals(res.mapValues { it.value.nivel }, Detector.evaluarCohorte(datos).mapValues { it.value.nivel })
    }

    @Test fun decisionSinJustificacionEsBloqueada() {
        for (j in listOf("", "   ", "corto")) {
            try { Workflow.validarDecision("Camila", "derivar", j); fail("debió bloquearse: '$j'") } catch (_: BloqueoHumano) {}
        }
        try { Workflow.validarDecision("", "derivar", "Justificación suficiente"); fail() } catch (_: BloqueoHumano) {}
        try { Workflow.validarDecision("Camila", "inventada", "Justificación suficiente"); fail() } catch (_: IllegalArgumentException) {}
        Workflow.validarDecision("Camila", "llamar", "Se llamará hoy para saber cómo está.") // válida: no lanza
    }

    @Test fun noExisteElSaltoDeRevisionAEjecucion() {
        assertFalse(Workflow.transicionPermitida("PENDIENTE_REVISION", "EJECUTADA"))
        assertFalse(Workflow.transicionPermitida("GENERADA", "EJECUTADA"))
        assertFalse(Workflow.transicionPermitida("DESCARTADA", "EJECUTADA"))
        assertFalse(Workflow.transicionPermitida("EJECUTADA", "APROBADA"))
        assertTrue(Workflow.transicionPermitida("PENDIENTE_REVISION", "APROBADA"))
        assertTrue(Workflow.transicionPermitida("APROBADA", "EJECUTADA"))
    }

    @Test fun losRelatosSeAgrupanEnCategoriasSimples() {
        assertEquals("Dolor o malestar físico", com.example.cuidadoyvirtudapp.domain.Social.categoria("Me duele mucho la rodilla"))
        assertEquals("Sueño", com.example.cuidadoyvirtudapp.domain.Social.categoria("No pude dormir anoche"))
        assertEquals("Soledad o tristeza", com.example.cuidadoyvirtudapp.domain.Social.categoria("Me siento muy sola"))
        assertEquals("Familia", com.example.cuidadoyvirtudapp.domain.Social.categoria("Extraño a mis nietos"))
        assertEquals("Otro motivo", com.example.cuidadoyvirtudapp.domain.Social.categoria("no sé, así nomás"))
    }

    @Test fun laCompaniaPrefiereMismaZonaYNuncaSugiereALaMismaPersona() {
        val yo = Persona("PS-a", "Ana", "75-84", "urbana", "mujer", false, "vigente", true)
        val rural = Persona("PS-b", "Berta", "75-84", "rural", "mujer", false, "vigente", true)
        val urbana = Persona("PS-c", "Carmen", "65-74", "urbana", "mujer", false, "vigente", true)
        val elegida = com.example.cuidadoyvirtudapp.domain.Social.elegirCandidato(yo, listOf(rural, yo, urbana))
        assertEquals("PS-c", elegida?.id)
        assertEquals(null, com.example.cuidadoyvirtudapp.domain.Social.elegirCandidato(yo, listOf(yo)))
        assertEquals(null, com.example.cuidadoyvirtudapp.domain.Social.elegirCandidato(yo, emptyList()))
    }

    @Test fun aprobarUnaPresentacionExigeJustificacion() {
        try { Workflow.exigirJustificacion("  corto "); fail("debió bloquearse") } catch (_: BloqueoHumano) {}
        Workflow.exigirJustificacion("Ambas viven cerca y lo desean.") // válida
    }

    @Test fun lasNotasSeBorranALosQuinceDias() {
        val R = com.example.cuidadoyvirtudapp.domain.Retencion
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        val ahora = fmt.parse("2026-11-20T12:00:00")!!
        assertEquals(15, R.DIAS)
        assertTrue("16 días debe estar vencida", R.vencida("2026-11-04T11:00:00", ahora))
        assertFalse("14 días no debe estar vencida", R.vencida("2026-11-06T12:00:00", ahora))
        assertFalse("justo antes del plazo no vence", R.vencida("2026-11-05T12:30:00", ahora))
        assertEquals("2026-11-21T10:30:00", R.venceEl("2026-11-06T10:30:00"))
    }

    @Test fun laAgendaCalculaLaProximaHoraDelAviso() {
        val A = com.example.cuidadoyvirtudapp.domain.Agenda
        val c = java.util.Calendar.getInstance().apply { set(2026, 10, 6, 10, 0, 0); set(java.util.Calendar.MILLISECOND, 0) }
        assertEquals(30 * 60 * 1000L, A.proximaMillis("10:30", "diaria", null, c)!! - c.timeInMillis)          // más tarde hoy
        assertEquals(22 * 60 * 60 * 1000L, A.proximaMillis("08:00", "diaria", null, c)!! - c.timeInMillis)     // ya pasó: mañana
        assertEquals(null, A.proximaMillis("08:00", "una_vez", "2026-11-06", c))                                // una vez y ya pasó
        assertTrue(A.proximaMillis("11:00", "una_vez", "2026-11-06", c)!! > c.timeInMillis)
        assertEquals(null, A.proximaMillis("11:00", "una_vez", null, c))
    }

    @Test fun laAgendaValidaHorasYMomentoDelAviso() {
        val A = com.example.cuidadoyvirtudapp.domain.Agenda
        for (h in listOf("08:05", "00:00", "23:59")) assertTrue(h, A.horaValida(h))
        for (h in listOf("8:5", "24:00", "12:60", "", "ab:cd")) assertFalse(h, A.horaValida(h))
        fun a(hh: Int, mm: Int) = java.util.Calendar.getInstance().apply { set(2026, 10, 6, hh, mm, 0) }
        assertTrue(A.esDebido("10:00", a(10, 0))); assertTrue(A.esDebido("10:00", a(10, 2)))
        assertFalse(A.esDebido("10:00", a(10, 3))); assertFalse(A.esDebido("10:00", a(9, 59)))
        assertTrue(A.textoAviso("remedio", "Pastilla de la presión").contains("Pastilla de la presión"))
    }

    @Test fun losRecorridosDeSeguimientoSoloAnotanLoQueLaPersonaEligio() {
        val S = com.example.cuidadoyvirtudapp.domain.Social
        assertEquals("Necesita ayuda con las compras", S.categoriaSeguimiento("actividades", "compras"))
        assertEquals("Remedios: se le olvidó", S.categoriaSeguimiento("medicacion", "olvido"))
        assertEquals("Quiere hablar con su familia", S.categoriaSeguimiento("contacto", "familia"))
        // "no, gracias" no anota nada; "equipo" y "programa" no son notas: son otras acciones (llamada y compañía)
        assertEquals(null, S.categoriaSeguimiento("actividades", "nada"))
        assertEquals(null, S.categoriaSeguimiento("contacto", "nada"))
        assertEquals(null, S.categoriaSeguimiento("contacto", "equipo"))
        assertEquals(null, S.categoriaSeguimiento("contacto", "programa"))
        assertEquals(null, S.categoriaSeguimiento("medicacion", "nada"))
    }

    @Test fun elMensajeDeDerivacionNoTieneVariablesDeSalud() {
        val m = Workflow.mensajeMinimo("Pedro").lowercase()
        for (p in listOf("ánimo", "animo", "remedio", "medic", "mal", "deterioro", "aislamiento")) assertFalse("contiene '$p'", m.contains(p))
    }
}

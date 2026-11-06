package com.example.cuidadoyvirtudapp.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.cuidadoyvirtudapp.domain.Datos
import com.example.cuidadoyvirtudapp.domain.Detector
import com.example.cuidadoyvirtudapp.domain.Persona
import com.example.cuidadoyvirtudapp.domain.Series

/**
 * SQLite local con datos 100 % SINTÉTICOS precargados desde los CSV de la carpeta assets.
 * Garantía en la base de datos: un trigger impide insertar derivaciones si la alerta no está APROBADA.
 */
class Db(private val ctx: Context) : SQLiteOpenHelper(ctx, "tucuidado.db", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        crearEsquema(db)
        precargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {
        if (o < 2) {
            db.execSQL("ALTER TABLE consentimientos ADD COLUMN socializar INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE consentimientos SET socializar = CASE WHEN rowid % 2 = 1 THEN 1 ELSE 0 END")
            crearTablasSocial(db)
        }
        if (o < 3) {
            db.execSQL("ALTER TABLE consentimientos ADD COLUMN recordatorios INTEGER NOT NULL DEFAULT 0")
            crearTablasAgenda(db)
        }
    }

    /**
     * Agenda de cada persona (remedios, citas, llamadas). Garantías: (1) el administrador debe habilitarla tras una solicitud
     * escrita del cuidador, y lo hace cumplir un trigger; (2) los avisos solo se entregan si la persona lo aceptó.
     */
    private fun crearTablasAgenda(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS agenda_solicitudes (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id TEXT NOT NULL,
            solicitante TEXT NOT NULL, motivo TEXT NOT NULL CHECK(length(motivo) >= 10), estado TEXT NOT NULL, ts TEXT NOT NULL,
            admin TEXT, resuelto_ts TEXT)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS agenda_eventos (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id TEXT NOT NULL,
            titulo TEXT NOT NULL, tipo TEXT NOT NULL, hora TEXT NOT NULL, repeticion TEXT NOT NULL, fecha TEXT, creado_por TEXT NOT NULL, ts TEXT NOT NULL)""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS recordatorios_log (id INTEGER PRIMARY KEY AUTOINCREMENT, evento_id INTEGER NOT NULL,
            fecha TEXT NOT NULL, estado TEXT NOT NULL, ts TEXT NOT NULL)""")
        db.execSQL("""CREATE TRIGGER IF NOT EXISTS agenda_requiere_habilitacion BEFORE INSERT ON agenda_eventos
            WHEN (SELECT COUNT(*) FROM agenda_solicitudes WHERE persona_id = NEW.persona_id AND estado = 'HABILITADA') = 0
            BEGIN SELECT RAISE(ABORT, 'BLOQUEADO: el administrador no ha habilitado la agenda de esta persona'); END""")
    }

    /** Notas que la persona decide anotar en su ficha, y propuestas de compañía entre personas (siempre con revisión humana). */
    private fun crearTablasSocial(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS notas_ficha (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id TEXT NOT NULL, ts TEXT NOT NULL,
            tipo TEXT NOT NULL, categoria TEXT NOT NULL, texto TEXT NOT NULL DEFAULT '')""")
        db.execSQL("""CREATE TABLE IF NOT EXISTS encuentros (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_a TEXT NOT NULL, persona_b TEXT NOT NULL,
            ts TEXT NOT NULL, estado TEXT NOT NULL, reviewer TEXT, justificacion TEXT, decidido_ts TEXT)""")
    }

    private fun crearEsquema(db: SQLiteDatabase) {
        listOf(
            """CREATE TABLE personas (persona_id TEXT PRIMARY KEY, alias TEXT, tramo TEXT, zona TEXT, sexo TEXT,
               med_cronica INTEGER, consent_inicial TEXT, comparte_inicial INTEGER)""",
            """CREATE TABLE checkins (persona_id TEXT, fecha TEXT, respondio INTEGER, canal TEXT,
               animo TEXT, contacto TEXT, actividades TEXT, medicacion TEXT)""",
            "CREATE INDEX idx_checkins ON checkins(persona_id, fecha)",
            "CREATE TABLE controles (persona_id TEXT, fecha TEXT, asistio INTEGER)",
            // Verdad simulada: SOLO para evaluar al detector. El detector no la lee.
            "CREATE TABLE etiquetas (persona_id TEXT PRIMARY KEY, escenario TEXT, riesgo INTEGER)",
            """CREATE TABLE consentimientos (persona_id TEXT PRIMARY KEY, comparte INTEGER NOT NULL,
               avisar_familia INTEGER NOT NULL DEFAULT 0, pausado INTEGER NOT NULL DEFAULT 0, ts TEXT NOT NULL,
               socializar INTEGER NOT NULL DEFAULT 0, recordatorios INTEGER NOT NULL DEFAULT 0)""",
            """CREATE TABLE alertas (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id TEXT NOT NULL, creada TEXT NOT NULL,
               origen TEXT NOT NULL, nivel TEXT NOT NULL, score REAL, senales TEXT NOT NULL, explicacion TEXT NOT NULL,
               estado TEXT NOT NULL)""",
            """CREATE TABLE decisiones (id INTEGER PRIMARY KEY AUTOINCREMENT, alerta_id INTEGER NOT NULL REFERENCES alertas(id),
               reviewer_id TEXT NOT NULL CHECK(length(reviewer_id) > 0), accion TEXT NOT NULL,
               justificacion TEXT NOT NULL CHECK(length(justificacion) >= 10), ts TEXT NOT NULL)""",
            """CREATE TABLE derivaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, alerta_id INTEGER NOT NULL REFERENCES alertas(id),
               decision_id INTEGER NOT NULL REFERENCES decisiones(id), destino TEXT NOT NULL, mensaje TEXT NOT NULL,
               simulada INTEGER NOT NULL DEFAULT 1, ts TEXT NOT NULL)""",
            """CREATE TABLE bitacora (id INTEGER PRIMARY KEY AUTOINCREMENT, ts TEXT NOT NULL, actor TEXT NOT NULL,
               evento TEXT NOT NULL, alerta_id INTEGER, persona_id TEXT, detalle TEXT)""",
            """CREATE TABLE checkins_vivo (id INTEGER PRIMARY KEY AUTOINCREMENT, persona_id TEXT NOT NULL, ts TEXT NOT NULL,
               canal TEXT NOT NULL, animo TEXT, contacto TEXT, actividades TEXT, medicacion TEXT)""",
            """CREATE TRIGGER derivacion_requiere_aprobacion BEFORE INSERT ON derivaciones
               WHEN (SELECT estado FROM alertas WHERE id = NEW.alerta_id) <> 'APROBADA'
               BEGIN SELECT RAISE(ABORT, 'BLOQUEADO: la alerta no tiene aprobación humana'); END""",
        ).forEach { db.execSQL(it) }
        crearTablasSocial(db)
        crearTablasAgenda(db)
    }

    private fun filas(nombre: String): List<List<String>> =
        ctx.assets.open(nombre).bufferedReader(Charsets.UTF_8).useLines { l ->
            l.drop(1).filter { it.isNotBlank() }.map { it.split(",") }.toList()
        }

    private fun precargar(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            for ((i, p) in filas("personas.csv").withIndex()) {
                // persona_id,alias_demo,tramo_etario,zona,sexo,medicacion_cronica,consentimiento,comparte_con_cuidador
                db.insert("personas", null, ContentValues().apply {
                    put("persona_id", p[0]); put("alias", p[1]); put("tramo", p[2]); put("zona", p[3]); put("sexo", p[4])
                    put("med_cronica", if (p[5] == "True") 1 else 0); put("consent_inicial", p[6])
                    put("comparte_inicial", if (p[7] == "True") 1 else 0)
                })
                db.insert("consentimientos", null, ContentValues().apply {
                    put("persona_id", p[0]); put("comparte", if (p[7] == "True") 1 else 0)
                    put("avisar_familia", 0); put("pausado", if (p[6] == "pausado") 1 else 0); put("ts", ahora())
                    put("socializar", if (i % 2 == 0) 1 else 0) // consentimiento sintético precargado para la demo
                })
            }
            for (c in filas("checkins.csv")) {
                // persona_id,fecha,respondio,canal,animo,contacto_social,actividades,medicacion
                db.insert("checkins", null, ContentValues().apply {
                    put("persona_id", c[0]); put("fecha", c[1]); put("respondio", if (c[2] == "True") 1 else 0)
                    put("canal", c[3]); put("animo", c[4]); put("contacto", c[5]); put("actividades", c[6]); put("medicacion", c[7])
                })
            }
            for (c in filas("controles_salud.csv")) {
                db.insert("controles", null, ContentValues().apply {
                    put("persona_id", c[0]); put("fecha", c[1]); put("asistio", if (c[2] == "True") 1 else 0)
                })
            }
            for (e in filas("etiquetas_simuladas.csv")) {
                // persona_id,escenario,es_riesgo_real,dia_inicio_cambio,fecha_inicio_cambio
                db.insert("etiquetas", null, ContentValues().apply {
                    put("persona_id", e[0]); put("escenario", e[1]); put("riesgo", if (e[2] == "True") 1 else 0)
                })
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Reinicia SOLO lo operativo (alertas, decisiones, bitácora, check-ins en vivo, consentimientos). Mantiene los datos precargados. */
    fun reiniciarDemo(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            listOf("derivaciones", "decisiones", "alertas", "bitacora", "checkins_vivo", "notas_ficha", "encuentros", "agenda_eventos", "agenda_solicitudes", "recordatorios_log", "consentimientos").forEach { db.execSQL("DELETE FROM $it") }
            db.execSQL("DELETE FROM sqlite_sequence")
            db.execSQL("""INSERT INTO consentimientos(persona_id, comparte, avisar_familia, pausado, ts, socializar)
                SELECT persona_id, comparte_inicial, 0, CASE consent_inicial WHEN 'pausado' THEN 1 ELSE 0 END, '${ahora()}',
                CASE WHEN rowid % 2 = 1 THEN 1 ELSE 0 END FROM personas""")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        fun ahora(): String = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).format(java.util.Date())

        /** Carga las series diarias desde SQLite (más los check-ins hechos en vivo, que sobrescriben el último día). */
        fun cargarDatos(db: SQLiteDatabase): Datos {
            val personas = LinkedHashMap<String, Persona>()
            db.rawQuery("SELECT persona_id, alias, tramo, zona, sexo, med_cronica, consent_inicial, comparte_inicial FROM personas", null).use { c ->
                while (c.moveToNext()) personas[c.getString(0)] = Persona(c.getString(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getInt(5) == 1, c.getString(6), c.getInt(7) == 1)
            }
            val fechas = ArrayList<String>()
            db.rawQuery("SELECT DISTINCT fecha FROM checkins ORDER BY fecha", null).use { c -> while (c.moveToNext()) fechas += c.getString(0) }
            val idx = fechas.withIndex().associate { it.value to it.index }
            val series = personas.keys.associateWith { Series(fechas.size) }
            db.rawQuery("SELECT persona_id, fecha, respondio, animo, contacto, actividades, medicacion FROM checkins", null).use { c ->
                while (c.moveToNext()) {
                    val s = series.getValue(c.getString(0)); val i = idx.getValue(c.getString(1))
                    s.respondio[i] = c.getInt(2).toDouble()
                    if (c.getInt(2) == 1) {
                        Detector.MAP_ANIMO[c.getString(3)]?.let { s.animo[i] = it }
                        Detector.MAP_CONT[c.getString(4)]?.let { s.contacto[i] = it }
                        Detector.MAP_ACT[c.getString(5)]?.let { s.actividades[i] = it }
                        Detector.MAP_MED[c.getString(6)]?.let { s.medicacion[i] = it }
                    }
                }
            }
            val controles = HashMap<String, MutableList<Pair<String, Boolean>>>()
            db.rawQuery("SELECT persona_id, fecha, asistio FROM controles", null).use { c ->
                while (c.moveToNext()) controles.getOrPut(c.getString(0)) { ArrayList() } += (c.getString(1) to (c.getInt(2) == 1))
            }
            val datos = Datos(fechas, personas, series, controles)
            // check-ins en vivo (demo): el último de cada persona sobrescribe el último día
            db.rawQuery("SELECT persona_id, animo, contacto, actividades, medicacion FROM checkins_vivo ORDER BY id", null).use { c ->
                while (c.moveToNext()) aplicarCheckin(datos, c.getString(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4))
            }
            return datos
        }

        fun aplicarCheckin(d: Datos, pid: String, animo: String?, contacto: String?, actividades: String?, medicacion: String?) {
            val s = d.series[pid] ?: return
            val i = d.fechas.size - 1
            s.respondio[i] = 1.0
            Detector.MAP_ANIMO[animo]?.let { s.animo[i] = it }
            Detector.MAP_CONT[contacto]?.let { s.contacto[i] = it }
            Detector.MAP_ACT[actividades]?.let { s.actividades[i] = it }
            Detector.MAP_MED[medicacion]?.let { s.medicacion[i] = it }
        }
    }
}

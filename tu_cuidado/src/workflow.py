"""Flujo con decisión humana obligatoria — Tu Cuidado.

Garantías (técnicas, no solo declarativas):
  1. Máquina de estados: GENERADA -> PENDIENTE_REVISION -> (APROBADA | DESCARTADA | EN_ESPERA) -> EJECUTADA.
     No existe la transición PENDIENTE_REVISION -> EJECUTADA.
  2. Una decisión exige reviewer, acción válida y justificación (>= 10 caracteres). Si no, se rechaza.
  3. Trigger de SQLite: no se puede insertar una derivación si la alerta no está APROBADA.
  4. La IA (detector) solo crea alertas en PENDIENTE_REVISION; no tiene función de envío.
  5. Bitácora de cada hecho (quién, qué, cuándo).
Los "envíos" son SIMULADOS (se registran, no salen del sistema).
"""
from __future__ import annotations

import json
import sqlite3
from datetime import datetime
from pathlib import Path

DB_PATH = Path(__file__).resolve().parent.parent / "data" / "tucuidado.db"

ACCIONES = {
    "descartar": "DESCARTADA",
    "esperar": "EN_ESPERA",
    "llamar": "APROBADA",
    "visita": "APROBADA",
    "derivar": "APROBADA",
}
ETIQUETA_ACCION = {
    "descartar": "Descartar (falso positivo)",
    "esperar": "Esperar y reevaluar",
    "llamar": "Llamar primero a la persona",
    "visita": "Visita presencial",
    "derivar": "Derivar a red de apoyo",
}
TRANSICIONES = {
    "GENERADA": {"PENDIENTE_REVISION"},
    "PENDIENTE_REVISION": {"APROBADA", "DESCARTADA", "EN_ESPERA"},
    "EN_ESPERA": {"APROBADA", "DESCARTADA", "EN_ESPERA"},
    "APROBADA": {"EJECUTADA"},
    "DESCARTADA": set(),
    "EJECUTADA": set(),
}
ABIERTAS = ("PENDIENTE_REVISION", "EN_ESPERA")


class BloqueoHumano(Exception):
    """Se intentó una acción sin la decisión humana requerida."""


def _ahora() -> str:
    return datetime.now().isoformat(timespec="seconds")


def conectar() -> sqlite3.Connection:
    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row
    con.execute("PRAGMA foreign_keys = ON")
    return con


def iniciar_bd(reiniciar: bool = False):
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    if reiniciar and DB_PATH.exists():
        DB_PATH.unlink()
    con = conectar()
    con.executescript(
        """
        CREATE TABLE IF NOT EXISTS alertas (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            persona_id TEXT NOT NULL,
            creada TEXT NOT NULL,
            origen TEXT NOT NULL,              -- detector | checkin | sos
            nivel TEXT NOT NULL,
            score REAL,
            senales TEXT NOT NULL,             -- JSON
            explicacion TEXT NOT NULL,
            estado TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS decisiones (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alerta_id INTEGER NOT NULL REFERENCES alertas(id),
            reviewer_id TEXT NOT NULL CHECK(length(reviewer_id) > 0),
            accion TEXT NOT NULL,
            justificacion TEXT NOT NULL CHECK(length(justificacion) >= 10),
            ts TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS derivaciones (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            alerta_id INTEGER NOT NULL REFERENCES alertas(id),
            decision_id INTEGER NOT NULL REFERENCES decisiones(id),
            destino TEXT NOT NULL,
            mensaje TEXT NOT NULL,
            simulada INTEGER NOT NULL DEFAULT 1,
            ts TEXT NOT NULL
        );
        CREATE TABLE IF NOT EXISTS bitacora (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            ts TEXT NOT NULL,
            actor TEXT NOT NULL,
            evento TEXT NOT NULL,
            alerta_id INTEGER,
            persona_id TEXT,
            detalle TEXT
        );
        CREATE TABLE IF NOT EXISTS checkins_vivo (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            persona_id TEXT NOT NULL, ts TEXT NOT NULL, canal TEXT NOT NULL,
            animo TEXT, contacto_social TEXT, actividades TEXT, medicacion TEXT
        );
        CREATE TABLE IF NOT EXISTS consentimientos (
            persona_id TEXT PRIMARY KEY,
            comparte_con_cuidador INTEGER NOT NULL,
            avisar_familia INTEGER NOT NULL DEFAULT 0,
            pausado INTEGER NOT NULL DEFAULT 0,
            ts TEXT NOT NULL
        );
        -- Garantía en la base de datos: no hay derivación sin alerta APROBADA.
        CREATE TRIGGER IF NOT EXISTS derivacion_requiere_aprobacion
        BEFORE INSERT ON derivaciones
        WHEN (SELECT estado FROM alertas WHERE id = NEW.alerta_id) <> 'APROBADA'
        BEGIN
            SELECT RAISE(ABORT, 'BLOQUEADO: la alerta no tiene aprobación humana');
        END;
        """
    )
    con.commit()
    con.close()


def bitacora(con, actor: str, evento: str, alerta_id=None, persona_id=None, detalle: str = ""):
    con.execute("INSERT INTO bitacora(ts, actor, evento, alerta_id, persona_id, detalle) VALUES (?,?,?,?,?,?)",
                (_ahora(), actor, evento, alerta_id, persona_id, detalle))


def _transicionar(con, alerta_id: int, nuevo: str):
    fila = con.execute("SELECT estado FROM alertas WHERE id=?", (alerta_id,)).fetchone()
    if fila is None:
        raise KeyError("alerta inexistente")
    if nuevo not in TRANSICIONES[fila["estado"]]:
        raise BloqueoHumano(f"Transición no permitida: {fila['estado']} -> {nuevo}")
    con.execute("UPDATE alertas SET estado=? WHERE id=?", (nuevo, alerta_id))


# ---------------------------------------------------------------------------
# Creación de alertas (lo único que puede hacer la IA)
# ---------------------------------------------------------------------------
def crear_alerta(con, alerta: dict, origen: str = "detector") -> int | None:
    """Crea la alerta en PENDIENTE_REVISION si no hay otra abierta para la persona. La IA no puede hacer más."""
    abierta = con.execute(
        "SELECT id FROM alertas WHERE persona_id=? AND estado IN ('PENDIENTE_REVISION','EN_ESPERA')",
        (alerta["persona_id"],)).fetchone()
    if abierta:
        return None
    cur = con.execute(
        "INSERT INTO alertas(persona_id, creada, origen, nivel, score, senales, explicacion, estado) VALUES (?,?,?,?,?,?,?,?)",
        (alerta["persona_id"], _ahora(), origen, alerta["nivel"], alerta.get("score"),
         json.dumps(alerta["senales"], ensure_ascii=False), alerta["explicacion"], "GENERADA"))
    aid = cur.lastrowid
    _transicionar(con, aid, "PENDIENTE_REVISION")
    bitacora(con, "IA (detector)", "Alerta generada y enviada a revisión humana", aid, alerta["persona_id"],
             f"Nivel {alerta['nivel']} · origen {origen}")
    return aid


# ---------------------------------------------------------------------------
# Decisión humana
# ---------------------------------------------------------------------------
def mensaje_minimo(alias: str) -> str:
    # Mensaje aprobado y mínimo: sin variables de salud.
    return f"Sugerimos comunicarse hoy con {alias}. Gracias por acompañarle."


def decidir(con, alerta_id: int, reviewer_id: str, accion: str, justificacion: str,
            destino: str = "Red de apoyo", alias: str = "la persona",
            comparte: bool = True, excepcion: str = "") -> dict:
    if accion not in ACCIONES:
        raise ValueError("Acción no válida")
    if not reviewer_id or not reviewer_id.strip():
        raise BloqueoHumano("Falta la identidad de quien decide")
    justificacion = (justificacion or "").strip()
    if len(justificacion) < 10:
        raise BloqueoHumano("Ninguna acción se envía sin su decisión y justificación explícita (mínimo 10 caracteres).")
    alerta = con.execute("SELECT * FROM alertas WHERE id=?", (alerta_id,)).fetchone()
    if alerta is None:
        raise KeyError("alerta inexistente")
    if alerta["estado"] not in ABIERTAS:
        raise BloqueoHumano(f"La alerta ya está {alerta['estado']}")
    if accion == "derivar" and not comparte and len(excepcion.strip()) < 10:
        raise BloqueoHumano("La persona no autorizó compartir su información: documente un motivo excepcional.")

    cur = con.execute("INSERT INTO decisiones(alerta_id, reviewer_id, accion, justificacion, ts) VALUES (?,?,?,?,?)",
                      (alerta_id, reviewer_id.strip(), accion, justificacion, _ahora()))
    decision_id = cur.lastrowid
    nuevo = ACCIONES[accion]
    _transicionar(con, alerta_id, nuevo)
    bitacora(con, reviewer_id.strip(), f"Decisión: {ETIQUETA_ACCION[accion]}", alerta_id, alerta["persona_id"], justificacion)

    resultado = {"decision_id": decision_id, "estado": nuevo, "derivacion": None}
    if nuevo == "APROBADA":
        if accion == "derivar":
            resultado["derivacion"] = _enviar(con, alerta_id, decision_id, destino, mensaje_minimo(alias))
        _transicionar(con, alerta_id, "EJECUTADA")
        resultado["estado"] = "EJECUTADA"
        bitacora(con, "Sistema", "Acción registrada para ejecución humana", alerta_id, alerta["persona_id"],
                 ETIQUETA_ACCION[accion])
    return resultado


def _enviar(con, alerta_id: int, decision_id: int, destino: str, mensaje: str) -> dict:
    try:
        con.execute("INSERT INTO derivaciones(alerta_id, decision_id, destino, mensaje, ts) VALUES (?,?,?,?,?)",
                    (alerta_id, decision_id, destino, mensaje, _ahora()))
    except sqlite3.DatabaseError as exc:  # trigger
        raise BloqueoHumano(str(exc)) from exc
    return {"destino": destino, "mensaje": mensaje, "simulada": True}


def intentar_envio_directo(con, alerta_id: int, actor: str = "IA (intento automático)") -> None:
    """Simula que el sistema/IA intenta derivar SIN decisión humana. Siempre debe ser bloqueado
    mientras la alerta no esté APROBADA. Se usa en la demo y en las pruebas."""
    alerta = con.execute("SELECT * FROM alertas WHERE id=?", (alerta_id,)).fetchone()
    if alerta is None:
        raise KeyError("alerta inexistente")
    decision = con.execute("SELECT id FROM decisiones WHERE alerta_id=? ORDER BY id DESC LIMIT 1", (alerta_id,)).fetchone()
    try:
        # Se usa un decision_id inexistente a propósito si no hay decisión; el trigger de la BD lo frena igualmente.
        _enviar(con, alerta_id, decision["id"] if decision else 0, "Red de apoyo", "intento sin aprobación")
    except BloqueoHumano as exc:
        bitacora(con, actor, "ENVÍO BLOQUEADO: no hay decisión humana", alerta_id, alerta["persona_id"], str(exc))
        con.commit()
        raise
    # Si la alerta estaba APROBADA el envío es legítimo (ya hubo decisión humana).

"""API + pantallas de "Tu Cuidado" (FastAPI).

Ejecutar desde la carpeta tu_cuidado:
    python -m uvicorn src.api:app --port 8000
"""
from __future__ import annotations

import json
import sqlite3
import sys
from pathlib import Path

from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

sys.path.insert(0, str(Path(__file__).resolve().parent))
import detector as det  # noqa: E402
import workflow as wf  # noqa: E402

RAIZ = Path(__file__).resolve().parent.parent
APP_DIR = RAIZ / "app"
REVISOR_POR_DEFECTO = "Camila Valenzuela (cuidadora territorial)"

app = FastAPI(title="Tu Cuidado — MVP Hackathon UC 2026")
app.mount("/static", StaticFiles(directory=APP_DIR / "static"), name="static")

datos: det.Datos
resultados: dict[str, dict] = {}


# ---------------------------------------------------------------------------
# Arranque: datos sintéticos -> detector -> alertas en cola (nunca ejecutadas)
# ---------------------------------------------------------------------------
def _sembrar():
    global datos, resultados
    datos = det.Datos()
    resultados = det.evaluar_cohorte(datos)
    wf.iniciar_bd(reiniciar=True)
    con = wf.conectar()
    for pid, p in datos.personas.items():
        con.execute("INSERT INTO consentimientos(persona_id, comparte_con_cuidador, avisar_familia, pausado, ts) VALUES (?,?,?,?,?)",
                    (pid, 1 if p["comparte_con_cuidador"] == "True" else 0, 0, 1 if p["consentimiento"] == "pausado" else 0, wf._ahora()))
    for pid, r in resultados.items():
        if r["nivel"] != "Sin novedad" and _puede_procesar(con, pid):
            wf.crear_alerta(con, r, "detector")
    con.commit()
    con.close()


def _consent(con, pid: str):
    return con.execute("SELECT * FROM consentimientos WHERE persona_id=?", (pid,)).fetchone()


def _puede_procesar(con, pid: str) -> bool:
    c = _consent(con, pid)
    return bool(c) and not c["pausado"] and bool(c["comparte_con_cuidador"])


@app.on_event("startup")
def _startup():
    _sembrar()


# ---------------------------------------------------------------------------
# Utilidades de presentación
# ---------------------------------------------------------------------------
def _persona_publica(pid: str) -> dict:
    p = datos.personas[pid]
    alias = p["alias_demo"]
    return {"persona_id": pid, "alias": alias, "iniciales": alias[:2].upper(),
            "tramo_etario": p["tramo_etario"], "zona": p["zona"]}


def _alerta_json(con, fila, detalle: bool = False) -> dict:
    pid = fila["persona_id"]
    c = _consent(con, pid)
    out = {
        "id": fila["id"], "nivel": fila["nivel"], "estado": fila["estado"], "origen": fila["origen"],
        "creada": fila["creada"], "explicacion": fila["explicacion"],
        "senales": json.loads(fila["senales"]), "score": fila["score"],
        "persona": _persona_publica(pid),
        "consentimiento": {"vigente": not c["pausado"], "comparte": bool(c["comparte_con_cuidador"]),
                           "avisar_familia": bool(c["avisar_familia"])},
        "spark": det.serie_bienestar(datos, pid, 30),
    }
    if detalle:
        out["media_base"] = det.media_base(datos, pid)
        principal = next((s["clave"] for s in out["senales"] if s["clave"] in det.SENALES), None)
        if principal:
            serie, base = det.serie_senal(datos, pid, principal)
            out["grafico"] = {"titulo": det.SENALES[principal]["titulo"], "serie": serie, "base": base}
        else:
            out["grafico"] = {"titulo": "Bienestar general", "serie": out["spark"], "base": out["media_base"]}
        bit = con.execute("SELECT * FROM bitacora WHERE alerta_id=? OR persona_id=? ORDER BY id DESC LIMIT 12", (fila["id"], pid)).fetchall()
        out["bitacora"] = [dict(b) for b in bit]
    return out


ORDEN = {"Revisar": 0, "Observar": 1}


# ---------------------------------------------------------------------------
# Páginas
# ---------------------------------------------------------------------------
@app.get("/")
def inicio():
    return FileResponse(APP_DIR / "login.html")


@app.get("/mayor")
def pagina_mayor():
    return FileResponse(APP_DIR / "mayor.html")


@app.get("/mayor/compartir")
def pagina_compartir():
    return FileResponse(APP_DIR / "compartir.html")


@app.get("/cuidador")
def pagina_cuidador():
    return FileResponse(APP_DIR / "cuidador.html")


@app.get("/admin")
def pagina_admin():
    return FileResponse(APP_DIR / "admin.html")


# ---------------------------------------------------------------------------
# API: persona mayor
# ---------------------------------------------------------------------------
def _persona_demo() -> str:
    """Persona de demostración para la pantalla de la persona mayor: hombre, estable,
    comparte con su cuidador y con consentimiento vigente. (Selección solo para la demo.)"""
    etiquetas = {e["persona_id"]: e for e in det._leer("etiquetas_simuladas.csv")}
    for pid, p in datos.personas.items():
        if (etiquetas[pid]["escenario"] == "estable" and p["sexo"] == "hombre" and p["consentimiento"] == "vigente"
                and p["comparte_con_cuidador"] == "True" and p["medicacion_cronica"] == "True"):
            return pid
    return next(iter(datos.personas))


@app.get("/api/demo/persona")
def demo_persona(p: str | None = None):
    pid = p if p in datos.personas else _persona_demo()
    pers = datos.personas[pid]
    honorifico = "don" if pers["sexo"] == "hombre" else "doña"  # solo para el saludo de la interfaz
    return {**_persona_publica(pid), "saludo": f"{honorifico} {pers['alias_demo']}",
            "medicacion_cronica": pers["medicacion_cronica"] == "True"}


class CheckinIn(BaseModel):
    persona_id: str
    canal: str = "botones"
    animo: str | None = None
    contacto_social: str | None = None
    actividades: str | None = None
    medicacion: str | None = None


@app.post("/api/checkin")
def checkin(c: CheckinIn):
    """Guarda SOLO respuestas codificadas (nunca audio ni texto). Una sola respuesta no dispara alerta:
    el detector compara la semana completa con la normalidad de la persona."""
    global resultados
    if c.persona_id not in datos.personas:
        raise HTTPException(404, "persona desconocida")
    con = wf.conectar()
    if _consent(con, c.persona_id)["pausado"]:
        con.close()
        raise HTTPException(409, "La persona pausó el acompañamiento")
    con.execute("INSERT INTO checkins_vivo(persona_id, ts, canal, animo, contacto_social, actividades, medicacion) VALUES (?,?,?,?,?,?,?)",
                (c.persona_id, wf._ahora(), c.canal, c.animo, c.contacto_social, c.actividades, c.medicacion))
    datos.registrar_checkin_hoy(c.persona_id, c.animo, c.contacto_social, c.actividades, c.medicacion)
    resultados = det.evaluar_cohorte(datos)
    r = resultados[c.persona_id]
    creada = None
    if r["nivel"] != "Sin novedad" and _puede_procesar(con, c.persona_id):
        creada = wf.crear_alerta(con, r, "checkin")
    wf.bitacora(con, "Persona mayor", "Check-in registrado", None, c.persona_id, f"canal={c.canal}")
    con.commit()
    con.close()
    return {"ok": True}  # la persona mayor no recibe etiquetas de riesgo


class SosIn(BaseModel):
    persona_id: str


@app.post("/api/sos")
def sos(s: SosIn):
    """Pedido de ayuda iniciado POR LA PERSONA. Crea una alerta prioritaria para un humano; no llama ni avisa a nadie por sí solo."""
    if s.persona_id not in datos.personas:
        raise HTTPException(404, "persona desconocida")
    con = wf.conectar()
    alerta = {"persona_id": s.persona_id, "nivel": "Revisar", "score": 99.0,
              "senales": [{"clave": "sos", "titulo": "Pidió ayuda", "z": None, "fuerza": "fuerte",
                           "texto": "La persona presionó «Necesito ayuda ahora» y confirmó."}],
              "explicacion": "La persona pidió ayuda por sí misma. Conviene comunicarse con ella cuanto antes."}
    # Si ya tiene una alerta abierta, se escala en lugar de duplicar.
    ab = con.execute("SELECT id FROM alertas WHERE persona_id=? AND estado IN ('PENDIENTE_REVISION','EN_ESPERA')", (s.persona_id,)).fetchone()
    if ab:
        con.execute("UPDATE alertas SET nivel='Revisar', origen='sos', explicacion=?, senales=? WHERE id=?",
                    (alerta["explicacion"], json.dumps(alerta["senales"], ensure_ascii=False), ab["id"]))
        wf.bitacora(con, "Persona mayor", "SOS: alerta existente escalada a Revisar", ab["id"], s.persona_id)
        aid = ab["id"]
    else:
        aid = wf.crear_alerta(con, alerta, "sos")
        wf.bitacora(con, "Persona mayor", "SOS solicitado por la persona", aid, s.persona_id)
    con.commit()
    con.close()
    return {"ok": True, "alerta_id": aid}


class ConsentIn(BaseModel):
    persona_id: str
    comparte_con_cuidador: bool
    avisar_familia: bool = False
    pausado: bool = False


@app.post("/api/consentimiento")
def consentimiento(c: ConsentIn):
    if c.persona_id not in datos.personas:
        raise HTTPException(404, "persona desconocida")
    con = wf.conectar()
    con.execute("UPDATE consentimientos SET comparte_con_cuidador=?, avisar_familia=?, pausado=?, ts=? WHERE persona_id=?",
                (int(c.comparte_con_cuidador), int(c.avisar_familia), int(c.pausado), wf._ahora(), c.persona_id))
    wf.bitacora(con, "Persona mayor", "Consentimiento actualizado", None, c.persona_id,
                f"comparte={c.comparte_con_cuidador}, familia={c.avisar_familia}, pausado={c.pausado}")
    con.commit()
    con.close()
    return {"ok": True}


@app.get("/api/consentimiento/{pid}")
def leer_consentimiento(pid: str):
    con = wf.conectar()
    c = _consent(con, pid)
    con.close()
    if c is None:
        raise HTTPException(404)
    return {"comparte_con_cuidador": bool(c["comparte_con_cuidador"]), "avisar_familia": bool(c["avisar_familia"]),
            "pausado": bool(c["pausado"])}


# ---------------------------------------------------------------------------
# API: cuidador (funcionario) — cola, detalle, decisión
# ---------------------------------------------------------------------------
@app.get("/api/alertas")
def cola():
    con = wf.conectar()
    filas = con.execute("SELECT * FROM alertas WHERE estado IN ('PENDIENTE_REVISION','EN_ESPERA')").fetchall()
    alertas = []
    for f in filas:
        if _puede_procesar(con, f["persona_id"]):
            alertas.append(_alerta_json(con, f))
    alertas.sort(key=lambda a: (ORDEN.get(a["nivel"], 2), 0 if a["origen"] == "sos" else 1, -(a["score"] or 0)))

    visibles = [pid for pid in datos.personas if _puede_procesar(con, pid)]
    con_alerta = {a["persona"]["persona_id"] for a in alertas}
    sin_novedad = [pid for pid in visibles if pid not in con_alerta]
    no_visibles = len(datos.personas) - len(visibles)
    cont = {"Revisar": sum(a["nivel"] == "Revisar" for a in alertas),
            "Observar": sum(a["nivel"] == "Observar" for a in alertas),
            "Sin novedad": len(sin_novedad)}
    con.close()
    return {"fecha": datos.hoy(), "contadores": cont, "alertas": alertas, "no_visibles": no_visibles}


@app.get("/api/alertas/{aid}")
def detalle(aid: int):
    con = wf.conectar()
    f = con.execute("SELECT * FROM alertas WHERE id=?", (aid,)).fetchone()
    if f is None:
        con.close()
        raise HTTPException(404, "alerta inexistente")
    out = _alerta_json(con, f, detalle=True)
    con.close()
    return out


class DecisionIn(BaseModel):
    accion: str
    justificacion: str = ""
    reviewer: str = REVISOR_POR_DEFECTO
    excepcion: str = ""


@app.post("/api/alertas/{aid}/decision")
def decision(aid: int, d: DecisionIn):
    con = wf.conectar()
    try:
        f = con.execute("SELECT persona_id FROM alertas WHERE id=?", (aid,)).fetchone()
        if f is None:
            raise HTTPException(404, "alerta inexistente")
        pid = f["persona_id"]
        c = _consent(con, pid)
        res = wf.decidir(con, aid, d.reviewer, d.accion, d.justificacion,
                         alias=datos.personas[pid]["alias_demo"], comparte=bool(c["comparte_con_cuidador"]),
                         excepcion=d.excepcion)
        con.commit()
        return res
    except wf.BloqueoHumano as exc:
        con.rollback()
        raise HTTPException(409, str(exc))
    except ValueError as exc:
        con.rollback()
        raise HTTPException(422, str(exc))
    finally:
        con.close()


@app.post("/api/alertas/{aid}/intento-envio")
def intento_envio(aid: int):
    """Demo del bloqueo: simula que el sistema/IA intenta derivar sin decisión humana. Debe fallar siempre."""
    con = wf.conectar()
    try:
        wf.intentar_envio_directo(con, aid)
        return {"ok": True, "nota": "La alerta ya tenía aprobación humana"}
    except wf.BloqueoHumano as exc:
        raise HTTPException(409, str(exc))
    except KeyError:
        raise HTTPException(404, "alerta inexistente")
    finally:
        con.close()


@app.get("/api/bitacora")
def ver_bitacora(limit: int = 60):
    con = wf.conectar()
    filas = [dict(r) for r in con.execute("SELECT * FROM bitacora ORDER BY id DESC LIMIT ?", (limit,)).fetchall()]
    con.close()
    return filas


@app.get("/api/personas")
def personas():
    """Para el administrador: gestión de accesos. NO incluye respuestas de salud ni alertas."""
    con = wf.conectar()
    out = []
    for pid, p in datos.personas.items():
        c = _consent(con, pid)
        out.append({**_persona_publica(pid), "consentimiento": "pausado" if c["pausado"] else "vigente",
                    "comparte": bool(c["comparte_con_cuidador"])})
    con.close()
    return out


@app.get("/api/equidad")
def equidad():
    ev = det.evaluar_desempeno(datos, resultados)
    return {"desempeno": ev, "equidad": det.auditoria_equidad(datos, resultados),
            "modelos": [
                {"nombre": "Línea base individual (z-score)", "tipo": "Estadística clásica", "uso": "Compara a cada persona consigo misma"},
                {"nombre": "Isolation Forest (scikit-learn)", "tipo": "Aprendizaje no supervisado", "uso": "Detecta combinaciones atípicas en la cohorte"},
                {"nombre": "Plantilla de lenguaje simple", "tipo": "Reglas (sin IA generativa)", "uso": "Explica cada alerta; no se usó un LLM en este MVP"},
            ],
            "nota": "Datos 100 % sintéticos. Métricas medidas contra escenarios simulados; no son evidencia clínica."}


@app.post("/api/demo/reiniciar")
def reiniciar():
    _sembrar()
    return {"ok": True}

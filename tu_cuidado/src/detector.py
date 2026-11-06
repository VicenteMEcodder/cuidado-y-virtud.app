"""Detector de cambios de rutina — Tu Cuidado.

La IA aquí NO decide ni diagnostica: PRIORIZA y EXPLICA cambios respecto de la normalidad
de cada persona. Toda alerta queda en cola para revisión humana (ver workflow.py).

Método (dos capas, ambas explicables):
  1. LÍNEA BASE INDIVIDUAL: para cada señal (contacto, actividades, ánimo, medicación) se compara
     la última semana con la historia previa de la MISMA persona (se excluyen las últimas 2 semanas
     de la base para no contaminarla). El resultado es un puntaje z (negativo = empeoró).
     Así, quien siempre habla poco no genera alertas, y quien cambia sí.
  2. COHORTE (Isolation Forest, scikit-learn): sobre el vector de cambios de todas las personas,
     detecta combinaciones atípicas. Se usa para escalar casos con señales moderadas, no para
     alertar por sí sola.

NO se usan edad, sexo, zona ni ingreso como variables. Las etiquetas simuladas solo se leen en
evaluar() para medir el desempeño del detector.
"""
from __future__ import annotations

import csv
import math
from datetime import date, timedelta
from pathlib import Path

import numpy as np
from sklearn.ensemble import IsolationForest

DATA = Path(__file__).resolve().parent.parent / "data"

MAP_ANIMO = {"bien": 1.0, "regular": 0.5, "mal": 0.0}
MAP_ACT = {"completo": 1.0, "parcial": 0.5, "no": 0.0}
MAP_CONT = {"si": 1.0, "no": 0.0}
MAP_MED = {"tomo": 1.0, "no_tomo": 0.0}

VENTANA = 7            # días recientes que se comparan
EXCLUIR_BASE = 14      # últimos días que NO entran a la línea base
Z_FUERTE = -3.0
Z_MODERADA = -2.0
Z_DEBIL = -1.5

SENALES = {
    "contacto":    {"titulo": "Aislamiento atípico",   "tipo": "bin"},
    "actividades": {"titulo": "Actividades diarias",    "tipo": "ord"},
    "animo":       {"titulo": "Ánimo",                  "tipo": "ord"},
    "medicacion":  {"titulo": "Medicación",             "tipo": "bin"},
}


# ---------------------------------------------------------------------------
# Carga de datos
# ---------------------------------------------------------------------------
def _leer(nombre: str) -> list[dict]:
    with (DATA / nombre).open(encoding="utf-8", newline="") as fh:
        return list(csv.DictReader(fh))


class Datos:
    """Carga los CSV y arma series diarias por persona (NaN = sin dato)."""

    def __init__(self):
        self.personas = {p["persona_id"]: p for p in _leer("personas.csv")}
        filas = _leer("checkins.csv")
        self.fechas = sorted({f["fecha"] for f in filas})
        idx = {f: i for i, f in enumerate(self.fechas)}
        n = len(self.fechas)
        self.series: dict[str, dict[str, np.ndarray]] = {}
        for pid in self.personas:
            self.series[pid] = {k: np.full(n, np.nan) for k in ("contacto", "actividades", "animo", "medicacion", "respondio")}
        for f in filas:
            s = self.series[f["persona_id"]]
            i = idx[f["fecha"]]
            s["respondio"][i] = 1.0 if f["respondio"] == "True" else 0.0
            if f["respondio"] != "True":
                continue
            s["contacto"][i] = MAP_CONT.get(f["contacto_social"], np.nan)
            s["actividades"][i] = MAP_ACT.get(f["actividades"], np.nan)
            s["animo"][i] = MAP_ANIMO.get(f["animo"], np.nan)
            s["medicacion"][i] = MAP_MED.get(f["medicacion"], np.nan)
        self.controles: dict[str, list[tuple[str, bool]]] = {}
        for c in _leer("controles_salud.csv"):
            self.controles.setdefault(c["persona_id"], []).append((c["fecha_control"], c["asistio"] == "True"))

    def hoy(self) -> str:
        return self.fechas[-1]

    def registrar_checkin_hoy(self, pid: str, animo=None, contacto=None, actividades=None, medicacion=None):
        """Sobrescribe el día de hoy con un check-in nuevo (demo en vivo). Solo códigos, nunca audio."""
        s = self.series[pid]
        i = len(self.fechas) - 1
        s["respondio"][i] = 1.0
        if animo in MAP_ANIMO:
            s["animo"][i] = MAP_ANIMO[animo]
        if contacto in MAP_CONT:
            s["contacto"][i] = MAP_CONT[contacto]
        if actividades in MAP_ACT:
            s["actividades"][i] = MAP_ACT[actividades]
        if medicacion in MAP_MED:
            s["medicacion"][i] = MAP_MED[medicacion]


# ---------------------------------------------------------------------------
# Capa 1: línea base individual
# ---------------------------------------------------------------------------
def _z(base: np.ndarray, rec: np.ndarray, tipo: str) -> tuple[float, dict]:
    b = base[~np.isnan(base)]
    r = rec[~np.isnan(rec)]
    if len(b) < 10 or len(r) < 3:
        return 0.0, {"n_rec": int(len(r)), "insuficiente": True}
    m0, m1 = float(b.mean()), float(r.mean())
    if tipo == "bin":
        p0 = min(max(m0, 0.05), 0.95)
        sd = math.sqrt(p0 * (1 - p0))
    else:
        sd = max(float(b.std()), 0.2)
    z = (m1 - m0) / (sd / math.sqrt(len(r)))
    return z, {"base": m0, "reciente": m1, "n_rec": int(len(r)), "k_rec": float(r.sum()) if tipo == "bin" else None}


def analizar_persona(datos: Datos, pid: str) -> dict:
    s = datos.series[pid]
    n = len(datos.fechas)
    rec_sl = slice(n - VENTANA, n)
    base_sl = slice(0, n - EXCLUIR_BASE)
    cronico = datos.personas[pid]["medicacion_cronica"] == "True"

    z, det = {}, {}
    for k, cfg in SENALES.items():
        if k == "medicacion" and not cronico:
            z[k], det[k] = 0.0, {"no_aplica": True}
            continue
        z[k], det[k] = _z(s[k][base_sl], s[k][rec_sl], cfg["tipo"])

    # señal secundaria: silencio (no contestó). Solo cuenta si su normalidad era contestar.
    resp_base = np.nanmean(s["respondio"][base_sl])
    resp_rec = np.nanmean(s["respondio"][rec_sl])
    silencio = bool(resp_base >= 0.8 and resp_rec <= 0.45)

    # controles de salud perdidos en los últimos 30 días (registro institucional)
    corte = (date.fromisoformat(datos.hoy()) - timedelta(days=30)).isoformat()
    control_perdido = any((f >= corte and not ok) for f, ok in datos.controles.get(pid, []))

    return {"z": z, "det": det, "silencio": silencio, "control_perdido": control_perdido,
            "resp_rec": float(resp_rec), "resp_base": float(resp_base)}


# ---------------------------------------------------------------------------
# Explicaciones en lenguaje simple (plantilla determinista, sin datos personales)
# ---------------------------------------------------------------------------
def _texto_senal(k: str, d: dict) -> str:
    n = d["n_rec"]
    base = d.get("base", 0) * 100
    if k == "contacto":
        return (f"Conversó con alguien solo {int(d['k_rec'])} de los últimos {n} días con respuesta, "
                f"distinto de lo habitual en esta persona (antes ~{base:.0f} % de los días).")
    if k == "actividades":
        return (f"Le ha costado más hacer sus actividades de siempre esta semana "
                f"(promedio {d['reciente']*100:.0f} %, antes {base:.0f} %).")
    if k == "animo":
        return (f"Su ánimo estuvo más bajo que lo habitual esta semana "
                f"(promedio {d['reciente']*100:.0f} %, antes {base:.0f} %).")
    if k == "medicacion":
        return (f"Dijo no haber tomado sus remedios en {n - int(d['k_rec'])} de los últimos {n} días "
                f"(antes tomaba ~{base:.0f} % de los días).")
    return ""


def construir_alerta(datos: Datos, pid: str, an: dict, if_atipico: bool) -> dict:
    z = an["z"]
    fuertes = [k for k, v in z.items() if v <= Z_FUERTE]
    moderadas = [k for k, v in z.items() if Z_FUERTE < v <= Z_MODERADA]
    debiles = [k for k, v in z.items() if Z_MODERADA < v <= Z_DEBIL]

    if fuertes or len(moderadas) >= 2:
        nivel = "Revisar"
    elif moderadas or (if_atipico and debiles):
        nivel = "Observar"
    else:
        nivel = "Sin novedad"

    ordenadas = sorted([k for k, v in z.items() if v <= Z_DEBIL], key=lambda k: z[k])
    senales = [{"clave": k, "titulo": SENALES[k]["titulo"], "z": round(z[k], 2),
                "texto": _texto_senal(k, an["det"][k]),
                "fuerza": "fuerte" if z[k] <= Z_FUERTE else "moderada" if z[k] <= Z_MODERADA else "leve"}
               for k in ordenadas]
    if an["silencio"]:
        senales.append({"clave": "silencio", "titulo": "Sin respuesta", "z": None, "fuerza": "leve",
                        "texto": "No contestó el check-in casi toda la semana, aunque normalmente lo hace."})
    if an["control_perdido"]:
        senales.append({"clave": "control", "titulo": "Control de salud", "z": None, "fuerza": "leve",
                        "texto": "Faltó a un control de salud en el último mes (registro institucional)."})
    if nivel == "Sin novedad":
        senales = []

    score = float(sum(-z[k] for k in z if z[k] < 0))
    if nivel == "Sin novedad":
        explicacion = "Sin cambios relevantes respecto de su rutina habitual."
    else:
        explicacion = " ".join(s["texto"] for s in senales[:2])
    return {"persona_id": pid, "nivel": nivel, "score": round(score, 2), "senales": senales,
            "explicacion": explicacion, "if_atipico": bool(if_atipico)}


# ---------------------------------------------------------------------------
# Capa 2 + evaluación de toda la cohorte
# ---------------------------------------------------------------------------
def evaluar_cohorte(datos: Datos, contaminacion: float = 0.2, semilla: int = 0) -> dict[str, dict]:
    pids = list(datos.personas)
    analisis = {pid: analizar_persona(datos, pid) for pid in pids}
    claves = list(SENALES)
    X = np.array([[analisis[p]["z"][k] for k in claves] + [analisis[p]["resp_rec"] - analisis[p]["resp_base"]] for p in pids])
    bosque = IsolationForest(n_estimators=200, contamination=contaminacion, random_state=semilla).fit(X)
    atipico = bosque.predict(X) == -1
    return {pid: construir_alerta(datos, pid, analisis[pid], bool(atipico[i])) for i, pid in enumerate(pids)}


def serie_bienestar(datos: Datos, pid: str, dias: int = 30) -> list[float | None]:
    """Índice diario 0-1 (promedio de señales disponibles), suavizado 3 días. Solo para graficar."""
    s = datos.series[pid]
    n = len(datos.fechas)
    mat = np.vstack([s["contacto"], s["actividades"], s["animo"], s["medicacion"]])
    with np.errstate(all="ignore"):
        diario = np.nanmean(mat, axis=0)
    out = []
    for i in range(n - dias, n):
        ventana = diario[max(0, i - 2): i + 1]
        ventana = ventana[~np.isnan(ventana)]
        out.append(round(float(ventana.mean()), 3) if len(ventana) else None)
    return out


def serie_senal(datos: Datos, pid: str, clave: str, dias: int = 30) -> tuple[list[float | None], float]:
    """Serie de UNA señal (suavizada 5 días) y su media de línea base. Para mostrar el cambio que motivó la alerta."""
    v = datos.series[pid][clave]
    n = len(datos.fechas)
    out = []
    for i in range(n - dias, n):
        w = v[max(0, i - 4): i + 1]
        w = w[~np.isnan(w)]
        out.append(round(float(w.mean()), 3) if len(w) else None)
    base = v[: n - EXCLUIR_BASE]
    base = base[~np.isnan(base)]
    return out, round(float(base.mean()), 3) if len(base) else 0.0


def media_base(datos: Datos, pid: str) -> float:
    s = datos.series[pid]
    n = len(datos.fechas)
    mat = np.vstack([s["contacto"], s["actividades"], s["animo"], s["medicacion"]])[:, : n - EXCLUIR_BASE]
    with np.errstate(all="ignore"):
        return round(float(np.nanmean(mat)), 3)


# ---------------------------------------------------------------------------
# Evaluación contra la verdad simulada y auditoría de equidad
# ---------------------------------------------------------------------------
def evaluar_desempeno(datos: Datos, resultados: dict[str, dict]) -> dict:
    etiquetas = {e["persona_id"]: e for e in _leer("etiquetas_simuladas.csv")}
    out = {}
    for nombre, niveles in (("Revisar", {"Revisar"}), ("Revisar+Observar", {"Revisar", "Observar"})):
        tp = fp = fn = tn = 0
        for pid, r in resultados.items():
            real = etiquetas[pid]["es_riesgo_real"] == "True"
            pred = r["nivel"] in niveles
            tp += real and pred
            fp += (not real) and pred
            fn += real and (not pred)
            tn += (not real) and (not pred)
        out[nombre] = {"tp": tp, "fp": fp, "fn": fn, "tn": tn,
                       "recall": round(tp / max(tp + fn, 1), 3), "precision": round(tp / max(tp + fp, 1), 3)}
    trampas = {}
    for esc in ("reservado_estable", "baja_conectividad"):
        ids = [p for p, e in etiquetas.items() if e["escenario"] == esc]
        trampas[esc] = {"n": len(ids), "alertadas": sum(resultados[p]["nivel"] != "Sin novedad" for p in ids)}
    por_escenario = {}
    for p, e in etiquetas.items():
        d = por_escenario.setdefault(e["escenario"], {"n": 0, "alertadas": 0})
        d["n"] += 1
        d["alertadas"] += resultados[p]["nivel"] != "Sin novedad"
    return {"clasificacion": out, "trampas": trampas, "por_escenario": por_escenario}


def auditoria_equidad(datos: Datos, resultados: dict[str, dict]) -> dict:
    """Tasa de alertas por subgrupo (solo auditoría). Incluye falsos positivos entre personas sin riesgo simulado."""
    etiquetas = {e["persona_id"]: e for e in _leer("etiquetas_simuladas.csv")}
    out = {}
    for atributo in ("zona", "sexo", "tramo_etario"):
        grupos: dict[str, dict] = {}
        for pid, p in datos.personas.items():
            g = grupos.setdefault(p[atributo], {"n": 0, "alertadas": 0, "sin_riesgo": 0, "falsos_positivos": 0})
            alerta = resultados[pid]["nivel"] != "Sin novedad"
            g["n"] += 1
            g["alertadas"] += alerta
            if etiquetas[pid]["es_riesgo_real"] != "True":
                g["sin_riesgo"] += 1
                g["falsos_positivos"] += alerta
        for g in grupos.values():
            g["tasa_alertas"] = round(g["alertadas"] / max(g["n"], 1), 3)
            g["tasa_falsos_positivos"] = round(g["falsos_positivos"] / max(g["sin_riesgo"], 1), 3)
        out[atributo] = grupos
    return out


if __name__ == "__main__":
    d = Datos()
    res = evaluar_cohorte(d)
    ev = evaluar_desempeno(d, res)
    from collections import Counter
    print("Niveles:", Counter(r["nivel"] for r in res.values()))
    print("Clasificación:", ev["clasificacion"])
    print("Trampas:", ev["trampas"])
    print("Por escenario:", ev["por_escenario"])
    for k, v in auditoria_equidad(d, res).items():
        print(k, {g: (x["n"], x["tasa_alertas"], x["tasa_falsos_positivos"]) for g, x in v.items()})

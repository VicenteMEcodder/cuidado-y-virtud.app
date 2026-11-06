"""Generador de datos 100 % SINTÉTICOS para "Tu Cuidado" (Hackathon IA Congreso Social UC 2026).

Principios:
  * Cero datos reales: todo es inventado con una semilla fija (reproducible).
  * Minimización: solo respuestas codificadas del check-in diario (sin audio, sin texto libre,
    sin diagnósticos, sin ubicación, sin nombres reales ni RUT).
  * Los atributos de grupo (tramo etario, zona, sexo) existen SOLO para auditar sesgos;
    el detector NO debe usarlos como variables.
  * Las etiquetas de escenario (verdad simulada) van en un archivo aparte y SOLO sirven para
    evaluar el detector; el detector no debe leerlas.

Uso:
    python src/generator.py                 # genera en ./data
    python src/generator.py --seed 7 --dias 60 --out data
"""
from __future__ import annotations

import argparse
import csv
import json
import random
from datetime import date, timedelta
from pathlib import Path

# ---------------------------------------------------------------------------
# Configuración de escenarios (60 personas por defecto)
# ---------------------------------------------------------------------------
# Escenarios de RIESGO real (el detector debería alertar) y TRAMPAS (no deberían alertar).
ESCENARIOS = {
    "estable":                 {"n": 24, "riesgo": False},
    "reservado_estable":       {"n": 6,  "riesgo": False},  # trampa: prefiere la soledad, siempre igual
    "baja_conectividad":       {"n": 6,  "riesgo": False},  # trampa: contesta poco por conexión, no por riesgo
    "aislamiento_progresivo":  {"n": 6,  "riesgo": True},
    "deterioro_funcional":     {"n": 6,  "riesgo": True},
    "abandono_tratamiento":    {"n": 6,  "riesgo": True},
    "cambio_animico":          {"n": 6,  "riesgo": True},
}

NOMBRES_MUJER = [
    "María", "Rosa", "Carmen", "Elena", "Ana", "Teresa", "Gladys", "Olga", "Marta", "Nora",
    "Luz", "Berta", "Cecilia", "Edith", "Graciela", "Inés", "Lidia", "Nelly", "Patricia", "Rebeca",
    "Sandra", "Úrsula", "Wilma", "Ximena", "Yolanda", "Zoila", "Blanca", "Delia", "Flora", "Hilda",
    "Josefina", "Mirta", "Silvia", "Eliana", "Margarita", "Juana", "Norma", "Gloria", "Ester", "Irma",
]
NOMBRES_HOMBRE = [
    "Luis", "Juan", "Pedro", "Jorge", "Raúl", "Hugo", "Mario", "Sergio", "Víctor", "Iván",
    "Aldo", "Darío", "Fidel", "Héctor", "Julio", "Manuel", "Óscar", "Tomás", "Vicente", "Armando",
    "César", "Ernesto", "Gustavo", "Ismael", "Lorenzo", "Nicolás", "Alberto", "Rubén", "Enrique", "Patricio",
]

RAMPA_DIAS = 14  # el deterioro se instala gradualmente en ~2 semanas


def _hex_id(rng: random.Random) -> str:
    return "PS-" + "".join(rng.choice("0123456789abcdef") for _ in range(6))


def _alias(rng: random.Random, disponibles: list[str], todos: list[str]) -> str:
    """Nombre ficticio sin repetir mientras queden; si se agotan, se reutilizan."""
    return disponibles.pop() if disponibles else rng.choice(todos)


def _elegir(rng: random.Random, opciones: dict[str, float]) -> str:
    r, acum = rng.random(), 0.0
    for k, p in opciones.items():
        acum += p
        if r <= acum:
            return k
    return list(opciones)[-1]


def _mezcla(a: dict[str, float], b: dict[str, float], t: float) -> dict[str, float]:
    """Interpolación lineal entre dos distribuciones (t=0 -> a, t=1 -> b)."""
    return {k: (1 - t) * a[k] + t * b[k] for k in a}


# Distribuciones base (comportamiento "normal" de una persona estable)
ANIMO_BASE = {"bien": 0.70, "regular": 0.25, "mal": 0.05}
ANIMO_BAJO = {"bien": 0.15, "regular": 0.40, "mal": 0.45}
ACT_BASE = {"completo": 0.86, "parcial": 0.11, "no": 0.03}
ACT_BAJO = {"completo": 0.30, "parcial": 0.40, "no": 0.30}


def generar(seed: int = 42, dias: int = 60, fin: date | None = None):
    rng = random.Random(seed)
    fin = fin or date(2026, 11, 6)
    fechas = [fin - timedelta(days=dias - 1 - d) for d in range(dias)]

    # --- asignar escenarios (independiente de los atributos de grupo => sin sesgo incorporado) ---
    lista_esc: list[str] = []
    for nombre, cfg in ESCENARIOS.items():
        lista_esc += [nombre] * cfg["n"]
    rng.shuffle(lista_esc)

    nombres_m, nombres_h = NOMBRES_MUJER[:], NOMBRES_HOMBRE[:]
    rng.shuffle(nombres_m)
    rng.shuffle(nombres_h)

    personas, etiquetas, checkins, controles = [], [], [], []
    ids_usados: set[str] = set()

    for i, esc in enumerate(lista_esc):
        pid = _hex_id(rng)
        while pid in ids_usados:
            pid = _hex_id(rng)
        ids_usados.add(pid)

        zona = rng.choices(["urbana", "rural"], weights=[0.7, 0.3])[0]
        tramo = rng.choices(["65-74", "75-84", "85+"], weights=[0.45, 0.40, 0.15])[0]
        sexo = rng.choices(["mujer", "hombre"], weights=[0.62, 0.38])[0]  # más mujeres mayores solas
        cronico = True if esc == "abandono_tratamiento" else rng.random() < 0.5
        comparte = rng.random() < 0.85  # la persona decide si comparte con su cuidador
        consentimiento = "vigente" if rng.random() < 0.96 else "pausado"

        # Perfil de comportamiento individual (su "normalidad" propia)
        if esc == "reservado_estable":
            p_contacto = 0.20
        else:
            p_contacto = rng.choice([0.55, 0.65, 0.75, 0.85])
        p_resp_base = 0.93 if zona == "urbana" else 0.85  # la zona rural tiene peor conectividad
        if esc == "baja_conectividad":
            p_resp_base = 0.55
        p_voz = 0.80 if tramo != "85+" else 0.65  # el resto responde con botones de respaldo

        inicio = None
        if ESCENARIOS[esc]["riesgo"]:
            inicio = rng.randint(int(dias * 0.5), int(dias * 0.75))  # hay línea base antes del cambio

        personas.append({
            "persona_id": pid,
            "alias_demo": _alias(rng, nombres_m if sexo == "mujer" else nombres_h, NOMBRES_MUJER if sexo == "mujer" else NOMBRES_HOMBRE),  # nombre FICTICIO solo para la demo
            "tramo_etario": tramo,       # solo auditoría de sesgos
            "zona": zona,                # solo auditoría de sesgos
            "sexo": sexo,                # solo auditoría de sesgos
            "medicacion_cronica": cronico,
            "consentimiento": consentimiento,
            "comparte_con_cuidador": comparte,
        })
        etiquetas.append({
            "persona_id": pid,
            "escenario": esc,
            "es_riesgo_real": bool(ESCENARIOS[esc]["riesgo"]),
            "dia_inicio_cambio": inicio if inicio is not None else "",
            "fecha_inicio_cambio": fechas[inicio].isoformat() if inicio is not None else "",
        })

        # --- check-ins diarios ---
        for d, f in enumerate(fechas):
            t = 0.0
            if inicio is not None and d >= inicio:
                t = min(1.0, (d - inicio + 1) / RAMPA_DIAS)

            p_c, p_resp = p_contacto, p_resp_base
            animo_dist, act_dist = ANIMO_BASE, ACT_BASE
            p_med = 0.90

            if esc == "aislamiento_progresivo":
                p_c = (1 - t) * p_contacto + t * 0.06
                animo_dist = _mezcla(ANIMO_BASE, {"bien": 0.40, "regular": 0.40, "mal": 0.20}, t)
            elif esc == "deterioro_funcional":
                act_dist = _mezcla(ACT_BASE, ACT_BAJO, t)
                p_resp = p_resp_base * (1 - 0.15 * t)
            elif esc == "abandono_tratamiento":
                p_med = (1 - t) * 0.90 + t * 0.15
            elif esc == "cambio_animico":
                animo_dist = _mezcla(ANIMO_BASE, ANIMO_BAJO, t)
                p_resp = p_resp_base * (1 - 0.20 * t)

            respondio = rng.random() < p_resp
            if not respondio:
                checkins.append({
                    "persona_id": pid, "fecha": f.isoformat(), "respondio": False, "canal": "sin_respuesta",
                    "animo": "sin_respuesta", "contacto_social": "sin_respuesta",
                    "actividades": "sin_respuesta", "medicacion": "sin_respuesta",
                })
                continue

            canal = "voz" if rng.random() < p_voz else "botones"

            def con_rechazo(valor: str) -> str:  # a veces la persona prefiere no contestar una pregunta
                return "no_responde" if rng.random() < 0.03 else valor

            animo = con_rechazo(_elegir(rng, animo_dist))
            contacto = con_rechazo("si" if rng.random() < p_c else "no")
            actividades = con_rechazo(_elegir(rng, act_dist))
            if cronico:
                medicacion = con_rechazo("tomo" if rng.random() < p_med else "no_tomo")
            else:
                medicacion = "no_aplica"

            checkins.append({
                "persona_id": pid, "fecha": f.isoformat(), "respondio": True, "canal": canal,
                "animo": animo, "contacto_social": contacto,
                "actividades": actividades, "medicacion": medicacion,
            })

        # --- registro institucional mínimo: controles de salud (un par en el período) ---
        for _ in range(2):
            d = rng.randint(0, dias - 1)
            p_asiste = 0.92
            if esc == "abandono_tratamiento" and inicio is not None and d >= inicio:
                p_asiste = 0.15
            controles.append({
                "persona_id": pid, "fecha_control": fechas[d].isoformat(),
                "asistio": rng.random() < p_asiste,
            })

    return personas, checkins, controles, etiquetas, fechas


def _escribir_csv(ruta: Path, filas: list[dict]):
    with ruta.open("w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=list(filas[0].keys()))
        w.writeheader()
        w.writerows(filas)


def main():
    ap = argparse.ArgumentParser(description="Generador de datos sintéticos — Tu Cuidado")
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--dias", type=int, default=60)
    ap.add_argument("--out", type=str, default=str(Path(__file__).resolve().parent.parent / "data"))
    a = ap.parse_args()

    personas, checkins, controles, etiquetas, fechas = generar(a.seed, a.dias)
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)

    _escribir_csv(out / "personas.csv", personas)
    _escribir_csv(out / "checkins.csv", checkins)
    _escribir_csv(out / "controles_salud.csv", controles)
    _escribir_csv(out / "etiquetas_simuladas.csv", etiquetas)  # SOLO evaluación; el detector no la lee

    (out / "checkins.json").write_text(json.dumps(checkins, ensure_ascii=False, indent=1), encoding="utf-8")
    (out / "README_datos.txt").write_text(
        "DATOS 100%% SINTÉTICOS (inventados, semilla=%d). No corresponden a personas reales.\n"
        "personas.csv: perfil mínimo seudonimizado. Atributos de grupo solo para auditar sesgos.\n"
        "checkins.csv: respuestas codificadas del check-in diario (sin audio ni texto libre).\n"
        "controles_salud.csv: asistencia a controles (registro institucional mínimo).\n"
        "etiquetas_simuladas.csv: verdad simulada; SOLO para evaluar el detector, no usarla como variable.\n"
        % a.seed,
        encoding="utf-8",
    )

    # --- resumen y verificaciones básicas ---
    n_riesgo = sum(e["es_riesgo_real"] for e in etiquetas)
    resp = sum(c["respondio"] for c in checkins) / len(checkins)
    print(f"Personas: {len(personas)} | Días: {a.dias} ({fechas[0]} → {fechas[-1]})")
    print(f"Check-ins: {len(checkins)} | Tasa de respuesta global: {resp:.1%}")
    print(f"Controles de salud: {len(controles)} | Personas con riesgo simulado: {n_riesgo}")
    print(f"Archivos escritos en: {out}")

    assert len({p['persona_id'] for p in personas}) == len(personas), "IDs duplicados"
    assert len(checkins) == len(personas) * a.dias, "faltan check-ins"
    assert not any(k in personas[0] for k in ("nombre", "rut", "direccion", "telefono")), "campo sensible"


if __name__ == "__main__":
    main()

"""Pruebas de la garantía "decisión humana final". Ejecutar: python -m pytest tu_cuidado/tests -q"""
import sqlite3
import sys
from pathlib import Path

import pytest
from fastapi.testclient import TestClient

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "src"))
import api  # noqa: E402
import workflow as wf  # noqa: E402


@pytest.fixture()
def cli():
    with TestClient(api.app) as c:   # dispara el arranque (reinicia la BD)
        yield c


def _abierta(cli, nivel="Revisar"):
    return next(a for a in cli.get("/api/alertas").json()["alertas"] if a["nivel"] == nivel)


def _n_derivaciones():
    con = wf.conectar()
    n = con.execute("SELECT COUNT(*) FROM derivaciones").fetchone()[0]
    con.close()
    return n


def test_la_ia_deja_todo_pendiente_y_nada_ejecutado(cli):
    d = cli.get("/api/alertas").json()
    assert d["alertas"], "el detector debería generar alertas"
    assert all(a["estado"] == "PENDIENTE_REVISION" for a in d["alertas"])
    assert _n_derivaciones() == 0


def test_envio_sin_decision_es_bloqueado(cli):
    a = _abierta(cli)
    r = cli.post(f"/api/alertas/{a['id']}/intento-envio")
    assert r.status_code == 409
    assert _n_derivaciones() == 0


def test_decision_sin_justificacion_es_rechazada(cli):
    a = _abierta(cli)
    r = cli.post(f"/api/alertas/{a['id']}/decision", json={"accion": "derivar", "justificacion": ""})
    assert r.status_code == 409
    assert cli.get(f"/api/alertas/{a['id']}").json()["estado"] == "PENDIENTE_REVISION"
    assert _n_derivaciones() == 0


def test_trigger_de_la_bd_bloquea_derivacion_sin_aprobacion(cli):
    a = _abierta(cli)
    con = wf.conectar()
    with pytest.raises(sqlite3.DatabaseError):
        con.execute("INSERT INTO derivaciones(alerta_id, decision_id, destino, mensaje, ts) VALUES (?,?,?,?,?)",
                    (a["id"], 0, "x", "y", "ahora"))
    con.close()


def test_decision_valida_deriva_con_mensaje_minimo(cli):
    a = next(x for x in cli.get("/api/alertas").json()["alertas"] if x["consentimiento"]["comparte"])
    r = cli.post(f"/api/alertas/{a['id']}/decision",
                 json={"accion": "derivar", "justificacion": "Se conversó con la persona y acepta apoyo."})
    assert r.status_code == 200, r.text
    j = r.json()
    assert j["estado"] == "EJECUTADA" and j["derivacion"]["simulada"] is True
    # el mensaje no contiene variables de salud
    assert not any(p in j["derivacion"]["mensaje"].lower() for p in ("ánimo", "remedio", "medic", "mal"))
    # no se puede decidir dos veces
    r2 = cli.post(f"/api/alertas/{a['id']}/decision", json={"accion": "descartar", "justificacion": "otra decisión distinta"})
    assert r2.status_code == 409


def test_un_solo_mal_dia_no_genera_alerta(cli):
    p = cli.get("/api/demo/persona").json()
    antes = {a["persona"]["persona_id"] for a in cli.get("/api/alertas").json()["alertas"]}
    cli.post("/api/checkin", json={"persona_id": p["persona_id"], "animo": "mal", "contacto_social": "no", "actividades": "no"})
    despues = {a["persona"]["persona_id"] for a in cli.get("/api/alertas").json()["alertas"]}
    assert p["persona_id"] not in despues - antes, "una sola respuesta no debería disparar una alerta"


def test_sos_crea_alerta_prioritaria_pero_no_ejecuta_nada(cli):
    p = cli.get("/api/demo/persona").json()
    r = cli.post("/api/sos", json={"persona_id": p["persona_id"]})
    assert r.status_code == 200
    mia = next(a for a in cli.get("/api/alertas").json()["alertas"] if a["persona"]["persona_id"] == p["persona_id"])
    assert mia["estado"] == "PENDIENTE_REVISION" and mia["nivel"] == "Revisar" and mia["origen"] == "sos"
    assert _n_derivaciones() == 0


def test_persona_pausada_no_aparece_en_la_cola(cli):
    ps = cli.get("/api/personas").json()
    pausada = next((p for p in ps if p["consentimiento"] == "pausado"), None)
    if pausada:
        ids = {a["persona"]["persona_id"] for a in cli.get("/api/alertas").json()["alertas"]}
        assert pausada["persona_id"] not in ids


def test_admin_no_recibe_datos_de_salud(cli):
    fila = cli.get("/api/personas").json()[0]
    assert not any(k in fila for k in ("animo", "senales", "nivel", "explicacion", "medicacion"))

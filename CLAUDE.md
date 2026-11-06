# CLAUDE.md — Registro Central del Proyecto (Equipo 19)

> **Hackathon de Inteligencia Artificial Aplicada — Congreso Social UC 2026**
> **Desafío 1:** Cambios demográficos — *Envejecer con autonomía: IA para el cuidado comunitario de personas mayores*
>
> **REGLA CERO:** Este archivo se actualiza autónomamente cada vez que se completa un paso, se modifica código o se toma una decisión de diseño. Es la fuente de verdad del proyecto.
> **Flujo de trabajo:** un paso a la vez; no avanzar al siguiente sin aprobación explícita del usuario.

_Última actualización: 2026-11-06 — **Paso 4 entregado** (carpeta `entregables/`): declaración de transparencia, guion del pitch de 5 min y 3 preguntas críticas del jurado con respuestas. Los 4 pasos están entregados; quedan pendientes las pruebas manuales en teléfono, verificar cifras demográficas y el checklist ético oficial._

---

## 1. Contexto del Proyecto

**Desafío demográfico:** Chile envejece aceleradamente; muchas personas mayores viven solas, con riesgo de aislamiento, deterioro funcional no detectado y abandono de tratamientos. Objetivo: usar IA para **detectar a tiempo** estas situaciones y **conectar** a la persona con redes de apoyo (comunitarias, municipales, de salud).

**Restricciones del diseño (no negociables):**
- Baja alfabetización digital: voz, interfaces muy simples o mediación de un tercero.
- Respeto a la autonomía y privacidad de la persona mayor.
- Decisión humana final ante cualquier alerta.
- Datos 100 % sintéticos en el prototipo.

**Usuarios:**
| Usuario | Rol | Necesidad |
|---|---|---|
| Persona mayor (vive sola) | Sujeto de cuidado | Ser acompañada sin perder autonomía; interacción por voz/simple; control sobre sus datos |
| Cuidador informal (familiar, vecino, voluntario) | Mediador / primer contacto | Recibir avisos claros y accionables, sin sobrecarga |
| Equipo municipal / comunitario / de salud (APS, DIDECO, centros de día) | Decisor humano final | Priorizar visitas y derivaciones; revisar y decidir sobre cada alerta |

**Criterios de evaluación:** ~50 % depende de impacto social, ética, privacidad y mitigación de sesgos.

**Fuentes:** Bases oficiales (`Bases_Hackathon_IA_Congreso_Social_UC_2026.pdf`, v1) y correo de selección de Duoc UC (Equipo 19). Leídas e incorporadas el 2026-11-06.

### 1.1 Datos clave de las Bases (verificados)
- **Evento:** viernes 6-nov-2026, 08:30–18:45, Duoc UC sede San Joaquín. Premiación: martes 10-nov (Congreso Social UC).
- **Equipo:** Equipo 19 (5 integrantes, mixto UC/Duoc). Contacto organización: citt@duoc.cl.
- **Pregunta desafío (D1):** detectar a tiempo riesgo (aislamiento, deterioro funcional, abandono de tratamientos) en personas mayores que viven solas y conectarlas con redes de apoyo, respetando autonomía y privacidad.
- **Usuarios oficiales:** personas mayores, cuidadores informales, equipos municipales (**Oficinas del Adulto Mayor**).
- **Alcance esperado:** prototipo funcional (app, chatbot WhatsApp/voz, panel territorial); baja alfabetización digital (voz, interfaz simple, tercero mediador); **modelo de operación: quién recibe las alertas y qué hace con ellas**.
- **Datos sugeridos:** Censo/proyecciones INE, CASEN, datos abiertos municipales, datasets sintéticos propios.
- **Restricciones éticas del desafío:** consentimiento informado, minimización de datos sensibles de salud, prevención de vigilancia invasiva, decisión humana final ante cualquier alerta.
- **Entregables:** (1) MVP con IA que aporte valor real (no decorativa); (2) pitch con demo; (3) declaración de transparencia (modelos/herramientas IA y cómo); (4) checklist ético completado en los 2 checkpoints de Pastoral.

### 1.2 Reglas críticas de evaluación
- **CONGELAMIENTO: 14:45 h (Cierre técnico).** Se evalúa solo la versión del prototipo a esa hora; cambios posteriores no cuentan.
- **ADMISIBILIDAD:** checklist ético completo y validado en los 2 checkpoints, presentado al cierre técnico (14:45). Sin él, el equipo **no es evaluado**.
- **Pitch:** 1–2 representantes, **5 min de pitch con demo + 3 min de preguntas** del jurado (16:15–18:15). Jurado: académicos UC/Duoc, Pastoral, industria/organizaciones sociales. El jurado hará **al menos una pregunta sobre riesgos éticos o mal uso**.
- El punto de decisión humana debe estar **explícito y demostrado en el prototipo**.

| Criterio | Peso |
|---|---|
| Comprensión del problema y del usuario (delimitado, validado con evidencia/datos) | 10 % |
| Uso pertinente de la IA, no decorativo (justificado vs. alternativas sin IA) | 15 % |
| Factibilidad técnica y calidad del prototipo (incl. adaptación a baja alfabetización digital) | 15 % |
| **Ética, privacidad y mitigación de sesgos** | **25 %** |
| **Impacto y escalabilidad** (beneficio verificable, modelo de operación realista, adopción) | **25 %** |
| Calidad del pitch | 10 % |

### 1.3 Agenda del día (hoy, 2026-11-06)
| Hora | Etapa |
|---|---|
| 10:00–10:45 | Inmersión |
| 10:45–11:15 | Definición |
| 11:15–11:45 | Diseño |
| 11:45–12:45 | Desarrollo |
| 12:45–13:30 | Almuerzo (pausa) |
| 13:30–14:15 | Integración y pruebas |
| 14:15–14:45 | Cierre técnico — **congelar prototipo + checklist ético** |
| 14:45–16:15 | Preparación del pitch |
| 16:15–18:15 | Presentación final |
| 18:15–18:35 | Evaluación del jurado |

> ⚠️ El tiempo de construcción es muy limitado: el plan debe priorizar un MVP mínimo pero demostrable antes de las 14:45.

### 1.4 Otras condiciones
- Datos sensibles reales prohibidos sin resguardo y consentimiento explícito; se privilegian datasets sintéticos/públicos.
- Prototipos de autoría compartida del equipo; se autoriza publicar una ficha resumen. Mínimo 2 mentorías por equipo.
- Principios: dignidad ante todo (la IA asiste, nunca sustituye el vínculo humano), transparencia, no discriminación, protección de datos, supervisión humana, discernimiento comunitario.

### 1.5 Socio del desafío: Fundación La Rosa (charla de apertura, ~10:00)
_Fuente: transcripción del equipo (VID_20261106_095217.txt), aproximada; las cifras son dichas de palabra y no están verificadas._
- **Expositora:** Tamara (Fundación La Rosa). Estará disponible después de la charla para consultas — **oportunidad de validar el problema con el socio real (criterio "Comprensión del usuario" 10 % e "Impacto/continuidad" 25 %)**.
- **Quiénes son:** atienden a ~2.300 personas mayores en ~20 regiones/sedes del país. Lema: *"Los cuidamos para siempre"*; acompañan hasta el final de la vida.
- **Cifras citadas (verificar con INE/fuente oficial antes de usarlas en el pitch):** ~2 millones de personas mayores hoy → ~6 millones hacia 2050; ~16 % de las personas mayores declara sentirse sola sin compañía.
- **Preocupaciones declaradas:** (1) cómo la sociedad acogerá y cuidará a las personas mayores; (2) **deterioro cognitivo creciente**: se busca *acompañarlo, no simplificarlo*; (3) **soledad**, que la IA puede ayudar a atender; (4) población mayor que desconoce la IA (refuerza baja alfabetización digital).
- **Mensaje ético central:** la IA **nunca debe deshumanizar el acompañamiento**; la dignidad de la persona en todo momento; "ponerle rostro" a la persona mayor. Coincide con la regla de decisión humana final.
- **Implicancia de diseño (a confirmar en Paso 1):** Fundación La Rosa es candidata a ser el *adoptante/operador real* del modelo de operación (quién recibe las alertas). Considerar el deterioro cognitivo como señal funcional relevante, con cautela (no diagnosticar).
- _Pendiente:_ las "instrucciones exactas" del desafío que el equipo esperaba a las 10:00 aún no se han incorporado más allá de esta charla; si hay más, agregarlas aquí.

---

## 2. Arquitectura y Stack Tecnológico

_Estado: **Propuesto en Paso 1 (2026-11-06) — pendiente de aprobación del equipo.**_

**Nombre de la solución (definido por el equipo):** **Tu Cuidado**. _(El nombre de trabajo "Compañía" y el canal de llamada telefónica quedan reemplazados por la propuesta del equipo; ver sección 2.11. El resto de esta sección 2 sigue vigente salvo donde 2.11 indique ajustes.)_

### 2.1 Principio rector
La IA **prioriza y explica**; las personas **deciden y actúan**. Nada sale hacia redes de apoyo sin aprobación humana registrada. La IA no diagnostica (en especial, no diagnostica deterioro cognitivo): solo señala *cambios respecto de la rutina de la propia persona*.

### 2.2 Stack (todo Python, un solo repo, corre en un notebook sin internet crítico)
| Capa | Tecnología | Por qué |
|---|---|---|
| API / lógica | **FastAPI** | Endpoints simples, validación (Pydantic), rápido de construir |
| Panel humano | **Streamlit** (multipágina) | Panel del coordinador (cola de alertas) + simulador del canal de la persona mayor; demo visual en horas |
| Persistencia | **SQLite** (+ SQLAlchemy) | Cero infraestructura; permite *constraints* y bitácora de auditoría |
| IA de detección | **pandas + scikit-learn** (línea base individual con z-score robusto/MAD + Isolation Forest sobre la cohorte) | Detecta anomalías de verdad, es explicable y no requiere GPU ni datos reales |
| IA de lenguaje | **Claude (API Anthropic)** solo para (a) explicación en lenguaje simple y (b) borrador de mensaje; **con plantilla determinista de respaldo si no hay red** | Valor en comunicación sin delegar la decisión; el demo no depende de la red |
| Voz (diseño) | Canal de llamada/audio de WhatsApp con STT/TTS | **En el MVP se simula** con una pantalla de botones grandes (Sí / No / Más o menos) + texto leído; se declara como simulado |

### 2.3 Captura de datos (baja alfabetización digital)
1. **Check-in conversacional breve** (≈30 s, 3 preguntas, voz o botones grandes): ¿habló hoy con alguien?, ¿pudo hacer sus cosas de siempre (vestirse, cocinar, salir)?, ¿tomó sus remedios? Respuesta "no quiero responder" siempre válida y **nunca se castiga**.
2. **Mediación de tercero:** cuidador informal, voluntario o funcionario de la Oficina del Adulto Mayor / Fundación La Rosa registra una **visita** con las mismas preguntas (formulario de 1 pantalla). Útil para personas sin teléfono o con dificultad.
3. **Registro institucional mínimo:** inasistencia a control de salud (dato que ya posee el municipio/APS).
4. **Lo que NO se captura:** sin cámaras, sin micrófono siempre activo, sin GPS, sin sensores en el hogar, sin diagnósticos ni historial clínico, sin contenido de conversaciones. Se guardan solo respuestas codificadas (sí/no/parcial) + fecha.

### 2.4 Flujo lógico (de punta a punta)
```
 Persona mayor ──voz/botones──┐
 Cuidador/voluntario (visita)─┼─► [1] Captura + CONSENTIMIENTO vigente ─► SQLite (ID seudónimo)
 Registro municipal (control) ┘                     │
                                                    ▼
                         [2] Features por persona (últimos 7/14/28 días):
                             días sin contacto, tendencia de AVD,
                             adherencia a medicación, controles perdidos
                                                    │
                                                    ▼
                         [3] IA de detección:
                             a) línea base PROPIA (z-score robusto): "¿cambió respecto de su normalidad?"
                             b) Isolation Forest en cohorte: casos atípicos combinados
                             c) Resultado: nivel (Sin novedad / Observar / Revisar) + señales que lo causaron
                                                    │
                                                    ▼
                         [4] Explicación en lenguaje simple (Claude o plantilla) — SIN datos personales
                                                    │
                                                    ▼
              ┌──────────────────────────────────────────────────────────┐
              │ [5] COLA DE ALERTAS · estado = PENDIENTE_REVISION        │
              │     (la IA no puede enviar nada desde aquí)              │
              └──────────────────────────────────────────────────────────┘
                                                    │
                                                    ▼
              [6] ★ PUNTO DE DECISIÓN HUMANA (coordinador/a OAM / gestor/a social)
                  Ve: señales, explicación, historial, consentimiento.
                  Debe elegir y justificar una acción:
                    • Descartar (falso positivo)       • Esperar y reevaluar
                    • Primero llamar a la PERSONA      • Visita presencial
                    • Derivar a red (cuidador / CESFAM / programa municipal / Fundación)
                                                    │  (solo si aprueba)
                                                    ▼
                         [7] Notificación mínima al cuidador/red: el MENSAJE aprobado,
                             sin variables de salud ("Sugerimos llamar a Don Luis hoy")
                                                    │
                                                    ▼
                         [8] Bitácora de auditoría + retroalimentación
                             (resultado de la acción alimenta calibración y métricas de sesgo)
```

### 2.5 Cómo se cumple "Decisión humana final ante cualquier alerta" (garantía técnica, no solo declarativa)
1. **Máquina de estados** de la alerta: `GENERADA → PENDIENTE_REVISION → (APROBADA | DESCARTADA | EN_ESPERA) → EJECUTADA`. No existe transición `GENERADA → EJECUTADA`.
2. **El módulo de envío rechaza** cualquier derivación sin `reviewer_id`, `decision`, `justificacion` y marca de tiempo (validación en la API y *constraint* en la base de datos).
3. **La IA solo tiene permisos de escritura sobre `alerta` en estado PENDIENTE_REVISION**; no tiene acceso a los conectores de envío.
4. **Primer paso por defecto: contactar a la persona mayor** (autonomía). Derivar a terceros requiere además consentimiento vigente; si la persona lo rechazó, el humano debe documentar un motivo excepcional.
5. **Bitácora inmutable** (quién vio, qué decidió, cuándo, qué señal motivó). Visible en el demo.
6. **Sin acciones de emergencia automáticas.** Ante una señal grave, la IA solo **destaca** el caso en rojo; un humano decide si activa el protocolo.
7. **Demostrable en la demo** (exigido por la rúbrica): mostrar un intento de envío bloqueado sin aprobación y luego el flujo correcto.

### 2.6 Modelo de operación (exigido por el desafío: ¿quién recibe las alertas y qué hace?)
| Rol | Recibe | Hace | Plazo sugerido |
|---|---|---|---|
| **Coordinador/a** (Oficina del Adulto Mayor o gestor/a social de Fundación La Rosa) | Cola priorizada con explicación | Decide la acción y la justifica | Revisión diaria; "Revisar" < 24 h |
| **Cuidador informal / voluntario** | Solo mensaje aprobado y mínimo | Llama o visita; registra resultado | Según la decisión |
| **Red de salud (CESFAM/APS)** | Derivación formal aprobada | Control o visita domiciliaria | Según protocolo local |
| **Persona mayor** | Llamada/pregunta amable; puede pausar o retirar consentimiento | Responde o no, sin penalización | — |

_Hipótesis a validar con Fundación La Rosa:_ qué rol cumple hoy el equivalente al coordinador y cuántos casos puede revisar por día.

### 2.7 Por qué IA y no reglas fijas (criterio "uso pertinente", 15 %)
Un umbral fijo ("alerta si no contesta 3 días") falla: hay personas que naturalmente contactan poco, y otras cuyo cambio sutil importa más. La **línea base individual** + detección combinada de señales reduce falsos positivos y evita etiquetar a quien simplemente prefiere la tranquilidad. La IA además **prioriza** una cola que un equipo pequeño no podría revisar caso a caso, y **explica** cada alerta.

### 2.8 Mitigación de sesgos (diseño)
- **No se usan como variables** edad exacta, sexo, ingreso ni comuna: el modelo compara a cada persona **consigo misma**.
- Las respuestas "no quiero responder" y la baja conectividad **no cuentan como riesgo por sí solas**.
- Auditoría en datos sintéticos: tasa de alertas por subgrupo (zona rural/urbana, género, tramo etario) con diferencias reportadas en el panel.
- Se priorizan **falsos positivos antes que falsos negativos** *sin* sobrecargar: nivel "Observar" no notifica a nadie.
- Riesgo declarado: el deterioro cognitivo puede reflejarse en respuestas inconsistentes; se trata como **señal para acompañar**, nunca como diagnóstico.

### 2.9 Alcance del MVP (realista para la ventana 11:45–14:45)
**Dentro:** datos sintéticos (Paso 2) → features → detector (línea base + Isolation Forest) → explicación → API FastAPI → panel Streamlit con cola, decisión humana, bitácora y bloqueo sin aprobación; simulador del check-in; panel de equidad básico.
**Fuera (hoja de ruta, se declara como tal):** voz/WhatsApp real, integración con sistemas municipales/APS, despliegue en producción, validación clínica.

### 2.11 Producto definido por el equipo: "Tu Cuidado" (2026-11-06)
**Propósito (texto del equipo):** plataforma para acompañar a personas mayores que viven solas, facilitando la comunicación diaria con su red de apoyo mediante tecnología accesible y adaptada a baja alfabetización digital.

**Funcionalidad core:** *check-in diario por voz*. Cada mañana la app interactúa proactivamente con la persona mayor sobre su estado físico y emocional; responde **solo por voz** (cero texto). Las respuestas se procesan y registran para detectar situaciones de riesgo (deterioro, aislamiento, alteraciones anímicas).

**Tres perfiles de acceso (decisión del equipo):**
| Perfil | Acceso | Interfaz |
|---|---|---|
| **Persona mayor** ("paciente" en el texto del equipo) | Fricción cero: un único inicio de sesión inicial configurado por el Administrador; no vuelve a ingresar credenciales | Minimalista, guiada por voz |
| **Cuidador** | Login requerido; opción "recordar dispositivo"; credenciales completas si el dispositivo es nuevo | Estado diario, alertas de la IA, facilitar intervención humana oportuna |
| **Administrador** | Seguridad máxima: login obligatorio en cada ingreso | Registra personas, enrola dispositivos, asigna cuidadores |

**Ajustes propuestos para alinear con la rúbrica (pendientes de confirmación del equipo):**
1. **Decisión humana final = rol Cuidador.** El cuidador (familiar, voluntario o gestor/a institucional, p. ej. Fundación La Rosa / Oficina del Adulto Mayor) es quien revisa cada alerta y decide; se mantiene la máquina de estados de 2.5 (`PENDIENTE_REVISION → APROBADA/DESCARTADA/EN_ESPERA`, con justificación y bitácora). La IA nunca contacta a terceros por sí sola.
2. **Voz como canal principal, con alternativa.** Si la voz no se reconoce o la persona prefiere no hablar, hay respuesta de respaldo (botones grandes) y la opción "No quiero responder" sin penalización. Riesgo declarado: reconocimiento de voz con acentos, hipoacusia o voz débil.
3. **Minimización de datos de voz:** no se almacena el audio; se guarda solo la respuesta codificada (p. ej., bien/regular/mal, comió, tomó remedios, habló con alguien) y la fecha. El audio se descarta tras el procesamiento.
4. **Alcance de lo que ve el Cuidador:** estado diario resumido (semáforo) y alertas explicadas; **no** transcripciones. La persona mayor consiente y puede pausar o retirar qué se comparte.
5. **Administrador con mínimo privilegio:** gestiona altas, dispositivos y asignaciones, pero **no ve** respuestas de salud ni alertas.
6. **Lenguaje:** evitar "monitoreo" y "paciente" en el pitch y la interfaz (connotan vigilancia y medicalización); usar "acompañamiento" y "persona mayor/persona usuaria".
7. **Alteraciones anímicas y deterioro:** se tratan como señales de *cambio respecto de su propia normalidad* para acompañar, nunca como diagnóstico.

**Decisión confirmada por el equipo (2026-11-06): el rol Cuidador lo ejerce un/a funcionario/a** (equipo institucional: Fundación La Rosa / Oficina del Adulto Mayor / equipo municipal o de salud), no un familiar. Implicancias:
- **Decisión humana final:** la toma un profesional con responsabilidad institucional, con justificación y bitácora (refuerza trazabilidad).
- **Carga de trabajo:** un funcionario atiende a muchas personas → la IA **prioriza la cola** (valor real de la IA) y el panel muestra primero los casos "Revisar".
- **Modelo de operación (2.6):** el rol "Coordinador/a" se fusiona con "Cuidador"; los familiares/cuidadores informales quedan como **red de contacto** a la que el funcionario puede derivar (mensaje mínimo aprobado), y como extensión futura (hoja de ruta).
- **Pitch:** las Bases nombran a cuidadores informales como beneficiarios; justificar que el funcionario es el primer filtro y que el familiar se involucra solo si la persona mayor lo consiente.
- **Privacidad:** acceso por institución con mínimo privilegio, cada funcionario solo ve a las personas que el Administrador le asignó; bitácora de quién consulta qué.

**Mapeo con la arquitectura:** el simulador de check-in pasa a ser la interfaz de voz de la persona mayor (en el MVP: voz del navegador, STT/TTS, más botones de respaldo); el panel del coordinador pasa a ser el panel del Cuidador; se agrega la gestión del Administrador (altas, enrolar dispositivo con código/QR, asignar cuidador). Login simulado en el MVP (sin proveedor externo real).

### 2.12 Diseño de interfaz generado en Stitch (2026-11-06)
**Ubicación:** `stitch_extraido/stitch_plataforma_web_tu_cuidado/` (7 pantallas, cada una con `code.html` + `screen.png`, y `DESIGN.md` con el sistema de diseño). Son HTML **estáticos** con Tailwind por CDN (necesitan internet; sin lógica ni datos reales).
**Pantallas:** persona mayor (1 check-in por voz, 2 confirmación, 3 qué quiere compartir), cuidador (4 cola de alertas, 5 revisar caso con decisión humana), administrador (6 gestión), 7 acceso.
**Sistema de diseño:** azul marino `#0F3D6E`, azul medio `#3B8DBF`, naranja `#E85D04` solo para SOS; tipografías Plus Jakarta Sans y Be Vietnam Pro; semáforo con color + ícono + texto; textos ≥ 24 px para la persona mayor.
**Cumple lo acordado:** "La IA sugiere, usted decide"; justificación obligatoria y mensaje "Ninguna acción se envía sin su decisión y justificación"; consentimiento visible; "No se graban audios"; Administrador sin acceso a datos de salud; botón "Prefiero no responder"; botones de respaldo a la voz.
**Observaciones a corregir/cuidar:**
- Nombre inconsistente: la app dice "Tu Cuidado" y el logo "Cuidado y Virtud". Decidir uno.
- Las tarjetas muestran edad exacta y barrio ("78 años · Cerro Alegre"): para minimizar datos, mostrar solo tramo etario o iniciales; son datos ficticios.
- Incongruencias de datos de ejemplo (fecha "Martes 24 de Octubre" vs gráfico "Hoy (Dom)"; "primera vez en 4 meses" cuando el período simulado es de 60 días).
- El SOS es un botón iniciado **por la persona**: debe crear una alerta prioritaria para un humano, no llamar automáticamente. El `DESIGN.md` propone presión sostenida de 2 s o doble confirmación, pendiente en la maqueta.
- `DESIGN.md` usa "monitoreo" en dos pasajes internos; no debe aparecer en la interfaz ni en el pitch.

### 2.13 MVP implementado (Paso 3, 2026-11-06) — pendiente de aprobación
**Cómo correrlo** (desde la carpeta del proyecto; dependencias: `fastapi uvicorn jinja2 scikit-learn numpy pytest httpx`):
```
python -m uvicorn api:app --app-dir tu_cuidado/src --port 8000
```
Pantallas: `/` login simulado · `/mayor` check-in por voz/botones (+ `/mayor/compartir`) · `/cuidador` cola, revisión y decisión · `/admin` gestión. Pruebas: `python -m pytest tu_cuidado/tests -q` (9 pasan).
**Archivos:** `src/detector.py` (IA), `src/workflow.py` (estados + SQLite + trigger), `src/api.py` (FastAPI), `app/*.html` (pantallas basadas en Stitch con datos reales; Tailwind y logo servidos localmente, sin depender de internet), `tests/test_humano.py`.
**IA (no decorativa):** (1) línea base individual con z-score por señal (contacto, actividades, ánimo, medicación) comparando la última semana con la historia previa de la misma persona (excluye las últimas 2 semanas de la base); (2) Isolation Forest sobre el vector de cambios de la cohorte para escalar casos con señales moderadas. Niveles: Revisar (≥1 señal con z ≤ -3 o ≥2 con z ≤ -2), Observar (1 señal moderada, o atípico + señal leve), Sin novedad. No usa edad, sexo, zona ni ingreso. Explicación en lenguaje simple por plantilla determinista (**no se usó LLM** en el MVP).
**Decisión humana final (demostrable):** máquina de estados `GENERADA → PENDIENTE_REVISION → APROBADA/DESCARTADA/EN_ESPERA → EJECUTADA`; decisión exige reviewer, acción y justificación ≥ 10 caracteres; **trigger SQLite** que impide insertar derivaciones sin alerta aprobada; botón de demo "simular que el sistema intenta enviar sin aprobación" → bloqueado y registrado en bitácora; envíos **simulados** con mensaje mínimo sin variables de salud; derivar a terceros sin consentimiento exige motivo excepcional.
**Consentimiento y autonomía:** personas con acompañamiento pausado o que no comparten su estado **no generan alertas visibles** (se informa cuántas, sin detalle); la persona puede cambiar lo que comparte en `/mayor/compartir`. Una sola respuesta mala **no** dispara alerta (probado). El SOS lo inicia la persona, con doble confirmación, y crea una alerta prioritaria para un humano; no llama ni avisa a nadie por sí solo.
**Desempeño medido sobre datos simulados (semilla 42):** detecta 22 de 24 personas con riesgo simulado (sensibilidad 92 %) con Revisar+Observar; precisión 79 % (6 falsas alarmas de 36 sin riesgo). Con solo "Revisar": sensibilidad 71 %, precisión 90 %. Trampas: "baja conectividad" 0/6 alertadas; "reservado" 2/6 alertadas (falsos positivos reconocidos). Por zona, la tasa de falsos positivos es parecida (urbana 16 %, rural 18 %); el tramo 85+ muestra 29 % pero con muestra mínima (~7 personas sin riesgo). Estas cifras NO son evidencia clínica.
**Limitaciones declaradas:** no detecta bien cambios en quien ya tenía muy poco contacto; requiere varias semanas de historia; la voz depende del navegador (Chrome) y siempre hay botones de respaldo; login, QR y envíos simulados; sin integración con sistemas municipales; datos sintéticos.
**Pendiente de decidir/corregir:** nombre final ("Tu Cuidado" vs "Cuidado y Virtud"); revisar en pantalla real de teléfono; opcional: explicación con LLM; opcional: voz real por teléfono/WhatsApp (hoja de ruta).

### 2.14 App Android nativa (2026-11-06) — pendiente de aprobación
**Decisión del equipo:** llevar el MVP a una app Android nativa (Kotlin + Jetpack Compose) con datos precargados y SQLite. Proyecto de Android Studio en la raíz de la carpeta (`CuidadoYVirtudAPP`, paquete `com.example.cuidadoyvirtudapp`, minSdk 24). La versión web de `tu_cuidado/` queda como prototipo de referencia.
**Datos:** los CSV sintéticos están en `app/src/main/assets/` y se cargan a SQLite (`tucuidado.db`) en el primer arranque (`data/Db.kt`).
**Código:** `domain/Detector.kt` (línea base individual + Isolation Forest propio, mismo método que la versión Python), `domain/Workflow.kt` (estados, decisión humana, trigger SQLite), `data/Repo.kt` (fachada), `ui/` (Inicio con 3 perfiles, persona mayor con voz del sistema + botones, consentimiento, Cuidador con 4 pestañas, Administrador).
**Mismas garantías:** decisión con justificación ≥ 10 caracteres; trigger que bloquea derivaciones sin aprobación; botón de demostración del bloqueo; envíos simulados con mensaje mínimo; SOS con doble confirmación que solo crea una alerta para un humano; personas que pausan o no comparten no aparecen en la cola; el Administrador no ve datos de salud.
**Verificado:** compila (`gradlew assembleDebug`); 8 pruebas JVM pasan (`app/src/test/.../DetectorYDecisionTest.kt`): sensibilidad 22/24, falsas alarmas 6/36, baja conectividad 0/6 (idéntico a Python), el resultado no cambia si se alteran edad/sexo/zona, es reproducible, y las reglas de decisión humana. APK instalado en un teléfono conectado; **solo se vio la pantalla de inicio**: el resto de las pantallas en el teléfono NO se probó (Android bloqueó los toques simulados).
**Nota técnica:** `gradlew testDebugUnitTest` falla en este equipo porque la ruta de usuario tiene espacios y tilde ("Joaquín Nieto"); las pruebas se corrieron con JUnit directamente. Esto no afecta a la app.
**Voz (actualizado 2026-11-06, a pedido del equipo):** el micrófono se activa **solo** al terminar de leer cada pregunta (lectura de voz del sistema + reconocimiento `SpeechRecognizer` dentro de la app); si no se entiende, repite una vez la escucha; siempre hay botones de respaldo y "Prefiero no responder". Requiere el permiso de micrófono (se pide una vez, al abrir el perfil de persona mayor) y el servicio de voz de Google. No se guarda audio; solo la respuesta codificada. El proveedor del servicio de voz puede procesar el audio (declarado en la sección 6).
**Diseño (actualizado 2026-11-06):** paleta más cálida (crema, durazno, menta, turquesa, verde vivo, amarillo, rosa) sobre la base azul del logo; fondos con formas suaves; banners con degradado; botones de respuesta de colores (verde = bien, amarillo = a medias, rosa = mal, gris azulado = neutro) con ícono grande; avatares de colores en el panel del Cuidador. El naranja queda reservado para pedir ayuda (SOS). Verificado en el teléfono: pantalla de inicio; el resto debe probarse a mano.
**Pendiente:** probar manualmente todo el flujo en el teléfono; nombre final de la app; el tamaño de letra de la persona mayor se puede subir más.

### 2.15 Preguntas de seguimiento y compañía entre personas (2026-11-06, a pedido del equipo) — pendiente de aprobación
**Qué se agregó en la app Android:**
1. **Pregunta de seguimiento:** si la persona responde "Más o menos" o "Mal", la app pregunta (con voz, micrófono automático) *"¿Quiere decirme por qué?"*. Puede (a) hablar con sus palabras, (b) elegir una opción codificada (Dormí mal, Me duele algo, Me siento solo/a, Mi familia, Mis remedios, Otro motivo) o (c) "Prefiero no contar".
2. **Anotar en la ficha, con consentimiento:** lo que dice con sus palabras se **transcribe, se le muestra y solo se anota si ella responde "Sí, anótelo"**. Se guarda en `notas_ficha` (categoría simple + texto) y el funcionario lo ve en el caso como "Lo que la persona quiso contar". La bitácora guarda solo la categoría, no el relato.
3. **Compañía:** si hoy dijo que no habló con nadie, y ella aceptó "Que me presenten a otras personas del programa", la app le sugiere a otra persona que **hoy también dijo que no habló con nadie y también lo aceptó** (misma zona preferida). Si responde "Sí, me gustaría", se crea una **propuesta**; no se conectan ni se comparten teléfonos o direcciones. En la pestaña **Compañía** del Cuidador, un funcionario aprueba o no la presentación **con justificación obligatoria** y debe consultar a la otra persona antes.
**⚠️ Cambio de postura de privacidad (reemplaza lo dicho en 2.3 y 2.11 sobre "no se guardan transcripciones"):** ahora **el audio sigue sin guardarse**, pero existe texto libre **opcional y con consentimiento explícito de la persona**. Reglas que lo acotan: la **IA (detector) NO lee ni interpreta ese texto**; solo lo ve el funcionario asignado; la persona puede negarse en cada vez; hay alternativa por opciones codificadas. Riesgos a declarar: el relato puede contener datos de salud sensibles; la retención está limitada: **se borra sola a los 15 días** (implementado, ver 2.16) y la persona puede borrarlo cuando quiera; falta el cifrado en un despliegue real.
**Riesgos de la compañía entre personas (declarar en el pitch):** exposición de la identidad de una persona a otra (mitigado: solo con consentimiento previo de ambas y nombre de pila), posible daño por contacto con desconocidos o fraude (mitigado: lo organiza un funcionario, no hay contacto directo desde la app), sesgo en quién recibe sugerencias (se usa solo cercanía de zona; no se usa edad ni salud), y carga operativa del funcionario.
**Verificado:** compila; 11 pruebas JVM pasan, incluidas categorización de relatos (una prueba detectó y corrigió un error: "Extraño a mis nietos" se clasificaba como soledad), elección de candidato (prefiere la misma zona, nunca a sí misma, vacío si no hay) y justificación obligatoria. La base de datos pasó a la versión 2 (migra sin perder datos). **No probado todavía en el teléfono:** el flujo completo (Android bloquea los toques simulados): pruebe responder "Más o menos", contar el motivo, y luego responder "No" a "¿Habló hoy con alguien?".
**Pendiente:** probar el flujo en teléfono; decidir con Fundación La Rosa si la función de compañía se activa en un piloto.

### 2.16 Retención de 15 días, reinicio de demo e impacto (2026-11-06, a pedido del equipo) — pendiente de aprobación
- **Borrado automático a los 15 días** (`domain/Retencion.kt`): las notas con texto libre se eliminan solas (al abrir la app, al abrir un caso); queda en la bitácora "Borrado automático de notas (15 días)" con el número borrado. Cada nota muestra en el panel "se borra el AAAA-MM-DD".
- **"Borrar lo que conté"** (pantalla "¿Qué quiere compartir?" de la persona mayor): elimina todas sus notas, con confirmación; queda registrado en la bitácora.
- **"↺ Reiniciar demostración"** (pantalla de inicio, con confirmación): borra alertas atendidas, decisiones, notas, propuestas y cambios de consentimiento de la sesión y vuelve a los datos de partida; los datos precargados no se tocan.
- **Pestaña "Impacto"** (Cuidador): cifras reales de la sesión sobre datos sintéticos (personas por revisar vs. total que comparte; decisiones humanas, falsas alarmas descartadas, envíos bloqueados, derivaciones, propuestas, notas), el **modelo de operación** (quién recibe las alertas y qué hace: funcionario, red de apoyo, persona mayor, administrador) y continuidad (piloto con Fundación La Rosa). **Supuesto declarado:** 10 min por revisión (a validar); las horas "ahorradas" son una estimación, no una medición.
- **Verificado:** compila; 12 pruebas JVM pasan (incluye retención: 16 días vence, 14 no, y la fecha de borrado). Instalada en el teléfono; sin cierres al abrir. El flujo completo en pantalla no se probó (Android bloquea los toques simulados).

### 2.17 Filtros por categoría y agenda de recordatorios (2026-11-06, a pedido del equipo) — pendiente de aprobación
**Filtros (Cuidador > Alertas):** los tres contadores (Revisar / Observar / Sin novedad) y una fila de chips funcionan como filtro: tocar uno muestra solo esa categoría; "Todos" muestra todo. "Sin novedad" lista a las personas sin alerta (con su tendencia). La pestaña "Personas" se reemplazó por "Agenda" porque el filtro "Todos"/"Sin novedad" ya lista a las personas.
**Agenda y recordatorios (nueva pestaña "Agenda"):**
- El funcionario elige una persona y agenda **remedios, citas, llamadas u otros** (hora fija, "todos los días" o "solo hoy"). Los avisos son **recordatorios a la hora que fija el funcionario; NO son una predicción de la IA** (así lo declaran la pantalla y los documentos).
- **Tres condiciones para que llegue un aviso:** (1) el **administrador habilitó** la agenda de esa persona tras una **solicitud escrita** del funcionario (motivo ≥ 10 caracteres, sin datos de salud); (2) la **persona aceptó** recibirlos (nuevo interruptor "Que mi cuidador me recuerde mis remedios y citas"); (3) hay eventos activos.
- **Garantía en la base de datos:** un trigger rechaza crear eventos si el administrador no habilitó la agenda (`agenda_requiere_habilitacion`), igual que el bloqueo de derivaciones. Todo queda en la bitácora (solicitud, habilitación, evento agendado, aviso confirmado).
- **Administrador:** nueva sección "Solicitudes de agenda" con Habilitar / Rechazar. No ve datos de salud (solo ID seudónimo, tramo, zona y el motivo escrito).
- **Persona mayor:** al llegar la hora, aparece un aviso grande con voz ("Es hora de su remedio…") con "✅ Ya lo hice" / "Lo veo después", y además una **notificación del teléfono** (alarma del sistema). Si el teléfono está bloqueado o la app cerrada, solo la notificación avisa.
- **Demostración:** el botón 🔔 junto a cada recordatorio muestra el aviso ahora mismo, sin esperar la hora.
**Límites declarados:** las alarmas son inexactas (pueden demorar unos minutos); si el teléfono se reinicia se reprograman al abrir la app; en este prototipo de un solo teléfono, las alarmas reales solo se programan para la persona de demostración; "Lo veo después" registra la respuesta del día y no vuelve a avisar ese día; el motivo escrito de la solicitud lo ve el administrador (se pide no escribir datos de salud).
**Verificado:** compila; 14 pruebas JVM pasan (incluye cálculo de la próxima hora, validación de horas y momento del aviso). La migración de la base de datos (v2→v3) se probó instalando sobre la app existente en el teléfono (sin errores al abrir). **No probado todavía en pantalla:** el flujo completo (Android bloquea los toques simulados) ni la notificación a la hora real.

### 2.18 Más recorridos según las respuestas (2026-11-06, a pedido del equipo) — pendiente de aprobación
Cada recorrido es **opcional**, se lee en voz alta, se puede responder hablando o con botones, y siempre incluye una salida "no, gracias" que se respeta. Solo se anota lo que la persona elige, como **opción codificada** (sin texto libre), y se borra a los 15 días como el resto de las notas.
| Si responde… | La app pregunta… | Qué ocurre según su elección |
|---|---|---|
| Ánimo "Más o menos" o "Mal" | ¿Quiere decirme por qué? (voz libre u opciones) | Se anota solo si ella dice "Sí, anótelo" (ver 2.15) |
| "¿Habló hoy con alguien?" → **No** | ¿Le gustaría hablar con alguien? | **Mi familia** → nota "Quiere hablar con su familia" · **Alguien del equipo** → crea una alerta "Observar" con la etiqueta "📞 Pidió una llamada" para que un humano decida (no llama a nadie) · **Otra persona del programa** → al final se sugiere a alguien que también quiere compañía (ver 2.15) · **No, estoy bien así** → no pasa nada (respeta a quien prefiere la tranquilidad) |
| "¿Pudo hacer sus cosas?" → **Algunas / No pude** | ¿Necesita ayuda con algo? | Compras · Comida · Aseo · Moverme o trámites → nota "Necesita ayuda con…" · No, gracias → nada |
| "¿Tomó sus remedios?" → **Todavía no** | ¿Por qué no los tomó todavía? | Se me olvidó · No me quedan · Me caen mal → nota "Remedios: …" · Prefiero no decir → nada |
**Cambio de comportamiento respecto de 2.15:** la sugerencia de compañía **ya no aparece automáticamente** al decir "No" a hablar con alguien; aparece solo si la persona elige "Otra persona del programa". Es más respetuoso de la autonomía y evita sugerir compañía a quien prefiere estar sola.
**Dónde lo ve el funcionario:** las notas en la tarjeta "Lo que la persona quiso contar" del caso (si la persona tiene un caso abierto); la llamada pedida como alerta con la etiqueta 📞.
**Límites:** si la persona no comparte su estado, la alerta de "pidió una llamada" no se muestra en la cola (se respeta su consentimiento, aunque haya pedido la llamada; en un servicio real habría que resolverlo con ella); no hay recorrido para quien responde "Bien"; las opciones y las palabras clave de voz son un primer conjunto, sin validar con personas mayores.
**Verificado:** compila; 15 pruebas JVM pasan (incluye que "no, gracias", "equipo" y "programa" no generen notas). **No probado en pantalla** (Android bloquea los toques simulados).

### 2.19 Mejoras de interfaz: persona mayor y administrador (2026-11-06, a pedido del equipo) — pendiente de aprobación
**Persona mayor:** botón **"Aa"** que cambia el tamaño de toda la letra (normal → grande → muy grande) sin tocar la configuración del teléfono · **indicador de progreso** (puntos y "2 de 4") · saludo **según la hora** (antes decía siempre "Buenos días"; ahora día, tarde o noche, también por voz) · botón **"🔊 Repetir la pregunta"** · botones de respuesta con **sombra y efecto al tocar**.
**Administrador:** **resumen** arriba (personas, vigentes, pausadas, solicitudes pendientes) · solicitudes de agenda resaltadas con etiqueta "Nueva" · enrolamiento con **dibujo tipo QR** (solo ilustrativo, no se puede escanear) · personas con **búsqueda por ID** y filtro (Todas / Vigentes / Pausadas). Se mantiene el mínimo privilegio: no muestra respuestas de salud ni alertas.
**Código:** `ui/MayorScreen.kt` (envoltorio de tamaño de letra + `MayorContenido`) y `ui/AdminScreen.kt` (nuevo; se quitó la versión anterior de `InicioAdminScreens.kt`).
**Verificado:** compila; 15 pruebas JVM pasan; instalada en el teléfono sin errores al abrir. **No revisado en pantalla** (Android bloquea los toques simulados): conviene mirar cómo queda la letra "muy grande" en las pantallas con muchos botones, y el resumen del administrador.

### 2.20 Pie del Cuidador simplificado (2026-11-06, a pedido del equipo)
El pie del panel del Cuidador pasó de 6 pestañas a **3 opciones principales (🔔 Alertas, 🤝 Compañía, 📅 Agenda) y un botón "☰ Menú"** con un desplegable que contiene **📈 Impacto, 🕘 Historial, ⚖️ Equidad y "↩ Cambiar perfil"**. Cuando se está en una de las secciones del menú, el botón ☰ queda marcado y muestra el nombre de esa sección. Letra del pie más grande (13 sp). Archivo: `ui/CuidadorScreen.kt`. Verificado: compila e instala sin errores; **no revisado en pantalla** (Android bloquea los toques simulados).

### 2.21 Foto de fondo en el inicio y vista de ejemplo del calendario de citas (2026-11-06)
**Aclaración de requisito (el equipo la dio a las 14:30):** la "agenda" que quieren es de **citas**, no de recordatorios repetidos: el cuidador **propone** una cita (qué, día, hora); el **administrador la aprueba si hay disponibilidad**; si no hay, el sistema le muestra al cuidador **qué días están libres dentro del mes pedido** (calendario); y al aprobarse, la persona mayor recibe un **aviso con voz**: "Tiene una hora para … el día … a las …". Lo construido hoy (recordatorios diarios con habilitación del administrador, sección 2.17) **no es eso**. **Decisión del equipo:** construirlo **después del congelamiento**; no entra en la versión evaluada.
**Hecho ahora (solo visual):** pantalla **"Citas con calendario — VISTA DE EJEMPLO, aún no funciona"** (Agenda → tarjeta naranja "Próximamente"): calendario de noviembre 2026 con día pedido sin cupo, días disponibles sugeridos y días sin atención, y los 4 pasos del flujo. Datos ficticios; ningún botón guarda nada. Los días sugeridos serían una **regla automática sobre el calendario del administrador, no IA** (así lo dice la pantalla).
**Foto de fondo** en la pantalla de elegir perfil (`res/drawable-nodpi/fondo_inicio.webp`), con velo claro para que se lean los textos. **Pendiente:** confirmar el origen de la imagen (si es generada con IA, declararlo en la declaración de transparencia).
**Verificado:** compila e instala; la pantalla de inicio se revisó en el teléfono; la vista de ejemplo del calendario no se revisó en pantalla.

### 2.22 Reorganización de la carpeta del proyecto (2026-11-06, a pedido del equipo)
**Objetivo:** que el repositorio quede ordenado y se parezca a `AkiraXleb/DDAM_Panol_Joaquin_Nieto__Benjamin_Godoy` (proyecto de Android Studio limpio en la raíz, README, y el código organizado por capas).
**⚠️ Rutas nuevas** (los textos de las secciones anteriores de este archivo usan las rutas viejas):
| Antes | Ahora |
|---|---|
| `entregables/` | `docs/entregables/` |
| `stitch_extraido/` | `docs/diseno_stitch/` |
| `tu_cuidado/` (prototipo web Python) | `docs/prototipo_web_python/` |
| Bases, correo de Duoc, transcripción y ZIP en la raíz | `material_evento/` (solo local; ignorada por git; **no se publica**) |
| `ui/*.kt` | `ui/screen/` (pantallas), `ui/components/` (`Comunes.kt`), `ui/theme/` |
| `data/Db.kt` | `data/database/Db.kt` |
| `ui/InicioAdminScreens.kt` | `ui/screen/InicioScreen.kt` |
**Raíz ahora:** `app/`, `docs/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew*`, `README.md`, `CLAUDE.md` (se queda en la raíz para que el registro siga cargándose), `.gitignore`, y la carpeta local `material_evento/`.
**Paquetes:** `domain/` se mantiene (el repositorio de referencia no la tiene; aquí agrupa el detector, la decisión humana, la retención, la compañía y la agenda).
**Verificado:** la app compila, las 15 pruebas JVM pasan, las 9 pruebas del prototipo web pasan desde su nueva ubicación y la app se reinstaló en el teléfono sin errores al abrir. **Sin cambios de comportamiento.** Aún **no se hizo commit** de la reorganización ni se subió a GitHub.
**Pendiente del README:** completar la tabla de integrantes (nombres y roles).

### 2.23 Presentación del pitch en PowerPoint (2026-11-06)
**Archivo:** `docs/presentacion/Tu_Cuidado_Pitch_Equipo19.pptx` (9 diapositivas, 16:9, con notas del orador). Se hizo a partir del PDF del grupo (`grupo_19.pdf`, el guion que el equipo ajustó: Problema y Solución en sus palabras; demo y propuesta de valor del guion de `docs/entregables`).
**Diapositivas:** 1 Portada · 2 Problema (2 M → 6 M, 1:1) · 3 Tres preguntas · 4 Solución (IA prioriza, personas deciden) · 5 Demo persona mayor (3 capturas) · 6 Demo funcionario (alertas y compañía + bloqueo sin aprobación) · 7 Propuesta de valor (con la captura de Impacto: 22 de 48) · 8 Límites y siguiente paso (22 de 24, 6 de 36, 100 % sintético, piloto) · 9 Cierre.
**Cambios respecto del PDF del equipo:** "Fundación Las Rosas" → **Fundación La Rosa** (nombre correcto); "[X de Y]" → **22 de 48** (cifra de la pestaña Impacto en la captura; si se reinicia la demo o cambian los datos, actualizar la diapositiva 7 y su nota); se agregó una viñeta de siguientes pasos (calendario de citas aprobado por el administrador, idea para después del congelamiento).
**Pendientes:** confirmar con el INE las cifras de la diapositiva 2 (2 M y 6 M las dijo de palabra la Fundación); confirmar el origen de la foto de la portada (si es generada con IA, declararlo en la transparencia); abrir el archivo en PowerPoint para revisar el aspecto final.
**Cómo se hizo/verificó:** pptxgenjs; pasa la validación estructural; la revisión visual fue con una vista previa aproximada en HTML (en este equipo no hay PowerPoint ni LibreOffice), así que pueden existir pequeñas diferencias de tipografía al abrirlo en PowerPoint.

### 2.10 Estructura de carpetas propuesta
```
compania/
  data/            # CSV/JSON sintéticos (generador en Paso 2)
  src/
    generator.py   # datos sintéticos
    features.py    # variables por persona
    detector.py    # línea base + Isolation Forest + explicación
    explain.py     # lenguaje simple (Claude + plantilla de respaldo)
    workflow.py    # máquina de estados + reglas de decisión humana
    api.py         # FastAPI
    db.py          # SQLite
  app/             # Streamlit: panel coordinador, simulador check-in, equidad
  tests/           # test: "no se puede enviar sin aprobación"
```

---

## 3. Esquema de Datos Sintéticos

_Estado: **Generador implementado (Paso 2, 2026-11-06) — pendiente de aprobación.**_

**Código:** `tu_cuidado/src/generator.py` (solo biblioteca estándar de Python, semilla fija = reproducible).
**Ejecutar:** `python tu_cuidado/src/generator.py` (opciones: `--seed 42 --dias 60 --out tu_cuidado/data`).
**Salida en `tu_cuidado/data/`:** `personas.csv`, `checkins.csv` (+ `checkins.json`), `controles_salud.csv`, `etiquetas_simuladas.csv`, `README_datos.txt`.
**Volumen por defecto:** 60 personas × 60 días (2026-09-08 → 2026-11-06) = 3.600 check-ins, 120 controles de salud.

### 3.1 Tablas
**`personas.csv`** — perfil mínimo seudonimizado (sin nombre real, RUT, dirección, teléfono ni diagnóstico):
| Campo | Valores | Uso |
|---|---|---|
| `persona_id` | `PS-xxxxxx` (hex aleatorio) | Clave seudónima |
| `alias_demo` | nombre **ficticio** coherente con `sexo` | Solo para la demo |
| `tramo_etario` | 65-74 / 75-84 / 85+ | **Solo auditoría de sesgos** (no es variable del modelo) |
| `zona` | urbana / rural | **Solo auditoría de sesgos** |
| `sexo` | mujer / hombre | **Solo auditoría de sesgos** |
| `medicacion_cronica` | True/False | Define si aplica la pregunta de medicación |
| `consentimiento` | vigente / pausado | El detector/panel debe respetarlo |
| `comparte_con_cuidador` | True/False | Decide qué ve el Cuidador |

**`checkins.csv`** — una fila por persona-día; respuestas **codificadas** (nunca audio ni texto libre):
| Campo | Valores |
|---|---|
| `respondio` | True / False (False = no contestó el check-in) |
| `canal` | voz / botones / sin_respuesta |
| `animo` | bien / regular / mal / no_responde / sin_respuesta |
| `contacto_social` | si / no / no_responde / sin_respuesta (¿habló hoy con alguien?) |
| `actividades` | completo / parcial / no / no_responde / sin_respuesta (actividades de la vida diaria) |
| `medicacion` | tomo / no_tomo / no_aplica / no_responde / sin_respuesta |

**`controles_salud.csv`** — registro institucional mínimo: `persona_id, fecha_control, asistio`.
**`etiquetas_simuladas.csv`** — verdad simulada (`escenario`, `es_riesgo_real`, `fecha_inicio_cambio`). **Solo para evaluar el detector; está prohibido usarla como variable.**

### 3.2 Escenarios simulados (60 personas)
| Escenario | N | ¿Riesgo real? | Qué simula |
|---|---|---|---|
| `estable` | 24 | No | Rutina normal con ruido |
| `reservado_estable` | 6 | No | **Trampa:** contacto social bajo (~20 %) *siempre*; prefiere la tranquilidad |
| `baja_conectividad` | 6 | No | **Trampa:** contesta ~55 % por mala conexión, respuestas normales |
| `aislamiento_progresivo` | 6 | Sí | El contacto social cae gradualmente (~14 días) tras una línea base |
| `deterioro_funcional` | 6 | Sí | Las actividades diarias pasan de "completo" a "parcial/no"; contesta algo menos |
| `abandono_tratamiento` | 6 | Sí | Deja de tomar medicación crónica y falta a controles |
| `cambio_animico` | 6 | Sí | El ánimo baja a regular/mal; contesta algo menos |
El cambio empieza entre el 50 % y el 75 % del período (siempre hay línea base previa) y se instala en una rampa de 14 días.

### 3.3 Decisiones de privacidad y sesgo en los datos
- **Cero datos reales**, nombres ficticios, IDs aleatorios; verificación automática de que no existan campos sensibles.
- **Minimización:** sin audio, texto libre, ubicación, diagnósticos ni historial clínico.
- **Sin sesgo incorporado:** el escenario se asigna al azar, **independiente** de zona, sexo y edad. Así la auditoría de equidad del Paso 3 mide al detector y no un sesgo plantado.
- **Trampas de falsos positivos** (reservado y baja conectividad) para demostrar que la línea base individual evita etiquetar a quien simplemente es reservado o tiene mala conexión.
- Límite honesto: la brecha rural/urbana de respuesta es **pequeña** en esta corrida (≈85 % vs 86 %), porque la baja conectividad se concentra en un escenario y no en la zona. Si se quiere probar sesgo por conectividad rural, se puede ajustar.

---

## 4. Checklist Ético

- [ ] **Minimización de datos sensibles de salud:** recolectar solo indicadores agregados/binarios necesarios; sin diagnósticos ni historial clínico.
- [ ] **Consentimiento informado** (restricción oficial del desafío): flujo explícito, simple y revocable, adaptado a baja alfabetización digital.
- [ ] **Checklist oficial de la hackathon** completado y validado en los 2 checkpoints de Pastoral — **requisito de admisibilidad, entregar antes de las 14:45**.
- [ ] **Texto libre opcional (2.15):** solo con "Sí, anótelo"; la IA no lo lee; solo lo ve el funcionario; se borra solo a los 15 días y la persona puede borrarlo cuando quiera (implementado).
- [ ] **Compañía entre personas (2.15):** solo con consentimiento de ambas, sin compartir contactos, con decisión humana justificada.
- [ ] **Datos sintéticos únicamente** en el prototipo; cero datos reales.
- [ ] **Prevención de vigilancia invasiva:** sin cámaras, sin micrófonos siempre activos, sin geolocalización continua; captura por interacción voluntaria.
- [ ] **Consentimiento y autonomía:** la persona mayor puede aceptar, pausar o retirar su participación; control sobre quién ve qué.
- [ ] **Decisión humana final (human-in-the-loop):** ninguna derivación ni contacto a redes de apoyo sin revisión y aprobación explícita de una persona.
- [ ] **Explicabilidad:** cada alerta indica en lenguaje simple qué señales la motivaron.
- [ ] **Mitigación de sesgos:** revisar sesgos por edad, género, nivel socioeconómico, territorio rural/urbano, discapacidad; evitar penalizar estilos de vida distintos (p. ej., vivir con poca vida social por elección).
- [ ] **Falsos positivos/negativos:** documentar costos de cada uno; umbrales conservadores y revisables.
- [ ] **Transparencia:** declarar modelos de IA usados y limitaciones.
- [ ] **Seguridad:** pseudonimización, mínimo acceso, retención limitada (diseño).

---

## 5. Progreso de Tareas

### To-Do
- [ ] Verificar con el INE las cifras demográficas del pitch (marcadas [VERIFICAR] en el guion)
- [ ] Probar en teléfono todo el flujo (voz automática, seguimiento, compañía, borrado, reinicio, impacto) y ensayar la demo
- [ ] Completar checklist ético oficial en los 2 checkpoints de Pastoral (antes de 14:45) — **es requisito de admisibilidad y solo lo puede hacer el equipo**
- [ ] Congelar prototipo a las 14:45

### Doing
- Repositorio publicado en https://github.com/VicenteMEcodator/cuidado-y-virtud.app (rama `main`, commit `e9b06c5`, 2026-11-06) con `README.md`. **No se subieron** (ver `.gitignore`): el correo de Duoc (datos personales), las Bases y la transcripción de la charla (material ajeno), el ZIP de Stitch, `.idea`, `.claude`, `local.properties`, compilaciones y la base de datos generada. Se quitó de `app/build.gradle.kts` un ajuste de pruebas con ruta local. _Esta nota se agregó después del push: el commit publicado no la incluye._
- Paso 4: Entregables finales — **entregados** en `entregables/` (`1_DECLARACION_DE_TRANSPARENCIA.md`, `2_GUION_PITCH_5_MINUTOS.md`, `3_PREGUNTAS_DEL_JURADO.md`); falta revisión y ensayo del equipo
- Paso 3: Prototipo funcional (MVP) — entregado en web (sección 2.13) y Android (2.14–2.16); aprobado en la práctica por el equipo (funciona, pidió mejoras que se incorporaron)

### Done
- [x] Regla Cero: creación de `CLAUDE.md` (2026-11-06)
- [x] Lectura de Bases y correo; datos clave, rúbrica y agenda incorporados a la sección 1 (2026-11-06)
- [x] Incorporada la charla de Fundación La Rosa (sección 1.5) (2026-11-06)
- [x] Paso 2 aprobado por el equipo (2026-11-06)
- [x] Diseño de interfaz recibido de Stitch y revisado (sección 2.12) (2026-11-06)
- [x] Paso 1 aprobado por el equipo, incluidos los 7 ajustes de 2.11 (2026-11-06)

---

## 6. Registro de Modelos IA (Declaración de Transparencia)

| Modelo / Herramienta | Uso en el proyecto | Fecha | Notas |
|---|---|---|---|
| Claude (Anthropic) vía Claude Code | Asistente de arquitectura, generación de código y redacción de documentos del equipo | 2026-11-06 | Supervisado y aprobado por el equipo en cada paso |
| **App Android: z-score + Isolation Forest implementados en Kotlin** | Detector del prototipo en ejecución (reemplaza a scikit-learn de la versión web) | 2026-11-06 | Mismas métricas que Python: 22/24 detectados, 6/36 falsas alarmas; sin LLM en la app |
| **SpeechRecognizer y TextToSpeech de Android (servicios de Google del teléfono)** | Escuchar respuestas cortas y leer preguntas; micrófono automático | 2026-11-06 | La app no guarda el audio; el servicio de Google puede procesarlo en línea |
> La **declaración de transparencia final** (versión para entregar) está en `entregables/1_DECLARACION_DE_TRANSPARENCIA.md`.

| Claude (Anthropic) vía Claude Code — **usado en el desarrollo** | Generación de código (generador de datos, detector, API, pantallas) y documentos del equipo | 2026-11-06 | Revisado y probado (9 pruebas automáticas); **no forma parte del prototipo en ejecución** |
| Claude (Anthropic) vía API — **NO usado en el MVP** | Estaba previsto para redactar explicaciones; se reemplazó por plantilla determinista | — | Opción de hoja de ruta; si se usa, solo recibiría señales seudonimizadas y no decidiría |
| Isolation Forest (scikit-learn 1.9) + línea base individual (z-score) — **usado en el MVP** | Detección de cambios de rutina sobre datos 100 % sintéticos; prioriza y explica, no decide | 2026-11-06 | Entrada: respuestas codificadas del check-in y asistencia a controles. No usa edad, sexo, zona ni ingreso |
| Google Stitch — **usado en el diseño** | Diseño de las pantallas (HTML de referencia) | 2026-11-06 | Adaptadas a datos reales por el equipo |
| Reconocimiento de voz del navegador (Web Speech API) — **usado en la interfaz** | Entender respuestas cortas de la persona mayor; el audio no se guarda ni se envía a nuestro servidor | — | El proveedor del navegador puede procesar el audio; siempre hay botones de respaldo |

_Se ampliará con los modelos usados en el prototipo (p. ej., detección de anomalías) y su propósito, entradas y limitaciones._

---

## 7. Registro de Decisiones de Diseño

| Fecha | Decisión | Motivo |
|---|---|---|
| 2026-11-06 | Priorizar el MVP por rúbrica: ética 25 % + impacto 25 % pesan la mitad; IA no decorativa 15 %; la demo debe mostrar el punto de decisión humana | Bases, sección 12 |
| 2026-11-06 | Stack: FastAPI + Streamlit + SQLite + scikit-learn; Claude solo para explicar/redactar (con plantilla de respaldo). Voz simulada en el MVP | Velocidad de construcción y demo sin depender de la red |
| 2026-11-06 | IA = línea base individual + Isolation Forest; no usa edad, sexo, ingreso ni comuna como variables | Mitigación de sesgos; comparar a la persona consigo misma |
| 2026-11-06 | Decisión humana forzada por máquina de estados y validación en API/BD; primer paso por defecto = contactar a la persona mayor | Cumplir "decisión humana final" de forma demostrable |
| 2026-11-06 | Producto "Tu Cuidado": check-in diario por voz y 3 roles (Persona mayor / Cuidador / Administrador). Ajustes propuestos en 2.11 (decisión humana en el Cuidador, no guardar audio, Admin sin acceso a datos de salud, voz con respaldo) | Propuesta del equipo + alineación con rúbrica ética; pendiente de confirmación |
| 2026-11-06 | El Cuidador es un/a **funcionario/a institucional** (confirmado por el equipo); familiares como red de derivación, no como usuarios principales | Operación realista, responsabilidad profesional en la decisión humana |
| 2026-11-06 | Datos sintéticos con escenarios de riesgo y 2 trampas de falso positivo (reservado, baja conectividad); escenario asignado al azar, independiente de zona/sexo/edad; etiquetas en archivo aparte | Medir al detector y no un sesgo plantado; demostrar mitigación de sesgos |
| 2026-11-06 | Tratar a Fundación La Rosa como socio/usuario institucional de referencia y enmarcar la IA como acompañamiento que no deshumaniza | Charla de apertura (transcripción del equipo) |
| 2026-11-06 | Trabajo por pasos con aprobación explícita; `CLAUDE.md` como registro central | Instrucción del equipo (Regla Cero) |

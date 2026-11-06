# Tu Cuidado — Cuidado y Virtud

**IA para el cuidado comunitario de personas mayores que viven solas, con la decisión final siempre en manos de una persona.**

Prototipo del **Equipo 19** para la *Hackathon de Inteligencia Artificial Aplicada · Congreso Social UC 2026* (UC · Duoc UC), **Desafío 1: «Envejecer con autonomía»**.

> ⚠️ **Es un prototipo de hackathon. Todos los datos son 100 % sintéticos** (personas, nombres y respuestas inventados). No hay datos de personas reales, y el inicio de sesión, el enrolamiento de dispositivos y el envío de mensajes están **simulados**. No es un producto clínico ni evidencia clínica.

---

## El problema

Chile envejece rápido y muchas personas mayores viven solas. Los equipos municipales y de salud no alcanzan a hacer seguimiento individual, y la mayoría de las personas mayores no usa aplicaciones. El desafío pide detectar a tiempo situaciones de riesgo (aislamiento, deterioro funcional, abandono de tratamientos) y conectar con redes de apoyo, **respetando autonomía y privacidad**, con baja alfabetización digital y **decisión humana final ante cualquier alerta**.

## La solución

**Tu Cuidado** es un acompañamiento de **un minuto al día**:

- **Persona mayor:** cada mañana la app le hace unas preguntas por voz y **el micrófono se enciende solo** al terminar de leer cada pregunta. Sin contraseñas ni texto; si no puede hablar, hay botones grandes de colores y "Prefiero no responder" (que nunca cuenta como riesgo).
- **IA que prioriza y explica, no decide:** compara a cada persona **consigo misma**. Quien siempre habla poco no genera alertas; quien cambia, sí. Cada alerta se explica en lenguaje simple.
- **Funcionario/a (Cuidador):** recibe una cola priorizada, elige una acción (descartar, esperar, llamar, visitar o derivar) y **debe justificarla**. Solo entonces puede salir un mensaje mínimo, sin datos de salud, a la red de apoyo.
- **Administrador/a:** da de alta personas, enrola dispositivos y asigna funcionarios. **No ve respuestas de salud ni alertas.**

### Qué incluye la app Android

| Para quién | Funciones |
|---|---|
| Persona mayor | Check-in por voz con micrófono automático y botones de respaldo · pregunta de seguimiento ("¿quiere decirme por qué?") si dice "más o menos" o "mal" · botón de ayuda con doble confirmación · consentimiento ("¿qué quiere compartir?") · borrar lo que contó · sugerencia de compañía con otra persona del programa (con consentimiento) |
| Funcionario/a | Cola de alertas con semáforo (color + ícono + texto) · caso con señales, gráfico "su normalidad vs. últimos 30 días" y consentimiento · **punto de decisión humana con justificación obligatoria** · bitácora · propuestas de compañía · pestaña de **impacto y modelo de operación** · panel de **equidad y transparencia** |
| Administración | Personas registradas, código de enrolamiento (simulado) |

## Cómo funciona la IA (sin cajas negras)

1. **Línea base individual (z-score):** compara la última semana de cada señal (contacto social, actividades diarias, ánimo, medicación) con la historia previa **de la misma persona** (excluye las últimas 2 semanas de la base para no contaminarla).
2. **Isolation Forest (implementado en Kotlin):** detecta combinaciones de cambios atípicas en el grupo y **escala** casos con señales moderadas. No alerta por sí solo.
3. **Explicación por plantillas:** el texto de cada alerta sale de reglas. **No hay ningún LLM dentro de la app.**
4. **No se usan** edad, sexo, zona ni ingreso como variables (una prueba automática lo verifica).

Niveles: **Revisar** (≥ 1 señal fuerte o ≥ 2 moderadas) · **Observar** · **Sin novedad**.

## Ética y privacidad por diseño

| Principio | Cómo se cumple |
|---|---|
| **Decisión humana final** | Máquina de estados `GENERADA → PENDIENTE_REVISION → APROBADA/DESCARTADA/EN_ESPERA → EJECUTADA`. Una decisión exige justificación (≥ 10 caracteres). **Un trigger de SQLite impide insertar una derivación sin alerta aprobada**; la app incluye una demostración de ese bloqueo. |
| **Minimización** | Solo respuestas codificadas (bien/regular/mal, sí/no) y asistencia a controles. Sin cámaras, sensores, GPS ni historial clínico. **El audio no se guarda.** |
| **Consentimiento y autonomía** | La persona elige qué comparte, puede pausar el acompañamiento y borrar lo que contó. Quienes pausan o no comparten **no aparecen** en la cola. |
| **Texto libre opcional** | Si la persona cuenta *por qué* se siente mal, **solo se anota si ella dice "Sí, anótelo"**, la IA **no lo lee**, solo lo ve el funcionario y **se borra solo a los 15 días**. |
| **Pedido de ayuda** | Lo inicia la persona (doble confirmación) y solo crea una alerta prioritaria para un humano. No llama ni avisa a nadie por sí solo. |
| **Compañía entre personas** | Solo entre quienes aceptaron conocer a otras, solo nombre de pila, sin teléfonos ni direcciones. Decir "sí" **no las conecta**: un funcionario decide, con justificación. |
| **Transparencia** | Declaración de transparencia, registro de modelos y panel de equidad dentro de la app. |

## Resultados medidos (datos simulados — no son evidencia clínica)

Sobre 60 personas sintéticas durante 60 días, 24 con un cambio de riesgo simulado:

- Detectó **22 de 24** (sensibilidad 92 %), con **6 falsas alarmas entre 36** personas sin riesgo (precisión 79 %). Un humano descarta esas falsas alarmas.
- Casos "trampa": personas con mala conexión → **0 de 6** con alerta; personas reservadas que siempre hablan poco → **2 de 6** con alerta (falsos positivos que reconocemos).

**Limitaciones conocidas:** no detecta bien cambios en quien ya tenía muy poco contacto; necesita varias semanas de historia; el reconocimiento de voz puede fallar con acentos, voz débil o hipoacusia; la tasa por grupo pequeño (p. ej. 85+) varía mucho por azar; sin cifrado de la base de datos; sin integración con sistemas municipales ni de salud.

## Tecnología

- **App:** Android nativa, **Kotlin + Jetpack Compose**, `minSdk 24`.
- **Datos:** **SQLite** local, con los datos sintéticos precargados desde `app/src/main/assets/*.csv`.
- **Voz:** `SpeechRecognizer` y `TextToSpeech` del sistema Android (servicios de Google del teléfono; la app no guarda el audio, pero el servicio puede procesarlo en línea).
- **Prototipo web previo** (`tu_cuidado/`): Python, FastAPI, scikit-learn y pantallas HTML basadas en el diseño de Stitch.

## Cómo ejecutarlo

### App Android
1. Abrir esta carpeta con **Android Studio** (usa el JDK que trae).
2. Conectar un teléfono Android con la depuración USB activada, o iniciar un emulador.
3. Ejecutar ▶ **Run**, o por terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   El APK queda en `app/build/outputs/apk/debug/`.
4. Al abrir **"Soy una persona mayor"**, Android pedirá el permiso de micrófono (necesario para el micrófono automático). Hace falta el servicio de voz de Google en el teléfono; si no está, los botones funcionan igual.
5. En la pantalla de inicio, **"↺ Reiniciar demostración"** deja todo como al principio.

### Prototipo web (opcional)
```bash
pip install fastapi uvicorn scikit-learn numpy pytest httpx
python tu_cuidado/src/generator.py          # regenera los datos sintéticos
python -m uvicorn api:app --app-dir tu_cuidado/src --port 8000
```
Luego abrir `http://localhost:8000`. Pruebas del prototipo web: `python -m pytest tu_cuidado/tests -q`.

## Pruebas

`app/src/test/.../DetectorYDecisionTest.kt` (12 pruebas JVM) verifica: sensibilidad y falsas alarmas del detector, que los casos de mala conexión no disparen alertas, que el resultado **no cambia si se alteran edad, sexo o zona**, que es reproducible, que no existe el salto de "en revisión" a "ejecutado", que una decisión sin justificación se bloquea, que el mensaje de derivación no lleva datos de salud, la clasificación de relatos, la elección de candidato para compañía y la retención de 15 días.

```bash
./gradlew testDebugUnitTest
```
> Nota honesta: en el equipo donde se desarrolló, `gradlew testDebugUnitTest` falló porque la ruta de usuario tenía espacios y tilde, y las 12 pruebas se ejecutaron con JUnit directamente (todas pasan). En una ruta sin espacios debería funcionar, pero **no lo hemos verificado**.

## Estructura del repositorio

```
app/                    App Android (Kotlin + Compose)
  src/main/java/...
    domain/             Detector (IA), flujo con decisión humana, retención, compañía
    data/               SQLite (esquema, precarga, trigger) y fachada de datos
    ui/                 Pantallas: inicio, persona mayor, Cuidador, administración
  src/main/assets/      Datos sintéticos (CSV)
  src/test/             Pruebas JVM
tu_cuidado/             Prototipo web previo (Python) y generador de datos sintéticos
stitch_extraido/        Diseño de pantallas generado en Stitch (referencia)
entregables/            Declaración de transparencia, guion del pitch y preguntas del jurado
CLAUDE.md               Registro central de decisiones del proyecto
```

## Documentos del equipo

- [Declaración de transparencia](entregables/1_DECLARACION_DE_TRANSPARENCIA.md)
- [Registro de decisiones y arquitectura (`CLAUDE.md`)](CLAUDE.md)

## Próximos pasos

Un **piloto pequeño con una organización socia** (por ejemplo, Fundación La Rosa) con unas 20 personas y 1–2 funcionarios, con consentimiento informado y revisión ética; cifrado y retención definida para todos los registros; revisión legal de protección de datos (Ley 19.628 y Ley 21.719); voz por teléfono o WhatsApp para quienes no usan app; y medir casos detectados a tiempo y satisfacción de personas mayores y funcionarios.

## Uso de herramientas de IA

El código, el diseño y los documentos se desarrollaron con apoyo de **Claude (Anthropic) mediante Claude Code** y **Google Stitch**, bajo decisión y revisión del equipo. Detalle en la [declaración de transparencia](entregables/1_DECLARACION_DE_TRANSPARENCIA.md).

---
*Equipo 19 · Hackathon IA Aplicada UC · Duoc UC 2026. Prototipo con fines académicos. La licencia del código debe definirla el equipo.*

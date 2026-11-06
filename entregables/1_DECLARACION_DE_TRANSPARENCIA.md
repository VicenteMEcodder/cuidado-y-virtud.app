# Declaración de transparencia — Tu Cuidado

**Hackathon de Inteligencia Artificial Aplicada · Congreso Social UC 2026 · Desafío 1**
**Equipo 19 · Viernes 6 de noviembre de 2026**

Esta declaración dice, sin adornos, qué modelos y herramientas de IA usamos, para qué, qué datos tocan y qué límites tienen.

---

## 1. Qué hace la IA de Tu Cuidado (y qué no)

**Hace:** compara a cada persona mayor *consigo misma* y señala cambios de rutina (menos contacto, más dificultad en sus actividades, ánimo más bajo, medicación que se deja). Ordena una lista para que un funcionario sepa a quién mirar primero y le explica en lenguaje simple por qué.

**No hace:** no diagnostica (tampoco deterioro cognitivo), no decide, no contacta a nadie, no deriva a nadie, no lee lo que la persona cuenta con sus palabras y no usa edad, sexo, zona ni ingreso como variable. **Toda acción la decide y la justifica una persona.**

## 2. Modelos y herramientas de IA usados

### 2.1 Dentro del prototipo en ejecución (app Android)
| Componente | Tipo | Para qué | Entradas | Observaciones |
|---|---|---|---|---|
| **Línea base individual (z-score)** | Estadística clásica | Medir cuánto cambió la última semana respecto de la historia de la misma persona | Respuestas codificadas del check-in (ánimo, contacto, actividades, medicación) y asistencia a controles | Implementación propia en Kotlin. Explicable: cada alerta muestra qué señal la causó |
| **Isolation Forest** | Aprendizaje no supervisado | Detectar combinaciones de cambios atípicas dentro del grupo y escalar casos con señales moderadas | Vector de cambios por persona (los z-score) | Implementación propia en Kotlin, semilla fija (resultado reproducible). Solo escala; no alerta por sí solo |
| **Plantillas de lenguaje simple** | Reglas (no es IA generativa) | Escribir la explicación de cada alerta | Resultado del detector | **No se usa ningún modelo de lenguaje (LLM) dentro de la app** |
| **Reconocimiento y lectura de voz del sistema Android** | Servicio del teléfono (Google) | Escuchar respuestas cortas y leer las preguntas en voz alta | Voz de la persona, solo mientras responde | **Nuestra app no guarda el audio.** Según el teléfono, el servicio de voz de Google puede procesarlo en línea; por eso siempre hay botones de respaldo y "Prefiero no responder" |

### 2.2 Durante el desarrollo
| Herramienta | Uso | Supervisión |
|---|---|---|
| **Claude (Anthropic), mediante Claude Code** | Apoyo para diseñar la arquitectura, escribir código (generador de datos sintéticos, detector, app Android, pruebas) y redactar estos documentos | El equipo decidió cada paso y aprobó cada entrega. El código se compiló y se probó (12 pruebas automáticas) |
| **Google Stitch** | Diseño de las pantallas de referencia | Adaptadas por el equipo |
| **scikit-learn (Python)** | Prototipo web previo del mismo detector, usado para contrastar resultados | La versión Android reproduce las mismas métricas |

## 3. Datos

- **Todos los datos son sintéticos**: 60 personas inventadas durante 60 días, generadas por un script nuestro con semilla fija. No hay datos reales de ninguna persona. Nombres e identificadores son ficticios.
- **Minimización:** solo respuestas codificadas (bien/regular/mal, sí/no) y asistencia a controles. No se captura audio, ubicación, cámara, sensores, diagnósticos ni historial clínico.
- **Texto libre opcional:** si la persona dice que se siente "más o menos" o "mal", puede contar por qué. **Solo se anota si ella responde "Sí, anótelo".** La IA no lo lee; solo lo ve el funcionario. **Se borra solo a los 15 días** y la persona puede borrarlo cuando quiera.
- **Consentimiento:** la persona decide si comparte su estado, si se avisa a su familia, si quiere que le presenten a otras personas y si pausa el acompañamiento. Quienes pausan o no comparten no aparecen en la cola.

## 4. Supervisión humana

- Cada alerta queda **pendiente de revisión**. Un funcionario elige una acción (descartar, esperar, llamar, visitar o derivar) y **debe escribir una justificación**.
- Solo después de esa decisión puede salir un mensaje a la red de apoyo, y es un mensaje mínimo, sin datos de salud.
- Esta regla no es solo una promesa: **la base de datos rechaza cualquier derivación sin aprobación**, y la demostración lo muestra en vivo.
- El pedido de ayuda ("Necesito ayuda ahora") lo inicia la persona, con doble confirmación, y solo crea una alerta prioritaria para un humano. No llama ni avisa a nadie por sí solo.
- Las presentaciones entre personas del programa las decide un funcionario con justificación; la app no comparte teléfonos ni direcciones.
- Todo queda en una bitácora (quién, qué, cuándo).

## 5. Medición, sesgos y límites (datos simulados; no son evidencia clínica)

- El detector identificó **22 de 24** personas con un cambio de riesgo simulado, con **6 falsas alarmas entre 36** personas sin riesgo. Esas falsas alarmas las revisa y descarta un humano.
- **Casos "trampa":** personas con mala conexión que contestan poco: 0 de 6 con alerta. Personas reservadas que siempre hablan poco: 2 de 6 con alerta (falsos positivos que reconocemos).
- **Sesgos:** el modelo no usa edad, sexo, zona ni ingreso; una prueba automática verifica que el resultado no cambia si se alteran esos datos. Aun así, el panel de equidad muestra tasas por grupo y los grupos pequeños (p. ej. 85+) varían mucho por azar. Con datos reales habría que repetir esta auditoría.
- **Límites conocidos:** no detecta bien cambios en quien ya tenía muy poco contacto; necesita varias semanas de historia; el reconocimiento de voz puede fallar con acentos, voz débil o hipoacusia; el deterioro cognitivo puede afectar las respuestas y se trata solo como señal para acompañar, nunca como diagnóstico.
- **Lo que el prototipo simula:** el inicio de sesión, el enrolamiento de dispositivos, el envío de mensajes a la red y la asignación de funcionarios. No hay integración con sistemas municipales ni de salud.
- **Lo que falta para un uso real:** cifrado de la base de datos, retención definida para el resto de los registros (hoy solo las notas se borran a los 15 días), revisión legal de protección de datos (Ley 19.628 y la nueva Ley 21.719), validación con personas mayores y funcionarios, y un piloto pequeño con una organización socia.

## 6. Responsabilidad

Las decisiones de diseño, los textos y los resultados que presentamos son responsabilidad del Equipo 19. Las herramientas de IA nos ayudaron a construir más rápido; no reemplazaron nuestro criterio ni el de las personas que usarían esta solución.

*Detalle técnico y registro de decisiones: archivo `CLAUDE.md` del proyecto.*

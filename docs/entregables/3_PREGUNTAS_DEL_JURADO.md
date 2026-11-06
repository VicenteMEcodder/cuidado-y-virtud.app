# Preguntas críticas del jurado y respuestas — Tu Cuidado

Las Bases dicen que el jurado hará **al menos una pregunta sobre riesgos éticos o escenarios de mal uso**, y esa respuesta se evalúa dentro del 25 % de ética. Tienen 3 minutos de preguntas: respondan en **30–40 segundos** cada una.

**Regla de oro para responder:** (1) reconozcan el riesgo, (2) digan qué hace el diseño al respecto, (3) digan con honestidad qué falta. Un jurado confía más en quien nombra sus límites.

---

## Pregunta 1 — Vigilancia invasiva
> **"Esto suena a vigilar a personas mayores que viven solas. ¿Quién les dio permiso? ¿Cómo evitan que se convierta en una forma de control?"**

**Respuesta (≈40 s):**
> "Es el riesgo más importante, y lo diseñamos pensando en eso. Primero, **no hay cámaras, sensores, GPS ni micrófono encendido todo el día**: solo un check-in de un minuto que la persona responde, o no. Segundo, **ella manda**: elige qué comparte, puede pausar el acompañamiento y puede borrar lo que contó; si no quiere hablar, 'Prefiero no responder' no se castiga ni cuenta como riesgo. Tercero, **el sistema no decide nada solo**: un funcionario revisa cada alerta y debe justificar; lo hace cumplir la base de datos, y lo mostramos en la demo. Y lo primero que se sugiere siempre es llamar a la propia persona. Lo que falta: en un piloto real el consentimiento informado tiene que explicarse en persona, con alguien de confianza, y revisarse con un comité de ética. No lo damos por resuelto."

**Si insisten:** "¿Y si la persona no entiende lo que consiente?" → "Por eso el alta la hace una persona del equipo, en persona, no la app; y un tercero de confianza puede acompañar. Ese es un requisito de nuestro piloto, no una casilla en pantalla."

---

## Pregunta 2 — Privacidad de datos de salud
> **"Usted guarda cómo se siente la persona y hasta lo que cuenta con sus palabras. ¿Dónde se guarda, quién lo ve, y qué pasa si hay una filtración o un funcionario abusa del acceso?"**

**Respuesta (≈40 s):**
> "Reducimos lo que existe para que haya menos que filtrar. **El audio nunca se guarda**; solo respuestas codificadas, como 'bien' o 'más o menos'. El texto libre es opcional: **solo se anota si la persona dice que sí, la IA no lo lee, y se borra solo a los 15 días**; ella puede borrarlo antes. En el mensaje a la red de apoyo **no viaja ningún dato de salud**. Cada decisión y cada hecho relevante queda en una bitácora (hoy no registramos cada vez que alguien mira una ficha: eso lo agregaríamos), y el administrador que da de alta a las personas **no ve respuestas de salud**. Y hoy todo es sintético. Lo que falta para un uso real: **cifrar la base de datos**, definir cuánto tiempo guardar el resto de los registros, asignar a cada funcionario solo a sus personas, y una revisión legal con la Ley 19.628 y la nueva Ley 21.719 de datos personales."

---

## Pregunta 3 — Mal uso de la función de compañía
> **"Usted le sugiere a una persona mayor conocer a otra persona del programa. ¿No expone la identidad de una a la otra? ¿Y si alguien usa eso para estafar o abusar de una persona vulnerable?"**

**Respuesta (≈40 s):**
> "Tiene razón en que es una función delicada, por eso es la más limitada. **Solo se sugiere entre personas que aceptaron expresamente conocer a otras, y solo se muestra el nombre de pila.** Nunca se comparten teléfonos ni direcciones. Y decir 'sí me gustaría' **no las conecta**: crea una propuesta que revisa un funcionario, que debe consultar a la otra persona antes y justificar su decisión. O sea, nadie contacta a nadie desde la app. Además, solo se usa la cercanía de zona; no edad ni salud. El riesgo que no podemos eliminar con software es la relación entre dos personas: por eso la función queda en piloto y, si Fundación La Rosa no la ve segura, se apaga sin afectar el resto."

---

## Preguntas de reserva (respuestas cortas)

**¿Qué pasa si la IA se equivoca y alguien queda sin ayuda (falso negativo)?**
> "Puede pasar: en la simulación detectó 22 de 24 casos. Por eso la IA **no reemplaza** el contacto humano habitual ni los controles; es una capa más. Y por eso preferimos dejar pasar falsas alarmas, que un humano descarta, antes que perder casos. No lo presentamos como evidencia clínica."

**¿El modelo discrimina por edad, género o zona rural?**
> "No usa esos datos como variables y una prueba automática lo verifica. Aun así medimos tasas de alertas por grupo y vimos diferencias en grupos pequeños, que con 60 personas simuladas son ruido. Con datos reales hay que repetir esa auditoría. Y la mala conexión no cuenta como riesgo."

**¿Y las personas con deterioro cognitivo o que no hablan bien?**
> "Siempre hay botones grandes y 'Prefiero no responder'. Un cambio en las respuestas se trata como una señal para **acompañar**, nunca como un diagnóstico. Para quienes no puedan usarla, el diseño contempla que un funcionario o voluntario registre la visita con las mismas preguntas; eso aún no está en el prototipo."

**¿Por qué usar IA y no reglas fijas?**
> "Una regla como 'alerta si no contesta 3 días' alerta mal: hay personas que hablan poco por naturaleza. Comparar a cada persona consigo misma evita etiquetarla y ordena una cola que un equipo pequeño no puede revisar caso a caso."

**¿Qué parte es real y qué parte simulada?**
> "Real: el detector, la base de datos, el bloqueo de envíos sin decisión humana, la voz y los consentimientos. Simulado: los datos (todos sintéticos), el inicio de sesión, el enrolamiento, y el envío de mensajes a la red."

**¿Quién se hace responsable si algo sale mal?**
> "La decisión es de la persona que revisa, con nombre y justificación en la bitácora. El sistema solo prioriza. Por eso diseñamos para que nada ocurra sin una decisión humana registrada."

**¿Se puede usar para controlar a una persona contra su voluntad?**
> "Una persona puede pausar o negarse a compartir y dejamos de mostrarla en la cola. Derivar a terceros sin su permiso exige un motivo excepcional escrito, la decisión queda en la bitácora con su justificación, y el mensaje no lleva datos de salud. No es infalible: un uso abusivo requeriría saltarse esas reglas, y por eso hay auditoría."

---

## Frases que conviene tener a mano
- "La IA **prioriza y explica**; las personas **deciden y actúan**."
- "No es una promesa: **la base de datos lo impide**."
- "**Menos datos**, menos riesgo."
- "Reconocemos este límite, y esto es lo que haríamos antes de un piloto real."
- Si no saben algo: "No lo sabemos todavía; lo validaríamos con Fundación La Rosa en el piloto." (Mejor que inventar.)

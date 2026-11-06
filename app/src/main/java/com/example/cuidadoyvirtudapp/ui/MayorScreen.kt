package com.example.cuidadoyvirtudapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.cuidadoyvirtudapp.R
import com.example.cuidadoyvirtudapp.data.PersonaPub
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.domain.Social
import com.example.cuidadoyvirtudapp.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

private data class Opcion(val valor: String, val emoji: String, val texto: String, val claves: List<String>)
private data class Pregunta(val campo: String, val texto: String, val soloCronico: Boolean, val opciones: List<Opcion>)

// Respuestas codificadas: nunca se guarda audio ni texto.
private val PREGUNTAS = listOf(
    Pregunta("animo", "¿Cómo amaneció hoy?", false, listOf(
        Opcion("bien", "😊", "Bien", listOf("bien", "bueno", "excelente", "contento", "contenta", "tranquilo", "tranquila")),
        Opcion("regular", "😐", "Más o menos", listOf("más o menos", "mas o menos", "regular", "así así", "asi asi", "normal")),
        Opcion("mal", "😟", "Mal", listOf("mal", "triste", "pésimo", "pesimo", "decaído", "decaido")))),
    Pregunta("contacto", "¿Habló hoy con alguien?", false, listOf(
        Opcion("si", "💬", "Sí", listOf("sí", "si", "claro", "hablé", "hable")),
        Opcion("no", "🤫", "No", listOf("no", "nadie")))),
    Pregunta("actividades", "¿Pudo hacer sus cosas de siempre?", false, listOf(
        Opcion("completo", "👍", "Sí, todo", listOf("sí", "si", "todo", "claro")),
        Opcion("parcial", "🤏", "Algunas", listOf("algunas", "un poco", "más o menos", "mas o menos", "a medias")),
        Opcion("no", "🛋️", "No pude", listOf("no")))),
    Pregunta("medicacion", "¿Tomó sus remedios?", true, listOf(
        Opcion("tomo", "💊", "Sí", listOf("sí", "si", "claro", "tomé", "tome")),
        Opcion("no_tomo", "⏳", "Todavía no", listOf("no", "todavía", "todavia", "aún", "aun")))),
)

/** Colores de los botones de respuesta: verde = bien, amarillo = a medias, rojo rosado = mal, gris azulado = neutro. */
private fun colorRespuesta(campo: String, valor: String): Pair<Color, Color> = when {
    campo == "contacto" && valor == "no" -> GrisAzul to Color.White
    valor in listOf("bien", "si", "completo", "tomo") -> VerdeVivo to Color.White
    valor in listOf("regular", "parcial", "no_tomo") -> Sol to Color(0xFF3B2A00)
    else -> Rosa to Color.White
}

private fun interpretar(frases: List<String>, q: Pregunta): String? {
    for (frase in frases) {
        val t = frase.lowercase(Locale.ROOT)
        val palabras = t.split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }.toSet()
        val mejor = q.opciones.map { o ->
            o to (o.claves.filter { k -> if (k.contains(' ')) t.contains(k) else k in palabras }.maxOfOrNull { it.length } ?: 0)
        }.filter { it.second > 0 }.maxByOrNull { it.second }
        if (mejor != null) return mejor.first.valor
    }
    return null
}

@Composable
fun MayorScreen(onSalir: () -> Unit) {
    val ctx = LocalContext.current
    val persona = remember { Repo.personaDemo() }
    val preguntas = remember { PREGUNTAS.filter { !it.soloCronico || persona.medCronica } }
    var fase by remember { mutableStateOf("preguntas") } // preguntas | gracias | sos | compartir
    var paso by remember { mutableIntStateOf(0) }
    val respuestas = remember { mutableMapOf<String, String>() }
    var canal by remember { mutableStateOf("botones") }
    var ayuda by remember { mutableStateOf("") }
    var dialogoSos by remember { mutableStateOf(false) }
    var sinResponder by remember { mutableStateOf(false) }
    var escuchando by remember { mutableStateOf(false) }
    var intentos by remember { mutableIntStateOf(0) }
    var textoPorque by remember { mutableStateOf("") }
    var candidato by remember { mutableStateOf<PersonaPub?>(null) }
    var socialMsg by remember { mutableStateOf<String?>(null) }
    val pausado = remember(Repo.version) { Repo.leerConsentimiento(persona.id).vigente.not() }
    val manejador = remember { Handler(Looper.getMainLooper()) }

    var permiso by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val reconocedor = remember { if (SpeechRecognizer.isRecognitionAvailable(ctx)) SpeechRecognizer.createSpeechRecognizer(ctx) else null }

    fun escuchar() {
        val r = reconocedor ?: return
        if (!permiso || (fase != "preguntas" && fase != "porque") || pausado) return
        try {
            r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CL")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            })
        } catch (e: Exception) { ayuda = "Use los botones grandes 👇" }
    }

    fun terminar(omitir: Boolean) {
        reconocedor?.cancel(); escuchando = false
        if (!omitir) Repo.registrarCheckin(persona.id, canal, respuestas["animo"], respuestas["contacto"], respuestas["actividades"], respuestas["medicacion"])
        sinResponder = omitir
        // Si hoy no habló con nadie, se le sugiere (con su permiso) conocer a otra persona del programa que también quiere compañía.
        if (!omitir && respuestas["contacto"] == "no") {
            val c = Repo.candidatoSocial(persona.id)
            if (c != null) { candidato = c; fase = "social"; return }
        }
        fase = "gracias"
    }
    fun avanzar() {
        ayuda = ""; intentos = 0
        if (paso + 1 < preguntas.size) { paso++; fase = "preguntas" } else terminar(false)
    }
    fun responder(valor: String, via: String) {
        reconocedor?.cancel(); escuchando = false
        respuestas[preguntas[paso].campo] = valor
        if (via == "voz") canal = "voz"
        // Respuestas distintas llevan a preguntas distintas: si no se siente bien, se le pregunta (opcionalmente) por qué.
        if (preguntas[paso].campo == "animo" && (valor == "regular" || valor == "mal")) { ayuda = ""; intentos = 0; fase = "porque" } else avanzar()
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        permiso = ok
        if (ok) escuchar() else ayuda = "Sin permiso de micrófono: use los botones grandes 👇"
    }

    // Reconocimiento de voz dentro de la app: se activa solo al terminar de leer la pregunta.
    DisposableEffect(reconocedor) {
        reconocedor?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p: Bundle?) { escuchando = true; ayuda = "Estoy escuchando… diga su respuesta" }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rms: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() { escuchando = false }
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
            override fun onResults(res: Bundle?) {
                escuchando = false
                val frases = res?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                if (fase == "porque") { // relato libre: se muestra a la persona y solo se anota si ella lo acepta
                    if (frases.isNotEmpty() && frases[0].isNotBlank()) { textoPorque = frases[0]; ayuda = ""; fase = "nota" } else reintentar()
                    return
                }
                val v = if (frases.isNotEmpty()) interpretar(frases, preguntas[paso]) else null
                if (v != null) {
                    val actual = paso
                    ayuda = "Le entendí: “${frases[0]}”"
                    manejador.postDelayed({ if (paso == actual && fase == "preguntas") responder(v, "voz") }, 700)
                } else reintentar()
            }
            override fun onError(error: Int) {
                escuchando = false
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) reintentar()
                else if (error != SpeechRecognizer.ERROR_CLIENT) ayuda = "Use los botones grandes 👇"
            }
            fun reintentar() {
                if (intentos < 1) {
                    intentos++; ayuda = "¿Me lo repite, por favor?"
                    val actual = paso
                    manejador.postDelayed({ if (paso == actual && (fase == "preguntas" || fase == "porque")) escuchar() }, 800)
                } else ayuda = "No le entendí. Puede usar los botones 👇"
            }
        })
        onDispose { reconocedor?.destroy(); manejador.removeCallbacksAndMessages(null) }
    }

    // Voz del sistema que lee las preguntas. Al terminar de hablar, se enciende el micrófono solo.
    var ttsListo by remember { mutableStateOf(false) }
    val tts = remember { TextToSpeech(ctx) { if (it == TextToSpeech.SUCCESS) ttsListo = true } }
    DisposableEffect(Unit) { onDispose { tts.stop(); tts.shutdown() } }
    LaunchedEffect(ttsListo) {
        if (ttsListo) {
            tts.language = Locale.forLanguageTag("es-CL")
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onError(id: String?) { manejador.post { if (id == "q$paso" || id == "p$paso") escuchar() } }
                override fun onDone(id: String?) { manejador.post { if ((id == "q$paso" && fase == "preguntas") || (id == "p$paso" && fase == "porque")) escuchar() } }
            })
        }
    }
    LaunchedEffect(Unit) { if (!permiso) pedirPermiso.launch(Manifest.permission.RECORD_AUDIO) }
    LaunchedEffect(paso, ttsListo, fase) {
        if (pausado || fase !in listOf("preguntas", "porque", "social")) return@LaunchedEffect
        reconocedor?.cancel(); escuchando = false
        val texto = when (fase) {
            "preguntas" -> (if (paso == 0) "Buenos días, ${persona.saludo}. " else "") + preguntas[paso].texto
            "porque" -> "Gracias por contarme. Si quiere, dígame por qué. Puede decirlo con sus palabras."
            else -> "Hoy también hay otra persona que quiere conversar. ¿Le gustaría conocerla?"
        }
        val id = when (fase) { "preguntas" -> "q$paso"; "porque" -> "p$paso"; else -> "s" }
        if (ttsListo) tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, id)
        else if (fase != "social") { delay(2500); if (!ttsListo) escuchar() } // sin voz del sistema: igual se activa el micrófono
    }

    fun tocarMicrofono() {
        when {
            reconocedor == null -> ayuda = "Este teléfono no tiene reconocimiento de voz. Use los botones 👇"
            !permiso -> pedirPermiso.launch(Manifest.permission.RECORD_AUDIO)
            escuchando -> { reconocedor.cancel(); escuchando = false }
            else -> { tts.stop(); intentos = 0; escuchar() }
        }
    }

    FondoCalido {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.background(Color.White, RoundedCornerShape(16.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Image(painterResource(R.drawable.logo), "Logo", Modifier.height(44.dp)) }
                Spacer(Modifier.size(10.dp))
                Text("Tu Cuidado", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Marino, modifier = Modifier.weight(1f))
                Text("Salir", color = Marino, fontWeight = FontWeight.Bold, fontSize = 16.sp, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { tts.stop(); reconocedor?.cancel(); onSalir() }.padding(8.dp))
            }

            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                when {
                    fase == "compartir" -> Compartir(persona.id) { fase = "gracias" }
                    fase == "gracias" || fase == "sos" -> Gracias(persona.saludo, sinResponder, fase == "sos", socialMsg) { fase = "compartir" }
                    fase == "porque" -> PantallaPorque(escuchando, ayuda, ::tocarMicrofono,
                        onOpcion = { cat -> Repo.guardarNota(persona.id, "opcion", cat, ""); reconocedor?.cancel(); tts.stop(); avanzar() },
                        onNo = { reconocedor?.cancel(); tts.stop(); avanzar() })
                    fase == "nota" -> PantallaNota(textoPorque,
                        onSi = { Repo.guardarNota(persona.id, "voz", Social.categoria(textoPorque), textoPorque); avanzar() },
                        onOtra = { fase = "porque" }, onNo = { avanzar() })
                    fase == "social" && candidato != null -> PantallaSocial(candidato!!,
                        onSi = {
                            Repo.proponerEncuentro(persona.id, candidato!!.id)
                            socialMsg = "Una persona del equipo hablará con ${candidato!!.alias} y con usted para presentarlos, si ambos quieren. No compartimos teléfonos ni direcciones."
                            tts.stop(); fase = "gracias"
                        },
                        onNo = { tts.stop(); fase = "gracias" })
                    pausado -> {
                        Banner(colores = listOf(Lila, AzulMedio)) {
                            Text("El acompañamiento está en pausa. Cuando quiera, puede reactivarlo.", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(16.dp))
                        BotonColor("⚙️", "¿Qué quiere compartir?", AzulMedio, Color.White) { fase = "compartir" }
                    }
                    else -> {
                        val q = preguntas[paso]
                        Spacer(Modifier.height(6.dp))
                        Banner(colores = listOf(Marino, AzulMedio)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                if (paso == 0) Text("☀️ Buenos días, ${persona.saludo}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE08A), textAlign = TextAlign.Center)
                                Spacer(Modifier.height(6.dp))
                                Text(q.texto, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Microfono(escuchando, ::tocarMicrofono)
                        Box(Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                            Text(ayuda, fontSize = 17.sp, color = Marino, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        }
                        q.opciones.forEach { o ->
                            val (bg, fg) = colorRespuesta(q.campo, o.valor)
                            BotonColor(o.emoji, o.texto, bg, fg) { responder(o.valor, "botones") }
                            Spacer(Modifier.height(10.dp))
                        }
                        Text("Prefiero no responder", fontSize = 22.sp, color = Marino, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { tts.stop(); terminar(true) }.padding(12.dp))
                    }
                }
            }

            if (fase != "sos" && !pausado) {
                Box(Modifier.fillMaxWidth().background(Naranja, RoundedCornerShape(22.dp)).clickable { dialogoSos = true }.padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
                    Text("🆘  Necesito ayuda ahora", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Text("Avisa a una persona real de la Oficina del Adulto Mayor. No se guarda su voz.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = Marino.copy(alpha = .75f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            }
        }
    }

    if (dialogoSos) {
        AlertDialog(
            onDismissRequest = { dialogoSos = false },
            title = { Text("¿Necesita ayuda?", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Marino) },
            text = { Text("Avisaremos a una persona real de la Oficina del Adulto Mayor.", fontSize = 20.sp, color = Marino) },
            confirmButton = {
                Box(Modifier.background(Naranja, RoundedCornerShape(16.dp)).clickable { dialogoSos = false; reconocedor?.cancel(); Repo.sos(persona.id); tts.stop(); fase = "sos" }.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("Sí, necesito ayuda", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            dismissButton = { Box(Modifier.clickable { dialogoSos = false }.padding(horizontal = 12.dp, vertical = 16.dp)) { Text("No, me equivoqué", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Marino) } },
            containerColor = Color.White,
        )
    }
}

@Composable
private fun Microfono(escuchando: Boolean, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "pulso")
    val s by t.animateFloat(1f, if (escuchando) 1.28f else 1.1f, infiniteRepeatable(tween(if (escuchando) 600 else 1400), RepeatMode.Reverse), label = "s")
    val color = if (escuchando) Naranja else Turquesa
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(176.dp)) {
        Box(Modifier.size(160.dp).scale(s).alpha(.3f).background(color, CircleShape))
        Box(Modifier.size(136.dp).alpha(.25f).background(color, CircleShape))
        Column(Modifier.size(118.dp).background(color, CircleShape).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("🎤", fontSize = 40.sp)
            Text(if (escuchando) "ESCUCHANDO" else "TOQUE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun BotonColor(emoji: String, texto: String, fondo: Color, letra: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(fondo, RoundedCornerShape(26.dp)).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(56.dp).background(Color.White.copy(alpha = .95f), CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 32.sp) }
        Spacer(Modifier.size(16.dp))
        Text(texto, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = letra)
    }
}

@Composable
private fun PantallaPorque(escuchando: Boolean, ayuda: String, onMic: () -> Unit, onOpcion: (String) -> Unit, onNo: () -> Unit) {
    Spacer(Modifier.height(6.dp))
    Banner(colores = listOf(Lila, AzulMedio)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Gracias por contarme 💛", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE08A), textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text("¿Quiere decirme por qué?", fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(10.dp))
    Microfono(escuchando, onMic)
    Box(Modifier.height(40.dp), contentAlignment = Alignment.Center) { Text(ayuda.ifEmpty { "Hable con sus palabras, o elija una opción 👇" }, fontSize = 17.sp, color = Marino, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center) }
    val colores = listOf(Cielo, Durazno, Menta, Color(0xFFEADCF8), Color(0xFFFFE9A8), Color(0xFFE2E8F0))
    Social.OPCIONES_MOTIVO.chunked(2).forEachIndexed { fila, par ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            par.forEachIndexed { j, (emoji, texto) ->
                Column(Modifier.weight(1f).background(colores[(fila * 2 + j) % colores.size], RoundedCornerShape(22.dp)).clickable { onOpcion(texto) }.padding(vertical = 14.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(emoji, fontSize = 34.sp)
                    Text(texto, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Marino, textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    Text("Prefiero no contar", fontSize = 22.sp, color = Marino, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = onNo).padding(12.dp))
}

@Composable
private fun PantallaNota(texto: String, onSi: () -> Unit, onOtra: () -> Unit, onNo: () -> Unit) {
    Banner(colores = listOf(Turquesa, Marino)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Le entendí:", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE08A))
            Spacer(Modifier.height(6.dp))
            Text("“$texto”", fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(12.dp))
    Text("¿Quiere que lo anotemos para su cuidador?", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Marino, textAlign = TextAlign.Center)
    Text("Solo lo verá su cuidador. No se guarda su voz.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Marino.copy(alpha = .75f), textAlign = TextAlign.Center)
    Spacer(Modifier.height(14.dp))
    BotonColor("✅", "Sí, anótelo", VerdeVivo, Color.White, onSi); Spacer(Modifier.height(10.dp))
    BotonColor("🎤", "Decirlo otra vez", AzulMedio, Color.White, onOtra); Spacer(Modifier.height(10.dp))
    BotonColor("🙅", "No, gracias", GrisAzul, Color.White, onNo)
}

@Composable
private fun PantallaSocial(otra: PersonaPub, onSi: () -> Unit, onNo: () -> Unit) {
    Banner(colores = listOf(Turquesa, VerdeVivo)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hoy también hay otra persona que quiere conversar", fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(14.dp))
    Box(Modifier.size(120.dp).background(Durazno, CircleShape), contentAlignment = Alignment.Center) { Text("🌸", fontSize = 64.sp) }
    Spacer(Modifier.height(8.dp))
    Text(otra.alias, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Marino)
    Text("también del programa, de su zona", fontSize = 18.sp, color = Marino.copy(alpha = .8f), fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(10.dp))
    Text("¿Le gustaría que una persona del equipo los presente?", fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black, color = Marino, textAlign = TextAlign.Center)
    Text("Solo si ambos quieren. No compartimos su teléfono.", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Marino.copy(alpha = .75f), textAlign = TextAlign.Center)
    Spacer(Modifier.height(14.dp))
    BotonColor("🤝", "Sí, me gustaría", VerdeVivo, Color.White, onSi); Spacer(Modifier.height(10.dp))
    BotonColor("🙂", "No, gracias", GrisAzul, Color.White, onNo)
}

@Composable
private fun Gracias(saludo: String, omitio: Boolean, sos: Boolean, extra: String?, onCompartir: () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Box(Modifier.size(150.dp).background(if (sos) Durazno else Menta, CircleShape), contentAlignment = Alignment.Center) {
        Text(if (sos) "🤝" else "🌻", fontSize = 76.sp)
    }
    Spacer(Modifier.height(16.dp))
    Banner(colores = if (sos) listOf(Naranja, Sol) else listOf(VerdeVivo, Turquesa)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (sos) "Ya avisamos" else "¡Gracias, $saludo!", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
            Text(if (sos) "Una persona se comunicará con usted" else "Que tenga un lindo día", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = .95f), textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(16.dp))
    Tarjeta(borde = Color.Transparent, fondo = Color.White.copy(alpha = .9f)) {
        Text(if (sos) "No está solo/a" else if (omitio) "Está bien" else "🌱 Acompañamiento activo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Marino)
        Spacer(Modifier.height(4.dp))
        Text(
            if (sos) "Una persona real de la Oficina del Adulto Mayor fue avisada y decidirá cómo ayudarle."
            else if (omitio) "No pasa nada. Hoy no tiene que responder. Nos saludamos mañana."
            else "Su respuesta quedó guardada con cariño. Nos volveremos a saludar mañana por la mañana.",
            fontSize = 18.sp, color = Marino.copy(alpha = .85f))
    }
    if (extra != null) {
        Spacer(Modifier.height(10.dp))
        Tarjeta(borde = Color.Transparent, fondo = Menta) { Text("🤝 $extra", fontSize = 17.sp, color = Marino, fontWeight = FontWeight.SemiBold) }
    }
    Spacer(Modifier.height(12.dp))
    Text("¿Qué quiere compartir?", fontSize = 22.sp, color = Marino, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable(onClick = onCompartir).padding(12.dp))
}

@Composable
private fun Compartir(pid: String, onListo: () -> Unit) {
    val actual = remember { Repo.leerConsentimiento(pid) }
    var comparte by remember { mutableStateOf(actual.comparte) }
    var familia by remember { mutableStateOf(actual.avisarFamilia) }
    var pausa by remember { mutableStateOf(!actual.vigente) }
    var conocer by remember { mutableStateOf(actual.socializar) }
    Banner(colores = listOf(Turquesa, AzulMedio)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¿Qué quiere compartir?", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
            Text("Usted decide. Puede cambiarlo cuando quiera.", fontSize = 19.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = .95f), textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(12.dp))
    FilaSwitch("🧑‍⚕️", "Que mi cuidador vea cómo estoy", Cielo, comparte) { comparte = it }
    FilaSwitch("👨‍👩‍👧", "Que avisen a mi familia si lo necesito", Durazno, familia) { familia = it }
    FilaSwitch("🌸", "Que me presenten a otras personas del programa para conversar", Color(0xFFEADCF8), conocer) { conocer = it }
    FilaSwitch("⏸️", "Pausar el acompañamiento por hoy", Menta, pausa) { pausa = it }
    Spacer(Modifier.height(6.dp))
    var notas by remember { mutableIntStateOf(Repo.contarNotas(pid)) }
    var confirmaBorrar by remember { mutableStateOf(false) }
    var avisoBorrado by remember { mutableStateOf("") }
    Surface(Modifier.fillMaxWidth().clickable { if (notas > 0) confirmaBorrar = true else avisoBorrado = "No hay nada guardado de lo que usted contó." },
        shape = RoundedCornerShape(26.dp), color = Color.White, border = BorderStroke(2.dp, Rosa.copy(alpha = .5f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(Color(0xFFFFE0E8), CircleShape), contentAlignment = Alignment.Center) { Text("🗑️", fontSize = 28.sp) }
            Spacer(Modifier.size(12.dp))
            Column {
                Text("Borrar lo que conté", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Rosa)
                Text("Lo que anota se borra solo a los ${com.example.cuidadoyvirtudapp.domain.Retencion.DIAS} días", fontSize = 14.sp, color = Marino.copy(alpha = .75f), fontWeight = FontWeight.SemiBold)
            }
        }
    }
    if (avisoBorrado.isNotEmpty()) Text(avisoBorrado, fontSize = 16.sp, color = Verde, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp), textAlign = TextAlign.Center)
    if (confirmaBorrar) {
        AlertDialog(
            onDismissRequest = { confirmaBorrar = false },
            title = { Text("¿Borrar lo que contó?", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino) },
            text = { Text("Se eliminan las $notas nota(s) que usted pidió anotar. No se puede deshacer.", fontSize = 19.sp, color = Marino) },
            confirmButton = {
                Box(Modifier.background(Rosa, RoundedCornerShape(16.dp)).clickable { Repo.borrarMisNotas(pid); notas = 0; confirmaBorrar = false; avisoBorrado = "Listo, ya borramos lo que contó." }.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Sí, borrar", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            dismissButton = { Box(Modifier.clickable { confirmaBorrar = false }.padding(12.dp)) { Text("No, dejar", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Marino) } },
            containerColor = Color.White,
        )
    }
    Spacer(Modifier.height(10.dp))
    Box(Modifier.fillMaxWidth().background(VerdeVivo, RoundedCornerShape(26.dp)).clickable { Repo.guardarConsentimiento(pid, comparte, familia, pausa, conocer); onListo() }.padding(vertical = 18.dp), contentAlignment = Alignment.Center) {
        Text("Listo", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
    }
}

@Composable
private fun FilaSwitch(emoji: String, texto: String, colorIcono: Color, valor: Boolean, cambio: (Boolean) -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { cambio(!valor) }, shape = RoundedCornerShape(26.dp), color = Color.White, shadowElevation = 3.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).background(colorIcono, CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 30.sp) }
            Spacer(Modifier.size(12.dp))
            Text(texto, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Marino, modifier = Modifier.weight(1f))
            Switch(valor, cambio, colors = SwitchDefaults.colors(checkedTrackColor = VerdeVivo))
        }
    }
}

package com.example.cuidadoyvirtudapp.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.data.AlertaVista
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.domain.BloqueoHumano
import com.example.cuidadoyvirtudapp.domain.Workflow
import com.example.cuidadoyvirtudapp.ui.theme.*
import kotlinx.coroutines.delay

private val TABS = listOf("🔔" to "Alertas", "🤝" to "Compañía", "👥" to "Personas", "📈" to "Impacto", "🕘" to "Historial", "⚖️" to "Equidad")

@Composable
fun CuidadorScreen(onSalir: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var caso by remember { mutableStateOf<Long?>(null) }
    BackHandler { if (caso != null) caso = null else onSalir() }

    if (caso != null) { CasoScreen(caso!!) { caso = null }; return }

    Scaffold(
        containerColor = Crema,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                TABS.forEachIndexed { i, (ic, nombre) ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Text(ic, fontSize = 20.sp) }, label = { Text(nombre, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(selectedTextColor = Marino, indicatorColor = AzulPalido))
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).statusBarsPadding().fillMaxSize()) {
            when (tab) {
                0 -> AlertasTab(onSalir) { caso = it }
                1 -> CompaniaTab()
                2 -> PersonasTab()
                3 -> ImpactoTab()
                4 -> HistorialTab()
                else -> EquidadTab()
            }
        }
    }
}

@Composable
private fun AlertasTab(onSalir: () -> Unit, abrir: (Long) -> Unit) {
    val cola = remember(Repo.version) { Repo.cola() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Banner(colores = listOf(Marino, Turquesa)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Buenos días, Camila 👋", fontSize = 15.sp, color = Color(0xFFFFE08A), fontWeight = FontWeight.Bold)
                        Text("Casos por revisar hoy", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("${fechaLinda(cola.fecha)} · datos sintéticos", fontSize = 12.sp, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.SemiBold)
                    }
                    Text("Cambiar perfil", fontSize = 12.sp, color = Color.White, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = onSalir).padding(6.dp))
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Contador("Revisar", cola.revisar, "casos", Modifier.weight(1f))
                Contador("Observar", cola.observar, "casos", Modifier.weight(1f))
                Contador("Sin novedad", cola.sinNovedad, "personas", Modifier.weight(1f))
            }
        }
        if (cola.noVisibles > 0) item {
            Text("🔒 ${cola.noVisibles} personas no aparecen aquí porque pausaron el acompañamiento o no autorizaron compartir su estado. Se respeta su decisión.",
                fontSize = 12.sp, color = Marino.copy(alpha = .75f), fontWeight = FontWeight.SemiBold)
        }
        item { Titulo("Casos priorizados para revisión", 16) }
        if (cola.alertas.isEmpty()) item { Tarjeta { Text("No quedan casos por revisar. 🌱", fontWeight = FontWeight.Bold, color = AzulMedio, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) } }
        items(cola.alertas, key = { it.id }) { TarjetaAlerta(it) { abrir(it.id) } }
    }
}

@Composable
private fun Contador(nivel: String, n: Int, unidad: String, modifier: Modifier) {
    val s = semaforo(nivel)
    Column(modifier.background(s.fondo, RoundedCornerShape(20.dp)).border(2.dp, s.borde, RoundedCornerShape(20.dp)).padding(12.dp)) {
        Text("${s.icono} ${nivel.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = s.texto)
        Text("$n", fontSize = 32.sp, fontWeight = FontWeight.Black, color = s.texto)
        Text(unidad, fontSize = 11.sp, color = Marino.copy(alpha = .75f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TarjetaAlerta(a: AlertaVista, onRevisar: () -> Unit) {
    val s = semaforo(a.nivel)
    Tarjeta(borde = s.borde, grosor = 2.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(44.dp).background(colorAvatar(a.persona.id), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(a.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(a.persona.alias, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Marino)
                Text("${a.persona.tramo} años · ${a.persona.zona}", fontSize = 12.sp, color = Marino.copy(alpha = .6f), fontWeight = FontWeight.SemiBold)
            }
            ChipNivel(a.nivel)
        }
        if (a.origen == "sos" || a.estado == "EN_ESPERA") {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (a.origen == "sos") Text("🆘 Pidió ayuda", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Naranja, modifier = Modifier.background(NaranjaPalido, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp))
                if (a.estado == "EN_ESPERA") Text("⏳ En espera", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(AzulPalido, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(a.explicacion, fontSize = 14.sp, color = Marino, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Text("Consentimiento: ${if (a.consent.vigente) "Vigente" else "Pausado"} · ${if (a.consent.avisarFamilia) "Autoriza avisar a su familia" else "No autorizó avisar a su familia"}", fontSize = 11.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Tendencia 30 días", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Marino.copy(alpha = .7f)); Spark(a.spark, s.borde) }
            Button(onClick = onRevisar, colors = ButtonDefaults.buttonColors(containerColor = if (a.origen == "sos") Naranja else Marino), shape = RoundedCornerShape(12.dp)) { Text("Revisar caso ›", fontWeight = FontWeight.Bold) }
        }
    }
}

// ---------------------------------------------------------------------------
// Revisión del caso + punto de decisión humana
// ---------------------------------------------------------------------------
private val ETIQ = listOf(
    "descartar" to "✕ Descartar", "esperar" to "⏳ Esperar y reevaluar", "llamar" to "📞 Llamar primero a la persona (recomendado)",
    "visita" to "🚶 Visita presencial", "derivar" to "🤝 Derivar a red de apoyo (CESFAM / Vínculos)",
)

@Composable
private fun CasoScreen(id: Long, cerrar: () -> Unit) {
    val d = remember(id, Repo.version) { Repo.detalle(id) }
    if (d == null) { cerrar(); return }
    val a = d.alerta
    val s = semaforo(a.nivel)
    var accion by remember { mutableStateOf<String?>(null) }
    var just by remember { mutableStateOf("") }
    var exc by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("🔒 Ninguna acción se envía sin su decisión y justificación explícita.") }
    var estado by remember { mutableStateOf("pendiente") } // pendiente | bloqueado | ok
    var hecho by remember { mutableStateOf(false) }
    LaunchedEffect(hecho) { if (hecho) { delay(1800); cerrar() } }

    Column(Modifier.fillMaxSize().background(Crema).statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹ Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulMedio, modifier = Modifier.clickable(onClick = cerrar).padding(end = 12.dp, top = 6.dp, bottom = 6.dp))
        }
        Banner(colores = listOf(colorAvatar(a.persona.id), Marino)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(56.dp).background(Color.White.copy(alpha = .25f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(a.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White, fontSize = 20.sp) }
                Column(Modifier.weight(1f)) {
                    Text(a.persona.alias, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("${a.persona.tramo} años · zona ${a.persona.zona} · ${a.persona.id}", fontSize = 12.sp, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.SemiBold)
                }
                ChipNivel(a.nivel)
            }
        }

        Tarjeta(borde = AzulMedio.copy(alpha = .4f), fondo = AzulPalido) {
            FilaIcono("⚖️", "La IA sugiere, usted decide", "Nunca se ejecutan llamadas ni derivaciones automáticas. Su criterio profesional es irremplazable.")
        }

        Tarjeta {
            Titulo("🔍 Señales que motivaron la alerta", 16); Spacer(Modifier.height(8.dp))
            a.senales.forEach { sg ->
                val c = when (sg.fuerza) { "fuerte" -> Rojo; "moderada" -> AmbarBorde; else -> AzulMedio }
                Row(Modifier.padding(vertical = 3.dp)) { Text("• ", color = c, fontWeight = FontWeight.Black); Text("${sg.titulo}: ${sg.texto}", fontSize = 14.sp, color = Marino) }
            }
            Row(Modifier.padding(vertical = 3.dp)) { Text("• ", color = AzulMedio, fontWeight = FontWeight.Black); Text("Respaldo de privacidad: el audio nunca se guarda. La IA solo usa respuestas codificadas (bien/regular/mal, sí/no); lo que la persona decide contar no es leído por la IA.", fontSize = 14.sp, color = Marino) }
        }

        if (d.notas.isNotEmpty()) {
            Tarjeta(borde = Lila.copy(alpha = .4f), fondo = Color(0xFFF3ECFB)) {
                Titulo("💬 Lo que la persona quiso contar", 16); Spacer(Modifier.height(4.dp))
                Text("La persona decidió anotarlo para usted. La IA no lo lee ni lo interpreta; úselo como contexto humano.", fontSize = 12.sp, color = Marino.copy(alpha = .75f), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                d.notas.forEach { n ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("${n.categoria} · ${n.ts.replace('T', ' ')} · se borra el ${n.vence.take(10)}", fontSize = 11.sp, color = Lila, fontWeight = FontWeight.Bold)
                        if (n.texto.isNotEmpty()) Text("“${n.texto}”", fontSize = 15.sp, color = Marino, fontWeight = FontWeight.Medium)
                        else Text("(Eligió esta opción; no relató con palabras)", fontSize = 13.sp, color = Marino.copy(alpha = .7f))
                    }
                }
            }
        }

        Tarjeta {
            Titulo("${d.grafico.titulo}: su rutina habitual vs. últimos 30 días", 15); Spacer(Modifier.height(8.dp))
            GraficoNormalidad(d.grafico.serie, d.grafico.base, s.borde)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hace 30 días", fontSize = 11.sp, color = Marino.copy(alpha = .6f), fontWeight = FontWeight.Bold)
                Text("- - -  su normalidad", fontSize = 11.sp, color = AzulCielo, fontWeight = FontWeight.Bold)
                Text("Hoy", fontSize = 11.sp, color = Marino.copy(alpha = .6f), fontWeight = FontWeight.Bold)
            }
        }

        Tarjeta {
            FilaIcono("🛡️", "Consentimiento explícito informado",
                (if (a.consent.comparte) "Autorizó que se vea su estado. " else "No autorizó compartir su estado. ") + (if (a.consent.avisarFamilia) "Autorizó avisar a su familia." else "No autorizó avisar a su familia."))
        }

        Tarjeta(borde = Marino.copy(alpha = .25f), grosor = 2.dp) {
            Titulo("Punto de decisión humana", 16); Spacer(Modifier.height(8.dp))
            ETIQ.forEach { (k, t) ->
                val sel = accion == k
                Box(Modifier.fillMaxWidth().padding(vertical = 3.dp).background(if (sel) AzulPalido else Color.White, RoundedCornerShape(12.dp))
                    .border(if (sel) 3.dp else 2.dp, if (sel) Marino else Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).clickable { accion = k }.padding(12.dp)) {
                    Text(t, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Marino)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(just, { just = it }, Modifier.fillMaxWidth(), label = { Text("Justificación profesional obligatoria *") }, minLines = 3,
                placeholder = { Text("Motivo de la decisión para el expediente…") })
            if (!a.consent.comparte) {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(exc, { exc = it }, Modifier.fillMaxWidth(), label = { Text("Motivo excepcional (la persona no autorizó compartir)") })
            }
            Spacer(Modifier.height(8.dp))
            val (bg, fg) = when (estado) { "ok" -> VerdePalido to Verde; "bloqueado" -> RojoPalido to Rojo; else -> RojoPalido to Rojo }
            Text(msg, Modifier.fillMaxWidth().background(bg, RoundedCornerShape(12.dp)).padding(12.dp), color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val ac = accion
                if (ac == null) { msg = "🔒 Elija primero una acción."; estado = "bloqueado"; return@Button }
                try {
                    val r = Repo.decidir(id, ac, just, exc)
                    estado = "ok"
                    msg = "✅ Decisión registrada." + (r.derivacionMensaje?.let { " Mensaje mínimo enviado (simulado) a «${r.derivacionDestino}»: “$it”" } ?: "")
                    hecho = true
                } catch (e: BloqueoHumano) { estado = "bloqueado"; msg = "🔒 ${e.message}" }
            }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Marino), shape = RoundedCornerShape(12.dp), enabled = !hecho) {
                Text("Confirmar y registrar decisión", fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().border(BorderStroke(2.dp, Rojo.copy(alpha = .5f)), RoundedCornerShape(12.dp)).clickable(enabled = !hecho) {
                estado = "bloqueado"; msg = "🛑 Bloqueado: " + Repo.intentoEnvioSinAprobacion(id)
            }.padding(10.dp), contentAlignment = Alignment.Center) {
                Text("🧪 Demostración: simular que el sistema intenta enviar sin su aprobación", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Rojo, textAlign = TextAlign.Center)
            }
        }

        Tarjeta {
            Titulo("📋 Bitácora", 16); Spacer(Modifier.height(6.dp))
            d.bitacora.forEach { b ->
                Column(Modifier.padding(vertical = 4.dp)) {
                    Text(b.evento, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (b.evento.startsWith("ENVÍO BLOQUEADO")) Rojo else Marino)
                    Text("${b.actor} · ${b.ts.replace('T', ' ')}", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
                    if (b.detalle.isNotEmpty()) Text(b.detalle, fontSize = 11.sp, color = Color(0xFF475569), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

// ---------------------------------------------------------------------------
// Compañía entre personas: la persona dijo "sí me gustaría"; un humano decide si las presenta.
@Composable
private fun CompaniaTab() {
    val props = remember(Repo.version) { Repo.propuestas() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Banner(colores = listOf(Turquesa, VerdeVivo)) {
                Column {
                    Text("Compañía entre personas 🌸", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Solo entre personas que aceptaron conocer a otras. La app no comparte teléfonos ni direcciones: usted decide si las presenta y lo organiza.", fontSize = 12.sp, color = Color.White.copy(alpha = .95f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (props.isEmpty()) item { Tarjeta { Text("No hay propuestas por ahora. Aparecen cuando una persona que hoy no habló con nadie dice que le gustaría conocer a otra. 🌱", color = AzulMedio, fontWeight = FontWeight.Bold) } }
        items(props, key = { it.id }) { TarjetaPropuesta(it) }
    }
}

@Composable
private fun TarjetaPropuesta(e: com.example.cuidadoyvirtudapp.data.Encuentro) {
    var just by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }
    Tarjeta(borde = Turquesa.copy(alpha = .5f), grosor = 2.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(44.dp).background(colorAvatar(e.a.id), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(e.a.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
            Text("＋", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Marino)
            Box(Modifier.size(44.dp).background(colorAvatar(e.b.id), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(e.b.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
        }
        Spacer(Modifier.height(8.dp))
        Text("${e.a.alias} (${e.a.tramo} · ${e.a.zona}) quiere conversar con ${e.b.alias} (${e.b.tramo} · ${e.b.zona}).", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Marino)
        Text("Ambas hoy dijeron que no hablaron con nadie y aceptaron conocer a otras personas. Antes de presentarlas, pregunte a ${e.b.alias} si quiere conocer a ${e.a.alias}.", fontSize = 12.sp, color = Marino.copy(alpha = .75f), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(just, { just = it }, Modifier.fillMaxWidth(), label = { Text("Justificación de su decisión *") }, minLines = 2, enabled = !ok)
        if (msg.isNotEmpty()) { Spacer(Modifier.height(6.dp)); Text(msg, color = if (ok) Verde else Rojo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                try { Repo.decidirEncuentro(e.id, true, just); ok = true; msg = "✅ Aprobada. Tarea registrada: consultar a ambas personas y presentarlas." } catch (x: BloqueoHumano) { msg = "🔒 ${x.message}" }
            }, Modifier.weight(1f), enabled = !ok, colors = ButtonDefaults.buttonColors(containerColor = VerdeVivo), shape = RoundedCornerShape(12.dp)) { Text("Aprobar", fontWeight = FontWeight.Bold) }
            Button(onClick = {
                try { Repo.decidirEncuentro(e.id, false, just); ok = true; msg = "Registrado: no se presentarán por ahora." } catch (x: BloqueoHumano) { msg = "🔒 ${x.message}" }
            }, Modifier.weight(1f), enabled = !ok, colors = ButtonDefaults.buttonColors(containerColor = GrisAzul), shape = RoundedCornerShape(12.dp)) { Text("No por ahora", fontWeight = FontWeight.Bold) }
        }
    }
}

// ---------------------------------------------------------------------------
// Impacto y modelo de operación. Cifras reales de la sesión sobre datos SINTÉTICOS; los supuestos se declaran.
private const val MIN_POR_REVISION = 10 // supuesto a validar con Fundación La Rosa

@Composable
private fun ImpactoTab() {
    val i = remember(Repo.version) { Repo.impacto() }
    val minSinIa = i.visibles * MIN_POR_REVISION
    val minConIa = i.enCola * MIN_POR_REVISION
    val pctFoco = if (i.visibles == 0) 0 else 100 * i.enCola / i.visibles
    fun horas(min: Int) = "%.1f".format(min / 60.0)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Banner(colores = listOf(Marino, Turquesa)) {
                Column {
                    Text("Impacto y operación 📈", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Simulación sobre datos sintéticos. Los supuestos están marcados y deben validarse con Fundación La Rosa.", fontSize = 12.sp, color = Color.White.copy(alpha = .95f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Tarjeta {
                Titulo("🎯 Dónde poner la atención hoy", 16); Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CifraGrande("${i.enCola}", "personas por revisar", Rojo, Modifier.weight(1f))
                    CifraGrande("${i.visibles}", "personas que comparten su estado", AzulMedio, Modifier.weight(1f))
                    CifraGrande("$pctFoco %", "del total requiere atención", VerdeVivo, Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Text("Sin priorizar, habría que revisar a las ${i.visibles} personas (≈ ${horas(minSinIa)} h). Con la cola priorizada por la IA: ${i.enCola} casos (≈ ${horas(minConIa)} h).", fontSize = 14.sp, color = Marino, fontWeight = FontWeight.Medium)
                Text("Supuesto: $MIN_POR_REVISION min por revisión (a validar). La IA no reemplaza la revisión: solo ordena a quién mirar primero.", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
            }
        }
        item {
            Tarjeta {
                Titulo("🛡️ Garantías ejercidas en esta sesión", 16); Spacer(Modifier.height(6.dp))
                FilaDato("Decisiones humanas registradas", "${i.decisiones}")
                FilaDato("Falsas alarmas descartadas por una persona", "${i.descartadas}")
                FilaDato("Envíos automáticos bloqueados por el sistema", "${i.bloqueados}")
                FilaDato("Derivaciones enviadas (todas con aprobación)", "${i.derivaciones}")
                FilaDato("Propuestas de compañía esperando decisión", "${i.propuestas}")
                FilaDato("Notas guardadas (se borran a los ${com.example.cuidadoyvirtudapp.domain.Retencion.DIAS} días)", "${i.notas}")
            }
        }
        item {
            Tarjeta {
                Titulo("👥 ¿Quién recibe las alertas y qué hace?", 16); Spacer(Modifier.height(6.dp))
                Rol("🧑‍💼 Funcionario/a (Cuidador)", "Recibe la cola priorizada con la explicación. Decide, justifica y registra: descartar, esperar, llamar, visitar o derivar. Revisa los casos «Revisar» dentro de 24 horas.")
                Rol("🏥 Red de apoyo (CESFAM, programa municipal, Fundación)", "Recibe solo el mensaje mínimo aprobado, sin datos de salud, cuando el funcionario decide derivar.")
                Rol("👵 Persona mayor", "Responde 1 minuto al día, decide qué comparte, puede pausar, borrar lo que contó y pedir ayuda. Nada se hace sin que ella lo sepa.")
                Rol("🔐 Administrador/a", "Registra personas, enrola dispositivos y asigna funcionarios. No ve respuestas de salud ni alertas.")
            }
        }
        item {
            Tarjeta {
                Titulo("🌱 Continuidad y siguientes pasos", 16); Spacer(Modifier.height(6.dp))
                Text("• Piloto pequeño con Fundación La Rosa (por ejemplo, 20 personas y 1–2 funcionarios) para validar tiempos y utilidad.", fontSize = 13.sp, color = Marino)
                Text("• Datos reales solo con consentimiento informado y revisión ética; hoy todo es sintético.", fontSize = 13.sp, color = Marino)
                Text("• Voz por teléfono o WhatsApp para quienes no usan app, cifrado y despliegue seguro.", fontSize = 13.sp, color = Marino)
                Text("• Medir con el piloto: casos detectados a tiempo, visitas evitadas, y satisfacción de personas mayores y funcionarios.", fontSize = 13.sp, color = Marino)
            }
        }
    }
}

@Composable
private fun CifraGrande(valor: String, texto: String, color: Color, modifier: Modifier) {
    Column(modifier.background(color.copy(alpha = .1f), RoundedCornerShape(16.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
        Text(texto, fontSize = 11.sp, color = Marino, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FilaDato(texto: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, Modifier.weight(1f), fontSize = 13.sp, color = Marino)
        Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Marino)
    }
}

@Composable
private fun Rol(titulo: String, texto: String) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Marino)
        Text(texto, fontSize = 13.sp, color = Marino.copy(alpha = .85f))
    }
}

@Composable
private fun PersonasTab() {
    val cola = remember(Repo.version) { Repo.cola() }
    val nivel = cola.alertas.associate { it.persona.id to it.nivel }
    val vis = remember(Repo.version) { Repo.personasAdmin().filter { it.consentimiento == "vigente" && it.comparte } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Titulo("Mis personas", 24); Text("Solo aparecen quienes aceptaron compartir su estado con usted.", fontSize = 13.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold) }
        items(vis, key = { it.persona.id }) { p ->
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.persona.alias, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Marino)
                        Text("${p.persona.id} · ${p.persona.tramo} · ${p.persona.zona}", fontSize = 11.sp, color = Marino.copy(alpha = .6f))
                    }
                    ChipNivel(nivel[p.persona.id] ?: "Sin novedad")
                }
            }
        }
    }
}

@Composable
private fun HistorialTab() {
    val b = remember(Repo.version) { Repo.bitacora() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Titulo("Historial y bitácora", 24); Text("Quién vio y decidió qué, y cuándo. Incluye cada envío que el sistema intentó y fue bloqueado.", fontSize = 13.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold) }
        if (b.isEmpty()) item { Text("Sin eventos todavía.") }
        items(b) { x ->
            Tarjeta(borde = if (x.evento.startsWith("ENVÍO BLOQUEADO")) Rojo else AzulCielo.copy(alpha = .3f)) {
                Text(x.evento, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Marino)
                Text("${x.actor} · ${x.ts.replace('T', ' ')}${x.personaId?.let { " · $it" } ?: ""}", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
                if (x.detalle.isNotEmpty()) Text(x.detalle, fontSize = 12.sp, color = Color(0xFF475569))
            }
        }
    }
}

@Composable
private fun EquidadTab() {
    val e = remember(Repo.version) { Repo.equidad() }
    fun pct(x: Double) = "%.0f".format(x * 100)
    val c = e.revisarObservar
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Titulo("Equidad y transparencia", 24); Text("Datos 100 % sintéticos. Métricas medidas contra escenarios simulados; no son evidencia clínica.", fontSize = 13.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold) }
        item {
            Tarjeta {
                Titulo("Cómo funciona la IA y qué modelos usamos", 16); Spacer(Modifier.height(6.dp))
                Text("• Línea base individual (z-score) — estadística clásica. Compara a cada persona consigo misma.", fontSize = 13.sp, color = Marino)
                Text("• Isolation Forest — aprendizaje no supervisado. Detecta combinaciones atípicas en la cohorte.", fontSize = 13.sp, color = Marino)
                Text("• Plantilla de lenguaje simple — reglas, sin IA generativa. No se usó un LLM en esta app.", fontSize = 13.sp, color = Marino)
                Spacer(Modifier.height(6.dp))
                Text("La IA no usa edad, sexo, zona ni ingreso como variable. Solo prioriza; las decisiones son humanas. Lo que una persona decide contar con sus palabras no es leído por la IA: solo lo ve el funcionario.", fontSize = 12.sp, color = Marino.copy(alpha = .7f))
            }
        }
        item {
            Tarjeta {
                Titulo("Desempeño sobre datos simulados", 16); Spacer(Modifier.height(6.dp))
                Text("Detectó ${c.tp} de ${c.tp + c.fn} personas con cambio de riesgo simulado (sensibilidad ${pct(c.recall)} %). Precisión ${pct(c.precision)} %: ${c.fp} falsas alarmas, que un humano descarta.", fontSize = 13.sp, color = Marino)
                Spacer(Modifier.height(4.dp))
                Text("Casos «trampa»: personas reservadas con alerta ${e.reservado.first} de ${e.reservado.second}; personas con mala conexión con alerta ${e.conectividad.first} de ${e.conectividad.second}.", fontSize = 13.sp, color = Marino)
                Spacer(Modifier.height(4.dp))
                Text("Limitaciones: no detecta bien cambios en quien ya tenía muy poco contacto; con poca historia es menos fiable; las cifras vienen de datos inventados.", fontSize = 12.sp, color = Marino.copy(alpha = .7f))
            }
        }
        e.porAtributo.forEach { (titulo, grupos) ->
            item {
                Tarjeta {
                    Titulo(titulo, 16); Spacer(Modifier.height(6.dp))
                    Row { Text("Grupo", Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Personas", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Con alerta", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text("Falsos +*", Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    grupos.forEach { g ->
                        Row(Modifier.padding(top = 4.dp)) { Text(g.nombre, Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 13.sp); Text("${g.n}", Modifier.weight(1f), fontSize = 13.sp); Text("${pct(g.tasaAlertas)} %", Modifier.weight(1f), fontSize = 13.sp); Text("${pct(g.tasaFp)} %", Modifier.weight(1f), fontSize = 13.sp) }
                    }
                }
            }
        }
        item { Text("* Falsos positivos: alertas entre personas sin riesgo simulado. Los grupos pequeños (p. ej. 85+) varían mucho por azar; revisar con datos reales antes de cualquier uso.", fontSize = 11.sp, color = Color(0xFF64748B)) }
    }
}

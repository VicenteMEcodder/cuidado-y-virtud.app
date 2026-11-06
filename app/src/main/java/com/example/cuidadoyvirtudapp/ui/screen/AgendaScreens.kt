package com.example.cuidadoyvirtudapp.ui.screen

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.domain.Agenda
import com.example.cuidadoyvirtudapp.domain.BloqueoHumano
import com.example.cuidadoyvirtudapp.ui.components.*
import com.example.cuidadoyvirtudapp.ui.theme.*

/** Lista de personas con el estado de su agenda. Tocar una persona abre su agenda. */
@Composable
fun AgendaTab(abrir: (String) -> Unit) {
    val lista = remember(Repo.version) { Repo.personasAgenda() }
    var maqueta by remember { mutableStateOf(false) }
    if (maqueta) { CalendarioMaqueta { maqueta = false }; return }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Tarjeta(Modifier.clickable { maqueta = true }, borde = Naranja.copy(alpha = .5f), fondo = NaranjaPalido) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📆", fontSize = 28.sp)
                    Column(Modifier.weight(1f)) {
                        Text("Próximamente: citas con calendario", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Marino)
                        Text("Vista de ejemplo, sin funcionalidad. Toque para verla.", fontSize = 12.sp, color = Marino.copy(alpha = .75f))
                    }
                    Text("VER", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Naranja)
                }
            }
        }
        item {
            Banner(colores = listOf(Lila, AzulMedio)) {
                Column {
                    Text("Agenda y recordatorios 📅", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Usted agenda remedios, citas y llamadas. Para activar la agenda de una persona hay que pedirlo al administrador, y la persona debe aceptar recibir avisos.", fontSize = 12.sp, color = Color.White.copy(alpha = .95f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        items(lista, key = { it.persona.id }) { p ->
            val (txt, col, fondo) = when (p.estado) {
                "HABILITADA" -> Triple("✅ Agenda habilitada (${p.eventos})", Verde, VerdePalido)
                "SOLICITADA" -> Triple("⏳ Esperando al administrador", Ambar, AmbarPalido)
                "RECHAZADA" -> Triple("✖ Solicitud rechazada", Rojo, RojoPalido)
                else -> Triple("Sin agenda", Marino.copy(alpha = .7f), AzulPalido)
            }
            Tarjeta(Modifier.clickable { abrir(p.persona.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).background(colorAvatar(p.persona.id), CircleShape), contentAlignment = Alignment.Center) { Text(p.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
                    Column(Modifier.weight(1f)) {
                        Text(p.persona.alias, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Marino)
                        Text("${p.persona.tramo} · ${p.persona.zona}", fontSize = 11.sp, color = Marino.copy(alpha = .6f))
                    }
                    Text(txt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = col, modifier = Modifier.background(fondo, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun AgendaPersonaScreen(pid: String, cerrar: () -> Unit) {
    val info = remember(Repo.version, pid) { Repo.personasAgenda().firstOrNull { it.persona.id == pid } }
    if (info == null) { cerrar(); return }
    val eventos = remember(Repo.version, pid) { Repo.eventosDe(pid) }
    var motivo by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("remedio") }
    var hh by remember { mutableIntStateOf(8) }
    var mm by remember { mutableIntStateOf(0) }
    var diaria by remember { mutableStateOf(true) }
    var msg by remember { mutableStateOf("") }
    var ok by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Crema).statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹ Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulMedio, modifier = Modifier.clickable(onClick = cerrar).padding(end = 12.dp, top = 6.dp, bottom = 6.dp))
        Banner(colores = listOf(colorAvatar(pid), Marino)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(52.dp).background(Color.White.copy(alpha = .25f), CircleShape), contentAlignment = Alignment.Center) { Text(info.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp) }
                Column {
                    Text("Agenda de ${info.persona.alias}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("${info.persona.tramo} · ${info.persona.zona} · ${info.persona.id}", fontSize = 12.sp, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (info.estado != "HABILITADA") {
            Tarjeta(borde = Ambar.copy(alpha = .5f), fondo = AmbarPalido, grosor = 2.dp) {
                Titulo("🔒 La agenda no está habilitada", 16); Spacer(Modifier.height(4.dp))
                if (info.estado == "SOLICITADA") {
                    Text("⏳ Su solicitud está esperando al administrador. Cuando la habilite, podrá agendar recordatorios aquí.", fontSize = 14.sp, color = Marino)
                } else {
                    Text(if (info.estado == "RECHAZADA") "El administrador rechazó la solicitud anterior. Puede enviar otra explicando mejor el motivo." else "Para agendar remedios, citas o llamadas, el administrador debe habilitar esta función para esta persona.", fontSize = 14.sp, color = Marino)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(motivo, { motivo = it }, Modifier.fillMaxWidth(), label = { Text("Motivo de la solicitud * (sin datos de salud)") }, minLines = 2)
                    if (msg.isNotEmpty()) Text(msg, color = if (ok) Verde else Rojo, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        try { Repo.solicitarAgenda(pid, motivo); ok = true; msg = "✅ Solicitud enviada al administrador." } catch (e: BloqueoHumano) { ok = false; msg = "🔒 ${e.message}" }
                    }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Marino), shape = RoundedCornerShape(12.dp)) { Text("Solicitar al administrador", fontWeight = FontWeight.Bold) }
                }
            }
        } else {
            if (!info.recordatorios) {
                Tarjeta(borde = Ambar.copy(alpha = .5f), fondo = AmbarPalido) {
                    Text("⚠️ La persona aún no aceptó recibir avisos. Puede agendar, pero los avisos no se enviarán hasta que ella lo active en «¿Qué quiere compartir?».", fontSize = 13.sp, color = Marino, fontWeight = FontWeight.SemiBold)
                }
            }
            Tarjeta {
                Titulo("📅 Agenda programada", 16); Spacer(Modifier.height(6.dp))
                if (eventos.isEmpty()) Text("Todavía no hay recordatorios. Agregue el primero abajo.", fontSize = 13.sp, color = Marino.copy(alpha = .7f))
                eventos.forEach { e ->
                    val (ic, nombre) = Agenda.TIPOS[e.tipo] ?: ("📝" to "Otro")
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$ic", fontSize = 26.sp); Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${e.hora} · ${e.titulo}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Marino)
                            Text("$nombre · ${if (e.repeticion == "diaria") "todos los días" else "solo hoy"}", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
                        }
                        Text("🔔", fontSize = 22.sp, modifier = Modifier.clickable { Repo.probarAviso(e.id) }.padding(8.dp))
                        Text("✕", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Rojo, modifier = Modifier.clickable { Repo.borrarEvento(e.id) }.padding(8.dp))
                    }
                }
                if (eventos.isNotEmpty()) Text("🔔 = probar el aviso ahora en la pantalla de la persona mayor (demostración).", fontSize = 11.sp, color = Marino.copy(alpha = .6f))
            }
            Tarjeta(borde = Turquesa.copy(alpha = .5f), grosor = 2.dp) {
                Titulo("➕ Agregar recordatorio", 16); Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Agenda.TIPOS.forEach { (k, v) ->
                        val sel = tipo == k
                        Text("${v.first} ${v.second}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Marino,
                            modifier = Modifier.background(if (sel) Turquesa else Color.White, RoundedCornerShape(50)).border(1.dp, Marino.copy(alpha = .2f), RoundedCornerShape(50)).clickable { tipo = k }.padding(horizontal = 9.dp, vertical = 7.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(titulo, { titulo = it }, Modifier.fillMaxWidth(), label = { Text("¿Qué hay que recordar?") }, singleLine = true)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Hora:", fontWeight = FontWeight.Bold, color = Marino)
                    Paso("−") { hh = (hh + 23) % 24 }; Text("%02d".format(hh), fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino); Paso("+") { hh = (hh + 1) % 24 }
                    Text(":", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino)
                    Paso("−") { mm = (mm + 55) % 60 }; Text("%02d".format(mm), fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino); Paso("+") { mm = (mm + 5) % 60 }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(true to "Todos los días", false to "Solo hoy").forEach { (v, t) ->
                        val sel = diaria == v
                        Text(t, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Marino,
                            modifier = Modifier.background(if (sel) Marino else Color.White, RoundedCornerShape(50)).border(1.dp, Marino.copy(alpha = .2f), RoundedCornerShape(50)).clickable { diaria = v }.padding(horizontal = 12.dp, vertical = 7.dp))
                    }
                }
                if (msg.isNotEmpty()) Text(msg, color = if (ok) Verde else Rojo, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    try {
                        Repo.crearEvento(pid, titulo, tipo, Agenda.texto(hh, mm), if (diaria) "diaria" else "una_vez")
                        ok = true; msg = "✅ Agendado."; titulo = ""
                    } catch (e: BloqueoHumano) { ok = false; msg = "🔒 ${e.message}" }
                }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VerdeVivo), shape = RoundedCornerShape(12.dp)) { Text("Agendar", fontWeight = FontWeight.Black) }
            }
            Text("Los avisos son recordatorios a la hora que usted fija; no son una predicción de la IA. Escriba solo lo necesario, sin datos de salud.", fontSize = 11.sp, color = Marino.copy(alpha = .65f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Paso(t: String, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).background(AzulPalido, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) { Text(t, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Marino) }
}

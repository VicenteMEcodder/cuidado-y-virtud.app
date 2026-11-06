package com.example.cuidadoyvirtudapp.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.R
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.ui.components.*
import com.example.cuidadoyvirtudapp.ui.theme.*
import kotlin.random.Random

/**
 * Panel del administrador: resumen, solicitudes de agenda, enrolamiento de dispositivos y personas registradas.
 * Mínimo privilegio: NO muestra respuestas de salud ni alertas; solo ID seudónimo, tramo, zona y estado de consentimiento.
 */
@Composable
fun AdminScreen(onSalir: () -> Unit) {
    val personas = remember(Repo.version) { Repo.personasAdmin() }
    val solicitudes = remember(Repo.version) { Repo.solicitudesAgenda() }
    var codigo by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf("Todas") } // Todas | Vigentes | Pausadas

    val vigentes = personas.count { it.consentimiento == "vigente" }
    val pausadas = personas.size - vigentes
    val visibles = personas.filter {
        (filtro == "Todas" || (filtro == "Vigentes" && it.consentimiento == "vigente") || (filtro == "Pausadas" && it.consentimiento != "vigente")) &&
            (busqueda.isBlank() || it.persona.id.contains(busqueda.trim(), ignoreCase = true))
    }

    FondoCalido {
        LazyColumn(Modifier.statusBarsPadding().navigationBarsPadding(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Banner(colores = listOf(Lila, Color(0xFF5A6FB8))) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(6.dp)) { Image(painterResource(R.drawable.logo), "Logo", Modifier.height(38.dp)) }
                        Spacer(Modifier.size(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Gestión", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Administrador/a · seguridad máxima", fontSize = 12.sp, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.SemiBold)
                        }
                        Text("Cerrar sesión", fontSize = 12.sp, color = Color.White, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = onSalir).padding(6.dp))
                    }
                }
            }
            item {
                Tarjeta(borde = AzulMedio.copy(alpha = .4f), fondo = AzulPalido) {
                    FilaIcono("🔐", "Mínimo privilegio", "Aquí se gestionan accesos y dispositivos. Este perfil no ve respuestas de salud ni alertas de las personas mayores.")
                }
            }
            // Resumen
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Resumen("👥", "${personas.size}", "Personas", Marino, Cielo, Modifier.weight(1f))
                    Resumen("✔", "$vigentes", "Vigentes", Verde, VerdePalido, Modifier.weight(1f))
                    Resumen("⏸", "$pausadas", "Pausadas", Ambar, AmbarPalido, Modifier.weight(1f))
                    Resumen("📅", "${solicitudes.size}", "Solicitudes", if (solicitudes.isEmpty()) Marino else Naranja, if (solicitudes.isEmpty()) AzulPalido else NaranjaPalido, Modifier.weight(1f))
                }
            }

            // Solicitudes de agenda
            item { Seccion("📅", "Solicitudes de agenda", if (solicitudes.isEmpty()) null else "${solicitudes.size} pendiente(s)") }
            if (solicitudes.isEmpty()) item { Tarjeta { Text("No hay solicitudes pendientes. Cuando un funcionario pida habilitar la agenda de una persona, aparecerá aquí.", fontSize = 13.sp, color = Marino.copy(alpha = .75f)) } }
            items(solicitudes, key = { "sol${it.id}" }) { s ->
                Tarjeta(borde = Naranja.copy(alpha = .6f), fondo = Color(0xFFFFFBF5), grosor = 2.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(42.dp).background(colorAvatar(s.persona.id), CircleShape), contentAlignment = Alignment.Center) { Text(s.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
                        Column(Modifier.weight(1f)) {
                            Text(s.persona.id, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Marino)
                            Text("${s.persona.tramo} · ${s.persona.zona} · pide ${s.solicitante}", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
                        }
                        Text("Nueva", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Naranja, modifier = Modifier.background(NaranjaPalido, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Motivo: “${s.motivo}”", fontSize = 14.sp, color = Marino)
                    Text("Habilitar permite al funcionario agendar recordatorios; la persona igual debe aceptar recibirlos.", fontSize = 11.sp, color = Marino.copy(alpha = .6f))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { Repo.resolverAgenda(s.id, true) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = VerdeVivo), shape = RoundedCornerShape(12.dp)) { Text("✔ Habilitar", fontWeight = FontWeight.Black) }
                        Button(onClick = { Repo.resolverAgenda(s.id, false) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = GrisAzul), shape = RoundedCornerShape(12.dp)) { Text("Rechazar", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // Enrolar dispositivo
            item { Seccion("📲", "Enrolar dispositivo", null) }
            item {
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        QrSimulado(codigo)
                        Column(Modifier.weight(1f)) {
                            Text(if (codigo.isEmpty()) "———  ———" else codigo, fontSize = 30.sp, fontWeight = FontWeight.Black, color = Marino, letterSpacing = 3.sp)
                            Text("Código de un solo uso (simulado). La persona mayor no necesitará ingresar contraseña.", fontSize = 12.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { val n = (100000..999999).random().toString(); codigo = n.substring(0, 3) + " " + n.substring(3) }, Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Marino), shape = RoundedCornerShape(12.dp)) { Text(if (codigo.isEmpty()) "Generar código" else "Generar uno nuevo", fontWeight = FontWeight.Bold) }
                }
            }

            // Personas registradas
            item { Seccion("👥", "Personas registradas", "${visibles.size} de ${personas.size}") }
            item {
                OutlinedTextField(busqueda, { busqueda = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Buscar por ID (por ejemplo PS-3a)") })
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Todas", "Vigentes", "Pausadas").forEach { k ->
                        val sel = filtro == k
                        Text(k, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Marino,
                            modifier = Modifier.background(if (sel) Lila else Color.White, RoundedCornerShape(50)).border(1.dp, Lila.copy(alpha = .4f), RoundedCornerShape(50)).clickable { filtro = k }.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
            }
            if (visibles.isEmpty()) item { Tarjeta { Text("Ninguna persona coincide con la búsqueda.", fontSize = 13.sp, color = Marino.copy(alpha = .75f)) } }
            items(visibles, key = { it.persona.id }) { p ->
                val vigente = p.consentimiento == "vigente"
                Tarjeta {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(40.dp).background(colorAvatar(p.persona.id), CircleShape), contentAlignment = Alignment.Center) { Text(p.persona.iniciales, fontWeight = FontWeight.Black, color = Color.White) }
                        Column(Modifier.weight(1f)) {
                            Text(p.persona.id, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Marino)
                            Text("${p.persona.tramo} · ${p.persona.zona} · ${if (p.comparte) "comparte su estado" else "no comparte su estado"}", fontSize = 11.sp, color = Marino.copy(alpha = .65f))
                        }
                        Text(if (vigente) "✔ Vigente" else "⏸ Pausado", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (vigente) Verde else Ambar,
                            modifier = Modifier.background(if (vigente) VerdePalido else AmbarPalido, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Seccion(icono: String, titulo: String, detalle: String?) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$icono ", fontSize = 20.sp)
        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Marino, modifier = Modifier.weight(1f))
        if (detalle != null) Text(detalle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Lila, modifier = Modifier.background(Color(0xFFEADCF8), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
private fun Resumen(icono: String, valor: String, texto: String, color: Color, fondo: Color, modifier: Modifier) {
    Column(modifier.background(fondo, RoundedCornerShape(18.dp)).padding(vertical = 10.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icono, fontSize = 18.sp)
        Text(valor, fontSize = 26.sp, fontWeight = FontWeight.Black, color = color)
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Marino.copy(alpha = .8f), textAlign = TextAlign.Center)
    }
}

/** Dibujo con aspecto de código QR, generado a partir del código. Es SOLO ilustrativo: no se puede escanear. */
@Composable
private fun QrSimulado(codigo: String) {
    val modulos = 13
    val azar = Random(codigo.hashCode())
    val celdas = remember(codigo) { List(modulos * modulos) { azar.nextFloat() < 0.5f } }
    Canvas(Modifier.size(104.dp).background(Color.White, RoundedCornerShape(10.dp)).border(3.dp, Marino, RoundedCornerShape(10.dp)).padding(6.dp)) {
        val c = size.width / modulos
        fun esquina(x: Int, y: Int) = x < 4 && y < 4 || x >= modulos - 4 && y < 4 || x < 4 && y >= modulos - 4
        for (y in 0 until modulos) for (x in 0 until modulos) {
            val marco = esquina(x, y)
            val lleno = if (codigo.isEmpty()) marco && (x % 3 != 1 || y % 3 != 1) && ((x % 6 < 4) && (y % 6 < 4)) else if (marco) ((x % 12 == 0 || x % 12 == 3 || y % 12 == 0 || y % 12 == 3) || (x % 12 in 1..2 && y % 12 in 1..2)) else celdas[y * modulos + x]
            if (lleno) drawRect(Marino, Offset(x * c, y * c), Size(c, c))
        }
    }
}

package com.example.cuidadoyvirtudapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.R
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.ui.theme.*

@Composable
fun InicioScreen(onRol: (String) -> Unit) {
    var confirmarReinicio by remember { mutableStateOf(false) }
    var reiniciado by remember { mutableStateOf(false) }
    if (confirmarReinicio) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("¿Reiniciar la demostración?", fontWeight = FontWeight.Black, color = Marino) },
            text = { Text("Se borran las alertas atendidas, decisiones, notas y cambios de consentimiento hechos en esta sesión, y se vuelve a los datos de partida. Los datos precargados no se tocan.", color = Marino) },
            confirmButton = { Text("Sí, reiniciar", fontWeight = FontWeight.Black, color = Rosa, modifier = Modifier.clickable { Repo.reiniciarDemo(); reiniciado = true; confirmarReinicio = false }.padding(12.dp)) },
            dismissButton = { Text("Cancelar", fontWeight = FontWeight.Bold, color = Marino, modifier = Modifier.clickable { confirmarReinicio = false }.padding(12.dp)) },
            containerColor = Color.White,
        )
    }
    FondoCalido {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.background(Color.White, RoundedCornerShape(28.dp)).padding(horizontal = 18.dp, vertical = 10.dp)) {
                Image(painterResource(R.drawable.logo), "Cuidado y Virtud", Modifier.height(110.dp))
            }
            Spacer(Modifier.height(10.dp))
            Titulo("Tu Cuidado", 34)
            Text("Acompañar sin dejar de ser humanos", fontSize = 16.sp, color = Marino.copy(alpha = .8f), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            BotonRol("👵", "Soy una persona mayor", "No necesita contraseña", listOf(VerdeVivo, Turquesa)) { onRol("mayor") }
            Spacer(Modifier.height(12.dp))
            BotonRol("🧑‍💼", "Soy cuidador/a (funcionario/a)", "Revisa y decide cada alerta", listOf(Marino, AzulMedio)) { onRol("cuidador") }
            Spacer(Modifier.height(12.dp))
            BotonRol("🔐", "Soy administrador/a", "Altas, dispositivos y asignaciones", listOf(Lila, Color(0xFF5A6FB8))) { onRol("admin") }
            Spacer(Modifier.height(18.dp))
            Text("Prototipo del Hackathon · datos 100 % sintéticos · ingreso simulado", fontSize = 12.sp, color = Marino.copy(alpha = .7f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(if (reiniciado) "✔ Demostración reiniciada" else "↺ Reiniciar demostración", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = if (reiniciado) Verde else Marino, textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { reiniciado = false; confirmarReinicio = true }.padding(10.dp))
        }
    }
}

@Composable
private fun BotonRol(emoji: String, titulo: String, sub: String, colores: List<Color>, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Brush.horizontalGradient(colores), RoundedCornerShape(26.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).background(Color.White.copy(alpha = .95f), androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 30.sp) }
        Spacer(Modifier.size(14.dp))
        Column { Text(titulo, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black); Text(sub, color = Color.White.copy(alpha = .9f), fontSize = 13.sp) }
    }
}

@Composable
fun AdminScreen(onSalir: () -> Unit) {
    val personas = remember(Repo.version) { Repo.personasAdmin() }
    var codigo by remember { mutableStateOf("——") }
    FondoCalido {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Banner(colores = listOf(Lila, Color(0xFF5A6FB8))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.background(Color.White, RoundedCornerShape(14.dp)).padding(6.dp)) { Image(painterResource(R.drawable.logo), "Logo", Modifier.height(38.dp)) }
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) { Text("Gestión", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White); Text("Administrador/a · seguridad máxima", fontSize = 12.sp, color = Color.White.copy(alpha = .9f), fontWeight = FontWeight.SemiBold) }
                    Text("Cerrar sesión", fontSize = 12.sp, color = Color.White, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = onSalir).padding(6.dp))
                }
            }
        }
        item {
            Tarjeta(borde = AzulMedio.copy(alpha = .4f), fondo = AzulPalido) {
                FilaIcono("🔐", "Mínimo privilegio", "El Administrador gestiona accesos y dispositivos, pero no ve respuestas de salud ni alertas personales de las personas mayores.")
            }
        }
        item {
            Tarjeta {
                Titulo("Enrolar dispositivo", 16); Spacer(Modifier.height(6.dp))
                Text(codigo, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Marino, letterSpacing = 4.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("Código de un solo uso (simulado). La persona mayor no necesitará ingresar contraseña.", fontSize = 12.sp, color = AzulMedio, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = { val n = (100000..999999).random().toString(); codigo = n.substring(0, 3) + " " + n.substring(3) }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Marino), shape = RoundedCornerShape(12.dp)) { Text("Generar código", fontWeight = FontWeight.Bold) }
            }
        }
        item { Titulo("Personas registradas (${personas.size})", 16) }
        items(personas, key = { it.persona.id }) { p ->
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.persona.id, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Marino)
                        Text("${p.persona.tramo} · ${p.persona.zona}", fontSize = 12.sp, color = Marino.copy(alpha = .65f))
                    }
                    Text(if (p.consentimiento == "vigente") "✔ Vigente" else "⏸ Pausado", fontWeight = FontWeight.Black, fontSize = 12.sp,
                        color = if (p.consentimiento == "vigente") Verde else Ambar,
                        modifier = Modifier.background(if (p.consentimiento == "vigente") VerdePalido else AmbarPalido, RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
    }
}

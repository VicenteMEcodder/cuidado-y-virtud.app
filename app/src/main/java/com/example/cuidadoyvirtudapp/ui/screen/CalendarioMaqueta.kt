package com.example.cuidadoyvirtudapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.ui.components.*
import com.example.cuidadoyvirtudapp.ui.theme.*

/**
 * VISTA DE EJEMPLO (sin funcionalidad): cómo se vería el calendario de citas.
 * Todo lo que se ve aquí es fijo y ficticio; ningún botón guarda nada. Es solo para mostrar la idea.
 */
@Composable
fun CalendarioMaqueta(cerrar: () -> Unit) {
    // Noviembre 2026 empieza en domingo. Semana de lunes a domingo.
    val completos = setOf(12, 19)          // sin cupo
    val libres = setOf(13, 16, 17, 18, 20, 23) // sugeridos por el sistema
    val solicitado = 12
    Column(Modifier.fillMaxSize().background(Crema).statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("‹ Volver", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulMedio, modifier = Modifier.clickable(onClick = cerrar).padding(end = 12.dp, top = 6.dp, bottom = 6.dp))
        Banner(colores = listOf(Naranja, Sol)) {
            Column {
                Text("Citas con calendario 📆", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("VISTA DE EJEMPLO · aún no funciona", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White,
                    modifier = Modifier.padding(top = 4.dp).background(Color.Black.copy(alpha = .25f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 3.dp))
            }
        }

        Tarjeta {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino.copy(alpha = .4f), modifier = Modifier.padding(horizontal = 8.dp))
                Text("Noviembre 2026", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Marino, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text("›", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Marino.copy(alpha = .4f), modifier = Modifier.padding(horizontal = 8.dp))
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) { listOf("L", "M", "M", "J", "V", "S", "D").forEach { Text(it, Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Marino.copy(alpha = .6f), textAlign = TextAlign.Center) } }
            Spacer(Modifier.height(4.dp))
            val celdas = List(6) { 0 } + (1..30).toList()  // 6 vacíos: el día 1 cae en domingo
            celdas.chunked(7).forEach { semana ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    for (i in 0 until 7) {
                        val d = semana.getOrNull(i) ?: 0
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                            if (d != 0) {
                                val finde = i >= 5
                                val (fondo, letra) = when {
                                    d == solicitado -> Rojo to Color.White
                                    d in completos -> RojoPalido to Rojo
                                    d in libres -> VerdePalido to Verde
                                    finde -> Color(0xFFEDEFF2) to Color(0xFF9AA5B1)
                                    else -> Color.White to Marino
                                }
                                Box(Modifier.fillMaxSize().background(fondo, CircleShape).then(if (d == 6) Modifier.border(2.dp, Marino, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                                    Text("$d", fontSize = 14.sp, fontWeight = if (d in libres || d == solicitado) FontWeight.Black else FontWeight.SemiBold, color = letra)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Leyenda(Rojo, "Día pedido (sin cupo)"); Leyenda(Verde, "Días disponibles sugeridos"); Leyenda(Color(0xFF9AA5B1), "Sin atención (fin de semana)")
        }

        Tarjeta(borde = Naranja.copy(alpha = .4f), fondo = NaranjaPalido) {
            Titulo("Cómo funcionaría", 16); Spacer(Modifier.height(6.dp))
            Paso("1", "El cuidador propone una cita", "Control médico para don Luis · jueves 12 · 10:00")
            Paso("2", "El administrador revisa la disponibilidad", "Aprueba si hay cupo ese día")
            Paso("3", "Si no hay cupo, el calendario sugiere otros días", "“El 12 no tiene cupo. Días disponibles en noviembre: 13, 16, 17, 18, 20 y 23.” (regla automática sobre el calendario; no es IA)")
            Paso("4", "Si se aprueba, la persona mayor recibe el aviso con voz", "“Tiene una hora para su control médico el día 13 a las 10:00.”")
        }

        Box(Modifier.fillMaxWidth().background(Color(0xFFE2E8F0), RoundedCornerShape(14.dp)).padding(14.dp), contentAlignment = Alignment.Center) {
            Text("Proponer cita (próximamente)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
        Text("Esta pantalla es solo una idea de cómo se vería. Los datos son ficticios y nada se guarda.", fontSize = 11.sp, color = Marino.copy(alpha = .65f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(Modifier.size(12.dp).background(color, CircleShape)); Spacer(Modifier.size(8.dp))
        Text(texto, fontSize = 12.sp, color = Marino.copy(alpha = .8f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Paso(n: String, titulo: String, detalle: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Box(Modifier.size(26.dp).background(Naranja, CircleShape), contentAlignment = Alignment.Center) { Text(n, fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White) }
        Spacer(Modifier.size(10.dp))
        Column { Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Marino); Text(detalle, fontSize = 12.sp, color = Marino.copy(alpha = .8f)) }
    }
}

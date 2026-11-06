package com.example.cuidadoyvirtudapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuidadoyvirtudapp.ui.theme.*

/** Fondo cálido con formas suaves de colores (menos "frío/genérico"). */
@Composable
fun FondoCalido(modifier: Modifier = Modifier, contenido: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
    Box(
        modifier.fillMaxSize()
            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Crema, Color(0xFFEAF4FC))))
            .drawBehind {
                drawCircle(Durazno.copy(alpha = .55f), size.width * 0.5f, Offset(size.width * 1.0f, size.height * 0.02f))
                drawCircle(Menta.copy(alpha = .7f), size.width * 0.45f, Offset(0f, size.height * 0.98f))
                drawCircle(Cielo.copy(alpha = .6f), size.width * 0.22f, Offset(size.width * 0.05f, size.height * 0.32f))
            },
        content = contenido,
    )
}

/** Banner con degradado de marca. */
@Composable
fun Banner(modifier: Modifier = Modifier, colores: List<Color> = listOf(Marino, AzulMedio), contenido: @Composable () -> Unit) {
    Box(
        modifier.fillMaxWidth()
            .background(androidx.compose.ui.graphics.Brush.horizontalGradient(colores), RoundedCornerShape(28.dp))
            .padding(20.dp),
    ) { contenido() }
}

private val PALETA_AVATAR = listOf(Coral, Sol, VerdeVivo, AzulMedio, Lila, Turquesa, Rosa)
fun colorAvatar(texto: String): Color = PALETA_AVATAR[(texto.hashCode() and 0x7fffffff) % PALETA_AVATAR.size]

/** Semáforo: color + ícono + texto (nunca solo color). */
data class Semaforo(val borde: Color, val fondo: Color, val texto: Color, val icono: String)

fun semaforo(nivel: String) = when (nivel) {
    "Revisar" -> Semaforo(Rojo, RojoPalido, Rojo, "⚠️")
    "Observar" -> Semaforo(AmbarBorde, AmbarPalido, Ambar, "👁️")
    else -> Semaforo(Verde, VerdePalido, Verde, "✅")
}

@Composable
fun ChipNivel(nivel: String) {
    val s = semaforo(nivel)
    Box(Modifier.background(s.fondo, RoundedCornerShape(50)).border(1.dp, s.borde.copy(alpha = .4f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text("${s.icono} $nivel", color = s.texto, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Composable
fun Tarjeta(modifier: Modifier = Modifier, borde: Color = AzulCielo.copy(alpha = .3f), fondo: Color = Color.White, grosor: Dp = 1.dp, contenido: @Composable () -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = fondo),
        border = BorderStroke(grosor, borde), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) { contenido() }
    }
}

@Composable
fun Titulo(texto: String, tam: Int = 20) = Text(texto, fontSize = tam.sp, fontWeight = FontWeight.Black, color = Marino)

/** Mini-gráfico de tendencia (30 días). */
@Composable
fun Spark(valores: List<Double?>, color: Color, modifier: Modifier = Modifier.size(92.dp, 28.dp)) {
    Canvas(modifier) {
        val pts = valores.mapIndexedNotNull { i, v -> v?.let { Offset(i * size.width / (valores.size - 1).coerceAtLeast(1), size.height - 3f - (it.toFloat() * (size.height - 6f))) } }
        if (pts.size < 2) return@Canvas
        val p = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
        drawPath(p, AzulCielo, style = Stroke(width = 4f, cap = StrokeCap.Round))
        drawCircle(color, 7f, pts.last())
    }
}

/** Gráfico grande: normalidad (punteada) vs últimos días. */
@Composable
fun GraficoNormalidad(serie: List<Double?>, base: Double, color: Color) {
    Canvas(Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(8.dp)) {
        val ys = { v: Double -> size.height - 6f - (v.toFloat() * (size.height - 12f)) }
        val vals = serie.map { it ?: base }
        drawLine(AzulCielo, Offset(0f, ys(base)), Offset(size.width, ys(base)), strokeWidth = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f)))
        val pts = vals.mapIndexed { i, v -> Offset(i * size.width / (vals.size - 1).coerceAtLeast(1), ys(v)) }
        val p = Path().apply { moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
        drawPath(p, color, style = Stroke(width = 8f, cap = StrokeCap.Round))
        drawCircle(color, 11f, pts.last())
    }
}

@Composable
fun FilaIcono(icono: String, titulo: String, sub: String = "", modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(icono, fontSize = 24.sp)
        Column { Text(titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Marino); if (sub.isNotEmpty()) Text(sub, fontSize = 12.sp, color = AzulMedio) }
    }
}

fun fechaLinda(iso: String): String {
    val p = iso.take(10).split("-")
    if (p.size != 3) return iso
    val meses = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
    val cal = java.util.Calendar.getInstance().apply { set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt()) }
    val dias = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
    return "${dias[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1].replaceFirstChar { it.uppercase() }}, ${p[2].toInt()} de ${meses[p[1].toInt() - 1]}"
}

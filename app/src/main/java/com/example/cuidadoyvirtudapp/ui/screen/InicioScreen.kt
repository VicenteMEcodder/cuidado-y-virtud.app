package com.example.cuidadoyvirtudapp.ui.screen

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
import com.example.cuidadoyvirtudapp.ui.components.*
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
        // Foto de fondo (arriba se ven las personas) con un velo claro hacia abajo para que los botones y textos se lean bien.
        Image(painterResource(R.drawable.fondo_inicio), null, Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop, alignment = Alignment.TopCenter)
        Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(0f to Color.Transparent, 0.30f to Color.White.copy(alpha = .55f), 0.50f to Color.White.copy(alpha = .93f), 1f to Color.White.copy(alpha = .97f))))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
            Box(Modifier.background(Color.White, RoundedCornerShape(28.dp)).padding(horizontal = 18.dp, vertical = 8.dp)) {
                Image(painterResource(R.drawable.logo), "Cuidado y Virtud", Modifier.height(84.dp))
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


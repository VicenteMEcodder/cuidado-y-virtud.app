package com.example.cuidadoyvirtudapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.cuidadoyvirtudapp.data.Repo
import com.example.cuidadoyvirtudapp.ui.screen.AdminScreen
import com.example.cuidadoyvirtudapp.ui.screen.CuidadorScreen
import com.example.cuidadoyvirtudapp.ui.screen.InicioScreen
import com.example.cuidadoyvirtudapp.ui.screen.MayorScreen
import com.example.cuidadoyvirtudapp.ui.theme.CuidadoYVirtudAPPTheme
import com.example.cuidadoyvirtudapp.ui.theme.Fondo
import com.example.cuidadoyvirtudapp.ui.theme.Marino
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuidadoYVirtudAPPTheme {
                val ctx = LocalContext.current
                var listo by remember { mutableStateOf(false) }
                var pantalla by rememberSaveable { mutableStateOf("inicio") }

                // Carga la SQLite (datos sintéticos precargados) y corre el detector, fuera del hilo principal.
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.Default) { Repo.iniciar(ctx) }
                    listo = true
                }

                if (!listo) {
                    Box(Modifier.fillMaxSize().background(Fondo), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Marino)
                    }
                } else {
                    BackHandler(enabled = pantalla != "inicio") { pantalla = "inicio" }
                    when (pantalla) {
                        "mayor" -> MayorScreen { pantalla = "inicio" }
                        "cuidador" -> CuidadorScreen { pantalla = "inicio" }
                        "admin" -> AdminScreen { pantalla = "inicio" }
                        else -> InicioScreen { pantalla = it }
                    }
                }
            }
        }
    }
}

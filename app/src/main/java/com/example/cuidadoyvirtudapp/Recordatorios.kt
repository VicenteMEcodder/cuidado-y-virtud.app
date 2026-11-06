package com.example.cuidadoyvirtudapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cuidadoyvirtudapp.data.EventoAgenda
import com.example.cuidadoyvirtudapp.domain.Agenda
import java.util.Calendar

/**
 * Avisos del teléfono (notificación) a la hora que fijó el funcionario. Usa alarmas inexactas del sistema (no exigen
 * permiso especial) y se reprograma cada vez que cambia la agenda o el consentimiento.
 * Límite conocido: si el teléfono se reinicia, los avisos se reprograman al abrir la app de nuevo.
 */
object Recordatorios {
    const val CANAL = "recordatorios"

    private fun intent(ctx: Context, id: Long, titulo: String, tipo: String, hora: String, repeticion: String) =
        Intent(ctx, RecordatorioReceiver::class.java).apply {
            putExtra("id", id); putExtra("titulo", titulo); putExtra("tipo", tipo); putExtra("hora", hora); putExtra("rep", repeticion)
        }

    private fun pendiente(ctx: Context, id: Long, i: Intent) =
        PendingIntent.getBroadcast(ctx, id.toInt(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun programar(ctx: Context, e: EventoAgenda) {
        val cuando = Agenda.proximaMillis(e.hora, e.repeticion, e.fecha, Calendar.getInstance()) ?: run { cancelar(ctx, e.id); return }
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pendiente(ctx, e.id, intent(ctx, e.id, e.titulo, e.tipo, e.hora, e.repeticion)))
    }

    fun cancelar(ctx: Context, id: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendiente(ctx, id, Intent(ctx, RecordatorioReceiver::class.java)))
    }

    fun crearCanal(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(NotificationChannel(CANAL, "Recordatorios", NotificationManager.IMPORTANCE_HIGH).apply { description = "Avisos de remedios y citas" })
        }
    }
}

class RecordatorioReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val id = i.getLongExtra("id", -1)
        if (id < 0) return
        val titulo = i.getStringExtra("titulo") ?: return
        val tipo = i.getStringExtra("tipo") ?: "otro"
        val hora = i.getStringExtra("hora") ?: return
        val rep = i.getStringExtra("rep") ?: "diaria"
        Recordatorios.crearCanal(ctx)
        val abrir = PendingIntent.getActivity(ctx, id.toInt(), Intent(ctx, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, Recordatorios.CANAL)
            .setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Tu Cuidado")
            .setContentText(Agenda.textoAviso(tipo, titulo)).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).setContentIntent(abrir).build()
        if (Build.VERSION.SDK_INT < 33 || ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            NotificationManagerCompat.from(ctx).notify(id.toInt(), n)
        if (rep == "diaria") Agenda.proximaMillis(hora, "diaria", null, Calendar.getInstance())?.let { cuando ->
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, PendingIntent.getBroadcast(ctx, id.toInt(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
    }
}

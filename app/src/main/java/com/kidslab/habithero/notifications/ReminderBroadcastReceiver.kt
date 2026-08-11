package com.kidslab.habithero.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kidslab.habithero.HabitHeroApp
import com.kidslab.habithero.MainActivity
import com.kidslab.habithero.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Se dispara a la hora elegida para un hábito. Si el hábito todavía toca hoy y
 * no está marcado, muestra el recordatorio; si no, no hace nada.
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("habitId", -1L)
        if (habitId < 0) return

        val resultadoPendiente = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                notificarSiCorresponde(context, habitId)
            } finally {
                resultadoPendiente.finish()
            }
        }
    }

    private suspend fun notificarSiCorresponde(context: Context, habitId: Long) {
        val repositorio = (context.applicationContext as HabitHeroApp).repositorio
        val habito = repositorio.obtenerHabito(habitId) ?: return
        if (!habito.activo) return

        val hoy = LocalDate.now()
        if (!habito.tocaHoy(hoy)) return
        if (repositorio.estaMarcado(habitId, hoy)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!concedido) return
        }

        val abrirApp = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CanalNotificaciones.ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${habito.icono} ${habito.nombre}")
            .setContentText("Todavía no lo has marcado hoy. ¡Tú puedes!")
            .setAutoCancel(true)
            .setContentIntent(abrirApp)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.toInt(), notificacion)
    }
}

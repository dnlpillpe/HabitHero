package com.kidslab.habithero.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kidslab.habithero.data.local.entity.Habit
import java.util.Calendar

/**
 * Programa/cancela las alarmas diarias de recordatorio de un hábito.
 *
 * Se usa [AlarmManager.setRepeating], inexacta desde la API 19: no necesita el
 * permiso especial de alarmas exactas (SCHEDULE_EXACT_ALARM), que en Android 12+
 * mandaría a un niño a Ajustes del sistema. Un aviso dentro de una ventana
 * aproximada al minuto elegido es un compromiso aceptable para un recordatorio
 * que no es crítico.
 */
object RecordatorioScheduler {

    private const val EXTRA_HABIT_ID = "habitId"

    fun programar(context: Context, habito: Habit) {
        val minutos = habito.horaRecordatorioMinutos ?: return
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return

        val proximaAlarma = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, minutos / 60)
            set(Calendar.MINUTE, minutos % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            proximaAlarma.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context, habito.id)
        )
    }

    fun cancelar(context: Context, habitId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(pendingIntent(context, habitId))
    }

    /** Vuelve a programar todos los recordatorios activos, p.ej. tras reiniciar el dispositivo. */
    fun reprogramarTodos(context: Context, habitos: List<Habit>) {
        habitos.forEach { habito -> if (habito.horaRecordatorioMinutos != null) programar(context, habito) }
    }

    private fun pendingIntent(context: Context, habitId: Long): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).putExtra(EXTRA_HABIT_ID, habitId)
        return PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

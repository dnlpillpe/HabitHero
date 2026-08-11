package com.kidslab.habithero.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** Crea el canal de notificaciones de recordatorios. Idempotente: se puede llamar siempre. */
object CanalNotificaciones {

    const val ID = "recordatorios_habito"

    fun crear(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            ID,
            "Recordatorios de hábitos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos opcionales para no olvidar un hábito del día."
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(canal)
    }
}

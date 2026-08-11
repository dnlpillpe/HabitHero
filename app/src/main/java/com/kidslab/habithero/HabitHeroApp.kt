package com.kidslab.habithero

import android.app.Application
import com.kidslab.habithero.data.local.AppDatabase
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.notifications.CanalNotificaciones

/**
 * Contenedor de dependencias hecho a mano: la app es pequeña y no necesita
 * una librería de inyección.
 */
class HabitHeroApp : Application() {

    val baseDatos: AppDatabase by lazy { AppDatabase.obtener(this) }
    val repositorio: HabitHeroRepository by lazy { HabitHeroRepository(baseDatos) }

    override fun onCreate() {
        super.onCreate()
        CanalNotificaciones.crear(this)
    }
}

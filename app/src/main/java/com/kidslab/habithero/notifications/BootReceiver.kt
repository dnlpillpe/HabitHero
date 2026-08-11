package com.kidslab.habithero.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kidslab.habithero.HabitHeroApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** El sistema borra todas las alarmas al reiniciar: hay que volver a programarlas. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val resultadoPendiente = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repositorio = (context.applicationContext as HabitHeroApp).repositorio
                val habitosConRecordatorio = repositorio.habitosConRecordatorio()
                RecordatorioScheduler.reprogramarTodos(context.applicationContext, habitosConRecordatorio)
            } finally {
                resultadoPendiente.finish()
            }
        }
    }
}

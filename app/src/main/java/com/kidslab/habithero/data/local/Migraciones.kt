package com.kidslab.habithero.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 -> 2: recordatorios y categorias en [entity.Habit], marco equipado en
 * [entity.UserProfile], y las tablas nuevas de la tienda y los desafios diarios.
 * Ninguna columna nueva es NOT NULL sin valor por defecto, asi que no hace falta
 * reconstruir ninguna tabla existente ni se pierde ningun dato del heroe.
 */
val MIGRACION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habit ADD COLUMN horaRecordatorioMinutos INTEGER")
        db.execSQL("ALTER TABLE habit ADD COLUMN categoria TEXT NOT NULL DEFAULT 'OTROS'")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN marcoSeleccionado TEXT")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_unlock (
                itemId          TEXT    NOT NULL PRIMARY KEY,
                fechaAdquirido  INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_challenge (
                fecha                 INTEGER NOT NULL PRIMARY KEY,
                tipo                  TEXT    NOT NULL,
                meta                  INTEGER NOT NULL,
                completado            INTEGER NOT NULL DEFAULT 0,
                recompensaMonedas     INTEGER NOT NULL,
                recompensaExperiencia INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

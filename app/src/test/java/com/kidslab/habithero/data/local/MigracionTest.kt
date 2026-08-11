package com.kidslab.habithero.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprueba que MIGRACION_1_2 preserva los datos de un heroe real y deja el
 * esquema nuevo utilizable, sin depender de un esquema exportado (app/schemas/1.json
 * no existe todavia porque este proyecto nunca se ha compilado en este entorno).
 *
 * Se construye a mano una base con el esquema v1 exacto de database/schema.sql,
 * se le insertan filas de ejemplo, y se reabre con Room aplicando la migracion:
 * si el ALTER TABLE no coincidiera con las entidades, Room lanzaria una excepcion
 * al validar el esquema en la apertura.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MigracionTest {

    private val nombreBase = "migracion-test.db"

    @Test
    fun `la migracion 1 a 2 preserva los datos existentes y agrega las columnas nuevas`() = runTest {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        contexto.deleteDatabase(nombreBase)

        crearBaseV1ConDatosDeEjemplo(contexto)

        val db = Room.databaseBuilder(contexto, AppDatabase::class.java, nombreBase)
            .addMigrations(MIGRACION_1_2)
            .build()

        // Forzar la apertura (y por tanto la migracion) ahora mismo.
        db.openHelper.writableDatabase

        val habito = db.habitDao().obtener(1L)
        assertEquals("Cepillarse los dientes", habito?.nombre)
        assertEquals("OTROS", habito?.categoria)
        assertNull(habito?.horaRecordatorioMinutos)

        val perfil = db.userProfileDao().obtener()
        assertEquals(42, perfil?.monedas)
        assertEquals(120, perfil?.experiencia)
        assertNull(perfil?.marcoSeleccionado)

        db.close()
        contexto.deleteDatabase(nombreBase)
    }

    /** Crea, con SQL crudo, una base con el esquema v1 tal como lo describe database/schema.sql. */
    private fun crearBaseV1ConDatosDeEjemplo(contexto: Context) {
        val ruta = contexto.getDatabasePath(nombreBase)
        ruta.parentFile?.mkdirs()
        val db = SQLiteDatabase.openOrCreateDatabase(ruta, null)

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_profile (
                id                    INTEGER NOT NULL PRIMARY KEY,
                nombre                TEXT    NOT NULL,
                avatar                TEXT    NOT NULL,
                monedas               INTEGER NOT NULL DEFAULT 0,
                experiencia           INTEGER NOT NULL DEFAULT 0,
                nivel                 INTEGER NOT NULL DEFAULT 1,
                fechaCreacion         INTEGER NOT NULL,
                onboardingCompletado  INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS habit (
                id                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                nombre            TEXT    NOT NULL,
                icono             TEXT    NOT NULL,
                diasSemana        TEXT    NOT NULL,
                colorIndex        INTEGER NOT NULL DEFAULT 0,
                esPredeterminado  INTEGER NOT NULL DEFAULT 0,
                activo            INTEGER NOT NULL DEFAULT 1,
                orden             INTEGER NOT NULL DEFAULT 0,
                fechaCreacion     INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS habit_completion (
                id                  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                habitId             INTEGER NOT NULL,
                fecha               INTEGER NOT NULL,
                monedasGanadas      INTEGER NOT NULL DEFAULT 0,
                experienciaGanada   INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (habitId) REFERENCES habit (id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_habit_completion_habitId_fecha ON habit_completion (habitId, fecha)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completion_fecha ON habit_completion (fecha)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS badge (
                id           TEXT    NOT NULL PRIMARY KEY,
                nombre       TEXT    NOT NULL,
                descripcion  TEXT    NOT NULL,
                icono        TEXT    NOT NULL,
                tipo         TEXT    NOT NULL,
                meta         INTEGER NOT NULL,
                orden        INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_badge (
                badgeId         TEXT    NOT NULL PRIMARY KEY,
                fechaObtencion  INTEGER NOT NULL,
                vista           INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY (badgeId) REFERENCES badge (id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            "INSERT INTO user_profile (id, nombre, avatar, monedas, experiencia, nivel, fechaCreacion, onboardingCompletado) " +
                "VALUES (1, 'Ana', '🦸', 42, 120, 2, 20000, 1)"
        )
        db.execSQL(
            "INSERT INTO habit (id, nombre, icono, diasSemana, colorIndex, esPredeterminado, activo, orden, fechaCreacion) " +
                "VALUES (1, 'Cepillarse los dientes', '🦷', '1,2,3,4,5,6,7', 0, 1, 1, 0, 20000)"
        )

        // Room guarda la version del esquema en PRAGMA user_version; hay que fijarla
        // a mano porque esta base se creo sin pasar por Room.
        db.version = 1
        db.close()
    }
}

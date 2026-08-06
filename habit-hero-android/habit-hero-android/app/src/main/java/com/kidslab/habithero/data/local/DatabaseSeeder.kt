package com.kidslab.habithero.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

/**
 * Datos semilla. Se ejecuta una unica vez, dentro de RoomDatabase.Callback.onCreate,
 * usando SQL directo para no depender de los DAO antes de que la base este lista.
 *
 * El equivalente legible de estas sentencias esta en database/sample_data.sql.
 */
object DatabaseSeeder {

    fun sembrar(db: SupportSQLiteDatabase, hoy: LocalDate = LocalDate.now()) {
        val hoyEpoch = hoy.toEpochDay()

        // --- Perfil unico ---
        db.execSQL(
            """
            INSERT OR IGNORE INTO user_profile
                (id, nombre, avatar, monedas, experiencia, nivel, fechaCreacion, onboardingCompletado)
            VALUES (1, 'Héroe', '🦸', 0, 0, 1, $hoyEpoch, 0)
            """.trimIndent()
        )

        // --- Hábitos predeterminados (1..7 = lunes..domingo) ---
        val habitos = listOf(
            Semilla(1, "Cepillarse los dientes", "🦷", "1,2,3,4,5,6,7", 0),
            Semilla(2, "Beber agua", "💧", "1,2,3,4,5,6,7", 1),
            Semilla(3, "Leer un rato", "📚", "1,2,3,4,5", 2),
            Semilla(4, "Ordenar el dormitorio", "🧹", "1,2,3,4,5,6,7", 3),
            Semilla(5, "Preparar la mochila", "🎒", "1,2,3,4,5", 4),
            Semilla(6, "Dormir a tiempo", "😴", "1,2,3,4,5,6,7", 5)
        )
        habitos.forEachIndexed { indice, h ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO habit
                    (id, nombre, icono, diasSemana, colorIndex, esPredeterminado, activo, orden, fechaCreacion)
                VALUES (${h.id}, '${h.nombre}', '${h.icono}', '${h.dias}', ${h.color}, 1, 1, $indice, $hoyEpoch)
                """.trimIndent()
            )
        }

        // --- Catálogo de insignias ---
        val insignias = listOf(
            Insignia("primer_paso", "Primer paso", "Marca tu primer hábito", "🌟", "TOTAL_MARCAS", 1, 0),
            Insignia("constante_10", "Diez marcas", "Completa 10 hábitos en total", "✅", "TOTAL_MARCAS", 10, 1),
            Insignia("racha_3", "Tres seguidos", "Consigue una racha de 3 días", "🔥", "RACHA", 3, 2),
            Insignia("racha_7", "Semana heroica", "Consigue una racha de 7 días", "🏆", "RACHA", 7, 3),
            Insignia("cofre_100", "Cofre lleno", "Junta 100 monedas", "💰", "MONEDAS", 100, 4),
            Insignia("nivel_5", "Nivel 5", "Alcanza el nivel 5", "🚀", "NIVEL", 5, 5),
            Insignia("creador", "Inventor de hábitos", "Crea un hábito propio", "🎨", "HABITO_PROPIO", 1, 6)
        )
        insignias.forEach { b ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO badge (id, nombre, descripcion, icono, tipo, meta, orden)
                VALUES ('${b.id}', '${b.nombre}', '${b.descripcion}', '${b.icono}', '${b.tipo}', ${b.meta}, ${b.orden})
                """.trimIndent()
            )
        }

        // --- Marcas de ejemplo de los días anteriores, para que la app tenga
        //     contenido visible desde el primer arranque. ---
        val ejemplos = listOf(
            1L to 1L, 1L to 2L, 1L to 3L,
            2L to 1L, 2L to 2L,
            3L to 2L,
            6L to 1L
        )
        ejemplos.forEach { (habitId, diasAtras) ->
            val fecha = hoyEpoch - diasAtras
            db.execSQL(
                """
                INSERT OR IGNORE INTO habit_completion (habitId, fecha, monedasGanadas, experienciaGanada)
                VALUES ($habitId, $fecha, 5, 10)
                """.trimIndent()
            )
        }
    }

    private data class Semilla(
        val id: Int,
        val nombre: String,
        val icono: String,
        val dias: String,
        val color: Int
    )

    private data class Insignia(
        val id: String,
        val nombre: String,
        val descripcion: String,
        val icono: String,
        val tipo: String,
        val meta: Int,
        val orden: Int
    )
}

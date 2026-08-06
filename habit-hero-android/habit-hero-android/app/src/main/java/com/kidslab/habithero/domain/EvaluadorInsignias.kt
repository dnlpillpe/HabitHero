package com.kidslab.habithero.domain

import com.kidslab.habithero.data.local.entity.Badge

/** Fotografía del progreso del héroe en un instante dado. */
data class EstadisticasHeroe(
    val totalMarcas: Int,
    val mejorRachaGlobal: Int,
    val monedas: Int,
    val nivel: Int,
    val habitosPropios: Int
)

/** Decide qué insignias del catálogo ya están conseguidas. */
object EvaluadorInsignias {

    fun cumple(insignia: Badge, stats: EstadisticasHeroe): Boolean = when (insignia.tipo) {
        Badge.TIPO_TOTAL_MARCAS -> stats.totalMarcas >= insignia.meta
        Badge.TIPO_RACHA -> stats.mejorRachaGlobal >= insignia.meta
        Badge.TIPO_MONEDAS -> stats.monedas >= insignia.meta
        Badge.TIPO_NIVEL -> stats.nivel >= insignia.meta
        Badge.TIPO_HABITO_PROPIO -> stats.habitosPropios >= insignia.meta
        else -> false
    }

    /** Progreso 0f..1f de una insignia todavía no conseguida. */
    fun progreso(insignia: Badge, stats: EstadisticasHeroe): Float {
        val actual = when (insignia.tipo) {
            Badge.TIPO_TOTAL_MARCAS -> stats.totalMarcas
            Badge.TIPO_RACHA -> stats.mejorRachaGlobal
            Badge.TIPO_MONEDAS -> stats.monedas
            Badge.TIPO_NIVEL -> stats.nivel
            Badge.TIPO_HABITO_PROPIO -> stats.habitosPropios
            else -> 0
        }
        if (insignia.meta <= 0) return 1f
        return (actual.toFloat() / insignia.meta).coerceIn(0f, 1f)
    }

    fun nuevas(catalogo: List<Badge>, yaObtenidas: Set<String>, stats: EstadisticasHeroe): List<Badge> =
        catalogo.filter { it.id !in yaObtenidas && cumple(it, stats) }
}

package com.kidslab.habithero.domain

/** Reglas de compra de la tienda de recompensas: nunca se queda en monedas negativas. */
object EvaluadorTienda {

    fun puedeComprar(monedas: Int, precio: Int, yaComprado: Boolean): Boolean =
        !yaComprado && monedas >= precio

    fun monedasTrasComprar(monedas: Int, precio: Int): Int =
        (monedas - precio).coerceAtLeast(0)
}

package com.kidslab.habithero.data.repository

import com.kidslab.habithero.util.TiendaCatalogo

/** Lo que ocurre al intentar comprar un item de la tienda. */
sealed interface ResultadoCompra {

    data class Exito(val item: TiendaCatalogo.ItemTienda) : ResultadoCompra

    data object MonedasInsuficientes : ResultadoCompra

    data object YaComprado : ResultadoCompra

    data object ItemNoEncontrado : ResultadoCompra
}

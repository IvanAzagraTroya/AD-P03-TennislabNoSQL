package com.example.tennislabspringboot.dto.producto

import com.example.tennislabspringboot.models.producto.TipoProducto
import java.util.*

data class ProductoDTOcreate(
    val uuid: UUID = UUID.randomUUID(),
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int = 0
)

data class ProductoDTOvisualize(
    val tipo: TipoProducto,
    val marca: String,
    val modelo: String,
    var precio: Double,
    val stock: Int
)

data class ProductoDTOvisualizeList(val productos: List<ProductoDTOvisualize>)
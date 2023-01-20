package mappers

import dto.producto.ProductoDTOcreate
import dto.producto.ProductoDTOvisualize
import models.producto.Producto

fun Producto.toDTO() =
    ProductoDTOvisualize (tipo, marca, modelo, precio, stock)

fun ProductoDTOcreate.fromDTO() = Producto(
    uuid = uuid,
    tipo = tipo,
    marca = marca,
    modelo = modelo,
    precio = precio,
    stock = stock
)

fun toDTO(list: List<Producto>) : List<ProductoDTOvisualize> {
    val res = mutableListOf<ProductoDTOvisualize>()
    list.forEach { res.add(it.toDTO()) }
    return res
}

fun fromDTO(list: List<ProductoDTOcreate>) : List<Producto> {
    val res = mutableListOf<Producto>()
    list.forEach { res.add(it.fromDTO()) }
    return res
}
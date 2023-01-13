package models.producto

sealed class ProductoResponse<Producto>

/**
 * @param code código HTTP que devolverá la clase
 * @param data son los datos u objeto que devolverá la clase si la respuesta es exitosa
 * Hereda de la clase ProductoResponse usando el tipo Any, puede usar cualquier tipo
 */
class ProductoResponseSuccess<T: Any>(val code: Int, val data: T) : ProductoResponse<T>()

/**
 * @param code código HTTP que devolverá la clase
 * @param message cadena de texto que devolverá en caso de error
 * Hereda de la clase ProductoResponse utilizando el tipo Nothing
 */
class PedidoResponseError(val code: Int, val message: String) : ProductoResponse<Nothing>()

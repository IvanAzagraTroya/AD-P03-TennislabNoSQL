package models.pedido

sealed interface PedidoResult<Pedido>

/**
 * @param code código que devolverá al realizar un pedido exitoso
 * @param data valor que contendrá la clase de tipo T
 * Hereda de la clase PedidoResult, PedidoSuccess acepta un pedido o listas de pedidos
 */
class PedidoSuccess<T: Any>(val code: Int, val data: T) : PedidoResult<T>

abstract class PedidoError<Nothing>(val code: Int, open val message: String?) : PedidoResult<Nothing>

/**
 * @param message mensaje de respuesta para el caso de error de pedido no encontrado
 * Hereda de la clase PedidoError con el código 404 y el mensaje pasado por parámetro.
 */
class PedidoErrorNotFound<Nothing>(message: String?) : PedidoError<Nothing>(404, message)

/**
 * @param message mensaje de respuesta para el caso de error de petición errónea
 * Hereda de la clase PedidoError con el código 400 y el mensaje pasado por parámetro.
 */
class PedidoErrorBadRequest<Nothing>(message: String?) : PedidoError<Nothing>(400, message)

/**
 * @param message es el mensaje de respuesta para el caso de error por excepción interna del servidor
 * Hereda de la clase PedidoError con el código 500 y el mensaje pasado por parámetro.
 */
class PedidoInternalException<Nothing>(message: String?) : PedidoError<Nothing>(500, message)
package models.pedido

sealed class PedidoResponse<Pedido>

/**
 * @param code es el código HTTP de respuesta que recibe
 * @param data es el contenido de la respuesta
 */
class PedidoResponseSuccess<T: Any>(val code: Int, val data: T) : PedidoResponse<T>()

/**
 * @param code es el código HTTP de respuesta que recibe en el caso del error
 * @param message es el contenido de la respuesta, al ser un caso de error podría no haber mensaje de respuesta
 */
class UserResponseError(val code: Int, val message: String?) : PedidoResponse<Nothing>()
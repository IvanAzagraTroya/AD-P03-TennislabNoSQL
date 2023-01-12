package models.user

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase sellada es la base de las respuestas de Users,
 * que seran devueltas por el UserController.
 */
sealed class UserResponse<User>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de UserResponse y la usara el UserController para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un User, o un Flow de Users, o una lista de Users)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
class UserResponseSuccess<T: Any>(val code: Int, val data: T) : UserResponse<T>()

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de UserResponse y la usara el UserController para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
class UserResponseError(val code: Int, val message: String?) : UserResponse<Nothing>()
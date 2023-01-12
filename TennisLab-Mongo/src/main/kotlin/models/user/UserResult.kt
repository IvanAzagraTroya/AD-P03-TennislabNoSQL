package models.user

/**
 * @author Daniel Rodriguez Muñoz
 * Esta interfaz sellada es la base de los resultados de Users,
 * que seran devueltos por el UserRepository.
 */
sealed interface UserResult<User>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase implementa UserResult y la usara el UserRepository para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un User, o un Flow de Users)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
class UserSuccess<T: Any>(val code: Int, val data: T) : UserResult<T>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase abstracta implementa UserResult y es la base que usara el UserRepository para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
abstract class UserError<Nothing>(val code: Int, open val message: String?) : UserResult<Nothing>

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de UserError y usara el UserRepository para devolver resultados fallidos cuando no
 * encuentre lo que se le ha pedido, devolviendo el codigo HTTP 404.
 * @param message mensaje de error resultante de la operacion.
 */
class UserErrorNotFound<Nothing>(message: String?) : UserError<Nothing>(404, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de UserError y usara el UserRepository para devolver resultados fallidos cuando
 * la request no sea valida, devolviendo el codigo HTTP 400.
 * @param message mensaje de error resultante de la operacion.
 */
class UserErrorBadRequest<Nothing>(message: String?) : UserError<Nothing>(400, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de UserError y usara el UserRepository para devolver resultados fallidos cuando
 * salte una excepcion inesperada, devolviendo el codigo HTTP 500.
 * @param message mensaje de error resultante de la operacion.
 */
class UserInternalException<Nothing>(message: String?) : UserError<Nothing>(500, message)
package models.maquina

/**
 * @author Daniel Rodriguez Muñoz
 * Esta interfaz sellada es la base de los resultados de Maquinas,
 * que seran devueltos por el MaquinaRepository.
 */
sealed interface MaquinaResult<Maquina>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase implementa MaquinaResult y la usara el MaquinaRepository para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un Maquina, o un Flow de Maquinas)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
class MaquinaSuccess<T: Any>(val code: Int, val data: T) : MaquinaResult<T>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase abstracta implementa MaquinaResult y es la base que usara el MaquinaRepository para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
abstract class MaquinaError<Nothing>(val code: Int, open val message: String?) : MaquinaResult<Nothing>

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de MaquinaError y usara el MaquinaRepository para devolver resultados fallidos cuando no
 * encuentre lo que se le ha pedido, devolviendo el codigo HTTP 404.
 * @param message mensaje de error resultante de la operacion.
 */
class MaquinaErrorNotFound<Nothing>(message: String?) : MaquinaError<Nothing>(404, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de MaquinaError y usara el MaquinaRepository para devolver resultados fallidos cuando
 * la request no sea valida, devolviendo el codigo HTTP 400.
 * @param message mensaje de error resultante de la operacion.
 */
class MaquinaErrorBadRequest<Nothing>(message: String?) : MaquinaError<Nothing>(400, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de MaquinaError y usara el MaquinaRepository para devolver resultados fallidos cuando
 * salte una excepcion inesperada, devolviendo el codigo HTTP 500.
 * @param message mensaje de error resultante de la operacion.
 */
class MaquinaInternalException<Nothing>(message: String?) : MaquinaError<Nothing>(500, message)
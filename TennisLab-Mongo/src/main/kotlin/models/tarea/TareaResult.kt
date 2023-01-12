package models.tarea

/**
 * @author Daniel Rodriguez Muñoz
 * Esta interfaz sellada es la base de los resultados de Tareas,
 * que seran devueltos por el TareaRepository.
 */
sealed interface TareaResult<Tarea>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase implementa TareaResult y la usara el TareaRepository para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un Tarea, o un Flow de Tareas)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
class TareaSuccess<T: Any>(val code: Int, val data: T) : TareaResult<T>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase abstracta implementa TareaResult y es la base que usara el TareaRepository para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
abstract class TareaError<Nothing>(val code: Int, open val message: String?) : TareaResult<Nothing>

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de TareaError y usara el TareaRepository para devolver resultados fallidos cuando no
 * encuentre lo que se le ha pedido, devolviendo el codigo HTTP 404.
 * @param message mensaje de error resultante de la operacion.
 */
class TareaErrorNotFound<Nothing>(message: String?) : TareaError<Nothing>(404, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de TareaError y usara el TareaRepository para devolver resultados fallidos cuando
 * la request no sea valida, devolviendo el codigo HTTP 400.
 * @param message mensaje de error resultante de la operacion.
 */
class TareaErrorBadRequest<Nothing>(message: String?) : TareaError<Nothing>(400, message)

/**
 * @author Daniel Rodriguez Muñoz
 * Clase que hereda de TareaError y usara el TareaRepository para devolver resultados fallidos cuando
 * salte una excepcion inesperada, devolviendo el codigo HTTP 500.
 * @param message mensaje de error resultante de la operacion.
 */
class TareaInternalException<Nothing>(message: String?) : TareaError<Nothing>(500, message)
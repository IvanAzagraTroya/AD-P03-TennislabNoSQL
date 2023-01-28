package models.tarea

import kotlinx.serialization.Serializable

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase sellada es la base de las respuestas de Tareas,
 * que seran devueltas por el TareaController.
 */
@Serializable sealed class TareaResponse<Tarea>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de TareaResponse y la usara el TareaController para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un Tarea, o un Flow de Tareas, o una lista de Tareas)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
@Serializable class TareaResponseSuccess<T: Any>(val code: Int, val data: T) : TareaResponse<T>()

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de TareaResponse y la usara el TareaController para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
@Serializable class TareaResponseError(val code: Int, val message: String?) : TareaResponse<Nothing>()
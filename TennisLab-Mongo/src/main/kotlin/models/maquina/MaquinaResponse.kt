package models.maquina

import kotlinx.serialization.Serializable

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase sellada es la base de las respuestas de Maquinas,
 * que seran devueltas por el MaquinaController.
 */
@Serializable sealed class MaquinaResponse<Maquina>

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de MaquinaResponse y la usara el MaquinaController para devolver
 * resultados exitosos, con el codigo correspondiente y un dato T (donde T podra ser
 * un Maquina, o un Flow de Maquinas, o una lista de Maquinas)
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param data objeto resultante de la operacion.
 */
@Serializable class MaquinaResponseSuccess<T: Any>(val code: Int, val data: T) : MaquinaResponse<T>()

/**
 * @author Daniel Rodriguez Muñoz
 * Esta clase hereda de MaquinaResponse y la usara el MaquinaController para devolver
 * resultados fallidos, con el codigo correspondiente y un mensaje de error, aunque
 * puede no haber mensaje de error, de ahi que sea nullable.
 * @param code codigo HTTP que dice el estado de la operacion.
 * @param message mensaje de error resultante de la operacion.
 */
@Serializable class MaquinaResponseError(val code: Int, val message: String?) : MaquinaResponse<Nothing>()
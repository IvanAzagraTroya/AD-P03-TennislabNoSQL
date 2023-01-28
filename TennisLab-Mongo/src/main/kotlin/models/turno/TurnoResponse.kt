import kotlinx.serialization.Serializable

/**
 * @author Iván Azagra Troya
 * Clase sellada que actúa como base para las respuestas de Turnos,
 * estas serán devueltas desde TurnoController
 */
@Serializable sealed class TurnoResponse<Turno>

/**
 * @param code Código HTTP del estado de la operación, Int
 * @param data Objeto resultante de la operación, T
 * Clase que hereda de TurnoResponse, se utilizará en TurnoController para devolver
 * resultados exitosos con el código de la operación y los datos de la misma, siendo estos
 * T que puede ser un Turno o un Flow de Turnos
 */
@Serializable class TurnoResponseSuccess<T: Any>(val code: Int, val data: T) : TurnoResponse<T>()

/**
 * @param code Código de respuesta HTTP del estado de la operación, Int
 * @param message Mensaje de error resultante dela operación, es nullable String?
 * Clase que hereda de TurnoResponse, se utilizará en TurnoController para devolver
 * resultados fallidos con el código de error de la operación y el mensaje de la misma,
 * puede no haber mensaje de error, por ello es nullable.
 */
@Serializable class TurnoResponseError(val code: Int, val message: String?) : TurnoResponse<Nothing>()

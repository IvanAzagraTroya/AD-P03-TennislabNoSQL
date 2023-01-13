package models.turno

/**
 * @author Iván Azagra Troya
 * Interfaz sellada que actúa como base para los resultados de Turno,
 * son devueltos por el TurnoRepository
 */
sealed interface TurnoResult<Turno>

/**
 * @param code Código HTTP del estado de la operación
 * @param data Objeto resultante de la operación
 * Esta clase implementa TurnoResult y la utilizará TurnoRepository para devolver
 * los resultados exitosos con su código correspondiente y un dato T que podrá ser un
 * Turno o un Flow de Turnos
 */
class TurnoSuccess<T: Any>(val code: Int, val data: T) : TurnoResult<T>

/**
 * @param code Código HTTP del estado de la operación, Int
 * @param message Error resultante de la operación, String?
 * Clase abstracta que implementa TurnoResult y actúa como base que usará la clase TurnoRepository
 * para devolver resultados fallidos con su código correspondiente y el mensaje de error, podría no haber
 * mensaje de error, por ello es nullable
 */
abstract class TurnoError<Nothing>(val code: Int, open val message: String?) : TurnoResult<Nothing>

/**
 * @param message Error resultante de la operación, String?
 * Hereda de TurnoError, es utilizada en la clase TurnoRepository para devolver el error en caso de
 * que no se encuentre el Turno requerido devolviendo consigo el código HTTP 404 junto al mensaje
 * pasado por parámetro
 */
class TurnoErrorNotFound<Nothing>(message: String?) : TurnoError<Nothing>(404, message)

/**
 * @param message Error resultante de la operación, String?
 * Hereda de TurnoError, es utilizada en TurnoRepository para devolver el error en caso de que la
 * petición sea no válida utilizando el código HTTP 400 y el mensaje pasado por parámetro
 */
class TurnoErrorBadRequest<Nothing>(message: String?) : TurnoError<Nothing>(400, message)

/**
 * @param message Error resultante de la operación, String?
 * Hereda de TurnoError, es utilizada en TurnoRepository para devolver el error en caso de haber un
 * fallo inesperado usando el código HTTP 500 junto al mensaje pasado por parámetro.
 */
class TurnoInternalException<Nothing>(message: String?) : TurnoError<Nothing>(500, message)
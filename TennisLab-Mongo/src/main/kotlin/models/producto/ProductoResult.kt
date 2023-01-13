package models.producto

/**
 * @author Iván Azagra Troya
 * Interfaz sellada que actúa como base para los resultados de Producto,
 * son devueltos por el ProductoRepository
 */
sealed interface ProductoResult<Producto>

/**
 * @param code Código HTTP del estado de la operación
 * @param data Objeto resultante de la operación
 * Esta clase implementa ProductoResult y la utilizará ProductoRepository para devolver
 * los resultados exitosos con su código correspondiente y un dato T que podrá ser un
 * Producto o un Flow de Productos
 */
class ProductoSuccess<T: Any>(val code: Int, val data: T) : ProductoResult<T>

/**
 * @param code Código HTTP del estado de la operación, Int
 * @param message Error resultante de la operación, String?
 * Clase abstracta que implementa ProductoResult y actúa como base que usará la clase ProductoRepository
 * para devolver resultados fallidos con su código correspondiente y el mensaje de error, podría no haber
 * mensaje de error, por ello es nullable
 */
abstract class ProductoError<Nothing>(val code: Int, open val message: String?) : ProductoResult<Nothing>

/**
 * @param message Error resultante de la operación, String?
 * Hereda de ProductoError, es utilizada en la clase ProductoRepository para devolver el error en caso de
 * que no se encuentre el producto requerido devolviendo consigo el código HTTP 404 junto al mensaje
 * pasado por parámetro
 */
class ProductoErrorNotFound<Nothing>(message: String?) : ProductoError<Nothing>(404, message)

/**
 * @param message Error resultante de la operación, String?
 * Hereda de ProductoError, es utilizada en ProductoRepository para devolver el error en caso de que la
 * petición sea no válida utilizando el código HTTP 400 y el mensaje pasado por parámetro
 */
class ProductoErrorBadRequest<Nothing>(message: String?) : ProductoError<Nothing>(400, message)

/**
 * @param message Error resultante de la operación, String?
 * Hereda de ProductoError, es utilizada en ProductoRepository para devolver el error en caso de haber un
 * fallo inesperado usando el código HTTP 500 junto al mensaje pasado por parámetro.
 */
class ProductoInternalException<Nothing>(message: String?) : ProductoError<Nothing>(500, message)
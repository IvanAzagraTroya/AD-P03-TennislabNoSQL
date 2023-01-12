package models.producto

sealed interface ProductoResult<Producto>

class ProductoSuccess<T: Any>(val code: Int, val data: T) : ProductoResult<T>

abstract class ProductoError<Nothing>(val code: Int, open val message: String?) : ProductoResult<Nothing>

class ProductoErrorNotFound<Nothing>(message: String?) : ProductoError<Nothing>(404, message)
class ProductoErrorBadRequest<Nothing>(message: String?) : ProductoError<Nothing>(400, message)
class ProductoInternalException<Nothing>(message: String?) : ProductoError<Nothing>(500, message)
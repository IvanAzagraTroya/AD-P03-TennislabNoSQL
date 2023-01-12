package models.tarea

sealed interface TareaResult<Tarea>

class TareaSuccess<T: Any>(val code: Int, val data: T) : TareaResult<T>

abstract class TareaError<Nothing>(val code: Int, open val message: String?) : TareaResult<Nothing>

class TareaErrorNotFound<Nothing>(message: String?) : TareaError<Nothing>(404, message)
class TareaErrorBadRequest<Nothing>(message: String?) : TareaError<Nothing>(400, message)
class TareaInternalException<Nothing>(message: String?) : TareaError<Nothing>(500, message)
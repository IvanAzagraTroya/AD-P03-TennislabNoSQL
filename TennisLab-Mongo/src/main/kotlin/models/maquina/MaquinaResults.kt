package models.maquina

sealed interface MaquinaResult<Maquina>

class MaquinaSuccess<T: Any>(val code: Int, val data: T) : MaquinaResult<T>

abstract class MaquinaError<Nothing>(val code: Int, open val message: String?) : MaquinaResult<Nothing>

class MaquinaErrorNotFound<Nothing>(message: String?) : MaquinaError<Nothing>(404, message)
class MaquinaErrorBadRequest<Nothing>(message: String?) : MaquinaError<Nothing>(400, message)
class MaquinaInternalException<Nothing>(message: String?) : MaquinaError<Nothing>(500, message)
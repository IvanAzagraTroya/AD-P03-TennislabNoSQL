package models.turno

sealed interface TurnoResult<Turno>

class TurnoSuccess<T: Any>(val code: Int, val data: T) : TurnoResult<T>

abstract class TurnoError<Nothing>(val code: Int, open val message: String?) : TurnoResult<Nothing>

class TurnoErrorNotFound<Nothing>(message: String?) : TurnoError<Nothing>(404, message)
class TurnoErrorBadRequest<Nothing>(message: String?) : TurnoError<Nothing>(400, message)
class TurnoInternalException<Nothing>(message: String?) : TurnoError<Nothing>(500, message)
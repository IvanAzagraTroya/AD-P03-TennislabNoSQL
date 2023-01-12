package models.tarea

sealed class TareaResponse<Tarea>

class TareaResponseSuccess<T: Any>(val code: Int, val data: T) : TareaResponse<T>()

class TareaResponseError(val code: Int, val message: String?) : TareaResponse<Nothing>()
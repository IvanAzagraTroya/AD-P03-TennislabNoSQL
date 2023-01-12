package models.maquina

sealed class MaquinaResponse<Maquina>

class MaquinaResponseSuccess<T: Any>(val code: Int, val data: T) : MaquinaResponse<T>()

class MaquinaResponseError(val code: Int, val message: String?) : MaquinaResponse<Nothing>()
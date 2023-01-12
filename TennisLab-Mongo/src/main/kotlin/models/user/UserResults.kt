package models.user

sealed interface UserResult<User>

class UserSuccess<T: Any>(val code: Int, val data: T) : UserResult<T>

abstract class UserError<Nothing>(val code: Int, open val message: String?) : UserResult<Nothing>

class UserErrorNotFound<Nothing>(message: String?) : UserError<Nothing>(404, message)
class UserErrorBadRequest<Nothing>(message: String?) : UserError<Nothing>(400, message)
class UserInternalException<Nothing>(message: String?) : UserError<Nothing>(500, message)
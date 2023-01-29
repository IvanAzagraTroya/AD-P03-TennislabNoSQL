package services.login

import dto.user.UserDTOLogin
import dto.user.UserDTORegister
import dto.user.UserDTOcreate
import dto.user.UserDTOvisualize
import mappers.fromDTO
import mappers.toDTO
import repositories.user.UserRepositoryCached
import services.utils.checkUserEmailAndPhone
import services.utils.fieldsAreIncorrect
import services.utils.matches

suspend fun login(user: UserDTOLogin, repo: UserRepositoryCached): String? {
    val u = repo.findByEmail(user.email) ?: return null
    return if (!matches(user.password, u.password.encodeToByteArray())) null
    else create(u)
}

suspend fun register(user: UserDTORegister, repo: UserRepositoryCached): String? {
    val u = createUserWithoutToken(user.fromDTO(), repo)
    return if (u != null) {
        val res = repo.findByEmail(u.email)
        if (res == null) null
        else create(res)
    }
    else null
}

private suspend fun createUserWithoutToken(user: UserDTOcreate, repo: UserRepositoryCached): UserDTOvisualize? {
    if (fieldsAreIncorrect(user) || checkUserEmailAndPhone(user, repo))
        return null

    val res = repo.save(user.fromDTO())
    return res.toDTO()
}
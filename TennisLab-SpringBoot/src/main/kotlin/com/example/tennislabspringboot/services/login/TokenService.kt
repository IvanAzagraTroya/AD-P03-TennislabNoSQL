package com.example.tennislabspringboot.services.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.tennislabspringboot.models.ResponseError
import com.example.tennislabspringboot.models.user.User
import com.example.tennislabspringboot.models.user.UserProfile
import java.time.Instant
import java.util.*

private val algorithm: Algorithm = Algorithm.HMAC256("Elden Ring game of the year lets goooo")

/**
 * @author Daniel Rodriguez Muñoz
 * Contiene los métodos necesarios para la creación de tokens, comprobación y decodificación para verificarlos
 */
fun create(user: User): String {
    return JWT.create()
        .withClaim("id", user.uuid.toString())
        .withClaim("name", user.nombre)
        .withClaim("surname", user.apellido)
        .withClaim("email", user.email)
        .withClaim("profile", user.perfil.name)
        .withClaim("active", user.activo)
        .withExpiresAt(Date(System.currentTimeMillis() + (24*60*60*1_000)))
        .sign(algorithm)
}

fun decode(token: String): DecodedJWT? {
    val verifier = JWT.require(algorithm).build()

    return try {
        verifier.verify(token)
    } catch (_: Exception) {
        null
    }
}

fun checkToken(token: String, profile: UserProfile): ResponseError? {
    val decoded = decode(token)
        ?: return ResponseError(401, "No token detected.")
    if (decoded.getClaim("profile").isMissing || decoded.getClaim("active").isMissing ||
        decoded.getClaim("profile").isNull || decoded.getClaim("active").isNull ||
        decoded.getClaim("active").asBoolean() == false)
        return ResponseError(401, "Invalid token.")
    if (decoded.expiresAtAsInstant.isBefore(Instant.now()))
        return ResponseError(401, "Token expired.")
    when (profile) {
        UserProfile.ADMIN -> {
            if (!decoded.getClaim("profile").asString().equals(UserProfile.ADMIN.name)) {
                return ResponseError(403, "You are not allowed to to this.")
            }
        }
        UserProfile.WORKER -> {
            if (!(decoded.getClaim("profile").asString().equals(UserProfile.ADMIN.name) ||
                decoded.getClaim("profile").asString().equals(UserProfile.WORKER.name))) {
                return ResponseError(403, "You are not allowed to to this.")
            }
        }
        UserProfile.CLIENT -> {}
    }
    return null
}
package com.example.tennislabspringboot.utils

import java.util.*

fun String.toUUID(): UUID {
    return try {
        UUID.fromString(this.trim())
    }catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("UUID no válido, no está en formato UUID")
    }
}
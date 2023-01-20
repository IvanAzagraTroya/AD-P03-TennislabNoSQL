package db

import dto.user.UserDTOcreate
import models.user.UserProfile
import java.util.*

fun getUsers() = listOf(
    UserDTOcreate(
        UUID.fromString("17071707-loli-1707-loli-loliadmin123"),
        "Daniel", "Rodriguez", "632855327",
        "loli@gmail.com", "quiero galletas", UserProfile.ADMIN,
        true
    ),
    UserDTOcreate(
        UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaaa"),
        "Armando", "Perez", "123456789",
        "prueba@uwu.ita", "1234", UserProfile.CLIENT,
        true
    ),
    UserDTOcreate(
        UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaab"),
        "Trabajador", "SinSueldo", "987654321",
        "prueba2@gmail.com", "1111", UserProfile.WORKER,
        true
    )
)
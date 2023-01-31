package koin.client

import koin.common.Request
import koin.dto.user.UserDTOLogin
import koin.dto.user.UserDTORegister
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import koin.models.Response
import koin.models.ResponseError
import koin.models.ResponseSuccess
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import javax.net.SocketFactory

private const val PORT = 1708
private const val SERVER = "localhost"
private var token: String? = null

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private lateinit var input: DataInputStream
private lateinit var output: DataOutputStream

fun main() = runBlocking {
    val clientFactory = SocketFactory.getDefault() as SocketFactory
    var salir = false
    while (!salir) {
        val socket = clientFactory.createSocket(SERVER, PORT) as Socket

        input = DataInputStream(socket.inputStream)
        output = DataOutputStream(socket.outputStream)

        salir = token?.let { menu(input, output, it) } ?: menuLogin(input, output)
    }
}

fun menuLogin(input: DataInputStream, output: DataOutputStream): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 3) {
        println("""
        Select something to do: 
        
        1. Login
        2. Register
        3. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var email = ""
            while (email.isBlank()) {
                print("email:")
                email = readlnOrNull() ?: ""
            }
            var password = ""
            while (password.isBlank()) {
                print("password:")
                password = readlnOrNull() ?: ""
            }
            val user = json.encodeToString(UserDTOLogin(email, password))
            val request = Request(null, null, user, Request.Type.LOGIN)
            output.writeUTF(json.encodeToString(request))
            val responseJSON = input.readUTF()
            try {
                when (val response = json.decodeFromString<Response<out String>>(responseJSON)) {
                    is ResponseError -> println("CODE ${response.code} - ${response.message}")
                    is ResponseSuccess -> {
                        println("CODE ${response.code} - Token acquired.")
                        token = response.data
                    }
                }
                false
            } catch (e: Exception) {
                println("Server sent an unexpected response type. Closing connection.")
                true
            }
        }
        2 -> {
            var name = ""
            while (name.isBlank()) {
                print("name:")
                name = readlnOrNull() ?: ""
            }
            var apellido = ""
            while (apellido.isBlank()) {
                print("surname:")
                apellido = readlnOrNull() ?: ""
            }
            var telefono = ""
            while (telefono.isBlank()) {
                print("phone:")
                telefono = readlnOrNull() ?: ""
            }
            var email = ""
            while (email.isBlank()) {
                print("email:")
                email = readlnOrNull() ?: ""
            }
            var password = ""
            while (password.isBlank()) {
                print("password:")
                password = readlnOrNull() ?: ""
            }
            var rPassword = ""
            while (rPassword.isBlank()) {
                print("password:")
                rPassword = readlnOrNull() ?: ""
            }
            if (rPassword != password) {
                println("Passwords do not match.")
                return false
            }
            val user = json.encodeToString(
                UserDTORegister(
                    name, apellido, telefono, email, password
                )
            )
            val request = Request(null, null, user, Request.Type.REGISTER)
            output.writeUTF(json.encodeToString(request))
            val responseJSON = input.readUTF()
            try {
                when (val response = json.decodeFromString<Response<out String>>(responseJSON)) {
                    is ResponseError -> println("CODE ${response.code} - ${response.message}")
                    is ResponseSuccess -> {
                        println("CODE ${response.code} - Token acquired.")
                        token = response.data
                    }
                }
                false
            } catch (e: Exception) {
                println("Server sent an unexpected response type. Closing connection.")
                true
            }
        }
        else -> true
    }
}

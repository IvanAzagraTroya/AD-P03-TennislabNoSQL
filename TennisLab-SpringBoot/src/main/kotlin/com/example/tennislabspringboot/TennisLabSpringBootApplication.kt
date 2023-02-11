package com.example.tennislabspringboot

import com.example.tennislabspringboot.controllers.Controller
import com.example.tennislabspringboot.db.*
import com.example.tennislabspringboot.dto.user.UserDTOLogin
import com.example.tennislabspringboot.dto.user.UserDTORegister
import com.example.tennislabspringboot.mappers.fromDTO
import com.example.tennislabspringboot.models.ResponseToken
import com.example.tennislabspringboot.services.login.create
import com.example.tennislabspringboot.services.utils.menu
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

/**
 * @author Daniel Rodriguez Muñoz
 *
 * El main de la aplicacion. Primero instanciamos el controlador con sus dependencias inyectadas gracias al autowired,
 * luego creamos un ObjectMapper para serializar y otro para deserializar, e instanciamos el token a nulo.
 * Cargamos los datos iniciales de la base de datos, lanzamos una corrutina que escuche los cambios en tiempo
 * real de los productos y finalmente lanzamos el menu que navegará el usuario. Si en algun momento el usuario
 * selecciona la opcion de salir, saldrá del bucle while, verá un mensaje de despedida por pantalla y la
 * aplicacion finalizará con un codigo de finalizacion 0.
 */
@SpringBootApplication
class TennisLabSpringBootApplication : CommandLineRunner {
    @Autowired
    lateinit var controller: Controller
    private val json = ObjectMapper()
        .registerModule(JavaTimeModule())
        .writerWithDefaultPrettyPrinter()
    private val reader = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .readerFor(ResponseToken::class.java)
    var token: String? = null

    override fun run(vararg args: String?) = runBlocking {
        val job = launch(coroutineContext) { loadData(controller) }
        job.join()

        val job2 = launch(coroutineContext) {
            controller.findAllProductosAsFlow()
                .onStart { println("Listening for changes in products...") }
                .distinctUntilChanged()
                .collect { println("Productos: ${json.writeValueAsString(it)}") }
        }

        var salir = false
        while (!salir) {
            salir = token?.let { menu(json, it, controller) } ?: menuLogin(controller)
        }
        println("See you later!")
        val x = exitProcess(0) // puesto como "val x = " porque si no el exitProcess lo ponia como return del runBlocking.
    }

    /**
     * @author Daniel Rodriguez Muñoz
     *
     * Este metodo es un menu para loguearse o registrarse. En caso de loguearse/registrarse correctamente, setea el token
     * al resultado que el controller le haya dado (y lo printea por pantalla). De lo contrario printea por pantalla la
     * respuesta del controlador y deja el token a nulo.
     *
     * Devuelve true si el usuario selecciona salir; false si selecciona cualquier otra cosa.
     */
    private suspend fun menuLogin(controller: Controller): Boolean {
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
                val user = UserDTOLogin(email, password)
                val responseJSON = controller.login(user)
                try {
                    val response = reader.readValue<ResponseToken>(responseJSON)
                    if (response.code !in 200..299) println(responseJSON)
                    else {
                        println("Logged in: CODE ${response.code} - Token acquired.")
                        println(responseJSON)
                        token = response.data
                    }
                    false
                } catch (e: Exception) {
                    println(e)
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
                val user = UserDTORegister(name, apellido, telefono, email, password)
                val responseJSON = controller.register(user)
                try {
                    val response = reader.readValue<ResponseToken>(responseJSON)
                    if (response.code !in 200..299) println(responseJSON)
                    else {
                        println("Logged in: CODE ${response.code} - Token acquired.")
                        println(responseJSON)
                        token = response.data
                    }
                    false
                } catch (e: Exception) {
                    println(e)
                    true
                }
            }
            else -> true
        }
    }

}

fun main(args: Array<String>) {
    runApplication<TennisLabSpringBootApplication>(*args)
}

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta funcion inicializa los datos de la base de datos.
 * Primero borra todas las colecciones de la base de dato concurrentemente,
 * luego crea un token de administrador y por ultimo, con ese token,
 * inicializara la base de datos con los distintos datos iniciales de cada coleccion.
 */
private suspend fun loadData(controller: Controller) = withContext(Dispatchers.IO) {
    println("Deleting previous data...")
    val delete = launch { controller.deleteAll() }
    delete.join()
    println("Previous data deleted.")
    println("Loading data...")
    val users = getUsers()
    val admin = users[0]
    val adminToken = create(admin.fromDTO())

    val job1: Job = launch { users.forEach { println(controller.createUser(it, adminToken)) } }
    val job2: Job = launch { getProducts().forEach { println(controller.createProducto(it, adminToken)) } }
    val job3: Job = launch { getMaquinas().forEach { println(controller.createMaquina(it, adminToken)) } }
    joinAll(job1, job2, job3)
    val job4: Job = launch { getTareas().forEach { println(controller.createTarea(it, adminToken)) } }
    job4.join()
    val job5: Job = launch { getPedidos().forEach { println(controller.createPedido(it, adminToken)) } }
    val job6: Job = launch { getTurnos().forEach { println(controller.createTurno(it, adminToken)) } }
    joinAll(job5, job6)
    println("Data loaded.")
}

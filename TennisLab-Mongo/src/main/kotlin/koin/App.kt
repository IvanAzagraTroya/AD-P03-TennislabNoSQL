package koin

import koin.controllers.Controller
import koin.db.*
import koin.dto.maquina.EncordadoraDTOvisualize
import koin.dto.maquina.MaquinaDTOvisualizeList
import koin.dto.maquina.PersonalizadoraDTOvisualize
import koin.dto.pedido.PedidoDTOvisualize
import koin.dto.pedido.PedidoDTOvisualizeList
import koin.dto.producto.ProductoDTOvisualize
import koin.dto.producto.ProductoDTOvisualizeList
import koin.dto.tarea.AdquisicionDTOvisualize
import koin.dto.tarea.EncordadoDTOvisualize
import koin.dto.tarea.PersonalizacionDTOvisualize
import koin.dto.tarea.TareaDTOvisualizeList
import koin.dto.turno.TurnoDTOvisualize
import koin.dto.turno.TurnoDTOvisualizeList
import koin.dto.user.UserDTOLogin
import koin.dto.user.UserDTORegister
import koin.dto.user.UserDTOvisualize
import koin.dto.user.UserDTOvisualizeList
import koin.mappers.fromDTO
import koin.models.ResponseToken
import koin.models.maquina.Maquina
import koin.models.pedido.Pedido
import koin.models.producto.Producto
import koin.models.tarea.Tarea
import koin.models.turno.Turno
import koin.models.user.User
import koin.services.koin.KoinModule
import koin.services.login.create
import koin.services.utils.menu
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import kotlin.system.exitProcess

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true

    /**
     * @author Loli
     * Esto sirve para serializar las Responses. Sin esto, no lo serializa bien.
     * Ha sido sacado de: https://github.com/Kotlin/kotlinx.serialization/issues/1341
     */
    useArrayPolymorphism = true
    encodeDefaults = true
    serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(String::class, String.serializer())
            subclass(UserDTOvisualize::class)
            subclass(UserDTOvisualizeList::class)
            subclass(ProductoDTOvisualize::class)
            subclass(ProductoDTOvisualizeList::class)
            subclass(PedidoDTOvisualize::class)
            subclass(PedidoDTOvisualizeList::class)
            subclass(AdquisicionDTOvisualize::class)
            subclass(EncordadoDTOvisualize::class)
            subclass(PersonalizacionDTOvisualize::class)
            subclass(TareaDTOvisualizeList::class)
            subclass(EncordadoraDTOvisualize::class)
            subclass(PersonalizadoraDTOvisualize::class)
            subclass(MaquinaDTOvisualizeList::class)
            subclass(TurnoDTOvisualize::class)
            subclass(TurnoDTOvisualizeList::class)
            subclass(List::class, ListSerializer(PolymorphicSerializer(Any::class).nullable))
        }
    }
}

private val jsonLoginRegister = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private var token: String? = null

/**
 * @author Daniel Rodriguez Muñoz
 *
 * El main de la aplicacion. Primero iniciamos koin, luego creamos una instancia de la clase Application,
 * cargamos los datos iniciales de la base de datos, lanzamos una corrutina que escuche los cambios en tiempo
 * real de los productos y finalmente lanzamos el menu que navegará el usuario. Si en algun momento el usuario
 * selecciona la opcion de salir, saldrá del bucle while, verá un mensaje de despedida por pantalla y la
 * aplicacion finalizará con un codigo de finalizacion 0.
 */
fun main() = runBlocking {
    startKoin { modules(KoinModule().module) }
    val app = Application()
    loadData(app)
    launch {
        app.controller.findAllProductosAsFlow()
            .onStart { println("Listening for changes in products...") }
            .distinctUntilChanged()
            .collect { println("Productos: ${json.encodeToString(it)}") }
    }

    var salir = false
    while (!salir) {
        salir = token?.let { menu(json, it, app.controller) } ?: menuLogin(app.controller)
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
suspend fun menuLogin(controller: Controller): Boolean {
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
                val response = jsonLoginRegister.decodeFromString<ResponseToken>(responseJSON)
                if (response.code !in 200..299) println(responseJSON)
                else {
                    println("Logged in: CODE ${response.code} - Token acquired.")
                    println(responseJSON)
                    token = response.data
                }
                false
            } catch (e: SerializationException) {
                println(e)
                true
            } catch (e: IllegalArgumentException) {
                println("ILLEGAL ARGUMENT EXCEPTION. ${e.stackTrace}")
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
                val response = jsonLoginRegister.decodeFromString<ResponseToken>(responseJSON)
                if (response.code !in 200..299) println(responseJSON)
                else {
                    println("Logged in: CODE ${response.code} - Token acquired.")
                    println(responseJSON)
                    token = response.data
                }
                false
            } catch (e: SerializationException) {
                println("SERIALIZATION EXCEPTION - Cannot deserialize. ${e.stackTrace}")
                true
            } catch (e: IllegalArgumentException) {
                println("ILLEGAL ARGUMENT EXCEPTION. ${e.stackTrace}")
                true
            }
        }
        else -> true
    }
}

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta clase es para cargar koin e inicializar el controller con todas sus dependencias inyectadas
 * (y dichas dependencias tienen a su vez inyectadas sus respectivas dependencias).
 */
class Application : KoinComponent {
    val controller : Controller by inject()
}

/**
 * @author Daniel Rodriguez Muñoz
 *
 * Esta funcion inicializa los datos de la base de datos.
 * Primero borra todas las colecciones de la base de dato concurrentemente,
 * luego crea un token de administrador y por ultimo, con ese token,
 * inicializara la base de datos con los distintos datos iniciales de cada coleccion.
 */
suspend fun loadData(app: Application) = runBlocking {
    let { DBManager.database.getCollection<User>().drop() }
    let { DBManager.database.getCollection<Producto>().drop() }
    let { DBManager.database.getCollection<Tarea>().drop() }
    let { DBManager.database.getCollection<Pedido>().drop() }
    let { DBManager.database.getCollection<Maquina>().drop() }
    let { DBManager.database.getCollection<Turno>().drop() }
    println("Loading data...")
    val users = getUsers()
    val admin = users[0]
    val adminToken = create(admin.fromDTO())

    val job1: Job = launch { users.forEach { println(app.controller.createUser(it, adminToken)) } }
    val job2: Job = launch { getProducts().forEach { println(app.controller.createProducto(it, adminToken)) } }
    val job3: Job = launch { getMaquinas().forEach { println(app.controller.createMaquina(it, adminToken)) } }
    joinAll(job1, job2, job3)
    val job4: Job = launch { getTareas().forEach { println(app.controller.createTarea(it, adminToken)) } }
    job4.join()
    val job5: Job = launch { getPedidos().forEach { println(app.controller.createPedido(it, adminToken)) } }
    val job6: Job = launch { getTurnos().forEach { println(app.controller.createTurno(it, adminToken)) } }
    joinAll(job5, job6)
    println("Data loaded.")
}
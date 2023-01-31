package server

import common.Request
import controllers.Controller
import db.*
import dto.maquina.MaquinaDTOcreate
import dto.pedido.PedidoDTOcreate
import dto.producto.ProductoDTOcreate
import dto.tarea.TareaDTOcreate
import dto.turno.TurnoDTOcreate
import dto.user.UserDTOLogin
import dto.user.UserDTORegister
import dto.user.UserDTOcreate
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mappers.fromDTO
import models.ResponseError
import models.pedido.PedidoState
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import services.koin.KoinModule
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.net.ServerSocketFactory
import org.koin.ksp.generated.*
import services.login.create
import java.time.LocalDateTime
import java.util.*

private const val PORT = 1708

private val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

private lateinit var input: DataInputStream
private lateinit var output: DataOutputStream

fun main() = runBlocking {
    startKoin { modules(KoinModule().module) }
    val app = Application()
    loadData(app)
    var numClients = 0
    val serverFactory = ServerSocketFactory.getDefault() as ServerSocketFactory
    val serverSocket = serverFactory.createServerSocket(PORT) as ServerSocket

    while (true) {
        val socket = serverSocket.accept()
        numClients++
        println("Attending client $numClients : ${socket.remoteSocketAddress}")

        coroutineScope { processClient(socket, numClients, app) }
    }
}

suspend fun processClient(socket: Socket, numClients: Int, app: Application) {
    input = DataInputStream(socket.inputStream)
    output = DataOutputStream(socket.outputStream)
    println("Attending client $numClients : ${socket.remoteSocketAddress}")

    val requestJson = input.readUTF()
    val request = json.decodeFromString<Request>(requestJson)

    when(request.type) {
        Request.Type.LOGIN -> { sendLogin(output, request, app) }
        Request.Type.REGISTER -> { sendRegister(output, request, app) }
        Request.Type.REQUEST -> { processRequest(output, request, app) }
    }

    output.close()
    input.close()
    socket.close()
}

fun processRequest(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    var response = ""
    if (request.code == null) response = json.encodeToString(ResponseError(400, "BAD REQUEST: no request code attached."))
    else {
        // primer numero: 1=user, 2=producto, 3=maquina, 4=tarea, 5=pedido, 6=turno
        // segundo numero: 1=findAll, 2=findById, 3=save, 4=falsoDelete, 5=delete, 6+=otros
        when (request.code) {
            1_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllUsers()
                else if (request.body.lowercase().toBooleanStrictOrNull() == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 1_1."))
                else app.controller.findAllUsersWithActivity(request.body.toBoolean())
            }
            1_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 1_2."))
                else if (request.body.toIntOrNull() != null)
                    app.controller.findUserById(request.body.toInt())
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findUserById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_2 is not an ID."))
                }
            }
            1_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_3."))
                else try {
                    val entity = json.decodeFromString<UserDTOcreate>(request.body)
                    app.controller.createUser(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_3 is not a UserDTOcreate."))
                }
            }
            1_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setInactiveUser(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_4 is not an ID."))
                }
            }
            1_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 1_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteUser(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 1_5 is not an ID."))
                }
            }

            2_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllProductos()
                else if (!request.body.lowercase().contentEquals("disponibles"))
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 2_1."))
                else app.controller.findAllProductosDisponibles()
            }
            2_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 2_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findProductoById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_2 is not an ID."))
                }
            }
            2_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_3."))
                else try {
                    val entity = json.decodeFromString<ProductoDTOcreate>(request.body)
                    app.controller.createProducto(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_3 is not a ProductoDTOcreate."))
                }
            }
            2_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.decreaseStockFromProducto(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_4 is not an ID."))
                }
            }
            2_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 2_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteProducto(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 2_5 is not an ID."))
                }
            }

            3_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllMaquinas()
                else json.encodeToString(ResponseError(400, "BAD REQUEST: Request 1_1 must not have a body."))
            }
            3_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 3_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findMaquinaById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_2 is not an ID."))
                }
            }
            3_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_3."))
                else try {
                    val entity = json.decodeFromString<MaquinaDTOcreate>(request.body)
                    app.controller.createMaquina(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_3 is not a UserDTOcreate."))
                }
            }
            3_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setInactiveMaquina(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_4 is not an ID."))
                }
            }
            3_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 3_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteMaquina(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 3_5 is not an ID."))
                }
            }

            4_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllTareas()
                else if (request.body.lowercase().toBooleanStrictOrNull() == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 4_1."))
                else app.controller.findAllTareasFinalizadas(request.body.toBoolean())
            }
            4_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 4_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findTareaById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_2 is not an ID."))
                }
            }
            4_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_3."))
                else try {
                    val entity = json.decodeFromString<TareaDTOcreate>(request.body)
                    app.controller.createTarea(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_3 is not a UserDTOcreate."))
                }
            }
            4_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setFinalizadaTarea(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_4 is not an ID."))
                }
            }
            4_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 4_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteTarea(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 4_5 is not an ID."))
                }
            }

            5_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllPedidos()
                else if (!request.body.uppercase().trim().contentEquals(PedidoState.PROCESO.name) &&
                    !request.body.uppercase().trim().contentEquals(PedidoState.TERMINADO.name) &&
                    !request.body.uppercase().trim().contentEquals(PedidoState.RECIBIDO.name))
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Incorrect request body for code 5_1."))
                else app.controller.findAllPedidosWithState(PedidoState.valueOf(request.body.uppercase().trim()))
            }
            5_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 5_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findPedidoById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_2 is not an ID."))
                }
            }
            5_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 5_3."))
                else try {
                    val entity = json.decodeFromString<PedidoDTOcreate>(request.body)
                    app.controller.createPedido(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_3 is not a UserDTOcreate."))
                }
            }
            5_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 5_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deletePedido(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 5_5 is not an ID."))
                }
            }

            6_1 -> {
                response = if (request.body.isNullOrBlank()) app.controller.findAllTurnos()
                else try {
                    val fecha = LocalDateTime.parse(request.body)
                    app.controller.findAllTurnosByFecha(fecha)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_1 is not a LocalDateTime."))
                }
            }
            6_2 -> {
                response = if (request.body.isNullOrBlank())
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body for code 6_2."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.findTurnoById(id)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_2 is not an ID."))
                }
            }
            6_3 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_3."))
                else try {
                    val entity = json.decodeFromString<TurnoDTOcreate>(request.body)
                    app.controller.createTurno(entity, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_3 is not a UserDTOcreate."))
                }
            }
            6_4 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_4."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.setFinalizadoTurno(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_4 is not an ID."))
                }
            }
            6_5 -> {
                response = if (request.body.isNullOrBlank() || request.token == null)
                    json.encodeToString(ResponseError(400, "BAD REQUEST: No body or token for code 6_5."))
                else try {
                    val id = UUID.fromString(request.body)
                    app.controller.deleteTurno(id, request.token)
                } catch (e: Exception) {
                    json.encodeToString(ResponseError(400, "BAD REQUEST: Body for code 6_5 is not an ID."))
                }
            }
        }
    }
    output.writeUTF(response)
}

fun sendLogin(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    val response = if (request.body == null) json.encodeToString(ResponseError(400, "BAD REQUEST: no body attached."))
    else {
        try {
            val body = json.decodeFromString<UserDTOLogin>(request.body)
            app.controller.login(body)
        } catch (e: Exception) {
            json.encodeToString(ResponseError(400, "BAD REQUEST: Invalid body."))
        }
    }
    output.writeUTF(response)
}

suspend fun sendRegister(output: DataOutputStream, request: Request, app: Application) = runBlocking {
    val response = if (request.body == null) json.encodeToString(ResponseError(400, "BAD REQUEST: no body attached."))
    else {
        try {
            val body = json.decodeFromString<UserDTORegister>(request.body)
            app.controller.register(body)
        } catch (e: Exception) {
            json.encodeToString(ResponseError(400, "BAD REQUEST: Invalid body."))
        }
    }
    output.writeUTF(response)
}

class Application : KoinComponent {
    val controller : Controller by inject()
}

suspend fun loadData(app: Application) = runBlocking {
    val users = getUsers()
    val admin = users[0]
    val adminToken = create(admin.fromDTO())

    val job1: Job = launch { users.forEach { app.controller.createUser(it, adminToken) } }
    val job2: Job = launch { getProducts().forEach { app.controller.createProducto(it, adminToken) } }
    val job3: Job = launch { getMaquinas().forEach { app.controller.createMaquina(it, adminToken) } }
    joinAll(job1, job2, job3)
    val job4: Job = launch { getTareas().forEach { app.controller.createTarea(it, adminToken) } }
    job4.join()
    val job5: Job = launch { getPedidos().forEach { app.controller.createPedido(it, adminToken) } }
    val job6: Job = launch { getTurnos().forEach { app.controller.createTurno(it, adminToken) } }
    joinAll(job5, job6)
}
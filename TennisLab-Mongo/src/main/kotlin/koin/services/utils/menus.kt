package koin.services.utils

import koin.controllers.Controller
import koin.dto.maquina.EncordadoraDTOcreate
import koin.dto.pedido.PedidoDTOcreate
import koin.dto.producto.ProductoDTOcreate
import koin.dto.tarea.AdquisicionDTOcreate
import koin.dto.turno.TurnoDTOcreate
import koin.dto.user.UserDTOcreate
import koin.models.ResponseError
import koin.models.pedido.PedidoState
import koin.models.producto.TipoProducto
import koin.models.user.UserProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.*

private val raqueta = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaac"),
    TipoProducto.RAQUETAS, "Marca Inicial",
    "Raqueta inicial", 40.0, 15
)

private val producto = ProductoDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaabb"),
    TipoProducto.FUNDAS, "Marca Inicial",
    "Funda inicial", 16.5, 15
)
private val user = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaaa"),
    "Armando", "Perez", "123456789",
    "prueba@uwu.ita", "1234", UserProfile.CLIENT,
    true
)
private val worker = UserDTOcreate(
    UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaaaab"),
    "Trabajador", "SinSueldo", "987654321",
    "prueba2@gmail.com", "1111", UserProfile.WORKER,
    true
)
private val encordadora = EncordadoraDTOcreate(
    modelo = "EncordadoraFalsa",
    marca =  "MarcaFalsa",
    numeroSerie = "NumeroDeSerieFalso123456",
    activa = false,
    isManual = true,
    maxTension = 69.69,
    minTension = 6.9
)
private val adquis = AdquisicionDTOcreate(
    raqueta = raqueta,
    precio = 69.69,
    pedidoId = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596aaabac"),
    productoAdquirido = producto
)

suspend fun menu(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 7) {
        println("""
        Select something to do: 
        
        1. Users
        2. Productos
        3. Maquinas
        4. Tareas
        5. Pedidos
        6. Turnos
        7. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }

    return when (userInput) {
        1 -> menuUsers(json, token, controller)
        2 -> menuProductos(json, token, controller)
        3 -> menuMaquinas(json, token, controller)
        4 -> menuTareas(json, token, controller)
        5 -> menuPedidos(json, token, controller)
        6 -> menuTurnos(json, token, controller)
        7 -> true
        else -> true
    }
}

suspend fun menuTurnos(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 2) {
                println("""
                        Find only turnos from the past 3 days?:
                        
                        1. no
                        2. Yes
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> controller.findAllTurnos()
                2 -> controller.findAllTurnosByFecha(LocalDateTime.now().minusDays(3L))
                else -> return false
            }
            println(request)
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.findTurnoById(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            val entity = TurnoDTOcreate(
                worker = worker,
                maquina = encordadora,
                tarea1 = adquis,
                tarea2 = null
            )
            println(controller.createTurno(entity, token))
            return false
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.setFinalizadoTurno(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.deleteTurno(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        6 -> true
        else -> true
    }
}

suspend fun menuPedidos(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6 || userInput == 4) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 4) {
                println("""
                        filter by pedido state?:
                        
                        1. no
                        2. Yes (PROCESO)
                        3. Yes (TERMINADO)
                        4. Yes (RECIBIDO)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> controller.findAllPedidos()
                2 -> controller.findAllPedidosWithState(PedidoState.PROCESO)
                3 -> controller.findAllPedidosWithState(PedidoState.TERMINADO)
                4 -> controller.findAllPedidosWithState(PedidoState.RECIBIDO)
                else -> return false
            }
            println(request)
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.findPedidoById(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            println(controller.createPedido(PedidoDTOcreate(user = user), token))
            return false
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.deletePedido(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        6 -> true
        else -> true
    }
}

suspend fun menuTareas(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 3) {
                println("""
                        filter by completed tareas?:
                        
                        1. no
                        2. Yes (show uncompleted tasks)
                        3. Yes (show completed tasks)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> controller.findAllTareas()
                2 -> controller.findAllTareasFinalizadas(false)
                3 -> controller.findAllTareasFinalizadas(true)
                else -> return false
            }
            println(request)
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.findTareaById(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            println(controller.createTarea(adquis, token))
            return false
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.setFinalizadaTarea(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.deleteTarea(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        6 -> true
        else -> true
    }
}

suspend fun menuMaquinas(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            println(controller.findAllMaquinas())
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.findMaquinaById(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            println(controller.createMaquina(encordadora, token))
            return false
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.setInactiveMaquina(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.deleteMaquina(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        6 -> true
        else -> true
    }
}

suspend fun menuProductos(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 2) {
                println("""
                        filter by available products?:
                        
                        1. no
                        2. Yes
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> controller.findAllProductos()
                2 -> controller.findAllProductosDisponibles()
                else -> return false
            }
            println(request)
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.findProductoById(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            val entity = ProductoDTOcreate(
                tipo = TipoProducto.OVERGRIPS,
                marca = "MarcaFalsa",
                modelo = "ProductoFalso",
                precio = 69.69,
                stock = 69
            )
            println(controller.createProducto(entity, token))
            return false
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.decreaseStockFromProducto(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = try {
                val id = UUID.fromString(i)
                controller.deleteProducto(id, token)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        6 -> true
        else -> true
    }
}

suspend fun menuUsers(json: Json, token: String, controller: Controller): Boolean {
    var userInput = 0
    while (userInput < 1 || userInput > 6) {
        println("""
        Select something to do: 
        
        1. Find All
        2. Find by Id
        3. Save
        4. Delete (safe)
        5. Delete (dangerous)
        6. Exit
        """.trimIndent())

        userInput = readln().toIntOrNull() ?: 0
    }
    return when (userInput) {
        1 -> {
            var i = 0
            while (i < 1 || i > 3) {
                println("""
                        filter by active users?:
                        
                        1. no
                        2. Yes (show active users)
                        3. Yes (show inactive users)
                    """.trimIndent())
                i = readln().toIntOrNull() ?: 0
            }
            val request = when (i) {
                1 -> controller.findAllUsers()
                2 -> controller.findAllUsersWithActivity(true)
                3 -> controller.findAllUsersWithActivity(false)
                else -> return false
            }
            println(request)
            return false
        }
        2 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            val request = if (i.toIntOrNull() != null) controller.findUserById(i.toInt())
            else try {
                val id = UUID.fromString(i)
                controller.findUserByUuid(id)
            } catch (e: Exception) {
                json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID."))
            }
            println(request)
            return false
        }
        3 -> {
            val user = UserDTOcreate(
                nombre = "Perico",
                apellido = "Palotes",
                telefono = "999666333",
                email = "pericoPalote@gmail.com",
                password = "me gustan los periquitos"
            )
            println(controller.createUser(user, token))
            return false
        }
        4 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            try {
                val id = UUID.fromString(i)
                println(controller.setInactiveUser(id, token))
                return false
            } catch (e: Exception) {
                println(json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID.")))
                return false
            }
        }
        5 -> {
            var i = ""
            while (i.isBlank()) {
                println("Type the id:")
                i = readln()
            }
            try {
                val id = UUID.fromString(i)
                println(controller.deleteUser(id, token))
                return false
            } catch (e: Exception) {
                println(json.encodeToString(
                    ResponseError(400, "BAD REQUEST: Input is not an ID.")))
                return false
            }
        }
        6 -> true
        else -> true
    }
}
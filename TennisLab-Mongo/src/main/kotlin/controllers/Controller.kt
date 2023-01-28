package controllers

import dto.user.UserDTOcreate
import dto.user.UserDTOvisualize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import mappers.fromDTO
import mappers.toDTO
import models.maquina.Maquina
import models.pedido.Pedido
import models.producto.Producto
import models.tarea.Tarea
import models.turno.Turno
import models.user.*
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.litote.kmongo.Id
import repositories.maquina.IMaquinaRepository
import repositories.pedido.IPedidoRepository
import repositories.producto.IProductoRepository
import repositories.tarea.ITareaRepository
import repositories.turno.ITurnoRepository
import repositories.user.UserRepositoryCached
import java.util.UUID

@Single
class Controller(
    @Named("UserRepositoryCached")
    private val uRepo: UserRepositoryCached,
    @Named("TurnoRepositoryCached")
    private val turRepo: ITurnoRepository<Id<Turno>>,
    @Named("TareaRepositoryCached")
    private val tarRepo: ITareaRepository<Id<Tarea>>,
    @Named("ProductoRepositoryCached")
    private val proRepo: IProductoRepository<Id<Producto>>,
    @Named("PedidoRepositoryCached")
    private val pedRepo: IPedidoRepository<Id<Pedido>>,
    @Named("MaquinaRepositoryCached")
    private val maRepo: IMaquinaRepository<Id<Maquina>>,
) {
    suspend fun findUserById(id: UUID) : UserResponse<out UserDTOvisualize> = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)

        if (user == null) UserResponseError(404, "User with id $id not found.")
        else UserResponseSuccess(200, user.toDTO())
    }

    suspend fun findUserById(id: Int) : UserResponse<out UserDTOvisualize> = withContext(Dispatchers.IO) {
        val user = uRepo.findById(id)

        if (user == null) UserResponseError(404, "User with id $id not found.")
        else UserResponseSuccess(200, user.toDTO())
    }

    suspend fun findAllUsers() : UserResponse<out Flow<UserDTOvisualize>> = withContext(Dispatchers.IO) {
        val users = uRepo.findAll().toList()

        if (users.isEmpty()) UserResponseError(404, "No users found.")
        else UserResponseSuccess(200, toDTO(users).asFlow())
    }

    suspend fun createUser(user: UserDTOcreate) : UserResponse<out UserDTOvisualize> = withContext(Dispatchers.IO) {
        if (user.nombre.isBlank() || !user.email.matches(Regex("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")) ||
            user.apellido.isBlank() || user.telefono.isBlank() || user.password.isBlank())
            return@withContext UserResponseError(400, "Cannot insert user. Incorrect fields.")

        val res = uRepo.save(user.fromDTO())
        UserResponseSuccess(201, res.toDTO())
    }

    suspend fun setInactiveUser(id: UUID) : UserResponse<out UserDTOvisualize> = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)
            ?: return@withContext UserResponseError(404, "Cannot set inactive. User with id $id not found.")
        val result = uRepo.setInactive(user.id)
            ?: return@withContext UserResponseError(500, "Unexpected error. Cannot find and set inactive user with id $id.")
        UserResponseSuccess(200, result.toDTO())
    }

    suspend fun deleteUser(id: UUID) : UserResponse<out UserDTOvisualize> = withContext(Dispatchers.IO) {
        val user = uRepo.findByUUID(id)
            ?: return@withContext UserResponseError(404, "Cannot delete. User with id $id not found.")
        val result = uRepo.delete(user.id)
            ?: return@withContext UserResponseError(500, "Unexpected error. Cannot delete user with id $id.")
        UserResponseSuccess(200, result.toDTO())
    }
}
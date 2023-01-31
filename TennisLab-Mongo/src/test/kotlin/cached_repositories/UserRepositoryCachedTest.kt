package cached_repositories

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import koin.models.pedido.Pedido
import koin.models.pedido.PedidoState
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
import koin.models.producto.Producto
import koin.models.producto.TipoProducto
import koin.models.tarea.Tarea
import koin.models.tarea.TipoTarea
import koin.models.turno.Turno
import koin.models.user.User
import koin.models.user.UserProfile
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.litote.kmongo.newId
import koin.repositories.tarea.TareaRepositoryCached
import koin.repositories.user.UserRepository
import koin.repositories.user.UserRepositoryCached
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class UserRepositoryCachedTest {

    val user = User(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0000-1111-0000-05b596ea83ba"),
        nombre = "loli",
        apellido = "test",
        telefono = "123456789",
        email = "loli@test.com",
        password = "lolitest",
        perfil = UserProfile.ADMIN,
        activo = true
    )

    @MockK
    lateinit var repo: UserRepository

    @SpyK
    var cache = TareaRepositoryCached()

    @InjectMockKs
    lateinit var repository: UserRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(user)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(user, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns user

        val result = repository.findByUUID(user.uuid)

        assertAll(
            { assertEquals(user.uuid, result!!.uuid)},
            { assertEquals(user.nombre, result!!.nombre)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns user

        val result = repository.findById(user.id)

        assertAll(
            { assertEquals(user.id, result!!.id)},
            { assertEquals(user.nombre, result!!.nombre)}
        )
        coVerify {repo.findById(any())}
    }

    @Test
    fun findByIdNotExists() = runTest {
        coEvery{ repo.findById(any())} returns null

        val result = repository.findById(newId())
        assertNull(result)

        coVerify {repo.findById(any())}
    }

    @Test
    fun findByUUIDNotExists() = runTest {
        coEvery{ repo.findByUUID(any())} returns null

        val result = repository.findByUUID(UUID.randomUUID())
        assertNull(result)

        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun save() = runTest {
        coEvery { repo.save(any()) } returns user

        val result = repository.save(user)

        assertAll(
            { assertEquals(user.uuid, result.uuid) },
            { assertEquals(user.nombre, result.nombre) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns user
        coEvery { repo.delete(any()) } returns user

        val result = repository.delete(user.id)!!

        assertAll(
            { assertEquals(user.uuid, result.uuid) },
            { assertEquals(user.nombre, result.nombre) },
        )

        coVerify { repo.delete(any()) }
    }
}
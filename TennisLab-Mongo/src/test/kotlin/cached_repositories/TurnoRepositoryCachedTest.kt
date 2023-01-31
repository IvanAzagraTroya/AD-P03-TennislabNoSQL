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
import koin.repositories.turno.TurnoRepository
import koin.repositories.turno.TurnoRepositoryCached
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class TurnoRepositoryCachedTest {

    val client = User(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0006-48a7-b34f-05b596ea839a"),
        nombre = "Maria",
        apellido = "Martinez",
        telefono = "632120281",
        email = "email2@email.com",
        password = "contra",
        perfil = UserProfile.CLIENT,
        activo = true
    )
    val worker = User(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0007-48a7-b34f-05b596ea839c"),
        nombre = "Luis",
        apellido = "Martinez",
        telefono = "632950281",
        email = "email@email.com",
        password = "estacontraseñanoestaensha512",
        perfil = UserProfile.WORKER,
        activo = true
    )
    val raqueta = Producto(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0001-48a7-b34f-05b596ea83ba"),
        tipo = TipoProducto.RAQUETAS,
        marca = "MarcaRaqueta",
        modelo = "ModeloRaqueta",
        precio = 150.5,
        stock = 3
    )
    val producto1 = Producto(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0002-48a7-b34f-05b596ea83ac"),
        tipo = TipoProducto.ANTIVIBRADORES,
        marca = "MarcaX",
        modelo = "ModeloX",
        precio = 10.5,
        stock = 5
    )
    val personalizadora1 = Maquina(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0008-48a7-b34f-05b596ea83bb"),
        modelo = "RTX-3080TI",
        marca = "Nvidia",
        fechaAdquisicion = LocalDate.parse("2022-11-10"),
        numeroSerie = "123456789X",
        tipo = TipoMaquina.PERSONALIZADORA,
        activa = true,
        isManual = null,
        maxTension = null,
        minTension = null,
        measuresRigidity = false,
        measuresBalance = true,
        measuresManeuverability = true
    )
    val pedido = Pedido(
        id = newId(),
        uuid = UUID.fromString("93a98d69-0010-48a7-b34f-05b596ea8acc"),
        userId = client.uuid,
        state = PedidoState.PROCESO,
        fechaEntrada = LocalDate.parse("2013-10-10"),
        fechaSalida = LocalDate.parse("2023-12-12"),
        topeEntrega = LocalDate.parse("2023-12-14"),
        precio = 0.0
    )
    val adquisicion1 = Tarea(
        id= newId(),
        uuid = UUID.fromString("93a98d69-0011-48a7-b34f-05b596ea83ca"),
        raquetaId = raqueta.uuid,
        precio = raqueta.precio,
        tipo = TipoTarea.ADQUISICION,
        finalizada = false,
        pedidoId = pedido.uuid,
        productoAdquiridoId = raqueta.uuid,
        peso = null,
        balance = null,
        rigidez = null,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null

    )
    val personalizacion = Tarea(
        uuid = UUID.fromString("93a98d69-0015-48a7-b34f-05b596ea8aab"),
        raquetaId = producto1.uuid,
        precio = producto1.precio,
        tipo = TipoTarea.PERSONALIZACION,
        finalizada = false,
        pedidoId = pedido.uuid,
        productoAdquiridoId = producto1.uuid,
        peso = 890,
        balance = 15.4,
        rigidez = 4,
        tensionHorizontal = null,
        cordajeHorizontalId = null,
        tensionVertical = null,
        cordajeVerticalId = null,
        dosNudos = null

    )
    val turno = Turno(
        uuid = UUID.fromString("93a98d69-0019-48a7-b34f-05b596ea8abc"),
        workerId = worker.uuid,
        maquinaId = personalizadora1.uuid,
        horaInicio = LocalDateTime.of(2002, 10, 14, 10, 9),
        horaFin = LocalDateTime.of(2002, 10, 14, 16, 49),
        numPedidosActivos = 2,
        tarea1Id = personalizacion.uuid,
        tarea2Id = adquisicion1.uuid,
        finalizado = false
    )

    @MockK
    lateinit var repo: TurnoRepository

    @SpyK
    var cache = TurnoRepositoryCached()

    @InjectMockKs
    lateinit var repository: TurnoRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(turno)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(turno, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns turno

        val result = repository.findByUUID(turno.uuid)

        assertAll(
            { assertEquals(turno.uuid, result!!.uuid)},
            { assertEquals(turno.horaInicio, result!!.horaInicio)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns turno

        val result = repository.findById(turno.id)

        assertAll(
            { assertEquals(turno.id, result!!.id)},
            { assertEquals(turno.horaInicio, result!!.horaInicio)}
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
        coEvery { repo.save(any()) } returns turno

        val result = repository.save(turno)

        assertAll(
            { assertEquals(turno.uuid, result.uuid) },
            { assertEquals(turno.horaInicio, result.horaInicio) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns turno
        coEvery { repo.delete(any()) } returns turno

        val result = repository.delete(turno.id)!!

        assertAll(
            { assertEquals(turno.uuid, result.uuid) },
            { assertEquals(turno.horaInicio, result.horaInicio) },
        )

        coVerify { repo.delete(any()) }
    }
}
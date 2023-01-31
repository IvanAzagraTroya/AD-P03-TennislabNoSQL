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
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.litote.kmongo.newId
import koin.repositories.maquina.IMaquinaRepository
import koin.repositories.maquina.MaquinaRepository
import koin.repositories.maquina.MaquinaRepositoryCached
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockKExtension::class)
class MaquinaRepositoryCachedTest {
    val maquina = Maquina(
        id = newId(),
        uuid = UUID.fromString("93a98d69-6da6-48a7-b34f-05b596ea83bc"),
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

    @MockK
    lateinit var repo: MaquinaRepository

    @SpyK
    var cache = MaquinaRepositoryCached()

    @InjectMockKs
    lateinit var repository: MaquinaRepositoryCached

    init {
        MockKAnnotations.init(this)
    }

    @Test
    fun findAll() = runTest {
        coEvery { repo.findAll() } returns flowOf(maquina)

        val result = repository.findAll().toList()

        assertAll(
            { assertEquals(maquina, result[0]) }
        )
        coVerify(exactly = 1) { repo.findAll() }
    }

    @Test
    fun findByUUID() = runTest {
        coEvery { repo.findByUUID(any()) } returns maquina

        val result = repository.findByUUID(maquina.uuid)

        assertAll(
            { assertEquals(maquina.uuid, result!!.uuid)},
            { assertEquals(maquina.marca, result!!.marca)}
        )
        coVerify {repo.findByUUID(any())}
    }

    @Test
    fun findById() = runTest {
        coEvery { repo.findById(any()) } returns maquina

        val result = repository.findById(maquina.id)

        assertAll(
            { assertEquals(maquina.id, result!!.id)},
            { assertEquals(maquina.marca, result!!.marca)}
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
        coEvery { repo.save(any()) } returns maquina

        val result = repository.save(maquina)

        assertAll(
            { assertEquals(maquina.uuid, result.uuid) },
            { assertEquals(maquina.marca, result.marca) },
        )

        coVerify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun delete() = runTest {
        coEvery { repo.findById(any()) } returns maquina
        coEvery { repo.delete(any()) } returns maquina

        val result = repository.delete(maquina.id)!!

        assertAll(
            { assertEquals(maquina.uuid, result.uuid) },
            { assertEquals(maquina.marca, result.marca) },
        )

        coVerify { repo.delete(any()) }
    }
}
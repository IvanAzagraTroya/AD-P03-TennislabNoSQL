package repositories

import koin.db.DBManager
import koin.db.readProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import koin.models.maquina.Maquina
import koin.models.maquina.TipoMaquina
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import koin.repositories.maquina.MaquinaRepository
import koin.utils.toUUID
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MaquinaRepositoryTest {
    private val repository = MaquinaRepository()

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
    companion object{
        @JvmStatic
        @BeforeAll
        fun initialize() = runBlocking {
            val properties = readProperties()
            val MONGO_TYPE = DBManager.properties.getProperty("MONGO_TYPE")
            val HOST = DBManager.properties.getProperty("HOST")
            val PORT = DBManager.properties.getProperty("PORT")
            val DATABASE = DBManager.properties.getProperty("DATABASE")
            val USERNAME = DBManager.properties.getProperty("USERNAME")
            val PASSWORD = DBManager.properties.getProperty("PASSWORD")
            val OPTIONS = DBManager.properties.getProperty("OPTIONS")

            val MONGO_URI = "$MONGO_TYPE$USERNAME:$PASSWORD@$HOST/$DATABASE"
        }
    }

    /*@BeforeEach
    fun setUp() {
        dataBaseService.clearDataBaseData()
        dataBaseService.initDataBaseData()
    }

    @AfterAll
    fun tearDown() {
        dataBaseService.clearDataBaseData()
    }*/

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findAll() = runTest {
        val result = repository.findAll().toList()

        assertAll(
            { assertNotNull(result) },
            { assertEquals("93a98d69-6da6-48a7-b34f-05b596ea83bc".toUUID(), result[0].uuid) },
            { assertEquals(0, result.size) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        val result = repository.findById(maquina.id)

        Assertions.assertAll(
            { assertEquals("93a98d69-6da6-48a7-b34f-05b596ea83bc".toUUID(), result?.uuid) },
            { assertEquals("Nvidia", result?.marca) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByIdNotExists() = runTest {
        val result = repository.findById(newId())

        assertNull(result)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findByUuid() = runTest {
        val result = repository.findByUUID("93a98d69-6da6-48a7-b34f-05b596ea83bc".toUUID())

        assertAll(
            { assertNotNull(result) },
            { assertEquals("Nvidia", result?.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        val result = repository.save(maquina)

        assertAll(
            { assertEquals(result.uuid, maquina.uuid) },
            { assertEquals(result.marca, maquina.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete() = runTest {
        val res = repository.save(maquina)
        val result = repository.delete(res.id)

        assertAll(
            { assertEquals(result?.uuid, res.uuid) },
            { assertEquals(result?.marca, res.marca) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNotExists() = runTest {
        val delete = maquina.copy(id = newId())
        val result = repository.delete(delete.id)

        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun decreaseStock() = runTest {
        val result = repository.setInactive(maquina.id)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(result?.activa, maquina.activa)}
        )
    }
}
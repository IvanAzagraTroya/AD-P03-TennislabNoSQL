package repositories

import db.DBManager
import db.readProperties
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import models.user.User
import models.user.UserProfile
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.litote.kmongo.newId
import repositories.user.UserRepository
import utils.toUUID
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {
    private val repository = UserRepository()

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
            { assertEquals("93a98d69-0000-1111-0000-05b596ea83ba".toUUID(), result[0].uuid) },
            { assertEquals(0, result.size) },
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun findById() = runTest {
        val result = repository.findById(user.id)

        Assertions.assertAll(
            { assertEquals("93a98d69-0000-1111-0000-05b596ea83ba".toUUID(), result?.uuid) },
            { assertEquals("loli", result?.nombre) },
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
        val result = repository.findByUUID("93a98d69-0000-1111-0000-05b596ea83ba".toUUID())

        assertAll(
            { assertNotNull(result) },
            { assertEquals("loli", result?.nombre) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun save() = runTest {
        val result = repository.save(user)

        assertAll(
            { assertEquals(result.uuid, user.uuid) },
            { assertEquals(result.nombre, user.nombre) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delete() = runTest {
        val res = repository.save(user)
        val result = repository.delete(res.id)

        assertAll(
            { assertEquals(result?.uuid, res.uuid) },
            { assertEquals(result?.nombre, res.nombre) }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteNotExists() = runTest {
        val delete = user.copy(id = newId())
        val result = repository.delete(delete.id)

        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setInactive() = runTest {
        val result = repository.setInactive(user.id)

        assertAll(
            { assertNotNull(result) },
            { assertEquals(result?.activo, user.activo)}
        )
    }
}
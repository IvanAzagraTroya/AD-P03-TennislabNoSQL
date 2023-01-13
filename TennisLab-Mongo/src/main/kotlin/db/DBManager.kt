package db

import mu.KotlinLogging
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.io.File
import java.io.FileInputStream
import java.util.*

private val logger = KotlinLogging.logger {}
object DBManager {
    private var mongoClient: CoroutineClient
    var database: CoroutineDatabase

    // configuraciones para mongoAtlas aqui
    val properties = readProperties()
    private val MONGO_TYPE = properties.getProperty("MONGO_TYPE")
    private val HOST = properties.getProperty("HOST")
    private val PORT = properties.getProperty("PORT")
    private val DATABASE = properties.getProperty("DATABASE")
    private val USERNAME = properties.getProperty("USERNAME")
    private val PASSWORD = properties.getProperty("PASSWORD")
    private val OPTIONS = properties.getProperty("OPTIONS")

    private val MONGO_URI = "$MONGO_TYPE$USERNAME:$PASSWORD@$HOST/$DATABASE"
    init {
        logger.debug("Initializing connection to MongoDB")
        println("Initializing connection to MongoDB : $MONGO_URI$OPTIONS")
        mongoClient = KMongo.createClient("$MONGO_URI$OPTIONS").coroutine
        database = mongoClient.getDatabase(DATABASE)
    }
}

fun readProperties(): Properties {
    val properties = Properties()
    properties.load(
        FileInputStream("${System.getProperty("user.dir")}${File.separator}" +
            "src${File.separator}main${File.separator}resources${File.separator}config.properties")
    )
    return properties
}
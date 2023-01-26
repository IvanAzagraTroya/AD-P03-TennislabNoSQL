package services.ktorfit

import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.create
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorFitClient {
    private const val URI = "https://jsonplaceholder.typicode.com/"

    private val ktorFit by lazy {
        Ktorfit.Builder().httpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    }
                )
            }
            defaultRequest {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
            .baseUrl(URI)
            .build()
    }

    val instance by lazy {
        ktorFit.create<IKtorFit>()
    }
}
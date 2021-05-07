import io.kotest.property.Arb
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.string
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.bioanalyzer.dtos.BioanalyzerReponseDto

val alphanumericStringGen = Arb.string(10, Arb.alphanumeric())

fun Map<String, BioanalyzerReponseDto>.serialize(): String {
    return Json.encodeToString(MapSerializer(String.serializer(), BioanalyzerReponseDto.serializer()), this)
}

data class BioAnalyzerRoute(
    val url: String,
    val dto: BioanalyzerReponseDto? = null,
    val code: HttpStatusCode = HttpStatusCode.OK,
    val text: String = ""
)

fun mockHttpClient(route: BioAnalyzerRoute): HttpClient {
    return HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
        engine {
            addHandler {
                val reqId = it.headers[HttpHeaders.XRequestId] ?: "noRequestId"
                when (it.url.toString()) {
                    route.url -> {
                        val responseBody = if (route.dto == null) {
                            route.text
                        } else {
                            mapOf(Pair(reqId, route.dto)).serialize()
                        }
                        respond(responseBody, route.code)
                    }
                    else -> respond("Unhandled ${it.url.fullPath}", HttpStatusCode.NotFound)
                }
            }
        }
    }
}

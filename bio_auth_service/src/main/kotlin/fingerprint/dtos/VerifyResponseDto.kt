package fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus

@Serializable
data class VerifyResponseDto(
    val status: ResponseStatus,
    @Deprecated("Prefer id over did")
    val did: String? = null,
    val matchingScore: Double? = null
) {
    val id: String? = did
}

package fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus

@Serializable
data class VerifyResponseDto(
    val status: ResponseStatus,
    val id: String? = null,
    @Deprecated("Prefer id over did")
    val did: String? = null,
    val nationalId: String? = null,
    val matchingScore: Double? = null
)

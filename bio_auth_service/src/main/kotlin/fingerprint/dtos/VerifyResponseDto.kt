package fingerprint.dtos

import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.fingerprint.enums.ResponseStatus

@Serializable
data class VerifyResponseDto(
    val status: ResponseStatus,
    val agentId: String,
    val matchingScore: Double? = null
)

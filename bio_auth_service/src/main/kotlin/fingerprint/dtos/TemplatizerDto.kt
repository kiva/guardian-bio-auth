package fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class TemplatizerDto(
    val did: String,
    val type_id: Int,
    val position: FingerPosition,
    val missing_code: String? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val capture_date: ZonedDateTime,
    val image: String? = null
) {

    @ExperimentalSerializationApi
    fun toSaveRequestDto(): SaveRequestDto {
        return SaveRequestDto(
            did,
            SaveRequestParamsDto(
                type_id,
                capture_date,
                position,
                image,
                missing_code = missing_code
            )
        )
    }
}

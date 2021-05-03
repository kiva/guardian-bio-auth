package fingerprint.dtos

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestFiltersDto
import org.kiva.bioauthservice.fingerprint.dtos.SaveRequestParamsDto
import org.kiva.bioauthservice.fingerprint.enums.FingerPosition
import java.time.ZonedDateTime

@ExperimentalSerializationApi
@Serializable
data class TemplatizerDto(
    val voter_id: String? = null,
    val did: String,
    val national_id: String? = null,
    val type_id: Int,
    val position: FingerPosition,
    val missing_code: String? = null,
    @Serializable(with = ZonedDateTimeSerializer::class) val capture_date: ZonedDateTime,
    val image: String? = null
) {
    override fun toString(): String {
        return "TemplatizerDto(voter_id='$voter_id', did='$did', national_id='$national_id', type_id=$type_id, position=$position, " +
            "missing_code=$missing_code, capture_date=$capture_date, image=$image)"
    }

    override fun hashCode(): Int {
        var result = voter_id?.hashCode() ?: 0
        result = 31 * result + did.hashCode()
        result = 31 * result + (national_id?.hashCode() ?: 0)
        result = 31 * result + type_id
        result = 31 * result + position.hashCode()
        result = 31 * result + (missing_code?.hashCode() ?: 0)
        result = 31 * result + capture_date.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TemplatizerDto

        if (did != other.did) return false
        if (position != other.position) return false

        return true
    }

    @ExperimentalSerializationApi
    fun toSaveRequestDto(): SaveRequestDto {
        return SaveRequestDto(
            did,
            SaveRequestFiltersDto(
                voter_id ?: "",
                national_id ?: ""
            ),
            SaveRequestParamsDto(
                type_id,
                capture_date,
                position,
                image ?: "",
                "",
                0.0,
                missing_code
            )
        )
    }
}

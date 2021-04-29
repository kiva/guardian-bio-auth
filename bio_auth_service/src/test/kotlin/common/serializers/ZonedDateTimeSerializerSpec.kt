package common.serializers

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.kiva.bioauthservice.common.serializers.ZonedDateTimeSerializer
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

@ExperimentalSerializationApi
@Serializable
data class MyDto(
    @Serializable(with = ZonedDateTimeSerializer::class) val myDate: ZonedDateTime
)

@ExperimentalSerializationApi
class ZonedDateTimeSerializerSpec : StringSpec({

    "should be able to convert an ISO-8601 Instant string to a ZonedDateTime" {
        val jsonInput = """
            {
              "myDate": "2011-12-03T10:15:30Z"
            }
        """.trimIndent()
        val dto: MyDto = Json.decodeFromString(jsonInput)
        val zdt: ZonedDateTime = dto.myDate
        zdt.year shouldBeExactly 2011
        zdt.month shouldBe Month.DECEMBER
        zdt.dayOfMonth shouldBeExactly 3
        zdt.zone.normalized() shouldBe ZoneId.of("UTC").normalized()
        zdt.hour shouldBeExactly 10
        zdt.minute shouldBeExactly 15
        zdt.second shouldBeExactly 30
        zdt.offset.totalSeconds shouldBeExactly 0
    }

    "should be able to convert an ISO-8601 string with offset to a ZonedDateTime" {
        val jsonInput = """
            {
              "myDate": "2011-12-03T10:15:30+01:00"
            }
        """.trimIndent()
        val dto: MyDto = Json.decodeFromString(jsonInput)
        val zdt: ZonedDateTime = dto.myDate
        zdt.year shouldBeExactly 2011
        zdt.month shouldBe Month.DECEMBER
        zdt.dayOfMonth shouldBeExactly 3
        zdt.zone.normalized() shouldBe ZoneId.of("UTC+1").normalized()
        zdt.hour shouldBeExactly 10
        zdt.minute shouldBeExactly 15
        zdt.second shouldBeExactly 30
        zdt.offset.totalSeconds shouldBeExactly TimeUnit.HOURS.toSeconds(1).toInt()
    }

    "should be able to convert an ISO-8601 string with offset and zone to a ZonedDateTime" {
        val jsonInput = """
            {
              "myDate": "2011-12-03T10:15:30+01:00[Europe/Paris]"
            }
        """.trimIndent()
        val dto: MyDto = Json.decodeFromString(jsonInput)
        val zdt: ZonedDateTime = dto.myDate
        zdt.year shouldBeExactly 2011
        zdt.month shouldBe Month.DECEMBER
        zdt.dayOfMonth shouldBeExactly 3
        zdt.zone shouldBe ZoneId.of("Europe/Paris")
        zdt.hour shouldBeExactly 10
        zdt.minute shouldBeExactly 15
        zdt.second shouldBeExactly 30
        zdt.offset.totalSeconds shouldBeExactly TimeUnit.HOURS.toSeconds(1).toInt()
    }

    "should fail to convert an ISO-8601 string with no offset and zone to a ZonedDateTime" {
        val jsonInput = """
            {
              "myDate": "2011-12-03T10:15:30"
            }
        """.trimIndent()
        shouldThrow<DateTimeParseException> {
            Json.decodeFromString<MyDto>(jsonInput)
        }
    }
})

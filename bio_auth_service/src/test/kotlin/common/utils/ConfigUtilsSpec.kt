package common.utils

import com.typesafe.config.ConfigFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.ktor.config.HoconApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import org.kiva.bioauthservice.common.utils.getBoolean
import org.kiva.bioauthservice.common.utils.getDouble
import org.kiva.bioauthservice.common.utils.getInt
import org.kiva.bioauthservice.common.utils.getLong
import org.kiva.bioauthservice.common.utils.getString
import java.lang.NumberFormatException

@KtorExperimentalAPI
class ConfigUtilsSpec : WordSpec({

    "getString" should {

        "be able to retrieve a string" {
            val value = "bar"
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = "$value"
                    """
                )
            )
            config.getString("foo") shouldBe value
        }

        "be able to retrieve a string that isn't quoted" {
            val value = "bar"
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """
                )
            )
            config.getString("foo") shouldBe value
        }

        "be able to retrieve a string from a nested path" {
            val value = "baz"
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo {
                      bar = "$value"
                    }
                    """
                )
            )
            config.getString("foo.bar") shouldBe value
        }
    }

    "getInt" should {

        "be able to retrieve a number as a Int" {
            val value = 123
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """
                )
            )
            config.getInt("foo") shouldBe value
        }

        "be able to retrieve a number as a Int from a nested path" {
            val value = 123
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo {
                      bar = "$value"
                    }
                    """
                )
            )
            config.getInt("foo.bar") shouldBe value
        }

        "fail to retrieve a string as a Int" {
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = "bar"
                    """
                )
            )
            shouldThrow<NumberFormatException> {
                config.getInt("foo")
            }
        }
    }

    "getLong" should {

        "be able to retrieve a number as a Long" {
            val value = 123
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """
                )
            )
            config.getLong("foo") shouldBe value
        }

        "be able to retrieve a number as a Long from a nested path" {
            val value = 123
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo {
                      bar = "$value"
                    }
                    """
                )
            )
            config.getLong("foo.bar") shouldBe value
        }

        "fail to retrieve a string as a Long" {
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = "bar"
                    """
                )
            )
            shouldThrow<NumberFormatException> {
                config.getLong("foo")
            }
        }
    }

    "getDouble" should {

        "be able to retrieve an integer value as a Double" {
            val value = 123
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """
                )
            )
            config.getDouble("foo") shouldBe value.toDouble()
        }

        "be able to retrieve a decimal value as a Double" {
            val value = 123.1
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """
                )
            )
            config.getDouble("foo") shouldBe value
        }

        "be able to retrieve a number as Double from a nested path" {
            val value = 123.1
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo {
                      bar = "$value"
                    }
                    """
                )
            )
            config.getDouble("foo.bar") shouldBe value
        }

        "fail to retrieve a string as a Double" {
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = "bar"
                    """
                )
            )
            shouldThrow<NumberFormatException> {
                config.getDouble("foo")
            }
        }
    }

    "getBoolean" should {

        "be able to retrieve a true value" {
            val value = true
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """.trimIndent()
                )
            )
            config.getBoolean("foo") shouldBe value
        }

        "be able to retrieve a false value" {
            val value = false
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """.trimIndent()
                )
            )
            config.getBoolean("foo") shouldBe value
        }

        "be able to retrieve a nested value" {
            val value = true
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo {
                      bar = "$value"
                    }
                    """.trimIndent()
                )
            )
            config.getBoolean("foo.bar") shouldBe value
        }

        "fail to receive a string as a boolean" {
            val value = "baz"
            val config = HoconApplicationConfig(
                ConfigFactory.parseString(
                    """
                    foo = $value
                    """.trimIndent()
                )
            )
            shouldThrow<IllegalArgumentException> {
                config.getBoolean("foo") shouldBe value
            }
        }
    }
})

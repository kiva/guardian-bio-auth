import io.kotest.property.Arb
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.string

val alphanumericStringGen = Arb.string(10, Arb.alphanumeric())

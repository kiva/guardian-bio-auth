package common.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.apache.tika.mime.MediaType
import org.kiva.bioauthservice.common.utils.detectContentType

class ImageUtilsSpec : StringSpec({

    "should be able to detect a .gif" {
        val expectedResult = MediaType.image("gif").toString()
        val imgBytes = this.javaClass.getResource("/images/sample.gif")?.readBytes()
        imgBytes shouldNotBe null
        imgBytes!!.detectContentType() shouldBe expectedResult
    }

    "should be able to detect a .jpeg" {
        val expectedResult = MediaType.image("jpeg").toString()
        val imgBytes = this.javaClass.getResource("/images/sample.jpeg")?.readBytes()
        imgBytes shouldNotBe null
        imgBytes!!.detectContentType() shouldBe expectedResult
    }

    "should be able to detect a .png" {
        val expectedResult = MediaType.image("png").toString()
        val imgBytes = this.javaClass.getResource("/images/sample.png")?.readBytes()
        imgBytes shouldNotBe null
        imgBytes!!.detectContentType() shouldBe expectedResult
    }

    "should detect .wsq as octet" {
        val expectedResult = MediaType.OCTET_STREAM.toString()
        val imgBytes = this.javaClass.getResource("/images/sample.wsq")?.readBytes()
        imgBytes shouldNotBe null
        imgBytes!!.detectContentType() shouldBe expectedResult
    }
})

package common.utils

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.tika.mime.MediaType
import org.kiva.bioauthservice.common.utils.detectContentType

class ImageUtilsSpec : StringSpec({

    "should be able to detect a .gif" {
        val imgBytes = this.javaClass.getResource("/images/sample.gif")?.readBytes()
        imgBytes!!.detectContentType() shouldBe MediaType.image("gif").toString()
    }

    "should be able to detect a .jpeg" {
        val imgBytes = this.javaClass.getResource("/images/sample.jpg")?.readBytes()
        imgBytes!!.detectContentType() shouldBe MediaType.image("jpeg").toString()
    }

    "should be able to detect a .png" {
        val imgBytes = this.javaClass.getResource("/images/sample.png")?.readBytes()
        imgBytes!!.detectContentType() shouldBe MediaType.image("png").toString()
    }

    "should detect .wsq as octet" {
        val imgBytes = this.javaClass.getResource("/images/sample.wsq")?.readBytes()
        imgBytes!!.detectContentType() shouldBe MediaType.OCTET_STREAM.toString()
    }
})

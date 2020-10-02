package org.kiva.bioanalyzerservice.services.analyzers

import net.sf.image4j.codec.bmp.BMPEncoder
import net.sf.image4j.util.ConvertUtil
import org.jnbis.api.Jnbis
import org.kiva.bioanalyzerservice.domain.AnalysisType
import org.kiva.bioanalyzerservice.domain.BioType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.zeroturnaround.exec.ProcessExecutor
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.imageio.ImageIO

/**
 *
 * Check ths image quality and matching performance
 *
 * NFIQ 2.0 is developed for images captured at 500 dpi and as such it shall not be used for images of different
 * resolution, e.g. 1000 dpi. NFIQ 2.0 is developed for plain impression captured using optical sensors or scanned from
 * inked card. Therefore, it shall not be used for images captured using other capture technologies, e.g. capacitive.
 */
@Service
class NFIQ2FingerPrintQualityChecker : BioDataAnalyzer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${bioanalyzerservice.nqis2.ldLibraryPath}")
    lateinit var LD_LIBRARY_PATH: String

    override fun type(): AnalysisType = AnalysisType.QUALITY

    override fun supported(): List<BioType> = listOf(BioType.FINGERPRINT)

    override fun analyze(data: ByteArray, bioType: BioType, format: String?): Mono<Any> {
        if (format!!.split("/")[0] != "image") return Mono.empty()

        // NFIQ2 needs a file to do comparison so we have to temporarily write to file
        // However, let's create tmp file here so we don't have to redo that for retries
        val imageData = format.let { if (it == "image/wsq") Jnbis.wsq().decode(data).toJpg().asByteArray() else data } ?: data

        var tmpFile: File?
        try {
            tmpFile = File.createTempFile("fingerprint-" + UUID.randomUUID().toString(), ".bmp")
        } catch (e: IOException) {
            return Mono.error(e)
        }

        try {
            tmpFile!!.deleteOnExit() // so if we crash we can delete
            ImageIO.write(ImageIO.read(ByteArrayInputStream(imageData)), "BMP", tmpFile)
            BMPEncoder.write(ConvertUtil.convert8(ImageIO.read(ByteArrayInputStream(imageData))), tmpFile)
            return ProcessExecutor()
                    .command(mutableListOf("NFIQ2", "SINGLE", tmpFile.absolutePath, "BMP", "false", "false"))
                    .timeout(5, TimeUnit.SECONDS)
                    .destroyOnExit()
                    .readOutput(true)
                    .execute()
                    .outputUTF8()
                    .let { Mono.just(getScore(it)) }
        } catch (e: Exception) {
            logger.error("NFIQ2 error - ${e.message}", e)
            return Mono.error(e)
        } finally {
            tmpFile.delete()
        }
    }

    private fun getScore(result: String): Float {
        val match = Pattern.compile("(?<=Achieved quality score: ).*").matcher(result)
        if (match.find()) return match.group().toFloat()
        else throw ParseException("Error parsing score from result $result", 0)
    }
}

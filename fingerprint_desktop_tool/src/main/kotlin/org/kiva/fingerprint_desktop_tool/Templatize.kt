package org.kiva.fingerprint_desktop_tool

import com.machinezoo.sourceafis.FingerprintImage
import com.machinezoo.sourceafis.FingerprintTemplate
import picocli.CommandLine
import picocli.CommandLine.*
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess


@Command(
    description = [
        "Converts an image into a fingerprint template.\n " +
            "Currently ony supports sourceAFIS but can easily be extended to support other templating algorithms"],
    name = "templatize",
    mixinStandardHelpOptions = true,
    version = ["templatize 1.0"]
)
class Templatize : Callable<Int> {

    @Parameters(index = "0", description = ["The image file we wish to templatize."])
    private lateinit var file: File
    // private  var file: File = File(Templatize::class.java.getResource("/fingerprint.jpg").toURI())

    @Option(names = ["-o", "--output"], description = ["Sets the output file. Defaults to stdout"])
    private var outputFile: File? = null

    @Option(names = ["-a", "--algorithm"], description = ["Templating algorithm to use. Only sourceAFIS supported"])
    private var algorithm = "sourceAFIS"

    @Throws(Exception::class)
    override fun call(): Int {

       // println("Generating $algorithm template for ${file.toPath()}")
        val fileContents = Files.readAllBytes(file.toPath())
        val template = Base64.getEncoder().encodeToString(
            FingerprintTemplate(FingerprintImage().decode(fileContents)).toByteArray())

        outputFile?.let {
            outputFile!!.writeText(template)
            println("Generated $algorithm template and saved to ${outputFile!!.toPath()}")
        } ?: println(template)

        return 0
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val exitCode = CommandLine(Templatize()).execute(*args)
            exitProcess(exitCode)
        }
    }
}

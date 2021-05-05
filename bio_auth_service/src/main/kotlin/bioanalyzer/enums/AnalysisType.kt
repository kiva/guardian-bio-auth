package org.kiva.bioauthservice.bioanalyzer.enums

/**
 * Categories of analysis we can run on a bio image
 */
enum class AnalysisType constructor(val code: String) {

    /**
     * Provides a quality score of bio data in a range of 0-100
     * complies with the international biometric sample quality standard ISO/IEC 29794-1:2016
     */
    QUALITY("quality"),

    /**
     * Returns the format of the image, e.g image/jpg
     */
    FORMAT("format"),

    /**
     * Retuns the resoltuion of the image, e.g. 500dpi
     */
    RESOLUTION("resolution");

    override fun toString(): String {
        return code
    }

    companion object {
        fun fromCode(value: String?): AnalysisType {
            if (value == null) {
                throw IllegalArgumentException()
            }
            for (position in values()) {
                if (value.equals(position.code, ignoreCase = true)) return position
            }

            throw IllegalArgumentException()
        }
    }
}

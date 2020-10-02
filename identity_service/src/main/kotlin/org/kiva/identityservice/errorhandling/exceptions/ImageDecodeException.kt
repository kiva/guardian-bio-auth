package org.kiva.identityservice.errorhandling.exceptions

/**
 * The exception class thrown when there is an error decoding given image.
 */
open class ImageDecodeException(override val message: String?) : Exception(message)

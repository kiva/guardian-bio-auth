package org.kiva.identityservice.services.backends

import java.io.IOException

/**
 * Flexible way to load backend definitions
 */
interface IBackendLoader {

    @Throws(IOException::class)
    fun load(): List<Definition>
}

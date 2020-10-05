package org.kiva.identityservice.services.backends

import java.io.IOException

/**
 * Flexible way of load definitions of backends
 */
interface IBackendLoader {

    @Throws(IOException::class)
    fun load(): List<Definition>
}

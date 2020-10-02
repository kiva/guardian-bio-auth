package org.kiva.identityservice.services.backends

import alexh.weak.Dynamic

/**
 * Wrapper that takes some of the insanity away from dealing with backend definitions. It also allows to transverse
 * the definitions using dot notification.
 */
data class Definition(
    val backend: String,
    val configMap: Map<String, Any>
) {
    val config: Dynamic = Dynamic.from(configMap)
}

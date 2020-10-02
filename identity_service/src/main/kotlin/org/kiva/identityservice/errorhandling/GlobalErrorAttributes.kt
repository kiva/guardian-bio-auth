package org.kiva.identityservice.errorhandling

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, includeStackTrace: Boolean): Map<String, Any> {
        val map = super.getErrorAttributes(request, includeStackTrace)
        val error = getError(request)
        if (error is ApiException) {
            map["code"] = error.code.name
        }
        return map
    }
}

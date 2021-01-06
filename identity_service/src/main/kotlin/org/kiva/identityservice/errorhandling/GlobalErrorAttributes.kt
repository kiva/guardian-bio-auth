package org.kiva.identityservice.errorhandling

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val map = super.getErrorAttributes(request, options)
        val error = getError(request)
        if (error is ApiException) {
            map["code"] = error.code.name
            map["message"] = error.reason
        }
        return map
    }
}

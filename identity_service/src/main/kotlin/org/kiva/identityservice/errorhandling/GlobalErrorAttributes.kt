package org.kiva.identityservice.errorhandling

import org.kiva.identityservice.errorhandling.exceptions.api.ApiException
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, includeStackTrace: Boolean): Map<String, Any> {
        val map = if (includeStackTrace) {
            super.getErrorAttributes(request, ErrorAttributeOptions.defaults())
        } else {
            super.getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE))
        }
        val error = getError(request)
        if (error is ApiException) {
            map["code"] = error.code.name
        }
        return map
    }
}

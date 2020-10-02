package org.kiva.identityservice.filter

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * The filter class for processing incoming requests.
 */
@Component
class LogFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        try {
            val reqid = exchange.request.headers.getFirst(REQUEST_ID_HEADER)
            MDC.put(REQUEST_ID, reqid)
        } catch (ex: Exception) {
            logger.error("Failure in getting the request id header.", ex)
        }

        return chain.filter(exchange)
    }

    /** The logger class. */
    private val logger = LoggerFactory.getLogger(javaClass)

    /** The request id filed name. */
    private val REQUEST_ID: String = "reqid"

    /** The request id header name. */
    private val REQUEST_ID_HEADER: String = "x-request-id"
}

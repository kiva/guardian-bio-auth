package org.kiva.identityservice.validators

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

/**
 * Used to validate that provide string is a valid backend declared in backend.yml
 */
@MustBeDocumented
@Constraint(validatedBy = [BackendValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Backend(
    val message: String = "Please provide a valid backend.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

package org.kiva.identityservice.validators

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [ContainsKeysValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ContainsKeys(
    val allOf: Array<String> = [],
    val oneOf: Array<String> = [],
    val message: String = "Missing required keys",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

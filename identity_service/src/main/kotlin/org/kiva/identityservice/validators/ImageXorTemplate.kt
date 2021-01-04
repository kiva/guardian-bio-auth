package org.kiva.identityservice.validators

import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [ImageXorTemplateValidator::class])
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ImageXorTemplate(
    val message: String = "Must have value for either image or template",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

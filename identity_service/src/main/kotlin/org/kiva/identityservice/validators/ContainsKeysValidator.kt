package org.kiva.identityservice.validators

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ContainsKeysValidator : ConstraintValidator<ContainsKeys, Map<*, *>> {

    lateinit var allOf: Array<String>
    lateinit var oneOf: Array<String>

    override fun initialize(constraintAnnotation: ContainsKeys?) {
        allOf = constraintAnnotation?.allOf ?: emptyArray()
        oneOf = constraintAnnotation?.oneOf ?: emptyArray()
    }

    override fun isValid(value: Map<*, *>?, context: ConstraintValidatorContext?): Boolean {
        return value?.let { m ->
            val hasAllOf = allOf.isEmpty() || allOf.all { m.containsKey(it) }
            val hasOneOf = oneOf.isEmpty() || oneOf.any { m.containsKey(it) }
            hasAllOf && hasOneOf
        } ?: true // Note: This means the map itself is optional
    }
}

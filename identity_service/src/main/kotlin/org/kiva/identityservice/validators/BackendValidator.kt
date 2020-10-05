package org.kiva.identityservice.validators

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import org.kiva.identityservice.errorhandling.exceptions.InvalidBackendException
import org.kiva.identityservice.services.backends.IBackendManager

class BackendValidator(val backendManager: IBackendManager) : ConstraintValidator<Backend, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.let {
            try {
                backendManager.getbyName(value)
                true
            } catch (e: InvalidBackendException) {
                false
            }
        } ?: true
    }
}

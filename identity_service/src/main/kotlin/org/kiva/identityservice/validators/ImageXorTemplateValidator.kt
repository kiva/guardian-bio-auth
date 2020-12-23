package org.kiva.identityservice.validators

import org.kiva.identityservice.domain.SaveRequestParams
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class ImageXorTemplateValidator : ConstraintValidator<ImageXorTemplate, SaveRequestParams> {
    override fun isValid(value: SaveRequestParams?, context: ConstraintValidatorContext?): Boolean {
        return value?.let {
            val imageValid = it.image.isNotBlank()
            val templateValid = it.template.isNotBlank()
            imageValid xor templateValid
        } ?: true // Note: This means the params object itself is optional
    }
}

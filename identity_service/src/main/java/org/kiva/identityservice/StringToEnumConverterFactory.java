package org.kiva.identityservice;

import org.kiva.identityservice.domain.FingerPosition;
import org.kiva.identityservice.errorhandling.exceptions.api.InvalidFingerPositionException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    public static Class<?> getEnumType(Class<?> targetType) {
        Class<?> enumType = targetType;
        while (enumType != null && !enumType.isEnum()) {
            enumType = enumType.getSuperclass();
        }
        Assert.notNull(enumType, () -> "The target type " + targetType.getName() + " does not refer to an enum");
        return enumType;
    }

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnum(getEnumType(targetType));
    }

    public class StringToEnum<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnum(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source.isEmpty()) {
                // It's an empty enum identifier: reset the enum value to null.
                return null;
            }

            for (final T value : this.enumType.getEnumConstants()) {
                if (value.name().equalsIgnoreCase(source.trim())) {
                    return value;
                }
            }
            // this is our catch all
            try {
                if (this.enumType == FingerPosition.class) {
                    return (T) FingerPosition.fromCode(Integer.valueOf(source.trim()));
                } else {
                    return (T) Enum.valueOf(this.enumType, source.trim().toUpperCase());
                }
            } catch (final IllegalArgumentException e) {
                if (this.enumType == FingerPosition.class) {
                    throw new InvalidFingerPositionException(
                            "Invalid position, must be one of "
                                    + Arrays.stream(FingerPosition.values()).map(i -> i.toString()).collect(Collectors.joining(", ")));
                }
                throw e;
            }

        }
    }

}

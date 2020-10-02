package org.kiva.identityservice

import java.lang.reflect.Field

/**
 * The utility class is used to setting environment variables during test run.
 */
class EnvironmentsTest {
    /**
     * This function sets the environment variable.
     * @param key the env variable name.
     * @param value the env variable value.
     *
     * @throws NoSuchFieldException if there is error.
     * @throws ReflectiveOperationException if there is error.
     */
    @Throws(NoSuchFieldException::class, ReflectiveOperationException::class)
    fun injectEnvironmentVariable(key: String, value: String) {
        val processEnvironment = Class.forName("java.lang.ProcessEnvironment")
        val unmodifiableMapField = getAccessibleField(processEnvironment, "theUnmodifiableEnvironment")
        val unmodifiableMap: Any = unmodifiableMapField.get(null)
        injectIntoUnmodifiableMap(key, value, unmodifiableMap)
        val mapField = getAccessibleField(processEnvironment, "theEnvironment")
        val map = mapField.get(null) as MutableMap<String, String>
        map[key] = value
    }

    /**
     * @param clazz the class name.
     * @param fieldName the fieldName.
     *
     * @throws NoSuchFieldException if there is error.
     */
    @Throws(NoSuchFieldException::class)
    private fun getAccessibleField(clazz: Class<*>, fieldName: String): Field {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }

    /**
     * @param key the key name.
     * @param value the key value.
     * @param map the collection map.
     *
     * @throws NoSuchFieldException if there is error.
     * @throws ReflectiveOperationException if there is error.
     */
    @Throws(ReflectiveOperationException::class)
    private fun injectIntoUnmodifiableMap(key: String, value: String, map: Any) {
        val unmodifiableMap = Class.forName("java.util.Collections\$UnmodifiableMap")
        val field = getAccessibleField(unmodifiableMap, "m")
        val obj: Any = field.get(map)
        (obj as MutableMap<String?, String?>)[key] = value
    }
}

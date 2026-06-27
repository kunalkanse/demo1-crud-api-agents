package com.ecommerce.product.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON serialization and deserialization.
 * Provides methods to convert between JSON strings and POJO objects.
 */
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Deserialize a JSON string to a POJO object.
     *
     * @param jsonString the JSON string to deserialize
     * @param clazz      the target class type
     * @param <T>        the generic type parameter
     * @return the deserialized object
     * @throws RuntimeException if deserialization fails
     */
    public static <T> T toPojo(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Serialize a POJO object to a JSON string.
     *
     * @param object the object to serialize
     * @return the JSON string representation
     * @throws RuntimeException if serialization fails
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Check if a string is valid JSON.
     *
     * @param jsonString the string to validate
     * @return true if the string is valid JSON, false otherwise
     */
    public static boolean isValidJson(String jsonString) {
        try {
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}

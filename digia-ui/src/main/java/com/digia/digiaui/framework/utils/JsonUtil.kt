package com.digia.digiaui.framework.utils

import com.digia.digiaui.framework.logging.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

class JsonUtil {
    companion object {
        private val jsonParser = Json { ignoreUnknownKeys = true }

        /**
         * Attempts to decode a JSON string, returning null if decoding fails.
         *
         * This function provides a safe way to decode JSON without throwing exceptions.
         *
         * [source] The JSON string to decode. Returns the decoded JSON object, or null if decoding
         * fails.
         */
        fun tryJsonDecode(source: String): Any? {
            return try {
                val element = jsonParser.parseToJsonElement(source)
                jsonElementToAny(element)
            } catch (e: Exception) {
                Logger.error("JSON decode error: $e", tag = "JsonUtil", error = e)
                null
            }
        }

        /**
         * Attempts to retrieve a value from a JSON object using multiple possible keys.
         *
         * [json] The JSON object to search in. [keys] An ordered list of keys to try. [parse]
         * Optional function to cast or transform the value if found.
         *
         * Returns the value associated with the first matching key, or null if no key is found.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> tryKeys(json: JsonLike, keys: List<String>, parse: ((Any?) -> T?)? = null): T? {
            for (key in keys) {
                if (json.containsKey(key)) {
                    val value = json[key]
                    return parse?.invoke(value) ?: value as? T
                }
            }
            return null
        }

        /**
         * Converts a JsonElement to a Kotlin Any? type, preserving type information.
         *
         * Handles primitives (Boolean, Int, Long, Float, Double, String), arrays (List), and
         * objects (Map). This is a recursive function that processes nested structures.
         */
        fun jsonElementToAny(element: JsonElement): Any? {
            return when (element) {
                is JsonPrimitive -> {
                    element.booleanOrNull
                            ?: element.intOrNull ?: element.longOrNull ?: element.floatOrNull
                                    ?: element.doubleOrNull ?: element.contentOrNull
                }
                is JsonArray -> element.jsonArray.map { jsonElementToAny(it) }
                is JsonObject -> element.jsonObject.mapValues { jsonElementToAny(it.value) }
            }
        }

        /**
         * Converts a JsonElement to a Map<String, Any>. Returns empty map if the element is not a
         * JsonObject.
         */
        fun jsonElementToMap(element: JsonElement): Map<String, Any> {
            return when (element) {
                is JsonObject -> element.jsonObject.mapValues { jsonElementToAny(it.value) as Any }
                else -> emptyMap()
            }
        }
    }
}

/**
 * Retrieves the value for a given key path in a nested map.
 *
 * The [keyPath] parameter is a dot-separated string representing the path to the desired value. For
 * example, 'a.b.c' will retrieve the value at map['a']['b']['c'].
 */
fun JsonLike.valueFor(keyPath: String): Any? {
    val keysSplit = keyPath.split('.').toMutableList()
    val thisKey = keysSplit.removeAt(0)
    val thisValue = this[thisKey]

    return if (keysSplit.isEmpty()) {
        thisValue
    } else if (thisValue is Map<*, *>) {
        @Suppress("UNCHECKED_CAST") val nested = thisValue as JsonLike
        nested.valueFor(keysSplit.joinToString("."))
    } else {
        null
    }
}

/**
 * Sets the value for a given key path in a nested map.
 *
 * The [keyPath] parameter is a dot-separated string representing the path to the desired value. For
 * example, 'a.b.c' will set the value at map['a']['b']['c']. If intermediate maps do not exist,
 * they will be created.
 */
// fun JsonLike.setValueFor(keyPath: String, value: Any?) {
//    val keysSplit = keyPath.split('.').toMutableList()
//    val thisKey = keysSplit.removeAt(0)
//
//    if (keysSplit.isEmpty()) {
//        this[thisKey] = value
//        return
//    }
//
//    if (this[thisKey] !is Map<*, *>) {
//        this[thisKey] = mutableMapOf<String, Any?>()
//    }
//
//    (this[thisKey] as JsonLike).setValueFor(keysSplit.joinToString("."), value)
// }

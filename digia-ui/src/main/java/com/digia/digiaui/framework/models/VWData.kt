package com.digia.digiaui.framework.models

/**
 * Virtual Widget Data - sealed class representing different types of widget nodes Mirrors the
 * Flutter VWData structure
 */
sealed class VWData {
    abstract val refName: String?

    companion object {
        fun fromJson(json: JsonLike): VWData {
            val nodeType = json["nodeType"] as? String ?: json["category"] as? String ?: "widget"

            return when (nodeType) {
                "widget" -> VWNodeData.fromJson(json)
                "component" -> VWComponentData.fromJson(json)
                "state" -> VWStateData.fromJson(json)
                else -> VWNodeData.fromJson(json)
            }
        }
    }
}

/** Regular widget node (Text, Button, Container, etc.) */
data class VWNodeData(
        override val refName: String? = null,
        val type: String, // Widget type (e.g., "digia/text")
        val props: JsonLike = emptyMap(), // Widget-specific properties
        val commonProps: CommonProps? = null, // Common properties
        val parentProps: JsonLike? = null, // Props for parent container
        val childGroups: Map<String, List<VWData>>? = null // Child widgets
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWNodeData {
            return VWNodeData(
                    refName = json["refName"] as? String,
                    type = json["type"] as? String ?: "",
                    props = json["props"] as? JsonLike ?: emptyMap(),
                    commonProps = CommonProps.fromJson(json["commonProps"] as? JsonLike),
                    parentProps = json["parentProps"] as? JsonLike,
                    childGroups = parseChildGroups(json["childGroups"])
            )
        }

        internal fun parseChildGroups(value: Any?): Map<String, List<VWData>>? {
            if (value !is Map<*, *>) return null

            return value.entries.associate { (key, children) ->
                val childList =
                        when (children) {
                            is List<*> ->
                                    children.mapNotNull { (it as? JsonLike)?.let { fromJson(it) } }
                            else -> emptyList()
                        }
                (key as String) to childList
            }
        }
    }
}

/** Component reference node */
data class VWComponentData(
        override val refName: String? = null,
        val id: String, // Component ID
        val args: Map<String, ExprOr<Any>?>? = null, // Arguments to pass
        val commonProps: CommonProps? = null,
        val parentProps: JsonLike? = null
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWComponentData {
            return VWComponentData(
                    refName = json["refName"] as? String,
                    id = json["id"] as? String ?: "",
                    args =
                            (json["args"] as? JsonLike)?.mapValues {
                                ExprOr.fromValue<Any>(it.value)
                            },
                    commonProps = CommonProps.fromJson(json["commonProps"] as? JsonLike),
                    parentProps = json["parentProps"] as? JsonLike
            )
        }
    }
}

/** State container node */
data class VWStateData(
        override val refName: String? = null,
        val initStateDefs: Map<String, Variable>, // State variables
        val childGroups: Map<String, List<VWData>>? = null,
        val parentProps: JsonLike? = null
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWStateData {
            return VWStateData(
                    refName = json["refName"] as? String,
                    initStateDefs = parseVariables(json["initStateDefs"]),
                    childGroups = VWNodeData.parseChildGroups(json["childGroups"]),
                    parentProps = json["parentProps"] as? JsonLike
            )
        }

        private fun parseVariables(value: Any?): Map<String, Variable> {
            if (value !is Map<*, *>) return emptyMap()
            return value.entries.associate { (key, varDef) ->
                (key as String) to Variable.fromJson(varDef as? JsonLike ?: emptyMap())
            }
        }
    }
}

/** Variable definition for state and arguments */
data class Variable(val name: String, val type: String, val defaultValue: Any? = null) {
    companion object {
        fun fromJson(json: JsonLike): Variable {
            return Variable(
                    name = json["name"] as? String ?: "",
                    type = json["type"] as? String ?: "any",
                    defaultValue = json["defaultValue"]
            )
        }
    }
}

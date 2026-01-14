package com.digia.digiaui.framework.models
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.JsonUtil.Companion.tryKeys

enum class NodeType {
    widget,
    state,
    component;

    companion object {
        fun fromString(value: String): NodeType? {
            return values().firstOrNull { it.name == value }
        }
    }
}

/**
 * Virtual Widget Data - sealed class representing different types of widget nodes Mirrors the
 * Flutter VWData structure
 */
sealed class VWData {
    abstract val refName: String?

    companion object {
        fun fromJson(json: JsonLike): VWData {
            val nodeType = NodeType.fromString(tryKeys(json,listOf("category","nodeType"))?: "widget") ?: NodeType.widget

            return when (nodeType) {
                NodeType.widget -> VWNodeData.fromJson(json)
                NodeType.component -> VWComponentData.fromJson(json)
                NodeType.state -> VWStateData.fromJson(json)
            }
        }
    }
}

/** Regular widget node (Text, Button, Container, etc.) */
data class VWNodeData(
    override val refName: String? = null,
    val type: String, // Widget type (e.g., "digia/text")
    val props: Props = Props.empty(), // Widget-specific properties
    val commonProps: CommonProps? = null, // Common properties
    val parentProps: Props? = null, // Props for parent container
    val childGroups: Map<String, List<VWData>>? = null // Child widgets
//    val repeatData: VWRepeatData? = null
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWNodeData {
            return VWNodeData(
                    refName = tryKeys<String>(json, listOf("varName", "refName")),
                    type = json["type"] as? String ?: "",
                    props  = (json["props"] as? JsonLike)?.let(::Props) ?: Props.empty(),
                    commonProps  = (json["containerProps"] as? JsonLike)?.let {
                        CommonProps.fromJson(it)
                    },
                    parentProps  = (json["parentProps"] as? JsonLike)?.let(::Props) ?: Props.empty(),
                childGroups = tryKeys(json, listOf("childGroups", "children"), VWNodeData::parseChildGroups),
            )
        }

        internal fun parseChildGroups(value: Any?): Map<String, List<VWData>>? {
            if (value !is Map<*, *>) return null

            return value.entries.associate { (key, children) ->
                val childList =
                        when (children) {
                            is List<*> ->
                                    children.mapNotNull { (it as? JsonLike)?.let { VWData.fromJson(it) } }
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
        val parentProps: Props? = null
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWComponentData {
            return VWComponentData(
                    refName = tryKeys(json, listOf("varName", "refName")),
                    id = json["componentId"] as? String ?: "",
                    args =
                            (json["componentArgs"] as? JsonLike)?.mapValues {
                                ExprOr.fromValue<Any>(it.value)
                            },
                    commonProps = CommonProps.fromJson(json["containerProps"] as? JsonLike),
                    parentProps = (json["parentProps"] as? JsonLike)?.let(::Props) ?: Props.empty()
            )
        }
    }
}

/** State container node */
data class VWStateData(
        override val refName: String? = null,
        val initStateDefs: Map<String, Variable>, // State variables
        val childGroups: Map<String, List<VWData>>? = null,
        val parentProps: Props? = null
) : VWData() {
    companion object {
        fun fromJson(json: JsonLike): VWStateData {
            return VWStateData(
                    refName = tryKeys(json, listOf("varName", "refName")),
                    initStateDefs = parseVariables(json["initStateDefs"]),
                    childGroups = VWNodeData.parseChildGroups(tryKeys(json, listOf("childGroups", "children","composites"))),
                    parentProps = (json["parentProps"] as? JsonLike)?.let(::Props) ?: Props.empty( )
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
        fun fromJson(json: Map<String, Any?>): Variable {
            return Variable(
                    name = json["name"] as? String ?: "",
                    type = json["type"] as? String ?: "any",
                    defaultValue = tryKeys(json, listOf("defaultValue", "default"))
            )
        }
    }
}

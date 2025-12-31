package com.digia.digiaui.framework.models

/**
 * Component definition - reusable UI component configuration Mirrors Flutter DUIComponentDefinition
 */
data class ComponentDefinition(
        val id: String,
        val argDefs: Map<String, Variable>? = null, // Component arguments
        val initStateDefs: Map<String, Variable>? = null, // Initial state
        val layout: ComponentLayout? = null // Widget tree
) {
    companion object {
        fun fromJson(json: JsonLike): ComponentDefinition {
            return ComponentDefinition(
                    id = json["id"] as? String
                                    ?: json["uid"] as? String ?: json["componentId"] as? String
                                            ?: "",
                    argDefs = parseVariables(json["argDefs"]),
                    initStateDefs = parseVariables(json["initStateDefs"]),
                    layout = (json["layout"] as? JsonLike)?.let { ComponentLayout.fromJson(it) }
            )
        }

        private fun parseVariables(value: Any?): Map<String, Variable>? {
            if (value !is Map<*, *>) return null
            return value.entries.associate { (key, varDef) ->
                (key as String) to Variable.fromJson(varDef as? JsonLike ?: emptyMap())
            }
        }
    }
}

/** Component layout wrapper */
data class ComponentLayout(val root: VWData?) {
    companion object {
        fun fromJson(json: JsonLike): ComponentLayout {
            return ComponentLayout(root = (json["root"] as? JsonLike)?.let { VWData.fromJson(it) })
        }
    }
}

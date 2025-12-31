package com.digia.digiaui.framework.models

import com.digia.digiaui.framework.actions.base.ActionFlow

/** Page definition - complete configuration for a page Mirrors Flutter DUIPageDefinition */
data class PageDefinition(
        val pageId: String,
        val pageArgDefs: Map<String, Variable>? = null, // Input argument definitions
        val initStateDefs: Map<String, Variable>? = null, // Initial state variables
        val layout: PageLayout? = null, // Widget tree layout
        val onPageLoad: ActionFlow? = null, // Actions to run on load
        val onBackPress: ActionFlow? = null // Actions on back button
) {
    companion object {
        fun fromJson(json: JsonLike): PageDefinition {
            return PageDefinition(
                    pageId = json["pageId"] as? String
                                    ?: json["uid"] as? String ?: json["pageUid"] as? String ?: "",
                    pageArgDefs =
                            parseVariables(
                                    json["pageArgDefs"] ?: json["inputArgs"] ?: json["argDefs"]
                            ),
                    initStateDefs = parseVariables(json["initStateDefs"] ?: json["variables"]),
                    layout = (json["layout"] as? JsonLike)?.let { PageLayout.fromJson(it) },
                    onPageLoad =
                            ((json["actions"] as? JsonLike)?.get("onPageLoadAction") as? JsonLike)
                                    ?.let { ActionFlow.fromJson(it) },
                    onBackPress =
                            ((json["actions"] as? JsonLike)?.get("onBackPress") as? JsonLike)?.let {
                                ActionFlow.fromJson(it)
                            }
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

/** Page layout wrapper */
data class PageLayout(val root: VWData?) {
    companion object {
        fun fromJson(json: JsonLike): PageLayout {
            return PageLayout(root = (json["root"] as? JsonLike)?.let { VWData.fromJson(it) })
        }
    }
}

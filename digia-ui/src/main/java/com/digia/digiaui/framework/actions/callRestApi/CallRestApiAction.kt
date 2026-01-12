//package com.digia.digiaui.framework.actions.callRestApi
//
//import LocalApiModels
//import android.content.Context
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.rememberCoroutineScope
//import com.digia.digiaui.framework.actions.base.Action
//import com.digia.digiaui.framework.actions.base.ActionFlow
//import com.digia.digiaui.framework.actions.base.ActionId
//import com.digia.digiaui.framework.actions.base.ActionProcessor
//import com.digia.digiaui.framework.actions.base.ActionType
//import com.digia.digiaui.framework.expr.ScopeContext
//import com.digia.digiaui.framework.models.ExprOr
//import com.digia.digiaui.framework.utils.JsonLike
//import com.digia.digiaui.init.DigiaUIManager
//import com.digia.digiaui.network.BodyType
//import com.digia.digiaui.network.HttpMethod
//import com.google.gson.Gson
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
///**
// * CallRestApi Action
// *
// * Makes a REST API call using the configured APIModel.
// * Supports dynamic parameter evaluation and state updates with response data.
// *
// * @param apiModelId The ID of the APIModel to use for the request
// * @param stateVarName Optional state variable name to store the response
// * @param onSuccess Optional action flow to execute on successful response
// * @param onError Optional action flow to execute on error
// */
//data class CallRestApiAction(
//    override var actionId: ActionId? = null,
//    override var disableActionIf: ExprOr<Boolean>? = null,
//    val apiModelId: ExprOr<String>?,
//    val stateVarName: ExprOr<String>? = null,
//    val onSuccess: ActionFlow? = null,
//    val onError: ActionFlow? = null
//) : Action {
//    override val actionType = ActionType.CALL_REST_API
//
//    override fun toJson(): JsonLike =
//        mapOf(
//            "type" to actionType.value,
//            "apiModelId" to apiModelId?.toJson(),
//            "stateVarName" to stateVarName?.toJson(),
//            "onSuccess" to onSuccess?.toJson(),
//            "onError" to onError?.toJson()
//        )
//
//    companion object {
//        fun fromJson(json: JsonLike): CallRestApiAction {
//            return CallRestApiAction(
//                apiModelId = ExprOr.fromValue(json["apiModelId"]),
//                stateVarName = ExprOr.fromValue(json["stateVarName"]),
//                onSuccess = json["onSuccess"]?.let { ActionFlow.fromJson(it as? JsonLike) },
//                onError = json["onError"]?.let { ActionFlow.fromJson(it as? JsonLike) }
//            )
//        }
//    }
//}
//
///** Processor for call REST API action */
//class CallRestApiProcessor : ActionProcessor<CallRestApiAction>() {
//    override fun execute(
//        context: Context,
//        action: CallRestApiAction,
//        scopeContext: ScopeContext?,
//        stateContext: com.digia.digiaui.framework.state.StateContext?,
//        id: String
//    ) {
//        // Evaluate apiModelId
//        val apiModelId = action.apiModelId?.evaluate(scopeContext) ?: ""
//        if (apiModelId.isEmpty()) {
//            println("CallRestApiAction: apiModelId is empty")
//            return
//        }
//
//        // Evaluate stateVarName
//        val stateVarName = action.stateVarName?.evaluate<String>(scopeContext)
//
//        // Get the NetworkClient from DigiaUIManager
//        val networkClient = DigiaUIManager.getInstance().networkClient
//
//        // Get the APIModel from DigiaUIManager config
//        val apiModel = DigiaUIManager.getInstance().config.getApiDataSource(apiModelId)
//        if (apiModel == null) {
//            println("CallRestApiAction: APIModel not found for ID: $apiModelId")
//            // Execute onError if provided
//            action.onError?.let { errorFlow ->
//                // TODO: Execute error flow with error context
//            }
//            return
//        }
//
//        println("CallRestApiAction: Making API call with model ID: $apiModelId")
//
//        // Launch coroutine for network call
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Evaluate variables from scope context
//                val evaluatedUrl = evaluateUrlWithVariables(apiModel.url, scopeContext)
//                val evaluatedHeaders = apiModel.headers?.mapValues { (_, value) ->
//                    evaluateValue(value, scopeContext).toString()
//                } ?: emptyMap()
//
//                // Prepare request body
//                val requestBody = apiModel.body?.let { body ->
//                    val evaluatedBody = body.mapValues { (_, value) ->
//                        evaluateValue(value, scopeContext)
//                    }
//                    Gson().toJson(evaluatedBody)
//                }
//
//                // Make the API call
//                val response = networkClient.requestProject(
//                    bodyType = apiModel.bodyType ?: BodyType.JSON,
//                    url = evaluatedUrl,
//                    method = apiModel.method,
//                    additionalHeaders = evaluatedHeaders,
//                    data = requestBody,
//                    apiName = apiModel.name
//                )
//
//                // Parse response
//                val responseBody = response.body?.string()
//                val responseData = if (responseBody != null) {
//                    try {
//                        Gson().fromJson(responseBody, Map::class.java) as? Map<String, Any?>
//                    } catch (e: Exception) {
//                        mapOf("data" to responseBody, "statusCode" to response.code)
//                    }
//                } else {
//                    mapOf("statusCode" to response.code)
//                }
//
//                // Store response in state if stateVarName is provided
//                withContext(Dispatchers.Main) {
//                    if (stateVarName != null && responseData != null) {
//                        stateContext?.setState(stateVarName, responseData)
//                    }
//
//                    // Execute onSuccess action flow if provided
//                    if (response.isSuccessful) {
//                        action.onSuccess?.let { successFlow ->
//                            // TODO: Execute success flow with response context
//                            // This would require ActionExecutor to be available
//                            println("CallRestApiAction: Success - would execute onSuccess flow")
//                        }
//                    } else {
//                        action.onError?.let { errorFlow ->
//                            // TODO: Execute error flow with error context
//                            println("CallRestApiAction: HTTP error ${response.code} - would execute onError flow")
//                        }
//                    }
//                }
//
//            } catch (e: Exception) {
//                println("CallRestApiAction: API call failed: ${e.message}")
//                withContext(Dispatchers.Main) {
//                    action.onError?.let { errorFlow ->
//                        // TODO: Execute error flow with exception context
//                        println("CallRestApiAction: Exception - would execute onError flow")
//                    }
//                }
//            }
//        }
//    }
//
//    private fun evaluateUrlWithVariables(url: String, scopeContext: ScopeContext?): String {
//        var evaluatedUrl = url
//        // Replace {variable} placeholders with actual values from scopeContext
//        val regex = "\\{([^}]+)\\}".toRegex()
//        regex.findAll(url).forEach { matchResult ->
//            val variableName = matchResult.groupValues[1]
//            val value = scopeContext?.get(variableName)?.toString() ?: ""
//            evaluatedUrl = evaluatedUrl.replace("{$variableName}", value)
//        }
//        return evaluatedUrl
//    }
//
//    private fun evaluateValue(value: Any?, scopeContext: ScopeContext?): Any? {
//        return when (value) {
//            is String -> {
//                // Check if it's a variable reference
//                if (value.startsWith("{") && value.endsWith("}")) {
//                    val variableName = value.substring(1, value.length - 1)
//                    scopeContext?.get(variableName) ?: value
//                } else {
//                    value
//                }
//            }
//            else -> value
//        }
//    }
//}

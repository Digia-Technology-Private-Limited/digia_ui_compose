package com.digia.digiaui.framework.widgets


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualCompositeNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.color
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.textStyle
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.utils.NumUtil
import com.digia.digiaui.framework.utils.ToUtils
import com.digia.digiaui.framework.utils.applyIf
import com.digia.digiaui.framework.widgets.textfield.CommonDecorationBox
import com.digia.digiaui.framework.widgets.textfield.DigiaTextFieldColors
import com.digia.digiaui.framework.widgets.textfield.TextFieldContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ---------------- Debouncer Utility ---------------- */

/**
 * A debouncer that delays execution of a function until a specified time has passed without the
 * function being called again.
 */
class Debouncer(private val delayMillis: Long, private val scope: CoroutineScope) {
    private var job: Job? = null

    fun call(action: () -> Unit) {
        job?.cancel()
        job =
            scope.launch {
                delay(delayMillis)
                action()
            }
    }

    fun cancel() {
        job?.cancel()
    }
}

/* ---------------- Validation ---------------- */

/** Represents a validation rule with associated error message */
data class ValidationIssue(val type: String, val data: ExprOr<Any>? = null, val errorMessage: ExprOr<String>?) {
    companion object {
        fun fromJson(json: JsonLike): ValidationIssue {
            return ValidationIssue(
                type = json["type"] as? String ?: "",
                data = ExprOr.fromValue(json["data"]),
                errorMessage = ExprOr.fromValue<String>(json["errorMessage"]?:"Validation Failed")
            )
        }
    }
}

/* ---------------- Border Configuration ---------------- */

/** Configuration for text field border */
data class BorderConfig(
    val borderType: JsonLike? = null,
    val borderStyle: String? = null,
    val borderWidth: Double? = null,
    val borderColor: String? = null,
    val borderRadius: Any? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): BorderConfig? {
            if (json == null) return null
            return BorderConfig(
                borderType = json["borderType"] as? JsonLike,
                borderStyle = json["borderStyle"] as? String,
                borderWidth = NumUtil.toDouble(json["borderWidth"]),
                borderColor = json["borderColor"] as? String,
                borderRadius = json["borderRadius"]
            )
        }
    }
}

/* ---------------- Icon Constraints ---------------- */

/** Configuration for icon constraints (min/max width/height) */
data class IconConstraints(
    val minWidth: Double? = null,
    val minHeight: Double? = null,
    val maxWidth: Double? = null,
    val maxHeight: Double? = null
) {
    companion object {
        fun fromJson(json: JsonLike?): IconConstraints? {
            if (json == null) return null
            return IconConstraints(
                minWidth = NumUtil.toDouble(json["minWidth"]),
                minHeight = NumUtil.toDouble(json["minHeight"]),
                maxWidth = NumUtil.toDouble(json["maxWidth"]),
                maxHeight = NumUtil.toDouble(json["maxHeight"])
            )
        }
    }
}



/* ---------------- TextFormField Props ---------------- */

/** Text Form Field widget properties */
data class TextFormFieldProps(
    val controller: ExprOr<Any>? = null,
    val initialValue: ExprOr<String>? = null,
    val textStyle: JsonLike? = null,
    val maxLength: ExprOr<Int>? = null,
    val minLines: ExprOr<Int>? = null,
    val maxLines: ExprOr<Int>? = null,
    val labelText: ExprOr<String>? = null,
    val labelStyle: JsonLike? = null,
    val keyboardType: ExprOr<String>? = null,
    val textInputAction: ExprOr<String>? = null,
    val textAlign: ExprOr<String>? = null,
    val readOnly: ExprOr<Boolean>? = null,
    val obscureText: ExprOr<Boolean>? = null,
    val cursorColor: ExprOr<String>? = null,
//    val regex: ExprOr<String>? = null,
//    val errorText: ExprOr<String>? = null,
    val errorStyle: JsonLike? = null,
    val validationRules: List<ValidationIssue>? = null,
    val enabledBorder: BorderConfig? = null,
    val disabledBorder: BorderConfig? = null,
    val focusedBorder: BorderConfig? = null,
    val focusedErrorBorder: BorderConfig? = null,
    val errorBorder: BorderConfig? = null,
    val focusColor: ExprOr<String>? = null,
    val autoFocus: ExprOr<Boolean>? = null,
    val enabled: ExprOr<Boolean>? = null,
    val fillColor: ExprOr<String>? = null,
    val hintText: ExprOr<String>? = null,
    val hintStyle: JsonLike? = null,
    val contentPadding: Any? = null,
    val onChanged: JsonLike? = null,
    val onSubmit: JsonLike? = null,
    val debounceValue: ExprOr<Int>? = null,
    val prefixIconConstraints: IconConstraints? = null,
    val suffixIconConstraints: IconConstraints? = null
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: JsonLike): TextFormFieldProps {
            return TextFormFieldProps(
                controller = ExprOr.fromValue(json["controller"]),
                initialValue = ExprOr.fromValue(json["initialValue"]),
                textStyle = json["textStyle"] as? JsonLike,
                maxLength = ExprOr.fromValue(json["maxLength"]),
                minLines = ExprOr.fromValue(json["minLines"]),
                maxLines = ExprOr.fromValue(json["maxLines"]),
                labelText = ExprOr.fromValue(json["labelText"]),
                labelStyle = json["labelStyle"] as? JsonLike,
                keyboardType = ExprOr.fromValue(json["keyboardType"]),
                textInputAction = ExprOr.fromValue(json["textInputAction"]),
                textAlign = ExprOr.fromValue(json["textAlign"]),
                readOnly = ExprOr.fromValue(json["readOnly"]),
                obscureText = ExprOr.fromValue(json["obscureText"]),
                cursorColor = ExprOr.fromValue(json["cursorColor"]),
//                regex = ExprOr.fromValue(json["regex"]),
//                errorText = ExprOr.fromValue(json["errorText"]),
                errorStyle = json["errorStyle"] as? JsonLike,
                validationRules = (json["validationRules"] as? List<Any?>)
                    ?.mapNotNull { item ->
                        (item as? JsonLike)?.let { ValidationIssue.fromJson(it) }
                    },
                enabledBorder = BorderConfig.fromJson(json["enabledBorder"] as? JsonLike),
                disabledBorder = BorderConfig.fromJson(json["disabledBorder"] as? JsonLike),
                focusedBorder = BorderConfig.fromJson(json["focusedBorder"] as? JsonLike),
                focusedErrorBorder =
                    BorderConfig.fromJson(json["focusedErrorBorder"] as? JsonLike),
                errorBorder = BorderConfig.fromJson(json["errorBorder"] as? JsonLike),
                focusColor = ExprOr.fromValue(json["focusColor"]),
                autoFocus = ExprOr.fromValue(json["autoFocus"]),
                enabled = ExprOr.fromValue(json["enabled"]),
                fillColor = ExprOr.fromValue(json["fillColor"]),
                hintText = ExprOr.fromValue(json["hintText"]),
                hintStyle = json["hintStyle"] as? JsonLike,
                contentPadding = json["contentPadding"],
                onChanged = json["onChanged"] as? JsonLike,
                onSubmit = json["onSubmit"] as? JsonLike,
                debounceValue = ExprOr.fromValue(json["debounceValue"]),
                prefixIconConstraints =
                    IconConstraints.fromJson(json["prefixIconConstraints"] as? JsonLike),
                suffixIconConstraints =
                    IconConstraints.fromJson(json["suffixIconConstraints"] as? JsonLike)
            )
        }
    }
}

///* ---------------- Virtual TextFormField Widget ---------------- */
//
///**
// * Virtual Text Form Field widget
// *
// * Renders a Material3 OutlinedTextField with validation, debouncing, and prefix/suffix slots
// */
class VWTextFormField(
    refName: String?,
    commonProps: CommonProps?,
    parent: VirtualNode?,
    parentProps: Props? = null,
    props: TextFormFieldProps,
    slots: Map<String, List<VirtualNode>>? = null
) :
    VirtualCompositeNode<TextFormFieldProps>(
        props = props,
        commonProps = commonProps,
        parent = parent,
        refName = refName,
        parentProps = parentProps,
        slots = slots
    ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate props
        val initialValue = payload.evalExpr(props.initialValue) ?: ""
        val enabled = payload.evalExpr(props.enabled) ?: true
        val readOnly = payload.evalExpr(props.readOnly) ?: false
        val obscureText = payload.evalExpr(props.obscureText) ?: false
        val autoFocus = payload.evalExpr(props.autoFocus) ?: false
        val maxLines = payload.evalExpr(props.maxLines) ?: 1
        val minLines = payload.evalExpr(props.minLines) ?: 1
        val maxLength = payload.evalExpr(props.maxLength)
        val debounceMs = payload.evalExpr(props.debounceValue) ?: 0

        // Text styles
        val textStyle = payload.textStyle(props.textStyle)
        val labelStyle = payload.textStyle(props.labelStyle)
        val hintStyle = payload.textStyle(props.hintStyle)
        val errorStyle = payload.textStyle(props.errorStyle)

        // Text content
        val labelText = payload.evalExpr(props.labelText)
        val hintText = payload.evalExpr(props.hintText)

        // Colors
        val cursorColor = payload.evalExpr(props.cursorColor)?.let { payload.color(it) }
        val fillColor = payload.evalExpr(props.fillColor)?.let { payload.color(it) }
        val focusColor = payload.evalExpr(props.focusColor)?.let { payload.color(it) }

        // Keyboard configuration
        val keyboardTypeStr = payload.evalExpr(props.keyboardType)
        val textInputActionStr = payload.evalExpr(props.textInputAction)
        val textAlignStr = payload.evalExpr(props.textAlign)

        // Validation

        val validationRules =
            props.validationRules

        // State management
        var textValue by remember { mutableStateOf(initialValue) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val debouncer =
            remember(debounceMs) {
                if (debounceMs > 0) Debouncer(debounceMs.toLong(), scope) else null
            }

        // Validation function
        fun validateField(value: String): String? {

            validationRules?.forEach { rule ->
                val data= rule.data?.evaluate<Any>(scopeContext = payload.scopeContext)
                val errorMsg= rule.errorMessage?.evaluate<String>(scopeContext = payload.scopeContext)
                when (rule.type) {
                    "required" -> {
                        if (value.trim().isEmpty()) {
                            return errorMsg
                        }
                    }
                    "minLength" -> {
                        val minLen = NumUtil.toDouble(data)?.toInt() ?: 0
                        if (value.length < minLen) {
                            return errorMsg
                        }
                    }
                    "maxLength" -> {
                        val maxLen = NumUtil.toDouble(data)?.toInt() ?: Int.MAX_VALUE
                        if (value.length > maxLen) {
                            return errorMsg
                        }
                    }
                    "pattern" -> {
                        val pattern = data as? String
                        if (pattern != null) {
                            val regex = Regex(pattern)
                            if (!regex.matches(value)) {
                                return errorMsg
                            }
                        }
                    }
                }
            }
            return null
        }

        // On value change handler
        fun onValueChange(newValue: String) {
            // Apply max length constraint
            val constrainedValue =
                if (maxLength != null && newValue.length > maxLength) {
                    newValue.take(maxLength)
                } else {
                    newValue
                }

            textValue = constrainedValue
            errorMessage = validateField(constrainedValue)

            // Trigger onChanged action with debouncing if configured
            val triggerAction = { 
                props.onChanged?.let {
//                    payload.executeAction(it, "onChanged")
                }
            }

            if (debouncer != null) {
                debouncer.call({triggerAction})
            } else {
                triggerAction()
            }
        }

        // Keyboard options
        val keyboardType =
            when (keyboardTypeStr) {
                "text" -> KeyboardType.Text
                "number" -> KeyboardType.Number
                "phone" -> KeyboardType.Phone
                "email" -> KeyboardType.Email
                "password" -> KeyboardType.Password
                "numberPassword" -> KeyboardType.NumberPassword
                "url" -> KeyboardType.Uri
                "decimal" -> KeyboardType.Decimal
                else -> KeyboardType.Text
            }

        val imeAction =
            when (textInputActionStr) {
                "done" -> ImeAction.Done
                "go" -> ImeAction.Go
                "next" -> ImeAction.Next
                "previous" -> ImeAction.Previous
                "search" -> ImeAction.Search
                "send" -> ImeAction.Send
                "none" -> ImeAction.None
                else -> ImeAction.Default
            }

        val textAlign =
            when (textAlignStr) {
                "left" -> TextAlign.Left
                "right" -> TextAlign.Right
                "center" -> TextAlign.Center
                "start" -> TextAlign.Start
                "end" -> TextAlign.End
                "justify" -> TextAlign.Justify
                else -> TextAlign.Start
            }

        // Build text field colors
        val colors =
            TextFieldDefaults.colors(
                cursorColor = cursorColor ?: Color.Unspecified,
                focusedIndicatorColor = focusColor ?: Color.Unspecified,
                errorIndicatorColor = Color.Red,
                focusedContainerColor = fillColor ?: Color.Transparent,
                unfocusedContainerColor = fillColor ?: Color.Transparent,
                disabledContainerColor = fillColor ?: Color.Transparent,
                errorContainerColor = fillColor ?: Color.Transparent,

            )

        // Content padding
        val contentPadding = ToUtils.edgeInsets(props.contentPadding)

        // Render prefix and suffix widgets
        val prefixWidget: @Composable (() -> Unit)? =
            slot("prefix")?.let { prefixNodes ->
                {
                    prefixNodes.ToWidget(payload)
                }
            }

        val suffixWidget: @Composable (() -> Unit)? =
            slot("suffix")?.let { suffixNodes ->
                {
                    suffixNodes.ToWidget(payload)
                }
            }


      ClearFocusOnTapOutside {
          InternalTextFormField(
              controller = TextController(initialText = textValue),
              autoFocus = autoFocus,
              enabled = enabled,
              readOnly = readOnly,
              obscureText = obscureText,
              keyboardType = keyboardType,
              imeAction = imeAction,
              textAlign = textAlign,
              textStyle = textStyle
                  ?: TextStyle.Default.copy(
                      textAlign = textAlign
                  ),
              maxLines = if (maxLines == 1) 1 else maxLines,
              minLines = minLines,
              maxLength = maxLength,
              fillColor = fillColor ?: Color.Transparent,
              labelText = labelText,
              hintText = hintText,
              cursorColor = cursorColor ?: MaterialTheme.colorScheme.primary,
              focusedBorder = VWInputBorder.None,
              enabledBorder = VWInputBorder.None,
              errorBorder = VWInputBorder.None,
              onValueChange = ::onValueChange,
              validate = ::validateField,
              onSubmit = { value ->
                  errorMessage = validateField(value)
                  if (errorMessage == null) {
                      props.onSubmit?.let {
//                        payload.executeAction(it, "onSubmit")
                      }
                  }
              },
              prefixWidget = prefixWidget,
              suffixWidget = suffixWidget
          )

      }
        // Cleanup debouncer on dispose
        DisposableEffect(Unit) { onDispose { debouncer?.cancel() } }
    }
}

/* ---------------- Builder Function ---------------- */

/** Builder function for TextFormField widget */
fun textFormFieldBuilder(
    data: VWNodeData,
    parent: VirtualNode?,
    registry: VirtualWidgetRegistry
): VirtualNode {
    val childrenData =
        data.childGroups?.mapValues { (_, childrenData) ->
            childrenData.map { childData -> registry.createWidget(childData, parent) }
        }

    return VWTextFormField(
        refName = data.refName,
        commonProps = data.commonProps,
        parent = parent,
        parentProps = data.props,
        props = TextFormFieldProps.fromJson(data.props.value),
        slots = childrenData
    )
}

//sealed class VWInputBorder {
//    data class Outline(
//        val strokeWidth: Dp,
//        val radius: Dp,
//        val dashed: Boolean
//    ) : VWInputBorder()
//
//    data class Underline(
//        val strokeWidth: Dp,
//        val dashed: Boolean
//    ) : VWInputBorder()
//
//    object None : VWInputBorder()
//}
//
//fun Modifier.dashedOutlineBorder(
//    strokeWidth: Dp,
//    radius: Dp,
//    color: Color
//) = drawBehind {
//    val strokePx = strokeWidth.toPx()
//    drawRoundRect(
//        color = color,
//        size = size,
//        cornerRadius = CornerRadius(radius.toPx()),
//        style = Stroke(
//            width = strokePx,
//            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
//        )
//    )
//}

//fun Modifier.dashedUnderlineBorder(
//    strokeWidth: Dp,
//    color: Color
//) = drawBehind {
//    val y = size.height - strokeWidth.toPx() / 2
//    drawLine(
//        color = color,
//        start = Offset(0f, y),
//        end = Offset(size.width, y),
//        strokeWidth = strokeWidth.toPx(),
//        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
//    )
//}


//
//@Composable
//fun InternalTextFormField(
//    controller: MutableState<String>,
//    autoFocus: Boolean = false,
//    enabled: Boolean = true,
//    readOnly: Boolean = false,
//    obscureText: Boolean = false,
//    keyboardType: KeyboardType = KeyboardType.Text,
//    imeAction: ImeAction = ImeAction.Done,
//    textAlign: TextAlign = TextAlign.Start,
//    debounceValue: Int = 0,
//    textStyle: TextStyle = LocalTextStyle.current,
//    labelStyle: TextStyle = LocalTextStyle.current,
//    hintStyle: TextStyle = LocalTextStyle.current,
//    focusColor: Color?= null,
//    contentPadding: PaddingValues? =null ,
//    maxLines: Int? = null,
//    minLines: Int? = null,
//    maxLength: Int? = null,
//    fillColor: Color? = null,
//    labelText: String? = null,
//    hintText: String? = null,
//    validationRules: List<ValidationIssue>?=null,
//    cursorColor: Color = MaterialTheme.colorScheme.primary,
//    focusedBorder: VWInputBorder = VWInputBorder.None,
//    enabledBorder: VWInputBorder = VWInputBorder.None,
//    errorBorder: VWInputBorder = focusedBorder,
//    disabledBorder: VWInputBorder = enabledBorder,
//    focusedErrorBorder: VWInputBorder = errorBorder,
//    onValueChange: (String) -> Unit,
//    onSubmit: (String) -> Unit,
//    prefixWidget: (@Composable () -> Unit)? = null,
//    suffixWidget: (@Composable () -> Unit)? = null
//) {
//    val focusRequester = remember { FocusRequester() }
//    val interactionSource = remember { MutableInteractionSource() }
//    val isFocused by interactionSource.collectIsFocusedAsState()
//
//    val effectiveMaxLines = if (obscureText) 1 else maxLines ?: Int.MAX_VALUE
//    val effectiveMinLines = if (obscureText) 1 else minLines ?: 1
//    val isMultiline = effectiveMinLines > 1
//
//    val border = when {
//        errorText != null -> errorBorder
//        isFocused -> focusedBorder
//        else -> enabledBorder
//    }
//
//    val baseModifier = Modifier
//        .fillMaxWidth()
//        .then(
//            when (border) {
//                is VWInputBorder.Outline -> {
//                    if (border.dashed)
//                        Modifier.dashedOutlineBorder(
//                            border.strokeWidth,
//                            border.radius,
//                            MaterialTheme.colorScheme.outline
//                        )
//                    else Modifier
//                }
//
//                is VWInputBorder.Underline -> {
//                    if (border.dashed)
//                        Modifier.dashedUnderlineBorder(
//                            border.strokeWidth,
//                            MaterialTheme.colorScheme.outline
//                        )
//                    else Modifier
//                }
//
//                else -> Modifier
//            }
//        )
//
//    LaunchedEffect(autoFocus) {
//        if (autoFocus) focusRequester.requestFocus()
//    }
//
//    Column {
//        when (border) {
//            is VWInputBorder.Outline -> {
//                OutlinedTextField(
//                    value = controller.value,
//                    onValueChange = {
//                        if (maxLength == null || it.length <= maxLength) {
//                            controller.value = it
//                            onValueChange(it)
//                        }
//                    },
//                    modifier = baseModifier.focusRequester(focusRequester),
//                    enabled = enabled,
//                    readOnly = readOnly,
//                    textStyle = textStyle,
//                    singleLine = effectiveMaxLines == 1,
//                    maxLines = effectiveMaxLines,
//                    minLines = effectiveMinLines,
//                    visualTransformation =
//                        if (obscureText) PasswordVisualTransformation()
//                        else VisualTransformation.None,
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = keyboardType,
//                        imeAction = imeAction
//                    ),
//                    keyboardActions = KeyboardActions(
//                        onDone = { onSubmit(controller.value) }
//                    ),
//                    interactionSource = interactionSource,
//                    isError = errorText != null,
//                    label = labelText?.let { { Text(it) } },
//                    placeholder = hintText?.let { { Text(it) } },
//                    leadingIcon = prefixWidget,
//                    trailingIcon = suffixWidget,
//                    colors = OutlinedTextFieldDefaults.colors(
//                        cursorColor = cursorColor,
//                        focusedContainerColor = fillColor ?: Color.Transparent,
//                        unfocusedContainerColor = fillColor ?: Color.Transparent,
//                        errorContainerColor = fillColor ?: Color.Transparent
//                    )
//                )
//            }
//
//            else -> {
//                TextField(
//                    value = controller.value,
//                    onValueChange = {
//                        if (maxLength == null || it.length <= maxLength) {
//                            controller.value = it
//                            onValueChange(it)
//                        }
//                    },
//                    modifier = baseModifier.focusRequester(focusRequester),
//                    enabled = enabled,
//                    readOnly = readOnly,
//                    textStyle = textStyle,
//                    singleLine = effectiveMaxLines == 1,
//                    maxLines = effectiveMaxLines,
//                    minLines = effectiveMinLines,
//                    visualTransformation =
//                        if (obscureText) PasswordVisualTransformation()
//                        else VisualTransformation.None,
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = keyboardType,
//                        imeAction = imeAction
//                    ),
//                    keyboardActions = KeyboardActions(
//                        onDone = { onSubmit(controller.value) }
//                    ),
//                    interactionSource = interactionSource,
//                    isError = errorText != null,
//                    label = labelText?.let { { Text(it) } },
//                    placeholder = hintText?.let { { Text(it) } },
//                    leadingIcon = prefixWidget,
//                    trailingIcon = suffixWidget,
//                    colors = TextFieldDefaults.colors(
//                        cursorColor = cursorColor,
//                        focusedContainerColor = fillColor ?: Color.Transparent,
//                        unfocusedContainerColor = fillColor ?: Color.Transparent,
//                        errorContainerColor = fillColor ?: Color.Transparent
//                    )
//                )
//            }
//        }
//
//        if (errorText != null) {
//            Spacer(Modifier.height(4.dp))
//            Text(
//                text = errorText,
//                color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.bodySmall
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternalTextFormField(
    controller: TextController,
    autoFocus: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    obscureText: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    textAlign: TextAlign = TextAlign.Start,
    debounceValue: Int = 0,
    textStyle: TextStyle = LocalTextStyle.current,
    labelStyle: TextStyle = LocalTextStyle.current,
    hintStyle: TextStyle = LocalTextStyle.current,
    focusColor: Color? = null,
    contentPadding: PaddingValues? = null,
    maxLines: Int? = null,
    minLines: Int? = null,
    maxLength: Int? = null,
    fillColor: Color? = null,
    labelText: String? = null,
    hintText: String? = null,
    validate: ((String) -> String?)? = null,
    cursorColor: Color = MaterialTheme.colorScheme.primary,
    focusedBorder: VWInputBorder = VWInputBorder.None,
    enabledBorder: VWInputBorder = VWInputBorder.None,
    errorBorder: VWInputBorder = focusedBorder,
    disabledBorder: VWInputBorder = enabledBorder,
    focusedErrorBorder: VWInputBorder = errorBorder,
    onValueChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    prefixWidget: (@Composable () -> Unit)? = null,
    suffixWidget: (@Composable () -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var errorText by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }

    fun validateNow(value: String) =
        validate?.invoke(value)

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            showError = true
            errorText = validateNow(controller.text)
        }
    }



    val padding = contentPadding ?: PaddingValues(
        horizontal = 12.dp,
        vertical = 14.dp
    )

    Column {
//        TextFieldDecoration(
//            modifier = Modifier
//                .fillMaxWidth()
//                .focusRequester(focusRequester),
//            border = VWInputBorder.Underline(
//                strokeWidth = 5.dp,
//                dashed = true
//            ),
//            isFocused = isFocused,
//            hasError = errorText != null,
//            focusColor = focusColor,
//        ) {

            BasicTextField(
                value = controller.text,
                onValueChange = {
                    if (maxLength == null || it.length <= maxLength) {
                        controller.text=it
                        onValueChange(it)
                        if (showError) errorText = validateNow(it)
                    }
                },
                enabled = enabled,
                readOnly = readOnly,
                textStyle = textStyle.copy(textAlign = textAlign),
                cursorBrush = SolidColor(cursorColor),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit(controller.text) }
                ),
                interactionSource = interactionSource,
                visualTransformation =
                    if (obscureText) PasswordVisualTransformation()
                    else VisualTransformation.None,
                decorationBox = { inner ->
                    CommonDecorationBox(
                        value = controller.text,
                        innerTextField = inner,
                        enabled = enabled,
                        singleLine = (maxLines ?: 1) == 1,
                        isError = errorText != null,
                        visualTransformation =
                            if (obscureText) PasswordVisualTransformation()
                            else VisualTransformation.None,
                        interactionSource = interactionSource,
                        contentPadding = padding,
                        leadingIcon = prefixWidget,
                        trailingIcon = suffixWidget,
                        placeholder = if (controller.text.isEmpty() && hintText != null) {
                            {
                                Text(
                                    hintText,
                                    style = hintStyle
                                )
                            }
                        } else null,
                        label = {
                            if (labelText != null) {
                                Text(
                                    labelText,
                                    style = labelStyle
                                )
                            }
                        },
                        prefix = {
                            if (prefixWidget != null) {
                                prefixWidget()
                            }
                        },
                        suffix = {
                            if (suffixWidget != null) {
                                suffixWidget()
                            }
                        },

                        container = {
                            TextFieldContainer(
                                enabled = enabled,
interactionSource= interactionSource,
                                focusedBorder = VWInputBorder.Underline(
                                    strokeWidth = 2.dp,
                                    dashed = false
                                ),
                                disabledBorder = VWInputBorder.Underline(
                                    strokeWidth = 1.dp,
                                    dashed = false
                                ),
                                focusedErrorBorder = VWInputBorder.Underline(
                                    strokeWidth = 2.dp,),
                                enabledBorder = enabledBorder,
                                errorBorder = errorBorder,
                                isError = false,
                            )
                        },

                    )
//                    OutlinedTextFieldDefaults.DecorationBox(
//                        value = controller.text,
//                        innerTextField = inner,
//                        enabled = enabled,
//                        singleLine = (maxLines ?: 1) == 1,
//                        isError = errorText != null,
//                        visualTransformation =
//                            if (obscureText) PasswordVisualTransformation()
//                            else VisualTransformation.None,
//                        interactionSource = interactionSource,
//                        contentPadding = padding,
//                        leadingIcon = prefixWidget,
//                        trailingIcon = suffixWidget,
//                        placeholder = if (controller.text.isEmpty() && hintText != null) {
//                            {
//                                Text(
//                                    hintText,
//                                    style = hintStyle
//                                )
//                            }
//                        } else null,
//                        label = if (labelText != null) {
//                            {
//                                Text(
//                                    labelText,
//                                    style = labelStyle
//                                )
//                            }
//                        } else null
//                    )
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        prefixWidget?.invoke()
//                        Box(Modifier.weight(1f)) {
//                            if (controller.text.isEmpty() && hintText != null) {
//                                Text(hintText, style = hintStyle)
//                            }
//                            inner()
//                        }
//                        suffixWidget?.invoke()
//                    }
                }
            )
//        }

        if (errorText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                errorText!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



// Helper function to use in your screen composable to clear focus on tap outside
@Composable
fun ClearFocusOnTapOutside(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current

    CompositionLocalProvider(
        LocalFocusManager provides focusManager
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus(true)
                }
        ) {
            content()
        }
    }
}


// Sealed class for borders
sealed class VWInputBorder {
    data class Outline(
        val strokeWidth: Dp = 1.dp,
        val radius: Dp = 4.dp,
       val color: Color = Color.Black,
        val dashed: Boolean = false,
        val strokeCap: StrokeCap= StrokeCap.Butt,
        val dashPattern: List<Float>? = null
    ) : VWInputBorder()

    data class Underline(
        val strokeWidth: Dp = 1.dp,
      val  color:Color = Color.Black,
        val dashed: Boolean = false
    ) : VWInputBorder()

    object None : VWInputBorder()
}

// Extension function for dashed outline border
fun Modifier.dashedOutlineBorder(
    strokeWidth: Dp,
    radius: Dp,
    color: Color
) =
    drawBehind {
        val strokePx = strokeWidth.toPx()
        val cornerRadiusPx = radius.toPx()
        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(
                width = strokePx,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
    }


// Extension function for solid outline border
fun Modifier.solidOutlineBorder(
    strokeWidth: Dp,
    radius: Dp,
    color: Color
) =
    drawBehind {
        val strokePx = strokeWidth.toPx()
        val cornerRadiusPx = radius.toPx()
        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(cornerRadiusPx),
            style = Stroke(width = strokePx)
        )
    }


// Extension function for dashed underline border
fun Modifier.dashedUnderlineBorder(
    strokeWidth: Dp,
    color: Color
) =
    drawBehind {
        val y = size.height - strokeWidth.toPx() / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )
    }


// Extension function for solid underline border
fun Modifier.solidUnderlineBorder(
    strokeWidth: Dp,
    color: Color
) =
    drawBehind {
        val y = size.height - strokeWidth.toPx() / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = strokeWidth.toPx()
        )
    }


// TextController class (assumed structure based on usage)
class TextController(
    initialText: String = ""
) {
    private val _text = mutableStateOf(initialText)
    var text: String
        get() = _text.value
        set(value) {
            _text.value = value
        }


}



@Composable
private fun TextFieldDecoration(
    modifier: Modifier,
    border: VWInputBorder,
    isFocused: Boolean,
    hasError: Boolean,
    focusColor: Color?,
    content: @Composable () -> Unit
) {
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> focusColor ?: MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .background(Color.Red)
            .then(
                when (border) {
                    is VWInputBorder.Outline ->
                        if (border.dashed)
                            Modifier.dashedOutlineBorder(
                                4.dp,
                                border.radius,
                                Color.Green
                            )
                        else
                            Modifier.border(
                                4.dp,
                                Color.Green,
                                RoundedCornerShape(border.radius)
                            )

                    is VWInputBorder.Underline ->
                        if (border.dashed)
                            Modifier.dashedUnderlineBorder(
                                4.dp,
                                Color.Green
                            )
                        else
                            Modifier.solidUnderlineBorder(
                                4.dp,
                                Color.Green
                            )

                    VWInputBorder.None -> Modifier
                }
            ).applyIf(
                border is VWInputBorder.Outline&& border.dashed,
                block = {
                    this.border(
                        4.dp,
                        Color.Green,
                        RoundedCornerShape(4.dp)
                    )
                }
            )
            .applyIf(
                border is VWInputBorder.Outline&& !border.dashed,
                block = {
                    this.dashedOutlineBorder(
                        4.dp,
                        4.dp,
//                        border.radius,
                        Color.Green
                    )
                }
            )
            .applyIf(
                border is VWInputBorder.Underline&& !border.dashed,
                block = {
                    this.solidUnderlineBorder(
                        4.dp,
                        Color.Green
                    )
                }
            ) .applyIf(
                border is VWInputBorder.Underline&& !border.dashed,
                block = {
                    this.solidUnderlineBorder(
                        4.dp,
                        Color.Green
                    )
                }
            )

    ) {
        content()
    }
}
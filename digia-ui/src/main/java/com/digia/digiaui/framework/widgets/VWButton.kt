package com.digia.digiaui.framework.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.digia.digiaui.framework.RenderPayload
import com.digia.digiaui.framework.VirtualWidgetRegistry
import com.digia.digiaui.framework.base.VirtualLeafNode
import com.digia.digiaui.framework.base.VirtualNode
import com.digia.digiaui.framework.models.CommonProps
import com.digia.digiaui.framework.models.ExprOr
import com.digia.digiaui.framework.models.Props
import com.digia.digiaui.framework.models.VWNodeData
import com.digia.digiaui.framework.utils.JsonLike
import com.digia.digiaui.framework.widgets.icon.IconProps
import com.digia.digiaui.framework.widgets.icon.VWIcon
import com.digia.digiaui.framework.widgets.text.TextProps

/** Properties for the button widget. */
data class ButtonProps(
        val onClick: JsonLike? = null,
        val isDisabled: ExprOr<Boolean>? = null,
        val variant: ExprOr<String>? = null, // e.g., "elevated", "filled"
        val borderRadius: ExprOr<Double>? = null,
        val contentPadding: JsonLike? = null,
        val backgroundColor: ExprOr<String>? = null,
        val text: JsonLike? = null,
        val leadingIcon: JsonLike? = null,
        val trailingIcon: JsonLike? = null,
        val disabledTextColor: ExprOr<String>? = null,
        val disabledIconColor: ExprOr<String>? = null,
        val disabledBackgroundColor: ExprOr<String>? = null,
        val iconSpacing: ExprOr<Double>? = null
) {
    companion object {
        fun fromJson(json: JsonLike): ButtonProps {
            return ButtonProps(
                    onClick = json["onClick"] as? JsonLike,
                    isDisabled = ExprOr.fromValue(json["isDisabled"]),
                    variant = ExprOr.fromValue(json["variant"]),
                    borderRadius = ExprOr.fromValue(json["borderRadius"]),
                    contentPadding = json["contentPadding"] as? JsonLike,
                    backgroundColor = ExprOr.fromValue(json["backgroundColor"]),
                    text = json["text"] as? JsonLike,
                    leadingIcon = json["leadingIcon"] as? JsonLike,
                    trailingIcon = json["trailingIcon"] as? JsonLike,
                    disabledTextColor = ExprOr.fromValue(json["disabledTextColor"]),
                    disabledIconColor = ExprOr.fromValue(json["disabledIconColor"]),
                    disabledBackgroundColor = ExprOr.fromValue(json["disabledBackgroundColor"]),
                    iconSpacing = ExprOr.fromValue(json["iconSpacing"])
            )
        }
    }
}

/** Virtual Button widget that renders a Jetpack Compose Button. */
class VWButton(
        refName: String?,
        commonProps: CommonProps?,
        parent: VirtualNode?,
        parentProps: Props? = null,
        props: ButtonProps
) :
        VirtualLeafNode<ButtonProps>(
                props = props,
                commonProps = commonProps,
                parent = parent,
                refName = refName,
                parentProps = parentProps
        ) {

    @Composable
    override fun Render(payload: RenderPayload) {
        // Evaluate expressions from props
        val isButtonDisabled = payload.evalExpr(props.isDisabled) ?: (props.onClick == null)
        val variant = payload.evalExpr(props.variant) ?: "filled"
        val borderRadius = payload.evalExpr(props.borderRadius)
        val padding = props.contentPadding
        val iconSpacing = payload.evalExpr(props.iconSpacing)?.dp ?: 8.dp

        // Evaluate colors
        val enabledBgColor =
                payload.evalExpr(props.backgroundColor)?.let { ColorUtil.fromHexString(it) }
        val disabledBgColor =
                payload.evalExpr(props.disabledBackgroundColor)?.let { ColorUtil.fromHexString(it) }
                        ?: Color.LightGray
        val disabledTextColor = payload.evalExpr(props.disabledTextColor)
        val disabledIconColor = payload.evalExpr(props.disabledIconColor)

        val buttonColors =
                ButtonDefaults.buttonColors(
                        containerColor = enabledBgColor
                                        ?: ButtonDefaults.buttonColors().containerColor,
                        disabledContainerColor = disabledBgColor
                )

        val contentPadding =
                padding?.let {
                    val h = (it["horizontal"] as? Number)?.toDouble()?.dp ?: 0.dp
                    val v = (it["vertical"] as? Number)?.toDouble()?.dp ?: 0.dp
                    PaddingValues(horizontal = h, vertical = v)
                }
                        ?: ButtonDefaults.ContentPadding

        val shape = borderRadius?.let { RoundedCornerShape(it.dp) } ?: ButtonDefaults.shape

        val buttonContent: @Composable RowScope.() -> Unit = {
            ButtonContent(
                    payload = payload,
                    isButtonDisabled = isButtonDisabled,
                    disabledTextColor = disabledTextColor,
                    disabledIconColor = disabledIconColor,
                    iconSpacing = iconSpacing
            )
        }

        val modifier = Modifier.buildModifier(payload)

        // Determine which button type to render based on the variant
        when (variant.lowercase()) {
            "elevated" ->
                    ElevatedButton(
                            onClick = { payload.executeAction(props.onClick, "onPressed") },
                            enabled = !isButtonDisabled,
                            modifier = modifier,
                            shape = shape,
                            colors = buttonColors,
                            contentPadding = contentPadding,
                            content = buttonContent
                    )
            else ->
                    Button( // Default to filled Button
                            onClick = { payload.executeAction(props.onClick, "onPressed") },
                            enabled = !isButtonDisabled,
                            modifier = modifier,
                            shape = shape,
                            colors = buttonColors,
                            contentPadding = contentPadding,
                            content = buttonContent
                    )
        }
    }

    /** Composable function to render the content of the button (icons and text). */
    @Composable
    private fun ButtonContent(
            payload: RenderPayload,
            isButtonDisabled: Boolean,
            disabledTextColor: String?,
            disabledIconColor: String?,
            iconSpacing: androidx.compose.ui.unit.Dp
    ) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
        ) {
            // Render leading icon if it exists
            props.leadingIcon?.let { iconJson ->
                val iconProps =
                        if (isButtonDisabled && disabledIconColor != null) {
                            val parsed = IconProps.fromJson(iconJson)
                            parsed.copy(color = ExprOr.fromValue(disabledIconColor))
                        } else {
                            IconProps.fromJson(iconJson)
                        }
                VWIcon(null, null, this@VWButton, null, iconProps).Render(payload)
                Spacer(modifier = Modifier.width(iconSpacing))
            }

            // Render text if it exists
            props.text?.let { textJson ->
                val modifiedTextJson =
                        if (isButtonDisabled && disabledTextColor != null) {
                            // Create a mutable copy of the text JSON and override the textColor
                            val mutableJson = textJson.toMutableMap()
                            val textStyleJson =
                                    (mutableJson["textStyle"] as? JsonLike)?.toMutableMap()
                                            ?: mutableMapOf()
                            textStyleJson["textColor"] = disabledTextColor
                            mutableJson["textStyle"] = textStyleJson
                            mutableJson
                        } else {
                            textJson
                        }

                val textProps =
                        com.digia.digiaui.framework.widgets.TextProps.fromJson(modifiedTextJson)
                VWText(null, null, this@VWButton, null, textProps).Render(payload)
            }

            // Render trailing icon if it exists
            props.trailingIcon?.let { iconJson ->
                Spacer(modifier = Modifier.width(iconSpacing))
                val iconProps =
                        if (isButtonDisabled && disabledIconColor != null) {
                            val parsed = IconProps.fromJson(iconJson)
                            parsed.copy(color = ExprOr.fromValue(disabledIconColor))
                        } else {
                            IconProps.fromJson(iconJson)
                        }
                VWIcon(null, null, this@VWButton, null, iconProps).Render(payload)
            }
        }
    }
}

/** Builder function to construct a VWButton from node data. */
fun buttonBuilder(
        data: VWNodeData,
        parent: VirtualNode?,
        @Suppress("UNUSED_PARAMETER") registry: VirtualWidgetRegistry
): VirtualNode {
    return VWButton(
            refName = data.refName,
            commonProps = data.commonProps,
            parent = parent,
            parentProps = data.props,
            props = ButtonProps.fromJson(data.props.value)
    )
}

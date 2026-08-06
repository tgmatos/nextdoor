package com.nextdoor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalCard
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.Rose50
import com.nextdoor.app.ui.theme.Rose700
import com.nextdoor.app.ui.theme.TextMuted

/**
 * Pill text field matching the front-end design: NaturalCard background,
 * 1dp NaturalBorder, optional leading icon, olive border + ring on focus,
 * optional trailing content (e.g. password visibility toggle).
 */
@Composable
fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isPassword: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
    placeholderTextAlign: TextAlign = TextAlign.Start,
    autofillTypes: List<androidx.compose.ui.autofill.AutofillType>? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    var showPassword by remember { mutableStateOf(false) }

    val borderColor = if (focused) Olive600 else NaturalBorder
    val shape = RoundedCornerShape(percent = 50)

    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (maxLength == null || new.length <= maxLength) onValueChange(new)
        },
        modifier = modifier
            .fillMaxWidth()
            .background(NaturalCard, shape)
            .border(if (focused) 1.5.dp else 1.dp, borderColor, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        enabled = enabled,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = Color(0xFF3D3D33),
            textAlign = textAlign
        ),
        cursorBrush = SolidColor(Olive600),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            autoCorrectEnabled = false
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        interactionSource = interaction,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = placeholderTextAlign
                        )
                    }
                    innerTextField()
                }
                if (isPassword) {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Ocultar senha" else "Mostrar senha",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    trailingIcon?.invoke()
                }
            }
        }
    )
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    if (message.isBlank()) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Rose50, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(text = message, color = Rose700, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

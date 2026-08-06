package com.nextdoor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextdoor.app.ui.theme.NaturalBorder
import com.nextdoor.app.ui.theme.NaturalCard
import com.nextdoor.app.ui.theme.Olive600
import com.nextdoor.app.ui.theme.TextMuted

/**
 * Pill search bar with a leading search icon.
 *  - [readOnly] = true: whole bar is a tap target (used on Home).
 *  - [readOnly] = false: editable inline field (used on the Search screen).
 */
@Composable
fun SearchBar(
    placeholder: String = "Buscar lojas e produtos",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    readOnly: Boolean = true,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    borderColor: Color = NaturalBorder
) {
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NaturalCard, shape)
            .border(if (readOnly) 1.dp else 1.5.dp, borderColor, shape)
            .then(if (readOnly) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Buscar",
            tint = Olive600,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.size(8.dp))
        if (readOnly) {
            Text(
                text = if (value.isBlank()) placeholder else value,
                color = TextMuted,
                fontSize = 13.sp
            )
        } else {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color(0xFF3D3D33), fontSize = 13.sp),
                cursorBrush = SolidColor(Olive600),
                decorationBox = { inner ->
                    if (value.isBlank()) {
                        Text(text = placeholder, color = TextMuted, fontSize = 13.sp)
                    }
                    inner()
                }
            )
        }
    }
}

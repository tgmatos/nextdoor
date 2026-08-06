package com.nextdoor.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CardShape = RoundedCornerShape(24.dp)
val InnerCardShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(percent = 50)
val TextAreaShape = RoundedCornerShape(16.dp)

val NextDoorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = InnerCardShape,
    large = CardShape,
    extraLarge = CircleShape
)

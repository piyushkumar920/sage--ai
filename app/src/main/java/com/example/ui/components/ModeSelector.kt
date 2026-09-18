package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary

@Composable
fun ModeSelector(
    currentMode: String,
    onModeChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf("NORMAL", "LEARNING", "SOCRATIC")

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(SageGlassL2)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.16f),
                        SageGlassBorderGlow.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val isSelected = currentMode.equals(mode, ignoreCase = true)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else 1f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                label = "mode_press"
            )

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) {
                    when (mode) {
                        "LEARNING" -> SagePrimaryStart
                        "SOCRATIC" -> SageGold.copy(alpha = 0.28f)
                        else -> SagePrimary.copy(alpha = 0.38f)
                    }
                } else Color.Transparent,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "mode_bg"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (mode == "SOCRATIC") SageGold else SageTextPrimary
                } else SageTextMuted,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "mode_text"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.3f),
                                        SagePrimaryLight.copy(alpha = 0.4f)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            )
                        } else Modifier
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onModeChanged(mode) }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("mode_${mode.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.3.sp,
                    color = textColor
                )
            }
        }
    }
}

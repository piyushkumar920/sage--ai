package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageRaisedSurface
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
            .clip(RoundedCornerShape(20.dp))
            .background(SageRaisedSurface)
            .border(1.dp, SageCardBorder, RoundedCornerShape(20.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val isSelected = currentMode.equals(mode, ignoreCase = true)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (mode == "LEARNING") SagePrimaryStart else if (mode == "SOCRATIC") SageGold.copy(alpha = 0.25f) else SagePrimary.copy(alpha = 0.3f)
                } else Color.Transparent,
                label = "mode_bg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (mode == "SOCRATIC") SageGold else SageTextPrimary
                } else SageTextMuted,
                label = "mode_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .clickable { onModeChanged(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("mode_${mode.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

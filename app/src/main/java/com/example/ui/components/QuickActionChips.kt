package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageTextPrimary

data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val prompt: String,
    val tag: String
)

@Composable
fun QuickActionChips(
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickActionItem(
            label = "My Roadmap",
            icon = Icons.Default.Map,
            prompt = "What is my current personalized learning roadmap and where am I on it?",
            tag = "quick_action_roadmap"
        ),
        QuickActionItem(
            label = "Quiz Me",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            prompt = "Quiz me on what we've covered so far. Ask one question at a time.",
            tag = "quick_action_quiz"
        ),
        QuickActionItem(
            label = "Recap",
            icon = Icons.Default.AutoStories,
            prompt = "Please give me a concise session summary: what was learned, what was understood, and what comes next.",
            tag = "quick_action_recap"
        ),
        QuickActionItem(
            label = "Explain Simply",
            icon = Icons.Default.Lightbulb,
            prompt = "Can you explain that concept more simply using an intuitive real-world analogy?",
            tag = "quick_action_explain_simply"
        ),
        QuickActionItem(
            label = "Socratic",
            icon = Icons.Default.Psychology,
            prompt = "Switch to Socratic mode: challenge me with a question to test my fundamental intuition on this.",
            tag = "quick_action_socratic"
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            var chipEntered by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { chipEntered = true }

            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.97f else 1f,
                animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
                label = "chip_press"
            )

            val chipAlpha by animateFloatAsState(
                targetValue = if (chipEntered) 1f else 0f,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "chip_alpha"
            )

            val chipY by animateFloatAsState(
                targetValue = if (chipEntered) 0f else 4f,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "chip_y"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = chipAlpha
                        translationY = chipY.dp.toPx()
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(SageGlassL2)
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.16f),
                                SageGlassBorderGlow.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onActionSelected(action.prompt) }
                    )
                    .padding(horizontal = 13.dp, vertical = 7.dp)
                    .testTag(action.tag),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = if (action.label == "My Roadmap") SageGold else SagePrimaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = action.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SageTextPrimary
                    )
                }
            }
        }
    }
}

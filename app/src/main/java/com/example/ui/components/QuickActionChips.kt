package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SageRaisedSurface
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

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
            FilterChip(
                selected = false,
                onClick = { onActionSelected(action.prompt) },
                label = {
                    Text(
                        text = action.label,
                        fontSize = 12.sp,
                        color = SageTextPrimary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = if (action.label == "My Roadmap") SageGold else SagePrimaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = SageRaisedSurface,
                    labelColor = SageTextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = false,
                    borderColor = SageCardBorder
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag(action.tag)
            )
        }
    }
}

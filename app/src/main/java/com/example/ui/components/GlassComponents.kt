package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SageAccentDark
import com.example.ui.theme.SageBackground
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassBorderGlow
import com.example.ui.theme.SageGlassBorderStrong
import com.example.ui.theme.SageGlassBorderSubtle
import com.example.ui.theme.SageGlassL1
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGlassL4
import com.example.ui.theme.SageGlowEnd
import com.example.ui.theme.SageGlowStart
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

enum class GlassLevel {
    L1, // Subtle glass for secondary / grouped content
    L2, // Medium glass for regular cards
    L3, // Strong glass for interactive surfaces
    L4  // Floating glass for overlays, modals, and primary action sheets
}

enum class GlassButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost
}

/**
 * Atmospheric background that renders a dark cinematic foundation (#08080F)
 * with vibrant, slow ambient purple/cyan/violet glows shining beneath glass surfaces.
 */
@Composable
fun AmbientGlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_motion")
    
    // Smooth cinematic ambient phase shifts
    val purpleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "purple_ambient_phase"
    )

    val blueOffset by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blue_ambient_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SageBackground)
            .drawBehind {
                val canvasW = size.width
                val canvasH = size.height

                // 1. Rich upper-left/center vibrant purple atmospheric glow
                val purpleCenter = Offset(
                    x = canvasW * (0.22f + 0.14f * purpleOffset),
                    y = canvasH * (0.12f + 0.10f * purpleOffset)
                )
                val purpleRadius = canvasW * 1.15f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            SageGlowStart.copy(alpha = 0.42f),
                            SagePrimary.copy(alpha = 0.28f),
                            SagePrimaryStart.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = purpleCenter,
                        radius = purpleRadius
                    ),
                    radius = purpleRadius,
                    center = purpleCenter
                )

                // 2. Rich lower-right cyan/electric-blue atmospheric glow
                val blueCenter = Offset(
                    x = canvasW * (0.85f - 0.14f * blueOffset),
                    y = canvasH * (0.76f - 0.10f * blueOffset)
                )
                val blueRadius = canvasW * 1.05f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF06B6D4).copy(alpha = 0.36f),
                            SageAccent.copy(alpha = 0.22f),
                            SageAccentDark.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = blueCenter,
                        radius = blueRadius
                    ),
                    radius = blueRadius,
                    center = blueCenter
                )

                // 3. Subtle mid-center ambient violet warmth
                val midCenter = Offset(
                    x = canvasW * (0.50f + 0.10f * (blueOffset - purpleOffset)),
                    y = canvasH * 0.46f
                )
                val midRadius = canvasW * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.26f),
                            SageGlowEnd.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = midCenter,
                        radius = midRadius
                    ),
                    radius = midRadius,
                    center = midCenter
                )
            },
        content = content
    )
}

/**
 * Premium Frosted Glass Card with translucent layered gradient, specular rim highlight,
 * and responsive micro-interaction press scaling.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.L2,
    shape: Shape = RoundedCornerShape(20.dp),
    border: androidx.compose.foundation.BorderStroke? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    glowColor: Color? = null,
    glowBorder: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring press animation: 1.0f -> 0.985f with fast recovery
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 600f
        ),
        label = "glass_card_press"
    )

    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 600f),
        label = "glass_card_press_glow"
    )

    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "glass_card_alpha"
    )

    // True translucent layered glass brush that lets background atmospheric glow shine through
    val glassSurfaceBrush = remember(level) {
        when (level) {
            GlassLevel.L1 -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color(0x22121226),
                    Color.White.copy(alpha = 0.03f)
                )
            )
            GlassLevel.L2 -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.12f),
                    Color(0x3514142E),
                    Color.White.copy(alpha = 0.04f)
                )
            )
            GlassLevel.L3 -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color(0x45181838),
                    Color.White.copy(alpha = 0.06f)
                )
            )
            GlassLevel.L4 -> Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color(0x60202048),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        }
    }

    // Top-catching specular highlight rim border
    val defaultBorderBrush = remember(borderColor, glowColor, glowBorder, level, border) {
        if (border != null) {
            border.brush
        } else if (borderColor != null) {
            Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.45f)))
        } else if (glowColor != null || glowBorder) {
            val effGlow = glowColor ?: SageGold
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.65f),
                    effGlow.copy(alpha = 0.75f),
                    effGlow.copy(alpha = 0.25f)
                )
            )
        } else {
            when (level) {
                GlassLevel.L1 -> Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.03f)
                    )
                )
                GlassLevel.L2 -> Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.38f),
                        SageGlassBorderGlow.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
                GlassLevel.L3 -> Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.50f),
                        SageGlassBorderGlow.copy(alpha = 0.60f),
                        Color.White.copy(alpha = 0.12f)
                    )
                )
                GlassLevel.L4 -> Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.65f),
                        SagePrimaryLight.copy(alpha = 0.70f),
                        Color.White.copy(alpha = 0.20f)
                    )
                )
            }
        }
    }
    val effectiveWidth = border?.width ?: borderWidth

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = pressAlpha
            }
            .clip(shape)
            .background(glassSurfaceBrush)
            .border(effectiveWidth, defaultBorderBrush, shape)
            .drawBehind {
                // Subtle top specular highlight line (real refractive glass sheen)
                val baseHighlightAlpha = if (level >= GlassLevel.L3) 0.35f else 0.22f
                val effectiveHighlightAlpha = (baseHighlightAlpha + (pressGlow * 0.20f)).coerceAtMost(0.85f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = effectiveHighlightAlpha),
                            Color.Transparent
                        )
                    ),
                    start = Offset(x = size.width * 0.15f, y = 1f),
                    end = Offset(x = size.width * 0.85f, y = 1f),
                    strokeWidth = 1.5f + (pressGlow * 0.5f)
                )
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        content = content
    )
}

/**
 * Premium Interactive Glass Button with translucent gradient,
 * specular highlight, soft glow, and refined 1.0f -> 0.96f tactile press spring.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: GlassButtonVariant = GlassButtonVariant.Primary,
    isPrimary: Boolean = (variant == GlassButtonVariant.Primary),
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "glass_button_scale"
    )

    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "glass_button_press_glow"
    )

    val buttonBrush = when (variant) {
        GlassButtonVariant.Primary -> Brush.verticalGradient(
            listOf(
                SagePrimaryLight.copy(alpha = 0.88f),
                SagePrimary.copy(alpha = 0.76f),
                SagePrimaryStart.copy(alpha = 0.80f)
            )
        )
        GlassButtonVariant.Secondary -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color(0x351A1A35),
                Color.White.copy(alpha = 0.05f)
            )
        )
        GlassButtonVariant.Outline -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.10f),
                Color(0x20141428),
                Color.White.copy(alpha = 0.03f)
            )
        )
        GlassButtonVariant.Ghost -> Brush.verticalGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    val borderBrush = when (variant) {
        GlassButtonVariant.Primary -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.75f),
                SageGlowEnd.copy(alpha = 0.80f),
                Color.White.copy(alpha = 0.25f)
            )
        )
        GlassButtonVariant.Secondary -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                SageGlassBorderGlow.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.10f)
            )
        )
        GlassButtonVariant.Outline -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.40f),
                SageGlassBorderGlow.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.15f)
            )
        )
        GlassButtonVariant.Ghost -> Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent)
        )
    }

    val contentColor = when (variant) {
        GlassButtonVariant.Primary -> Color.White
        GlassButtonVariant.Secondary -> SageTextPrimary
        GlassButtonVariant.Outline -> SagePrimaryLight
        GlassButtonVariant.Ghost -> SageTextSecondary
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .clip(shape)
            .background(buttonBrush)
            .border(1.2.dp, borderBrush, shape)
            .drawBehind {
                // Top inner specular sheen + press brightness
                val baseSheen = if (variant == GlassButtonVariant.Primary) 0.50f else 0.25f
                val effectiveSheen = (baseSheen + (pressGlow * 0.25f)).coerceAtMost(0.90f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = effectiveSheen),
                            Color.Transparent
                        )
                    ),
                    start = Offset(x = size.width * 0.15f, y = 1f),
                    end = Offset(x = size.width * 0.85f, y = 1f),
                    strokeWidth = 1.5f + (pressGlow * 0.5f)
                )
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * Premium Glass Category / Filter Chip with smooth selection transition.
 */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "chip_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) SagePrimaryStart.copy(alpha = 0.75f) else SageGlassL1,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "chip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) SagePrimaryLight.copy(alpha = 0.8f) else SageGlassBorder,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "chip_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else SageTextSecondary,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "chip_text"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else SagePrimaryLight,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Refined Glass Status Badge for Topic states, difficulty levels, and tags.
 */
@Composable
fun GlassBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.38f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * Premium Floating Glass Dock for Bottom Navigation
 * Features a floating rounded pill container with translucent frosted glass,
 * ambient edge highlight, and an animated sliding glowing glass pill indicator.
 */
@Composable
fun GlassDock(
    currentTab: com.example.NavTab,
    onTabSelected: (com.example.NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIndex = when (currentTab) {
        com.example.NavTab.HOME -> 0
        com.example.NavTab.EXPLORE -> 1
        com.example.NavTab.PROGRESS -> 2
        com.example.NavTab.SETTINGS -> 3
    }

    // Physical traveling active pill with spring physics
    val animatedIndex by animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.76f,
            stiffness = 450f
        ),
        label = "dock_traveling_pill"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("bottom_navigation_bar"),
        contentAlignment = Alignment.Center
    ) {
        // Floating glass capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color(0x65151532),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.55f),
                            SageGlassBorderGlow.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.14f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .drawBehind {
                    // Top rim highlight for the floating dock
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(x = size.width * 0.12f, y = 1f),
                        end = Offset(x = size.width * 0.88f, y = 1f),
                        strokeWidth = 1.5f
                    )
                }
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val tabWidth = maxWidth / 4

                // Smooth traveling active glowing glass pill
                Box(
                    modifier = Modifier
                        .offset(x = tabWidth * animatedIndex)
                        .width(tabWidth)
                        .height(56.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SagePrimaryLight.copy(alpha = 0.48f),
                                    SagePrimaryStart.copy(alpha = 0.38f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.65f),
                                    SageGlassBorderGlow.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.20f)
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .drawBehind {
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.70f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(x = size.width * 0.15f, y = 1f),
                                end = Offset(x = size.width * 0.85f, y = 1f),
                                strokeWidth = 1.5f
                            )
                        }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassDockItem(
                        selected = currentTab == com.example.NavTab.HOME,
                        onClick = { onTabSelected(com.example.NavTab.HOME) },
                        icon = androidx.compose.material.icons.Icons.Default.Home,
                        label = "Home",
                        testTag = "nav_tab_home",
                        modifier = Modifier.weight(1f)
                    )
                    GlassDockItem(
                        selected = currentTab == com.example.NavTab.EXPLORE,
                        onClick = { onTabSelected(com.example.NavTab.EXPLORE) },
                        icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.MenuBook,
                        label = "Explore",
                        testTag = "nav_tab_explore",
                        modifier = Modifier.weight(1f)
                    )
                    GlassDockItem(
                        selected = currentTab == com.example.NavTab.PROGRESS,
                        onClick = { onTabSelected(com.example.NavTab.PROGRESS) },
                        icon = androidx.compose.material.icons.Icons.Default.AutoAwesome,
                        label = "Progress",
                        testTag = "nav_tab_progress",
                        modifier = Modifier.weight(1f)
                    )
                    GlassDockItem(
                        selected = currentTab == com.example.NavTab.SETTINGS,
                        onClick = { onTabSelected(com.example.NavTab.SETTINGS) },
                        icon = androidx.compose.material.icons.Icons.Default.Settings,
                        label = "Settings",
                        testTag = "nav_tab_settings",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlassDockItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "dock_item_scale"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.10f else 1f,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 550f),
        label = "dock_icon_scale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.60f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "dock_alpha"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else SageTextSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(3.dp))
            Text(
                text = label,
                color = if (selected) Color.White else SageTextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 11.sp,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha }
            )
        }
    }
}


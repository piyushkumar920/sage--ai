package com.example.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Sage Motion & Animation System
 *
 * Provides calibrated spring physics, easing curves, and transition specs
 * inspired by modern cinematic and Apple-level fluidity:
 * - Damped springs for tactile feedback without abrupt bounce
 * - Smooth cubic beziers for calm, confident transitions
 * - Seamless screen, card, and dialog animations
 */
object SageMotion {
    // Standard durations (ms)
    const val DURATION_FAST = 180
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 420

    // Calibrated Easing Curves
    val EaseOutCubic = CubicBezierEasing(0.215f, 0.610f, 0.355f, 1.0f)
    val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)
    val CinematicDecel = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    // Touch Feedback Spring Physics
    // Responsive, slightly under-damped feel with immediate response
    fun <T> responsiveSpring() = spring<T>(
        dampingRatio = 0.72f,
        stiffness = 650f
    )

    // Gentle Card Spring
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Fluid Screen Transitions
    val ScreenEnter: EnterTransition = fadeIn(
        animationSpec = tween(DURATION_MEDIUM, easing = EaseOutCubic)
    ) + scaleIn(
        initialScale = 0.982f,
        animationSpec = tween(DURATION_MEDIUM, easing = EaseOutCubic)
    )

    val ScreenExit: ExitTransition = fadeOut(
        animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.99f,
        animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)
    )

    // Push / Sub-Screen Transitions
    val SubScreenEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
        animationSpec = tween(DURATION_MEDIUM, easing = EaseOutCubic)
    ) + fadeIn(
        animationSpec = tween(DURATION_MEDIUM, easing = LinearOutSlowInEasing)
    )

    val SubScreenExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() },
        animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(DURATION_FAST, easing = FastOutSlowInEasing)
    )
}

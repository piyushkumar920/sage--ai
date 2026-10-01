package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL2
import com.example.ui.theme.SageGlassL3
import com.example.ui.theme.SageGold
import com.example.ui.theme.SagePrimary
import com.example.ui.theme.SagePrimaryLight
import com.example.ui.theme.SagePrimaryStart
import com.example.ui.theme.SageTextMuted
import com.example.ui.theme.SageTextPrimary
import com.example.ui.theme.SageTextSecondary

/**
 * High-performance, textbook-grade LaTeX & Mathematical Typesetting Engine for Sage.
 * Converts LaTeX formulas into beautifully formatted math expressions with proper fractions,
 * Greek letters, superscripts, subscripts, roots, operators, and mathematical typography.
 */
object MathFormatter {

    private val GREEK_MAP = mapOf(
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "Gamma" to "Γ",
        "delta" to "δ", "Delta" to "Δ", "epsilon" to "ε", "varepsilon" to "ε",
        "zeta" to "ζ", "eta" to "η", "theta" to "θ", "Theta" to "Θ",
        "iota" to "ι", "kappa" to "κ", "lambda" to "λ", "Lambda" to "Λ",
        "mu" to "μ", "nu" to "ν", "xi" to "ξ", "Xi" to "Ξ",
        "pi" to "π", "Pi" to "Π", "rho" to "ρ", "varrho" to "ϱ",
        "sigma" to "σ", "Sigma" to "Σ", "tau" to "τ", "upsilon" to "υ",
        "Upsilon" to "Υ", "phi" to "φ", "varphi" to "ϕ", "Phi" to "Φ",
        "chi" to "χ", "psi" to "ψ", "Psi" to "Ψ", "omega" to "ω", "Omega" to "Ω"
    )

    private val OPERATOR_MAP = mapOf(
        "approx" to "≈", "neq" to "≠", "ne" to "≠", "leq" to "≤", "le" to "≤",
        "geq" to "≥", "ge" to "≥", "pm" to "±", "mp" to "∓", "times" to "×",
        "cdot" to "·", "div" to "÷", "circ" to "°", "degree" to "°",
        "infty" to "∞", "to" to "→", "rightarrow" to "→", "leftarrow" to "←",
        "Rightarrow" to "⇒", "Leftarrow" to "⇐", "iff" to "⇔",
        "forall" to "∀", "exists" to "∃", "in" to "∈", "notin" to "∉",
        "subset" to "⊂", "subseteq" to "⊆", "supset" to "⊃", "supseteq" to "⊇",
        "cap" to "∩", "cup" to "∪", "land" to "∧", "lor" to "∨", "neg" to "¬",
        "sim" to "∼", "simeq" to "≃", "cong" to "≅", "equiv" to "≡", "propto" to "∝",
        "angle" to "∠", "perp" to "⊥", "parallel" to "∥",
        "partial" to "∂", "nabla" to "∇", "int" to "∫", "iint" to "∬",
        "iiint" to "∭", "oint" to "∮", "sum" to "∑", "prod" to "∏",
        "hbar" to "ℏ", "ell" to "ℓ"
    )

    private val SUPERSCRIPT_MAP = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
        'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ',
        'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ',
        'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
        'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ', 'z' to 'ᶻ'
    )

    private val SUBSCRIPT_MAP = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
        'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
        'v' to 'ᵥ', 'x' to 'ₓ', 'β' to 'ᵦ', 'γ' to 'ᵧ', 'ρ' to 'ᵨ',
        'φ' to 'ᵩ', 'χ' to 'ᵪ'
    )

    /**
     * Cleans raw LaTeX by stripping enclosing markdown code blocks and delimiters ($$, $, \[, \]).
     */
    fun cleanRawLatex(input: String): String {
        var s = input.trim()
        // Strip markdown code fences e.g. ```latex ... ``` or ```text ... ```
        if (s.startsWith("```")) {
            val lines = s.lines()
            if (lines.size >= 2) {
                s = lines.drop(1).dropLastWhile { it.startsWith("```") || it.isBlank() }.joinToString("\n").trim()
            }
        }
        // Strip outer delimiters
        if (s.startsWith("$$") && s.endsWith("$$") && s.length >= 4) {
            s = s.substring(2, s.length - 2).trim()
        } else if (s.startsWith("\\[") && s.endsWith("\\]") && s.length >= 4) {
            s = s.substring(2, s.length - 2).trim()
        } else if (s.startsWith("$") && s.endsWith("$") && s.length >= 2) {
            s = s.substring(1, s.length - 1).trim()
        } else if (s.startsWith("\\(") && s.endsWith("\\)") && s.length >= 4) {
            s = s.substring(2, s.length - 2).trim()
        }
        return s
    }

    /**
     * Converts a single LaTeX fraction or expression to readable Unicode representation.
     */
    fun latexToUnicode(input: String): String {
        var text = cleanRawLatex(input)

        // Replace spacing macros
        text = text.replace(Regex("\\\\(quad|qquad)"), "   ")
        text = text.replace(Regex("\\\\([,;:!])"), " ")

        // Replace \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}
        text = text.replace(Regex("\\\\(text|mathrm|mathbf|mathit|operatorname)\\{([^{}]*)\\}"), "$2")

        // Replace \left and \right delimiters
        text = text.replace("\\left(", "(")
            .replace("\\right)", ")")
            .replace("\\left[", "[")
            .replace("\\right]", "]")
            .replace("\\left\\{", "{")
            .replace("\\right\\}", "}")
            .replace("\\left|", "|")
            .replace("\\right|", "|")
            .replace("\\{", "{")
            .replace("\\}", "}")

        // Replace sqrt e.g. \sqrt{x} -> √(x), \sqrt[3]{x} -> ³√(x)
        text = text.replace(Regex("\\\\sqrt\\[([^\\]]+)\\]\\{([^{}]+)\\}")) { m ->
            val root = m.groupValues[1].map { SUPERSCRIPT_MAP[it] ?: it }.joinToString("")
            "${root}√(${m.groupValues[2]})"
        }
        text = text.replace(Regex("\\\\sqrt\\{([^{}]+)\\}")) { m ->
            "√(${m.groupValues[1]})"
        }

        // Replace \frac{a}{b} recursively
        while (text.contains("\\frac")) {
            val prev = text
            text = text.replace(Regex("\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}")) { m ->
                val num = latexToUnicode(m.groupValues[1])
                val den = latexToUnicode(m.groupValues[2])
                if (needsParensInFraction(num) || needsParensInFraction(den)) {
                    "($num) / ($den)"
                } else {
                    "$num / $den"
                }
            }
            if (prev == text) {
                // If regex couldn't resolve nested braces, break to avoid infinite loop
                text = text.replace("\\frac", "")
                break
            }
        }

        // Replace Greek letters
        GREEK_MAP.forEach { (latex, unicode) ->
            text = text.replace(Regex("\\\\$latex(?![a-zA-Z])"), unicode)
        }

        // Replace Operators
        OPERATOR_MAP.forEach { (latex, unicode) ->
            text = text.replace(Regex("\\\\$latex(?![a-zA-Z])"), " $unicode ")
        }

        // Replace common functions \sin, \cos, \tan, \ln, \log, \exp, \lim, \det, \min, \max
        val funcs = listOf("sin", "cos", "tan", "csc", "sec", "cot", "arcsin", "arccos", "arctan", "sinh", "cosh", "tanh", "ln", "log", "exp", "lim", "det", "min", "max")
        funcs.forEach { fn ->
            text = text.replace(Regex("\\\\$fn(?![a-zA-Z])"), fn)
        }

        // Replace superscripts ^{...} or ^x
        text = text.replace(Regex("\\^\\{([^{}]+)\\}")) { m ->
            val content = m.groupValues[1]
            content.map { SUPERSCRIPT_MAP[it] ?: it }.joinToString("")
        }
        text = text.replace(Regex("\\^([0-9a-zA-Z+\\-])")) { m ->
            val char = m.groupValues[1][0]
            SUPERSCRIPT_MAP[char]?.toString() ?: "^$char"
        }

        // Replace subscripts _{...} or _x
        text = text.replace(Regex("_\\{([^{}]+)\\}")) { m ->
            val content = m.groupValues[1]
            val allSubscriptable = content.all { SUBSCRIPT_MAP.containsKey(it) }
            if (allSubscriptable) {
                content.map { SUBSCRIPT_MAP[it]!! }.joinToString("")
            } else {
                "_${content}"
            }
        }
        text = text.replace(Regex("_([0-9a-zA-Z])")) { m ->
            val char = m.groupValues[1][0]
            SUBSCRIPT_MAP[char]?.toString() ?: "_$char"
        }

        // Clean up excessive whitespace
        text = text.replace(Regex("\\s{2,}"), " ").trim()

        return text
    }

    private fun needsParensInFraction(expr: String): Boolean {
        return expr.contains("+") || expr.contains("-") || expr.contains("=") || expr.contains("/")
    }

    /**
     * Builds a richly formatted Jetpack Compose AnnotatedString with Serif Math Typography,
     * italic variables, upright numbers/operators, baseline shifted subscripts and superscripts.
     */
    fun buildMathAnnotatedString(
        latex: String,
        baseColor: Color = SageTextPrimary,
        accentColor: Color = SagePrimaryLight,
        fontSize: TextUnit = 16.sp
    ): AnnotatedString {
        val cleaned = cleanRawLatex(latex)
        val unicode = latexToUnicode(cleaned)

        return buildAnnotatedString {
            var i = 0
            while (i < unicode.length) {
                val c = unicode[i]

                when {
                    // Greek characters
                    c in 'α'..'ω' || c in 'Α'..'Ω' -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    // Operators and mathematical symbols
                    c in listOf('≈', '≠', '≤', '≥', '±', '∓', '×', '·', '÷', '∫', '∬', '∭', '∮', '∑', '∏', '∂', '∇', '√', '∝', '≡', '≅', '∼', '→', '⇒', '⇔', '∞', '∈', '∉', '⊂', '⊆', '∩', '∪', '∧', '∨', '¬') -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = SageGold
                            )
                        ) {
                            append(" $c ")
                        }
                        i++
                    }

                    // Equal sign
                    c == '=' -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                color = SagePrimaryLight
                            )
                        ) {
                            append(" = ")
                        }
                        i++
                    }

                    // Superscript Unicode characters
                    c in SUPERSCRIPT_MAP.values -> {
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Superscript,
                                fontSize = fontSize * 0.75f,
                                fontFamily = FontFamily.Serif,
                                color = baseColor
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    // Subscript Unicode characters
                    c in SUBSCRIPT_MAP.values -> {
                        withStyle(
                            SpanStyle(
                                baselineShift = BaselineShift.Subscript,
                                fontSize = fontSize * 0.75f,
                                fontFamily = FontFamily.Serif,
                                color = baseColor.copy(alpha = 0.9f)
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    // Latin single letters (variables like I, m, x, y, z, t, r) -> Italic Serif
                    c.isLetter() -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                color = baseColor
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    // Numbers and decimal points -> Upright Sans/Serif
                    c.isDigit() || c == '.' -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                color = baseColor
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    // Parentheses, brackets, braces
                    c in listOf('(', ')', '[', ']', '{', '}', '|') -> {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                                color = baseColor.copy(alpha = 0.85f)
                            )
                        ) {
                            append(c)
                        }
                        i++
                    }

                    else -> {
                        withStyle(SpanStyle(color = baseColor)) {
                            append(c)
                        }
                        i++
                    }
                }
            }
        }
    }
}

/**
 * Parses LaTeX stacked fractions to enable stacked Composable rendering if present.
 */
data class FractionParts(val numerator: String, val denominator: String)

/**
 * Model representing a parsed formula equation or block.
 */
sealed class MathExpression {
    data class Simple(val latex: String) : MathExpression()
    data class EquationWithFraction(
        val leftSide: String,
        val fraction: FractionParts,
        val rightSide: String = ""
    ) : MathExpression()
}

object MathExpressionParser {
    /**
     * Attempts to parse a formula with a single prominent fraction, e.g. "I_{avg} = \frac{2 I_m}{\pi} \approx 0.637 I_m"
     */
    fun parse(latex: String): MathExpression {
        val cleaned = MathFormatter.cleanRawLatex(latex)
        val fracRegex = Regex("^(.*?)\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}(.*)$")
        val match = fracRegex.find(cleaned)

        return if (match != null) {
            val left = match.groupValues[1].trim()
            val num = match.groupValues[2].trim()
            val den = match.groupValues[3].trim()
            val right = match.groupValues[4].trim()
            MathExpression.EquationWithFraction(
                leftSide = left,
                fraction = FractionParts(num, den),
                rightSide = right
            )
        } else {
            MathExpression.Simple(cleaned)
        }
    }
}

/**
 * Dedicated, textbook-grade Formula Card Component.
 * Displays formulas with stacked fractions or formatted serif math, copy button, and high contrast glass design.
 */
@Composable
fun MathFormulaCard(
    formula: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    units: String? = null,
    variables: List<String> = emptyList(),
    usage: String? = null,
    example: String? = null
) {
    val context = LocalContext.current
    val parsedExpr = remember(formula) { MathExpressionParser.parse(formula) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SageGlassL3)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        SagePrimaryLight.copy(alpha = 0.5f),
                        SageCardBorder,
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row (if title or units or copy action)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!title.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = SagePrimaryLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            color = SageTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = SageGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "FORMULA",
                            color = SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!units.isNullOrBlank()) {
                        Text(
                            text = "[$units]",
                            color = SageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Math Formula", MathFormatter.latexToUnicode(formula))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Formula copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy formula",
                            tint = SageTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Formula Visual Render Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF070913))
                    .border(1.dp, SagePrimaryStart.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .horizontalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                when (parsedExpr) {
                    is MathExpression.EquationWithFraction -> {
                        StackedFractionFormulaView(
                            left = parsedExpr.leftSide,
                            fraction = parsedExpr.fraction,
                            right = parsedExpr.rightSide
                        )
                    }
                    is MathExpression.Simple -> {
                        val formattedAnnotated = remember(parsedExpr.latex) {
                            MathFormatter.buildMathAnnotatedString(
                                latex = parsedExpr.latex,
                                baseColor = Color.White,
                                accentColor = SagePrimaryLight,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = formattedAnnotated,
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Variables breakdown
            if (variables.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    variables.forEach { v ->
                        val parsedVar = remember(v) { MathFormatter.buildMathAnnotatedString(v, baseColor = SageTextSecondary, fontSize = 12.sp) }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "•", color = SageGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = parsedVar, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Usage & Example
            if (!usage.isNullOrBlank()) {
                Text(
                    text = "Usage: $usage",
                    color = SageTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (!example.isNullOrBlank()) {
                Text(
                    text = "💡 Example: $example",
                    color = com.example.ui.theme.SageSuccess,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Beautiful stacked fraction visual layout:
 * Left side (e.g. I_avg =)  +  Stacked fraction (2I_m / π)  +  Right side (≈ 0.637 I_m)
 */
@Composable
fun StackedFractionFormulaView(
    left: String,
    fraction: FractionParts,
    right: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (left.isNotBlank()) {
            val leftAnnotated = remember(left) {
                MathFormatter.buildMathAnnotatedString(left, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 18.sp)
            }
            Text(
                text = leftAnnotated,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Stacked Vertical Fraction
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            val numAnnotated = remember(fraction.numerator) {
                MathFormatter.buildMathAnnotatedString(fraction.numerator, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 16.sp)
            }
            Text(
                text = numAnnotated,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            // Horizontal Fraction Dividing Bar
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(1.5.dp)
                    .background(SagePrimaryLight)
                    .padding(vertical = 1.dp)
            )

            val denAnnotated = remember(fraction.denominator) {
                MathFormatter.buildMathAnnotatedString(fraction.denominator, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 16.sp)
            }
            Text(
                text = denAnnotated,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        if (right.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            val rightAnnotated = remember(right) {
                MathFormatter.buildMathAnnotatedString(right, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 18.sp)
            }
            Text(
                text = rightAnnotated,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Text component that parses Markdown mixed with LaTeX math ($...$ inline or $$...$$ block)
 * and formats math natively with math typography.
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SageTextPrimary,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    val annotated = remember(text, color, fontSize) {
        buildAnnotatedMathMarkdown(text, color, fontSize)
    }

    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight
    )
}

/**
 * Helper to build an AnnotatedString from markdown text supporting:
 * - Bold: `**bold**`
 * - Inline Math: `$math$` or `\(math\)`
 * - Code: `` `code` ``
 */
fun buildAnnotatedMathMarkdown(
    text: String,
    baseColor: Color = SageTextPrimary,
    fontSize: TextUnit = 15.sp
): AnnotatedString {
    return buildAnnotatedString {
        // Regex that finds inline math ($...$), code (`...`), or bold (**...**)
        val tokenRegex = Regex("(\\$\\$([\\s\\S]*?)\\$\\$|\\$([^$\\n]+)\\$|\\*\\*(.*?)\\*\\*|`([^`]+)`)")
        var lastIndex = 0

        tokenRegex.findAll(text).forEach { match ->
            val plainBefore = text.substring(lastIndex, match.range.first)
            if (plainBefore.isNotEmpty()) {
                withStyle(SpanStyle(color = baseColor)) {
                    append(plainBefore)
                }
            }

            val fullMatch = match.value
            when {
                // Block math $$...$$
                fullMatch.startsWith("$$") && fullMatch.endsWith("$$") -> {
                    val math = match.groupValues[2]
                    val mathAnnotated = MathFormatter.buildMathAnnotatedString(
                        latex = math,
                        baseColor = SagePrimaryLight,
                        accentColor = SageGold,
                        fontSize = fontSize
                    )
                    append(mathAnnotated)
                }

                // Inline math $...$
                fullMatch.startsWith("$") && fullMatch.endsWith("$") -> {
                    val math = match.groupValues[3]
                    val mathAnnotated = MathFormatter.buildMathAnnotatedString(
                        latex = math,
                        baseColor = SagePrimaryLight,
                        accentColor = SageGold,
                        fontSize = fontSize
                    )
                    append(mathAnnotated)
                }

                // Bold **...**
                fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                    val boldText = match.groupValues[4]
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldText)
                    }
                }

                // Inline Code `...`
                fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                    val codeText = match.groupValues[5]
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = SagePrimaryLight,
                            background = Color(0xFF1E1F30)
                        )
                    ) {
                        append(" $codeText ")
                    }
                }
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            val remaining = text.substring(lastIndex)
            withStyle(SpanStyle(color = baseColor)) {
                append(remaining)
            }
        }
    }
}

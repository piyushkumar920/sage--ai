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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Functions
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SageCardBorder
import com.example.ui.theme.SageGlassBorder
import com.example.ui.theme.SageGlassL1
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
 * Unified LaTeX, Mathematical Typesetting, and Markdown Engine for Sage AI.
 * Handles standard engineering formulas, inline math, display equations, markdown headings,
 * bullet lists, numbered lists, blockquotes, bold/italics, and code blocks seamlessly.
 */
object MathFormatter {

    val GREEK_MAP = mapOf(
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

    val OPERATOR_MAP = mapOf(
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

    val SUPERSCRIPT_MAP = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'a' to 'ᵃ', 'b' to 'ᵇ', 'c' to 'ᶜ', 'd' to 'ᵈ', 'e' to 'ᵉ',
        'f' to 'ᶠ', 'g' to 'ᵍ', 'h' to 'ʰ', 'i' to 'ⁱ', 'j' to 'ʲ',
        'k' to 'ᵏ', 'l' to 'ˡ', 'm' to 'ᵐ', 'n' to 'ⁿ', 'o' to 'ᵒ',
        'p' to 'ᵖ', 'r' to 'ʳ', 's' to 'ˢ', 't' to 'ᵗ', 'u' to 'ᵘ',
        'v' to 'ᵛ', 'w' to 'ʷ', 'x' to 'ˣ', 'y' to 'ʸ', 'z' to 'ᶻ'
    )

    val SUBSCRIPT_MAP = mapOf(
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
     * Cleans raw LaTeX by stripping enclosing code fences, $ delimiters, and labels.
     */
    fun cleanRawLatex(input: String): String {
        try {
            var s = input.trim()
            if (s.startsWith("```")) {
                val lines = s.lines()
                if (lines.size >= 2) {
                    s = lines.drop(1).dropLastWhile { it.startsWith("```") || it.isBlank() }.joinToString("\n").trim()
                }
            }
            // Strip leading label if someone generated "**Formula:** $...$"
            s = s.replace(Regex("^\\*\\*(Formula|Equation|Magnitude|Definition):?\\*\\*\\s*"), "")

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
        } catch (_: Exception) {
            return input
        }
    }

    /**
     * Converts a single LaTeX fraction or expression to readable Unicode representation safely.
     */
    fun latexToUnicode(input: String): String {
        try {
            var text = cleanRawLatex(input)

            // Replace spacing macros
            text = text.replace(Regex("\\\\(quad|qquad)"), "   ")
            text = text.replace(Regex("\\\\([,;:!])"), " ")

            // Replace \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}, \operatorname{...}
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

            // Replace square root and nth roots e.g. \sqrt{x} -> √(x), \sqrt[3]{x} -> ³√(x)
            text = text.replace(Regex("\\\\sqrt\\[([^\\]]+)\\]\\{([^{}]+)\\}")) { m ->
                val root = m.groupValues[1].map { SUPERSCRIPT_MAP[it] ?: it }.joinToString("")
                "${root}√(${m.groupValues[2]})"
            }
            text = text.replace(Regex("\\\\sqrt\\{([^{}]+)\\}")) { m ->
                "√(${m.groupValues[1]})"
            }

            // Replace \frac, \dfrac, \tfrac
            var guard = 0
            while ((text.contains("\\frac") || text.contains("\\dfrac") || text.contains("\\tfrac")) && guard < 10) {
                guard++
                val prev = text
                text = text.replace(Regex("\\\\(frac|dfrac|tfrac)\\{([^{}]+)\\}\\{([^{}]+)\\}")) { m ->
                    val num = latexToUnicode(m.groupValues[2])
                    val den = latexToUnicode(m.groupValues[3])
                    if (needsParensInFraction(num) || needsParensInFraction(den)) {
                        "($num) / ($den)"
                    } else {
                        "$num / $den"
                    }
                }
                if (prev == text) {
                    text = text.replace(Regex("\\\\(frac|dfrac|tfrac)"), "")
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

            // Replace common math functions
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
        } catch (_: Exception) {
            return input.replace("$", "")
        }
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
        fontSize: TextUnit = 15.sp
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
 * Model representing a parsed formula equation or block.
 */
data class FractionParts(val numerator: String, val denominator: String)

sealed class MathExpression {
    data class Simple(val latex: String) : MathExpression()
    data class EquationWithFraction(
        val leftSide: String,
        val fraction: FractionParts,
        val rightSide: String = ""
    ) : MathExpression()
}

object MathExpressionParser {
    fun parse(latex: String): MathExpression {
        try {
            val cleaned = MathFormatter.cleanRawLatex(latex)
            val fracRegex = Regex("^(.*?)\\\\(?:frac|dfrac|tfrac)\\{([^{}]+)\\}\\{([^{}]+)\\}(.*)$")
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
        } catch (_: Exception) {
            return MathExpression.Simple(latex)
        }
    }
}

/**
 * Structured block model for Sage Markdown + Math Rendering
 */
sealed class SageRichBlock {
    data class Heading(val level: Int, val text: String) : SageRichBlock()
    data class Paragraph(val text: String) : SageRichBlock()
    data class BulletList(val items: List<String>) : SageRichBlock()
    data class NumberedList(val items: List<Pair<String, String>>) : SageRichBlock()
    data class DisplayMath(val latex: String) : SageRichBlock()
    data class CodeBlock(val code: String, val language: String) : SageRichBlock()
    data class Blockquote(val text: String) : SageRichBlock()
    object Divider : SageRichBlock()
}

/**
 * Robust, production-grade parser for mixed Markdown and LaTeX equations.
 */
object SageMarkdownParser {

    fun parseBlocks(content: String): List<SageRichBlock> {
        val blocks = mutableListOf<SageRichBlock>()
        if (content.isBlank()) return blocks

        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            // 1. Check for Code Fence ```
            if (trimmed.startsWith("```")) {
                val lang = trimmed.removePrefix("```").trim()
                val codeBuilder = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeBuilder.append(lines[i]).append("\n")
                    i++
                }
                if (i < lines.size) i++ // skip closing ```
                val code = codeBuilder.toString().trimEnd()
                if (lang.lowercase() in listOf("latex", "math", "tex")) {
                    blocks.add(SageRichBlock.DisplayMath(code))
                } else {
                    blocks.add(SageRichBlock.CodeBlock(code = code, language = lang))
                }
                continue
            }

            // 2. Check for Display Math $$...$$ on single or multi-line
            if (trimmed.startsWith("$$") || trimmed.startsWith("\\[")) {
                val isBracket = trimmed.startsWith("\\[")
                val closingDelimiter = if (isBracket) "\\]" else "$$"
                
                if (trimmed.endsWith(closingDelimiter) && trimmed.length >= 4) {
                    val math = trimmed.substring(2, trimmed.length - 2).trim()
                    blocks.add(SageRichBlock.DisplayMath(math))
                    i++
                    continue
                } else {
                    val mathBuilder = StringBuilder(trimmed.removePrefix("$$").removePrefix("\\["))
                    i++
                    while (i < lines.size && !lines[i].trim().endsWith(closingDelimiter)) {
                        mathBuilder.append("\n").append(lines[i])
                        i++
                    }
                    if (i < lines.size) {
                        mathBuilder.append("\n").append(lines[i].trim().removeSuffix(closingDelimiter))
                        i++
                    }
                    blocks.add(SageRichBlock.DisplayMath(mathBuilder.toString().trim()))
                    continue
                }
            }

            // 3. Check for Headings (#, ##, ###, ####, #####, ######)
            val headingMatch = Regex("^(#{1,6})\\s+(.*)$").find(trimmed)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length
                val title = headingMatch.groupValues[2].trim()
                blocks.add(SageRichBlock.Heading(level = level, text = title))
                i++
                continue
            }

            // 4. Check for Horizontal Rule
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                blocks.add(SageRichBlock.Divider)
                i++
                continue
            }

            // 5. Check for Blockquote
            if (trimmed.startsWith(">")) {
                val quoteBuilder = StringBuilder(trimmed.removePrefix(">").trim())
                i++
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    quoteBuilder.append(" ").append(lines[i].trim().removePrefix(">").trim())
                    i++
                }
                blocks.add(SageRichBlock.Blockquote(quoteBuilder.toString()))
                continue
            }

            // 6. Check for Bullet List (- , * , + )
            if (isBulletLine(trimmed)) {
                val listItems = mutableListOf<String>()
                while (i < lines.size && isBulletLine(lines[i].trim())) {
                    val itemText = lines[i].trim().replaceFirst(Regex("^[-*+]\\s+"), "")
                    listItems.add(itemText)
                    i++
                }
                blocks.add(SageRichBlock.BulletList(listItems))
                continue
            }

            // 7. Check for Numbered List (1. , 2. )
            val numMatch = Regex("^([0-9]+[.)])\\s+(.*)$").find(trimmed)
            if (numMatch != null) {
                val listItems = mutableListOf<Pair<String, String>>()
                while (i < lines.size) {
                    val curMatch = Regex("^([0-9]+[.)])\\s+(.*)$").find(lines[i].trim())
                    if (curMatch != null) {
                        listItems.add(Pair(curMatch.groupValues[1], curMatch.groupValues[2]))
                        i++
                    } else {
                        break
                    }
                }
                blocks.add(SageRichBlock.NumberedList(listItems))
                continue
            }

            // 8. Skip blank lines
            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // 9. Check if paragraph is standalone display math formula line
            if (isStandaloneMathFormula(trimmed)) {
                blocks.add(SageRichBlock.DisplayMath(trimmed))
                i++
                continue
            }

            // 10. General Paragraph (aggregates consecutive non-empty lines)
            val paraBuilder = StringBuilder(trimmed)
            i++
            while (i < lines.size) {
                val nextTrimmed = lines[i].trim()
                if (nextTrimmed.isEmpty() ||
                    nextTrimmed.startsWith("```") ||
                    nextTrimmed.startsWith("$$") ||
                    nextTrimmed.startsWith("\\[") ||
                    nextTrimmed.startsWith("#") ||
                    nextTrimmed.startsWith(">") ||
                    isBulletLine(nextTrimmed) ||
                    Regex("^([0-9]+[.)])\\s+").containsMatchIn(nextTrimmed) ||
                    nextTrimmed == "---" ||
                    isStandaloneMathFormula(nextTrimmed)
                ) {
                    break
                }
                paraBuilder.append("\n").append(nextTrimmed)
                i++
            }

            blocks.add(SageRichBlock.Paragraph(paraBuilder.toString()))
        }

        return blocks
    }

    private fun isBulletLine(line: String): Boolean {
        return line.startsWith("- ") || line.startsWith("* ") || line.startsWith("+ ")
    }

    private fun isStandaloneMathFormula(line: String): Boolean {
        // e.g. "X_L = 2\pi fL" or "Z = R + jX" or "V_{rms} = \frac{V_m}{\sqrt{2}}"
        if (line.startsWith("$$") || line.startsWith("\\[")) return true
        if (line.contains("\\frac") || line.contains("\\sqrt") || line.contains("\\sum") || line.contains("\\int") || line.contains("\\approx")) {
            if (!line.contains(" ") || (line.contains("=") && line.length < 60)) {
                return true
            }
        }
        return false
    }
}

/**
 * Builds an AnnotatedString from inline markdown + LaTeX expressions.
 * Handles:
 * - Bold: `**text**` or `__text__`
 * - Italic: `*text*`
 * - Inline Code: `` `code` ``
 * - Inline Math: `$math$` or `\(math\)`
 * - Formatted Greek and Math operators
 */
fun buildAnnotatedMathMarkdown(
    text: String,
    baseColor: Color = SageTextPrimary,
    fontSize: TextUnit = 14.sp
): AnnotatedString {
    return buildAnnotatedString {
        // Regex matches $$...$$, $...$, \(...\), `...`, **...**, *...*
        val tokenRegex = Regex("(\\$\\$([\\s\\S]*?)\\$\\$|\\$([^$\\n]+)\\$|\\\\\\(([\\s\\S]*?)\\\\\\)|`([^`]+)`|\\*\\*(.*?)\\*\\*|\\*([^*\\n]+)\\*)")
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
                // Display Math $$...$$
                fullMatch.startsWith("$$") && fullMatch.endsWith("$$") -> {
                    val math = match.groupValues[2]
                    append(MathFormatter.buildMathAnnotatedString(math, baseColor = SagePrimaryLight, accentColor = SageGold, fontSize = fontSize))
                }

                // Inline Math $...$
                fullMatch.startsWith("$") && fullMatch.endsWith("$") -> {
                    val math = match.groupValues[3]
                    append(MathFormatter.buildMathAnnotatedString(math, baseColor = SagePrimaryLight, accentColor = SageGold, fontSize = fontSize))
                }

                // Inline Math \(...\)
                fullMatch.startsWith("\\(") && fullMatch.endsWith("\\)") -> {
                    val math = match.groupValues[4]
                    append(MathFormatter.buildMathAnnotatedString(math, baseColor = SagePrimaryLight, accentColor = SageGold, fontSize = fontSize))
                }

                // Inline Code `...`
                fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                    val codeText = match.groupValues[5]
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = SagePrimaryLight,
                            background = Color(0xFF1E1F30),
                            fontSize = fontSize * 0.9f
                        )
                    ) {
                        append(" $codeText ")
                    }
                }

                // Bold **...**
                fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                    val boldText = match.groupValues[6]
                    // Inside bold text, check if there's math or greek
                    val boldAnnotated = buildAnnotatedMathMarkdown(boldText, baseColor = baseColor, fontSize = fontSize)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(boldAnnotated)
                    }
                }

                // Italic *...*
                fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
                    val italicText = match.groupValues[7]
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(italicText)
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

/**
 * Universal Master Rich Text Component for Sage AI.
 * Use this across all Study Tools, Chat messages, and academic cards to render
 * Markdown + LaTeX mathematics consistently and beautifully.
 */
@Composable
fun SageRichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SageTextPrimary,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    blockSpacing: Dp = 6.dp,
    textAlign: TextAlign = TextAlign.Start
) {
    val blocks = remember(text) { SageMarkdownParser.parseBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(blockSpacing)
    ) {
        blocks.forEach { block ->
            when (block) {
                is SageRichBlock.Heading -> {
                    val headingFontSize = when (block.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        3 -> 16.sp
                        4 -> 14.sp
                        else -> 13.sp
                    }
                    val headingColor = when (block.level) {
                        1, 2 -> SageTextPrimary
                        3 -> SageGold
                        4 -> SagePrimaryLight
                        else -> SageTextPrimary
                    }
                    val headingAnnotated = remember(block.text, headingColor, headingFontSize) {
                        buildAnnotatedMathMarkdown(block.text, baseColor = headingColor, fontSize = headingFontSize)
                    }
                    Text(
                        text = headingAnnotated,
                        fontSize = headingFontSize,
                        fontWeight = FontWeight.Bold,
                        color = headingColor,
                        lineHeight = (headingFontSize.value * 1.35f).sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is SageRichBlock.Paragraph -> {
                    val annotated = remember(block.text, color, fontSize) {
                        buildAnnotatedMathMarkdown(block.text, baseColor = color, fontSize = fontSize)
                    }
                    Text(
                        text = annotated,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        color = color,
                        textAlign = textAlign
                    )
                }

                is SageRichBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        block.items.forEach { item ->
                            val itemAnnotated = remember(item, color, fontSize) {
                                buildAnnotatedMathMarkdown(item, baseColor = color, fontSize = fontSize)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = SageGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize
                                )
                                Text(
                                    text = itemAnnotated,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                    color = color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                is SageRichBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        block.items.forEach { (prefix, item) ->
                            val itemAnnotated = remember(item, color, fontSize) {
                                buildAnnotatedMathMarkdown(item, baseColor = color, fontSize = fontSize)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = prefix,
                                    color = SagePrimaryLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize
                                )
                                Text(
                                    text = itemAnnotated,
                                    fontSize = fontSize,
                                    lineHeight = lineHeight,
                                    color = color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                is SageRichBlock.DisplayMath -> {
                    MathFormulaCard(formula = block.latex)
                }

                is SageRichBlock.CodeBlock -> {
                    CodeBlockView(code = block.code, language = block.language)
                }

                is SageRichBlock.Blockquote -> {
                    val quoteAnnotated = remember(block.text, fontSize) {
                        buildAnnotatedMathMarkdown(block.text, baseColor = SageTextSecondary, fontSize = fontSize)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SageGlassL1)
                            .border(1.dp, SageCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .background(SageGold, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = quoteAnnotated,
                                fontSize = fontSize,
                                fontStyle = FontStyle.Italic,
                                color = SageTextSecondary,
                                lineHeight = lineHeight
                            )
                        }
                    }
                }

                is SageRichBlock.Divider -> {
                    HorizontalDivider(
                        color = SageGlassBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact inline text renderer for single lines, chip labels, definitions, and headers.
 */
@Composable
fun SageInlineRichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SageTextPrimary,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start
) {
    val annotated = remember(text, color, fontSize) {
        buildAnnotatedMathMarkdown(text, baseColor = color, fontSize = fontSize)
    }

    Text(
        text = annotated,
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign
    )
}

/**
 * Backwards-compatibility alias for MathText -> delegates to SageRichText
 */
@Composable
fun MathText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = SageTextPrimary,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    SageRichText(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight
    )
}

/**
 * Dedicated textbook-grade Formula Card Component.
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
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row (title / units / copy)
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
                        SageInlineRichText(
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
                            text = "EQUATION",
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
                    .padding(horizontal = 14.dp, vertical = 10.dp)
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
                                fontSize = 17.sp
                            )
                        }
                        Text(
                            text = formattedAnnotated,
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "•", color = SageGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            SageInlineRichText(text = v, color = SageTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Usage & Example
            if (!usage.isNullOrBlank()) {
                SageRichText(
                    text = "Usage: $usage",
                    color = SageTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (!example.isNullOrBlank()) {
                SageRichText(
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
 * Stacked fraction visual layout
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
                MathFormatter.buildMathAnnotatedString(left, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 17.sp)
            }
            Text(
                text = leftAnnotated,
                fontSize = 17.sp,
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
                MathFormatter.buildMathAnnotatedString(fraction.numerator, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 15.sp)
            }
            Text(
                text = numAnnotated,
                fontSize = 15.sp,
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
                MathFormatter.buildMathAnnotatedString(fraction.denominator, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 15.sp)
            }
            Text(
                text = denAnnotated,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        if (right.isNotBlank()) {
            Spacer(modifier = Modifier.width(6.dp))
            val rightAnnotated = remember(right) {
                MathFormatter.buildMathAnnotatedString(right, baseColor = Color.White, accentColor = SagePrimaryLight, fontSize = 17.sp)
            }
            Text(
                text = rightAnnotated,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

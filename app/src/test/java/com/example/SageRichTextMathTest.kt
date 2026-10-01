package com.example

import com.example.ui.components.MathExpression
import com.example.ui.components.MathExpressionParser
import com.example.ui.components.MathFormatter
import com.example.ui.components.SageMarkdownParser
import com.example.ui.components.SageRichBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SageRichTextMathTest {

    // TEST 1: Inline math rendering
    @Test
    fun `test 1 inline math formatting with Greek letters and subscripts`() {
        val input = "\$X_L = 2\\pi fL\$"
        val unicode = MathFormatter.latexToUnicode(input)
        assertTrue("Should contain subscript L: $unicode", unicode.contains("X_L") || unicode.contains("Xₗ") || unicode.contains("X_L"))
        assertTrue("Should contain pi π: $unicode", unicode.contains("π"))
        assertFalse("Should not have literal backslash: $unicode", unicode.contains("\\pi"))
        assertFalse("Should not have dollar signs: $unicode", unicode.contains("$"))
    }

    // TEST 2: Display math rendering with fraction
    @Test
    fun `test 2 display math with fraction`() {
        val input = "\$\$X_C = \\frac{1}{2\\pi fC}\$\$"
        val unicode = MathFormatter.latexToUnicode(input)
        assertTrue("Should contain pi π: $unicode", unicode.contains("π"))
        assertTrue("Should contain division /: $unicode", unicode.contains("/"))
        assertFalse("Should not have raw frac: $unicode", unicode.contains("\\frac"))
    }

    // TEST 3: Square root + subscripts + superscripts + parentheses
    @Test
    fun `test 3 square root with superscripts and subscripts`() {
        val input = "\$\$|Z| = \\sqrt{R^2 + (X_L-X_C)^2}\$\$"
        val unicode = MathFormatter.latexToUnicode(input)
        assertTrue("Should contain root √: $unicode", unicode.contains("√"))
        assertTrue("Should contain superscript 2: $unicode", unicode.contains("²") || unicode.contains("^2"))
        assertFalse("Should not contain raw sqrt: $unicode", unicode.contains("\\sqrt"))
    }

    // TEST 4: Subscripts + fraction + square root
    @Test
    fun `test 4 subscripts fraction square root`() {
        val input = "\$\$V_{rms} = \\frac{V_m}{\\sqrt{2}}\$\$"
        val unicode = MathFormatter.latexToUnicode(input)
        assertTrue("Should contain root √: $unicode", unicode.contains("√"))
        assertTrue("Should contain division /: $unicode", unicode.contains("/"))
        assertFalse("Should not contain raw frac: $unicode", unicode.contains("\\frac"))
        assertFalse("Should not contain raw sqrt: $unicode", unicode.contains("\\sqrt"))
    }

    // TEST 5: Heading + bold Markdown
    @Test
    fun `test 5 heading and bold markdown parsing`() {
        val input = "#### Impedance\n\n**Definition:** Total opposition to AC."
        val blocks = SageMarkdownParser.parseBlocks(input)
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is SageRichBlock.Heading)
        assertEquals(4, (blocks[0] as SageRichBlock.Heading).level)
        assertEquals("Impedance", (blocks[0] as SageRichBlock.Heading).text)
        assertTrue(blocks[1] is SageRichBlock.Paragraph)
        assertTrue((blocks[1] as SageRichBlock.Paragraph).text.contains("**Definition:**"))
    }

    // TEST 6: Markdown + display math together
    @Test
    fun `test 6 markdown and display math together`() {
        val input = "**Inductive Reactance**\n\n\$\$X_L = 2\\pi fL\$\$"
        val blocks = SageMarkdownParser.parseBlocks(input)
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is SageRichBlock.Paragraph)
        assertTrue(blocks[1] is SageRichBlock.DisplayMath)
        assertEquals("X_L = 2\\pi fL", (blocks[1] as SageRichBlock.DisplayMath).latex)
    }

    // TEST 7: Bullet list parsing
    @Test
    fun `test 7 bullet list parsing`() {
        val input = "- X_L = inductive reactance\n- f = frequency\n- L = inductance"
        val blocks = SageMarkdownParser.parseBlocks(input)
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is SageRichBlock.BulletList)
        val list = (blocks[0] as SageRichBlock.BulletList).items
        assertEquals(3, list.size)
        assertEquals("X_L = inductive reactance", list[0])
        assertEquals("f = frequency", list[1])
        assertEquals("L = inductance", list[2])
    }

    // TEST 8: Malformed LaTeX must not crash
    @Test
    fun `test 8 malformed latex safety`() {
        val malformed1 = "\$\\frac{1}{2\\pi\$"
        val out1 = MathFormatter.latexToUnicode(malformed1)
        assertNotNull(out1)

        val malformed2 = "\$\\sqrt{R^2 + X\$"
        val out2 = MathFormatter.latexToUnicode(malformed2)
        assertNotNull(out2)

        val parsed = MathExpressionParser.parse(malformed1)
        assertNotNull(parsed)
    }

    // TEST 9, 10, 11: Complex Academic Markdown with multiple formulas
    @Test
    fun `test complex academic RLC explanation parsing`() {
        val sample = "#### Impedance in an RLC Circuit\n\n" +
                "**Definition:** Impedance is the total opposition offered by an RLC circuit to alternating current.\n\n" +
                "The impedance is:\n\n" +
                "\$\$Z = R + jX\$\$\n\n" +
                "where:\n\n" +
                "\$\$X = X_L - X_C\$\$\n\n" +
                "Inductive reactance:\n\n" +
                "\$\$X_L = 2\\pi fL\$\$\n\n" +
                "Capacitive reactance:\n\n" +
                "\$\$X_C = \\frac{1}{2\\pi fC}\$\$\n\n" +
                "Magnitude of impedance:\n\n" +
                "\$\$|Z| = \\sqrt{R^2 + (X_L-X_C)^2}\$\$"

        val blocks = SageMarkdownParser.parseBlocks(sample)
        assertTrue("Should have multiple structured blocks", blocks.size >= 8)

        val headings = blocks.filterIsInstance<SageRichBlock.Heading>()
        assertEquals(1, headings.size)
        assertEquals("Impedance in an RLC Circuit", headings[0].text)

        val mathBlocks = blocks.filterIsInstance<SageRichBlock.DisplayMath>()
        assertTrue("Should identify all display math equations", mathBlocks.size >= 5)
    }

    // TEST 12: Stacked fraction extraction for formula card
    @Test
    fun `test equation with stacked fraction parsing`() {
        val formula = "I_{avg} = \\frac{2I_m}{\\pi} \\approx 0.637 I_m"
        val expr = MathExpressionParser.parse(formula)
        assertTrue(expr is MathExpression.EquationWithFraction)
        val fracExpr = expr as MathExpression.EquationWithFraction
        assertEquals("I_{avg} =", fracExpr.leftSide)
        assertEquals("2I_m", fracExpr.fraction.numerator)
        assertEquals("\\pi", fracExpr.fraction.denominator)
        assertTrue(fracExpr.rightSide.contains("0.637"))
    }

    // TEST 13: Numbered list and blockquote parsing
    @Test
    fun `test numbered list and blockquote`() {
        val text = "1. First step\n2. Second step\n\n> Important exam tip: always verify resonance frequency."
        val blocks = SageMarkdownParser.parseBlocks(text)
        assertTrue(blocks.any { it is SageRichBlock.NumberedList })
        assertTrue(blocks.any { it is SageRichBlock.Blockquote })
    }
}

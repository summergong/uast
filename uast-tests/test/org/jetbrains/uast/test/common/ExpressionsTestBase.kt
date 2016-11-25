package org.jetbrains.uast.test.common

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UFile
import org.jetbrains.uast.evaluation.valueOf
import org.jetbrains.uast.test.env.assertEqualsToFile
import org.jetbrains.uast.visitor.UastVisitor
import java.io.File

interface ExpressionsTestBase {
    fun getValuesFile(testName: String): File

    private fun UFile.asLogValues(): String {
        return ValueLogger().apply {
            this@asLogValues.accept(this)
        }.toString()
    }

    fun check(testName: String, file: UFile) {
        val valuesFile = getValuesFile(testName)

        assertEqualsToFile("Log values", valuesFile, file.asLogValues())
    }

    class ValueLogger : UastVisitor {

        val builder = StringBuilder()

        var level = 0

        override fun visitElement(node: UElement): Boolean {
            val initialLine = node.asLogString() + " [" + run {
                val renderString = node.asRenderString().lines()
                if (renderString.size == 1) {
                    renderString.single()
                } else {
                    renderString.first() + "..." + renderString.last()
                }
            } + "]"

            (1..level).forEach { builder.append("    ") }
            builder.append(initialLine)
            if (node is UExpression) {
                val value = node.valueOf()
                builder.append(" = ").append(value)
            }
            builder.appendln()
            level++
            return false
        }

        override fun afterVisitElement(node: UElement) {
            level--
        }

        override fun toString() = builder.toString()
    }
}
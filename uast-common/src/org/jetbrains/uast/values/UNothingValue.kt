package org.jetbrains.uast.values

import org.jetbrains.uast.*

// Something that never can be reached / created
internal class UNothingValue private constructor(
        val containingLoopOrSwitch: UExpression?,
        val kind: JumpKind
) : UValueBase() {

    constructor(jump: UJumpExpression) : this(jump.containingLoopOrSwitch(), jump.kind())

    constructor() : this(null, JumpKind.OTHER)

    enum class JumpKind {
        BREAK,
        CONTINUE,
        OTHER;
    }

    override val reachable = false

    override fun merge(other: UValue) = when (other) {
        is UNothingValue -> {
            val mergedLoopOrSwitch =
                    if (containingLoopOrSwitch == other.containingLoopOrSwitch) containingLoopOrSwitch
                    else null
            val mergedKind = if (mergedLoopOrSwitch == null || kind != other.kind) JumpKind.OTHER else kind
            UNothingValue(mergedLoopOrSwitch, mergedKind)
        }
        else -> super.merge(other)
    }

    override fun toString() = "Nothing" + when (kind) {
        JumpKind.BREAK -> "(break)"
        JumpKind.CONTINUE -> "(continue)"
        else -> ""
    }

    companion object {
        private fun UJumpExpression.containingLoopOrSwitch(): UExpression? {
            var containingElement = uastParent
            while (containingElement != null) {
                if (this is UBreakExpression && label == null && containingElement is USwitchExpression) {
                    return containingElement
                }
                if (containingElement is ULoopExpression) {
                    val containingLabeled = containingElement.uastParent as? ULabeledExpression
                    if (label == null || label == containingLabeled?.label) {
                        return containingElement
                    }
                }
                containingElement = containingElement.uastParent
            }
            return null
        }

        private fun UExpression.kind(): JumpKind = when (this) {
            is UBreakExpression -> JumpKind.BREAK
            is UContinueExpression -> JumpKind.CONTINUE
            else -> JumpKind.OTHER
        }
    }
}

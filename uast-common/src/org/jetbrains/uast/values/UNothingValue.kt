package org.jetbrains.uast.values

import org.jetbrains.uast.*

// Something that never can be reached / created
class UNothingValue private constructor(val containingLoopOrSwitch: UExpression?, val kind: JumpKind) : UValueBase() {

    constructor(jump: UExpression?) : this(jump?.containingLoopOrSwitch(), jump?.kind() ?: JumpKind.OTHER)

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
        private fun UExpression.containingLoopOrSwitch(): UExpression? {
            val label = (this as? UJumpExpression)?.label
            var containingElement = containingElement
            while (containingElement != null) {
                if (this is UBreakExpression && label == null && containingElement is USwitchExpression) {
                    return containingElement
                }
                if (containingElement is ULoopExpression) {
                    val containingLabeled = containingElement.containingElement as? ULabeledExpression
                    if (label == null || label == containingLabeled?.label) {
                        return containingElement
                    }
                }
                containingElement = containingElement.containingElement
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

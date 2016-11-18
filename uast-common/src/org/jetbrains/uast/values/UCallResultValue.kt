package org.jetbrains.uast.values

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UResolvable

// Value of something resolvable (e.g. call or property access)
// that we cannot or do not want to evaluate
class UCallResultValue(val resolvable: UResolvable) : UValueBase(), UDependency {
    override fun equals(other: Any?) = other is UCallResultValue && resolvable == other.resolvable

    override fun hashCode() = resolvable.hashCode()

    override fun toString(): String {
        return "external ${(resolvable as? UElement)?.asRenderString() ?: "???"}"
    }
}

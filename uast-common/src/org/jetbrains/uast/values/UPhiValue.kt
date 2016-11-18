package org.jetbrains.uast.values

class UPhiValue private constructor(val values: Set<UValue>): UValueBase() {

    override val dependencies: Set<UDependency> = values.flatMapTo(linkedSetOf()) { it.dependencies }

    override fun equals(other: Any?) = other is UPhiValue && values == other.values

    override fun hashCode() = values.hashCode()

    override fun toString() = values.joinToString(prefix = "Phi(", postfix = ")", separator = ", ")

    companion object {
        fun create(values: Iterable<UValue>): UPhiValue {
            val flattenedValues = values.flatMapTo(linkedSetOf<UValue>()) { (it as? UPhiValue)?.values ?: listOf(it) }
            if (flattenedValues.size <= 1) {
                throw AssertionError("UPhiValue should contain two or more values: $flattenedValues")
            }
            return UPhiValue(flattenedValues)
        }

        fun create(vararg values: UValue) = create(values.asIterable())
    }
}


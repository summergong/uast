package org.jetbrains.uast

interface UImportStatement : UResolvable {
    val onDemand: Boolean
}
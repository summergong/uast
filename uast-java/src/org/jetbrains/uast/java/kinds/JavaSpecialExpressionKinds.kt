package org.jetbrains.uast.java.kinds

import org.jetbrains.uast.UastSpecialExpressionKind

object JavaSpecialExpressionKinds {
    @JvmField
    val SWITCH = UastSpecialExpressionKind("switch")

    @JvmField
    val SWITCH_ENTRY = UastSpecialExpressionKind("switch_entry")
}
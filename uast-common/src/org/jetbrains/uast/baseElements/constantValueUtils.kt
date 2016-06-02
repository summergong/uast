/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("UastConstantValueUtils")
package org.jetbrains.uast.util

import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UastExpressionFactory

fun createUConstantValue(o: Any?, factory: UastExpressionFactory) = when (o) {
    null -> UNullValue(factory)
    is Int -> UIntValue(o, factory)
    is Byte -> UByteValue(o, factory)
    is Short -> UShortValue(o, factory)
    is Boolean -> UBooleanValue(o, factory)
    is Long -> ULongValue(o, factory)
    is Float -> UFloatValue(o, factory)
    is Double -> UDoubleValue(o, factory)
    is String -> UStringValue(o, factory)
    is Char -> UCharValue(o, factory)
    else -> UErrorValue(factory)
}
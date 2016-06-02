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

package org.jetbrains.uast

import org.jetbrains.uast.expressions.UastExpressionFactory

interface UConstantValue<T> {
    val value: T
    val original: UExpression?
}

interface USimpleConstantValue<T> : UConstantValue<T>

open class UAnnotationValue(override val value: UAnnotation, originalFactory: UastExpressionFactory) : UConstantValue<UAnnotation> {
    override val original by lz { originalFactory() }
}

open class UArrayValue(
        override val value: List<UConstantValue<*>>,
        originalFactory: UastExpressionFactory
) : UConstantValue<List<UConstantValue<*>>> {
    override val original by lz { originalFactory() }
}

open class UEnumValue(
        override val value: UType?,
        val enumType: UType,
        val valueName: String,
        originalFactory: UastExpressionFactory) : UConstantValue<UType?> {
    override val original by lz { originalFactory() }
}

open class UErrorValue(originalFactory: UastExpressionFactory) : UConstantValue<Unit> {
    override val value = Unit
    override val original by lz { originalFactory() }
}

interface UIntegralValue<T> : USimpleConstantValue<T>

interface URealValue<T> : USimpleConstantValue<T>

open class UDoubleValue(override val value: Double, originalFactory: UastExpressionFactory) : URealValue<Double> {
    override val original by lz { originalFactory() }
}

open class UFloatValue(override val value: Float, originalFactory: UastExpressionFactory) : URealValue<Float> {
    override val original by lz { originalFactory() }
}

open class UCharValue(override val value: Char, originalFactory: UastExpressionFactory) : USimpleConstantValue<Char> {
    override val original by lz { originalFactory() }
}

open class UByteValue(override val value: Byte, originalFactory: UastExpressionFactory) : UIntegralValue<Byte> {
    override val original by lz { originalFactory() }
}

open class UIntValue(override val value: Int, originalFactory: UastExpressionFactory) : UIntegralValue<Int> {
    override val original by lz { originalFactory() }
}

open class ULongValue(override val value: Long, originalFactory: UastExpressionFactory) : UIntegralValue<Long> {
    override val original by lz { originalFactory() }
}

open class UShortValue(override val value: Short, originalFactory: UastExpressionFactory) : UIntegralValue<Short> {
    override val original by lz { originalFactory() }
}

open class UBooleanValue(override val value: Boolean, originalFactory: UastExpressionFactory) : USimpleConstantValue<Boolean> {
    override val original by lz { originalFactory() }
}

open class UStringValue(override val value: String, originalFactory: UastExpressionFactory) : USimpleConstantValue<String> {
    override val original by lz { originalFactory() }
}

open class UTypeValue(override val value: UType, originalFactory: UastExpressionFactory) : UConstantValue<UType> {
    override val original by lz { originalFactory() }
}

open class UExpressionValue(override val value: UExpression, originalFactory: UastExpressionFactory) : UConstantValue<UExpression> {
    override val original by lz { originalFactory() }
}

open class UNullValue(originalFactory: UastExpressionFactory) : UConstantValue<Any?> {
    override val value: Any?
        get() = null

    override val original by lz { originalFactory() }
}
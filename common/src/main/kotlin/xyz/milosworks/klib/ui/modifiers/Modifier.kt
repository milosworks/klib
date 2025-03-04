/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.milosworks.klib.ui.modifiers

import net.minecraft.network.chat.Component

/**
 * An ordered, immutable collection of [modifier elements][Modifier.Element] that decorate or add
 * behavior to Compose UI elements. For example, backgrounds, padding and click event listeners
 * decorate or add behavior to rows, text or buttons.
 *
 * This class is taken from the androidx Jetpack Compose UI library so as to avoid extra dependencies.
 */
interface Modifier {

	/**
	 * Accumulates a value starting with [initial] and applying [operation] to the current value
	 * and each element from outside in.
	 *
	 * Elements wrap one another in a chain from left to right; an [Element] that appears to the
	 * left of another in a `+` expression or in [operation]'s parameter order affects all
	 * of the elements that appear after it. [foldIn] may be used to accumulate a value starting
	 * from the parent or head of the modifier chain to the final wrapped child.
	 */
	fun <R> foldIn(initial: R, operation: (R, Element<*>) -> R): R

	/**
	 * Accumulates a value starting with [initial] and applying [operation] to the current value
	 * and each element from inside out.
	 *
	 * Elements wrap one another in a chain from left to right; an [Element] that appears to the
	 * left of another in a `+` expression or in [operation]'s parameter order affects all
	 * of the elements that appear after it. [foldOut] may be used to accumulate a value starting
	 * from the child or tail of the modifier chain up to the parent or head of the chain.
	 */
	fun <R> foldOut(initial: R, operation: (Element<*>, R) -> R): R

	/**
	 * Returns `true` if [predicate] returns true for any [Element] in this [Modifier].
	 */
	fun any(predicate: (Element<*>) -> Boolean): Boolean

	/**
	 * Returns `true` if [predicate] returns true for all [Element]s in this [Modifier] or if
	 * this [Modifier] contains no [Element]s.
	 */
	fun all(predicate: (Element<*>) -> Boolean): Boolean

	/**
	 * Concatenates this modifier with another.
	 *
	 * Returns a [Modifier] representing this modifier followed by [other] in sequence.
	 */
	infix fun then(other: Modifier): Modifier =
		if (other === Modifier) this else CombinedModifier(this, other)

	/**
	 * Returns the toString representation of this modifier as a [Component]
	 */
	fun toComponent(): Component = Component.literal(toString())

	/**
	 * A single element contained within a [Modifier] chain.
	 */
	interface Element<Self : Element<Self>> : Modifier {
		override fun <R> foldIn(initial: R, operation: (R, Element<*>) -> R): R =
			operation(initial, this)

		override fun <R> foldOut(initial: R, operation: (Element<*>, R) -> R): R =
			operation(this, initial)

		override fun any(predicate: (Element<*>) -> Boolean): Boolean = predicate(this)

		override fun all(predicate: (Element<*>) -> Boolean): Boolean = predicate(this)

		fun mergeWith(other: Self): Self

		@Suppress("UNCHECKED_CAST")
		fun unsafeMergeWith(other: Element<*>) = mergeWith(other as Self)
	}

	/**
	 * The companion object `Modifier` is the empty, default, or starter [Modifier]
	 * that contains no [elements][Element]. Use it to create a new [Modifier] using
	 * modifier extension factory functions.
	 */
	// The companion object implements `Modifier` so that it may be used  as the start of a
	// modifier extension factory expression.
	companion object : Modifier {
		override fun <R> foldIn(initial: R, operation: (R, Element<*>) -> R): R = initial
		override fun <R> foldOut(initial: R, operation: (Element<*>, R) -> R): R = initial
		override fun any(predicate: (Element<*>) -> Boolean): Boolean = false
		override fun all(predicate: (Element<*>) -> Boolean): Boolean = true
		override infix fun then(other: Modifier): Modifier = other
		override fun toString() = "Modifier"
	}
}

inline fun <reified T : Modifier> Modifier.contains(): Boolean =
	foldIn(false) { acc, modifier -> acc || modifier is T }

inline fun <reified T : Modifier> Modifier.firstOrNull(): T? =
	foldIn<T?>(null) { acc, modifier -> acc ?: modifier as? T }

inline fun <reified T : Modifier.Element<T>> Modifier.get(): T? =
	foldIn<T?>(null) { acc, element -> if (element is T) acc?.mergeWith(element) ?: element else acc }

inline fun <reified T : Modifier.Element<T>> Modifier.getAll(): List<T> = buildList<T> {
	foldIn(Unit) { acc, element -> if (element is T) add(element) }
}

/**
 * A node in a [Modifier] chain. A CombinedModifier always contains at least two elements;
 * a Modifier [outer] that wraps around the Modifier [inner].
 */
class CombinedModifier(
	private val outer: Modifier,
	private val inner: Modifier
) : Modifier {
	override fun <R> foldIn(initial: R, operation: (R, Modifier.Element<*>) -> R): R =
		inner.foldIn(outer.foldIn(initial, operation), operation)

	override fun <R> foldOut(initial: R, operation: (Modifier.Element<*>, R) -> R): R =
		outer.foldOut(inner.foldOut(initial, operation), operation)

	override fun any(predicate: (Modifier.Element<*>) -> Boolean): Boolean =
		outer.any(predicate) || inner.any(predicate)

	override fun all(predicate: (Modifier.Element<*>) -> Boolean): Boolean =
		outer.all(predicate) && inner.all(predicate)

	override fun equals(other: Any?): Boolean =
		other is CombinedModifier && outer == other.outer && inner == other.inner

	override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

	override fun toComponent(): Component = Component.literal("[").apply {
		append(foldIn(Component.empty() as Component) { acc, element ->
			if (acc == Component.empty()) element.toComponent()
			else Component.empty().append(acc).append(Component.literal(", ")).append(element.toComponent())
		})
		append(Component.literal("]"))
	}

	override fun toString() = "[" + foldIn("") { acc, element ->
		if (acc.isEmpty()) element.toString() else "$acc, $element"
	} + "]"
}
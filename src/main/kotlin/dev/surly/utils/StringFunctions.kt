package dev.surly.utils

import java.util.*

fun String.octalToDecimal(): Int = Integer.parseInt(this, 8)

fun String.startsWithOneOf(vararg prefixes: String): Boolean = prefixes.any { this.startsWith(it, true) }

fun String.sanitize(): String = this.replace("[^a-zA-Z0-9-.]".toRegex(), "-").lowercase(Locale.getDefault())

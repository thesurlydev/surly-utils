package dev.surly.utils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class IoExtensionsTests {

  @Test
  fun testBinary() {
    val src = Paths.get("gradle/wrapper/gradle-wrapper.jar")
    assertTrue(src.toFile().isBinary())
  }

  @Test
  fun testAscii() {
    val src = Paths.get("gradle/wrapper/gradle-wrapper.properties")
    assertTrue(src.toFile().isAscii())
  }
}

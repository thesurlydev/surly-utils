package dev.surly.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class FileUtilsTests {

  @Test
  fun copyBinary() {
    val src = Paths.get("gradle/wrapper/gradle-wrapper.jar")
    println(src.toAbsolutePath())
    val tmpDir = Files.createTempDirectory("FileUtilsTests")
    val dest = Paths.get(tmpDir.toString(), src.fileName.toString())
    val srcLen = src.toFile().length()
    val numBytesCopied = FileUtils.copyBinary(src, dest)
    assertEquals(srcLen, numBytesCopied)
  }
}

package dev.surly.utils

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Character.isISOControl
import java.lang.Character.isWhitespace
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

inline fun File.mapLines(crossinline transform: (line: String) -> String) {
  val tempFile = kotlin.io.path.createTempFile(prefix = "transform", suffix = ".txt").toFile()
  tempFile.printWriter().use { writer ->
    this.forEachLine { line -> writer.println(transform(line)) }
  }
  check(this.delete() && tempFile.renameTo(this)) { "failed to replace file" }
}

infix fun File.fromInputStream(inputStream: InputStream) {
  inputStream.use { input ->
    this.outputStream().use { fileOut ->
      input.copyTo(fileOut)
    }
  }
}

fun File.deleteDirRecursivelyIfExists() {
  if (this.exists() && this.isDirectory) {
    this.walkTopDown().forEach { it.deleteRecursively() }
  }
}

fun File.isBinary(): Boolean = !isAscii()

fun File.sampleBytes(max: Int): Int = when {
  this.length() > max -> max
  else -> this.length().toInt()
}

@Throws(IOException::class)
fun File.isAscii(): Boolean {

  val sampleBytes = this.sampleBytes(500)
  val bytes = ByteArray(sampleBytes)

  FileInputStream(this).use {
    it.read(bytes, 0, bytes.size)
    var bin: Short = 0
    for (thisByte in bytes) {
      val testChar = thisByte.toInt().toChar()
      if (!isWhitespace(testChar) && isISOControl(testChar)) {
        bin++
      }
      if (bin >= 5) {
        return false
      }
    }
  }
  return true
}

fun File.renamePackageDeclarations(placeHolder: String, basePackage: String) {
  println("Renaming all package declarations within ${this.absolutePath} from $placeHolder to $basePackage")
  // for now, adding non-executable clause to prevent perms from getting clobbered for files such as 'gradlew'
  this.walkTopDown()
    .filter { it.isFile && it.isAscii() && !it.canExecute() }
    .forEach { it.mapLines { line -> line.replace(placeHolder, basePackage) } }
}

fun File.renamePackageDirectories(placeHolder: String, basePackage: String) {
  val newDirName = FileUtils.packageToDirs(basePackage)
  println("Renaming all directories within ${this.absolutePath} from $placeHolder to $newDirName")
  this.walkTopDown()
    .filter { it.isDirectory && it.endsWith(placeHolder) }
    .forEach {
      val s = it.toString().replace(placeHolder, newDirName)
      Files.createDirectories(Paths.get(s))
      it.copyRecursively(File(s), overwrite = true)
      it.deleteRecursively()
    }
}

fun Path.clean(): Path = FileUtils.clean(this)

fun File.zip(outputStream: ZipArchiveOutputStream) {

  outputStream.use { out ->
    this.walkTopDown().filter { it.isFile }.forEach { f ->
      val relativePathAsString = this.toURI().relativize(f.toURI()).path

      val entry = ZipArchiveEntry(relativePathAsString)

      // if origin is executable
      if (f.canExecute()) {
        println("Setting $f as executable")
        entry.unixMode = "0755".octalToDecimal() // converted 0755 from octal literal (not supported by Kotlin) to decimal literal
      }

      out.putArchiveEntry(entry)
      FileInputStream(f).use { fi ->
        BufferedInputStream(fi).use { origin ->
          IOUtils.copy(origin, out)
        }
      }
      out.closeArchiveEntry()
    }
  }
}

/**
 * This works great aside from not being able to retain file attributes. (known limitation of java.util.zip)
 */
fun File.zipMeh(zipFile: File) {

  if (zipFile.exists()) {
    zipFile.delete()
  }

  ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
    val data = ByteArray(1024)
    this.walkTopDown().filter { it.isFile }.forEach { f ->
      val relativePathAsString = this.toURI().relativize(f.toURI()).path
      FileInputStream(f).use { fi ->
        BufferedInputStream(fi).use { origin ->
          val entry = ZipEntry(relativePathAsString)
          entry.extra
          out.putNextEntry(entry)
          while (true) {
            val readBytes = origin.read(data)
            if (readBytes == -1) {
              break
            }
            out.write(data, 0, readBytes)
          }
        }
      }
    }
  }
}

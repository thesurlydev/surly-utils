package dev.surly.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.Reader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.asSequence

class FileUtils {
  companion object {
    fun loadFileContentFromClasspath(path: String): String? = FileUtils::class.java.getResource(path)?.readText()

    fun loadStreamFromClasspath(path: String): InputStream? = FileUtils::class.java.getResourceAsStream(path)

    fun streamReaderFromClasspath(path: String): Reader? = FileUtils::class.java.getResourceAsStream(path)?.reader()

    fun streamReaderFromPath(path: Path): Reader = FileInputStream(path.toString()).reader()

    fun streamReaderFromString(str: String): Reader = FileInputStream(str).reader()

    fun copyBinary(src: Path, dest: Path): Long = FileInputStream(src.toFile()).copyTo(FileOutputStream(dest.toFile()))

    fun copyRecursively(srcPath: Path, targetPath: Path) {
      srcPath.toFile().copyRecursively(targetPath.toFile())
    }

    fun packageToDirs(layerPackage: String) = layerPackage.replace('.', File.separatorChar)

    fun listFilePathsByExtension(path: Path, extension: String): List<Path> {
      if (!path.toFile().isDirectory) {
        throw IllegalArgumentException("Provided path is not a directory: ${path.toAbsolutePath()}")
      }
      return Files.list(path).asSequence().filter { it.toFile().extension == extension }.toList()
    }

    fun clean(dirPath: Path): Path {
      val outputDirFile = dirPath.toFile()
      println("Cleaning ${outputDirFile.absolutePath}")
      outputDirFile.deleteRecursively()
      if (!outputDirFile.exists()) {
        outputDirFile.mkdir()
      }
      return dirPath
    }

    fun createDirIfNotExists(d: String): Path {
      val dirPath = Paths.get(d)
      val dir = dirPath.toFile()
      if (!dir.exists()) {
        val created = dir.mkdirs()
        if (created) {
          println("Created ${dir.absolutePath}")
        }
      }
      return dirPath
    }
  }
}

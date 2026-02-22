package com.example.modul_4_pract_1_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.modul_4_pract_1_4.ui.theme.Modul_4_pract_14Theme
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import java.io.File
import java.security.MessageDigest
import kotlin.coroutines.cancellation.CancellationException


class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Modul_4_pract_14Theme {
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            findDuplicates()
        }

    }


    fun findDuplicates() {
        val time = measureTimeMillis {
            runBlocking {
                val timeoutSeconds = 5L

                val directoryPath = File(filesDir, "test_json").absolutePath
                println("Сканируем директорию: $directoryPath")


                val result = withTimeoutOrNull(timeoutSeconds * 1000) {
                    findDuplicateFiles(directoryPath)
                }

                if (result == null) {
                    println("Поиск прерван по таймауту ($timeoutSeconds сек)")
                } else {
                    printDuplicates(result)
                }
            }
        }
        println("Общее время выполнения: ${time / 1000.0} секунд")
    }

//    private fun createTestJsonFiles() {
//        val testDir = File(filesDir, "test_json")
//
//        File(testDir, "user1.json").writeText("""{"id": 1, "name": "Alice"}""")
//        File(testDir, "user2.json").writeText("""{"id": 2, "name": "Bob"}""")
//        File(testDir, "user3.json").writeText("""{"id": 3, "name": "Charlie"}""")
//
//        File(testDir, "duplicate1.json").writeText("""{"product": "Coffee", "qty": 42, "price": 250}""")
//        File(testDir, "duplicate2.json").writeText("""{"product": "Coffee", "qty": 42, "price": 250}""")
//        File(testDir, "duplicate3.json").writeText("""{"product": "Coffee", "qty": 42, "price": 250}""")
//
//        File(testDir, "group2_a.json").writeText("""{"city": "Moscow", "temp": -18, "condition": "snow"}""")
//        File(testDir, "group2_b.json").writeText("""{"city": "Moscow", "temp": -18, "condition": "snow"}""")
//
//        val subDir = File(testDir, "subdir")
//        subDir.mkdirs()
//        File(subDir, "nested1.json").writeText("""{"city": "New York", "temp": -5, "condition": "cloudy"}""")
//        File(subDir, "nested2.json").writeText("""{"city": "New York", "temp": -5, "condition": "cloudy"}""")
//
//        File(subDir, "unique_nested.json").writeText("""{"city": "Tokyo", "temp": 11, "condition": "rain"}""")
//
//        println("Тестовые JSON файлы созданы в: ${testDir.absolutePath}")
//        println("Создано файлов: ${testDir.walkTopDown().filter { it.isFile && it.extension == "json" }.count()}")
//
//    }


    private suspend fun findDuplicateFiles(rootPath: String): Map<String, List<File>> {
        return withContext(Dispatchers.IO) {
            //поиск файлов json
            val jsonFiles = findJsonFiles(File(rootPath))
            println("Найдено JSON файлов: ${jsonFiles.size}")

            if (jsonFiles.isEmpty()) {
                return@withContext emptyMap()
            }

            println("\nСписок файлов:")
            jsonFiles.forEachIndexed { index, file ->
                println("   ${index + 1}. ${file.relativeTo(File(rootPath))} (${file.length()} байт)")
            }
            println()


            // для каждого файла вычисляем SHA-256 параллельно
            val deferredResults = jsonFiles.map { file ->
                async {
                    file to computeSha256(file)
                }
            }

            val filesWithHashes = deferredResults.awaitAll()

            // группируем по хэшу и оставляем только дубликаты (где больше 1 файла)
            val duplicates = filesWithHashes
                .filter { it.second != null }
                .groupBy({ it.second!! }, { it.first })
                .filter { it.value.size > 1 }

            println("Найдено групп дубликатов: ${duplicates.size}")

            duplicates
        }
    }
    private fun findJsonFiles(directory: File): List<File> {
        val result = mutableListOf<File>()

        if (!directory.exists()) {
            println("Директория не существует: ${directory.absolutePath}")
            return result
        }

        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                result.addAll(findJsonFiles(file))
            } else if (file.isFile && file.extension.equals("json", ignoreCase = true)) {
                result.add(file)
            }
        }

        return result
    }

    private suspend fun computeSha256(file: File): String? {
        return withContext(Dispatchers.IO) {
            try {
                delay(500)

                val digest = MessageDigest.getInstance("SHA-256")

                file.inputStream().use { inputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                        yield()
                    }
                }

                // конвертируем в hex строку
                digest.digest().joinToString("") { "%02x".format(it) }.also { hash ->
                    println("Хеш для ${file.name}: ${hash.take(8)}...")
                }
            } catch (e: CancellationException) {
                println("Отмена вычисления хеша для ${file.name}")
                throw e
            } catch (e: Exception) {
                println("Ошибка чтения файла ${file.name}: ${e.message}")
                null
            }
        }
    }

    private fun printDuplicates(duplicates: Map<String, List<File>>) {
        println("\nНайденные дубликаты:")
        var groupIndex = 1
        duplicates.forEach { (hash, files) ->
            println("\nГруппа ${groupIndex++} (SHA-256: ${hash.take(8)}...):")
            files.forEachIndexed { index, file ->
                println("   ${index + 1}. ${file.name} (${file.length()} байт)")
            }
        }
        println("\nВсего групп дубликатов: ${duplicates.size}")
    }
}




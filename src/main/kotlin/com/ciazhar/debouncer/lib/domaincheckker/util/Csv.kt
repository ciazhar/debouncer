package com.ciazhar.debouncer.lib.domaincheckker.util

import com.ciazhar.debouncer.lib.domaincheckker.model.Dnsbl
import com.ciazhar.debouncer.verticle.MainVerticle
import java.io.*

val DNSBL_NAME = 0
val CSV_HEADER = "name"

fun writeToCsv(fileName: String, dnsbls : List<Dnsbl>) : String{
    var dnsbls = dnsbls.sortedBy{ it.name }
    var fileWriter: FileWriter? = null
    try {
        fileWriter = FileWriter(fileName)
        fileWriter.append(CSV_HEADER)
        fileWriter.append('\n')

        for (dnsbl in dnsbls) {
            fileWriter.append(dnsbl.name)
            fileWriter.append('\n')
        }

        return "Write CSV successfully!"
    } catch (e: Exception) {
        e.printStackTrace()
        return "Writing CSV error!"
    } finally {
        try {
            fileWriter!!.flush()
            fileWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return "Flushing/closing error!"
        }
    }
}

fun readFromCsv(fileName: String) : MutableList<Dnsbl>{
    var fileReader: BufferedReader? = null
    val dnsbls = mutableListOf<Dnsbl>()

    try {
        var line: String?

        fileReader = BufferedReader(FileReader(fileName))

        // Read CSV header
        fileReader.readLine()

        // Read the file line by line starting from the second line
        line = fileReader.readLine()
        while (line != null) {
            val tokens = line.split(",")
            if (tokens.isNotEmpty()) {
                val dnsbl = Dnsbl(
                        tokens[DNSBL_NAME])
                dnsbls.add(dnsbl)
            }

            line = fileReader.readLine()
        }
    } catch (e: Exception) {
        println("Reading CSV Error!")
        e.printStackTrace()
    } finally {
        try {
            fileReader!!.close()
        } catch (e: IOException) {
            println("Closing fileReader Error!")
            e.printStackTrace()
        }
    }
    return dnsbls
}

fun removeLines(fileName: String, startLine: Int, numLines: Int) {
    require(!fileName.isEmpty() && startLine >= 1 && numLines >= 1)
    val f = File(fileName)
    if (!f.exists()) {
        println("$fileName does not exist")
        return
    }
    var lines = f.readLines()
    val size = lines.size
    if (startLine > size) {
        println("The starting line is beyond the length of the file")
        return
    }
    var n = numLines
    if (startLine + numLines - 1 > size) {
        println("Attempting to remove some lines which are beyond the end of the file")
        n = size - startLine + 1
    }
    lines = lines.take(startLine - 1) + lines.drop(startLine + n - 1)
    val text = lines.joinToString(System.lineSeparator())
    f.writeText(text)
}
package com.ciazhar.debouncer.lib.svmchecker.util

import java.io.*

fun saveObjectTo(o: Any?, path: String): Boolean {
    try {
        val oos = ObjectOutputStream(FileOutputStream(path))
        oos.writeObject(o)
        oos.close()
    } catch (e: IOException) {
        println("Pengecualian terjadi saat menyimpan objek $o ke $path$e")
        return false
    }

    return true
}

fun readObjectFrom(path: String): Any? {
    val ois: ObjectInputStream?
    try {
        ois = ObjectInputStream(FileInputStream(path))
        val o = ois.readObject()
        ois.close()
        return o
    } catch (e: Exception) {
        println("Dari $path pengecualian terjadi saat membaca objek $e")
    }

    return null
}
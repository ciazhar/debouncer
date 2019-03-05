package com.ciazhar.debouncer.extension

import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.core.logging.SLF4JLogDelegateFactory
import kotlin.reflect.KClass

/**
 * Created by ciazhar on 8/29/17.
 */

/**
 * KClass sama seperti Barang.class di java
 */
fun logger(clz: KClass<*>): Logger {
    return LoggerFactory.getLogger(clz.qualifiedName)
}

// konfigurasi logback secara default menggunakan SLF4J
fun useLogBack() {
    System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory::class.java.name)
}
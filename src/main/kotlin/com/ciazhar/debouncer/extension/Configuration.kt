package com.ciazhar.debouncer.extension

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import rx.Observable


/**
 * Created by ciazhar on 8/29/17.
 */

/**
 * konfigurasi file properties yang ada di dalam folder resources
 */
fun propertiesConfiguration(path: String): ConfigStoreOptions {
    return ConfigStoreOptions(
            type = "file",
            format = "properties",
            config = json {
                obj("path" to path)
            }
    )
}

fun environtmentConfiguration(): ConfigStoreOptions {
    return ConfigStoreOptions(
            type = "env"
    )
}

/**
 * konfigurasi vertx untuk load dari hasil konfigurasi file properties diatas
 */
fun Vertx.retrieveConfig(vararg stores:ConfigStoreOptions): Observable<JsonObject> {
    val options = ConfigRetrieverOptions(
            stores = stores.toList().plus(environtmentConfiguration())
    )
    val retriever = ConfigRetriever.create(this, options)
    return observable { retriever.getConfig(it) }
}
package com.ciazhar.domaincheckerservice.extension

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

/**
 * Created by ciazhar on 8/29/17.
 */

/**
 * fungsi pretty json
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.prettyJson(obj: Any) {
    val response = this.response()
    response.putHeader("Content-Type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(obj))
}

/**
 * fungsi pesan OK
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.OK(message: String = "", headers: Map<String, String> = emptyMap()) {
    this.response().let {
        it.statusCode = HttpResponseStatus.OK.code()
        headers.entries.fold(it) { response, entries ->
            response.putHeader(entries.key, entries.value)
        }
        it.end(message)
    }
}

/**
 * atur header
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.header(key: String): String? {
    return this.request().headers().get(key)
}

/**
 * fungsi mengambil nilai param
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.param(key: String): String? {
    return this.request().getParam(key)
}

/**
 * encode menjadi json
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.json(obj: Any) {
    val response = this.response()
    response.putHeader("Content-Type", "application/json; charset=utf-8")
            .end(Json.encode(obj))
}

/**
 * encode menjadi json dan tentukan header
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.json(headers: Map<String, String> = emptyMap(), message: Any) {
    this.response().apply {
        headers.entries.fold(this) { response, entries ->
            response.putHeader(entries.key, entries.value)
        }
        putHeader("Content-Type", "application/json; charset=utf-8")
        end(Json.encode(message))
    }
}

/**
 * proses terima json object
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.jsonBody(): JsonObject? {
    val result: JsonObject? = try {
        this.bodyAsJson
    } catch (e: Exception) {
        null
    }
    return result
}

/**
 * proses terima json array
 */
@Suppress("NOTHING_TO_INLINE")
inline fun RoutingContext.jsonArrayBody(): JsonArray? {
    val result: JsonArray? = try {
        this.bodyAsJsonArray
    } catch (e: Exception) {
        null
    }
    return result
}
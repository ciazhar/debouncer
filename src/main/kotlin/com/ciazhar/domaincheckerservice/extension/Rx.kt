package com.ciazhar.domaincheckerservice.extension

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.rx.java.ObservableFuture
import io.vertx.rx.java.SingleOnSubscribeAdapter
import rx.Observable
import rx.Single

/**
 * Created by ciazhar on 8/29/17.
 */

inline fun <T : Any> observable(operation: (Handler<AsyncResult<T>>) -> Unit): Observable<T> {
    val future = ObservableFuture<T>()
    operation(future.toHandler())
    return future
}

inline fun <T : Any> single(crossinline operation: (Handler<AsyncResult<T>>) -> Unit): Single<T> {
    return Single.create(SingleOnSubscribeAdapter<T> { fut -> operation(fut) })
}
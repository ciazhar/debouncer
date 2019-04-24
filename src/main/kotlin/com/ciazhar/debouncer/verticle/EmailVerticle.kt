package com.ciazhar.debouncer.verticle

import com.ciazhar.debouncer.extension.logger
import com.ciazhar.debouncer.extension.single
import com.ciazhar.debouncer.lib.emailsender.EmailSender
import com.ciazhar.debouncer.lib.emailsender.model.Mail
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler

class EmailVerticle : AbstractVerticle() {

    private val log = logger(EmailVerticle::class)

    private val config by lazy { config() }

    override fun start(startFuture: Future<Void>) {
        println("Initialize Main Verticle...")

        println("Initialize Router...")
        val router = Router.router(vertx)

        router.route("/api/email*").handler(BodyHandler.create())
        router.post("/api/email").handler(this::sendEmail)

        println("Starting HttpServer...")
        val httpServer = single<HttpServer> { it ->
            vertx.createHttpServer()
                    .requestHandler { router.accept(it) }
                    .listen(config.getInteger("HTTP_PORT"), it)
        }

        httpServer.subscribe(
                {
                    println("HttpServer started in port ${config.getInteger("HTTP_PORT")}")
                    println("Main Verticle Deployed!")
                    startFuture.complete()
                },
                {
                    log.error("Failed to start HttpServer. [${it.message}]", it)
                    log.error("Main Verticle Failed to Deploy!")
                    startFuture.fail(it)
                }
        )
    }

    /**
     * CSV
     */

    private fun sendEmail(routingContext: RoutingContext){
        //request body
        val mail = Json.decodeValue(routingContext.bodyAsString,
                Mail::class.java)

        println(mail)
        EmailSender.sendFromGMail(mail)

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily("cek"))
    }
}
package com.ciazhar.domaincheckerservice.verticle


import com.ciazhar.domaincheckerservice.extension.logger
import com.ciazhar.domaincheckerservice.extension.param
import com.ciazhar.domaincheckerservice.extension.single
import com.ciazhar.domaincheckerservice.model.Dnsbl
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.BodyHandler
import java.util.stream.Collectors


class MainVerticle (private var Mongo : MongoClient): AbstractVerticle() {

    private val log = logger(MainVerticle::class)

    private val config by lazy { config() }

    override fun start(startFuture: Future<Void>) {
        println("Initialize Main Verticle...")

        println("Initialize Router...")
        val router = Router.router(vertx)
        router.get("/").handler(this::getAll)
        router.route("/*").handler(BodyHandler.create())
        router.post("/").handler(this::addOne)
        router.get("/:id").handler(this::getOne)


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

    private fun addOne(routingContext: RoutingContext) {
        val dnsbl = Json.decodeValue(routingContext.bodyAsString,
                    Dnsbl::class.java)

        Mongo.insert(dnsblCollectionName, dnsbl?.toJson()) {res ->
            dnsbl.id=res.result()
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(dnsbl))
        }
    }

    private var dnsblCollectionName = "dnsbl"

    private fun getAll(routingContext : RoutingContext ) {
        Mongo.find(dnsblCollectionName, JsonObject()) {res ->
            val objects = res.result()
            val whiskies = objects.stream().map{ Dnsbl(it) }.collect(Collectors.toList())

            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(whiskies))
        }
    }

    private fun getOne(routingContext : RoutingContext ) {
        val id = routingContext.param("id")
        if (id ==null){
            routingContext.response().setStatusCode(400).end()
        }
        Mongo.find(dnsblCollectionName, JsonObject().put("_id", id)) { res ->
            if (res.succeeded()) {
                if (res.result() == null) {
                    routingContext.response().setStatusCode(404).end()
                }
                val dnsbl = Dnsbl(res.result()[0])
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(dnsbl))
            } else {
                routingContext.response().setStatusCode(404).end()
            }
        }
    }
}

package com.ciazhar.debouncer.verticle


import com.ciazhar.debouncer.extension.logger
import com.ciazhar.debouncer.extension.param
import com.ciazhar.debouncer.extension.single
import com.ciazhar.debouncer.lib.domaincheckker.DomainChecker
import com.ciazhar.debouncer.lib.domaincheckker.model.Dnsbl
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

class MainVerticle : AbstractVerticle() {

    private val log = logger(MainVerticle::class)

    private val config by lazy { config() }

    override fun start(startFuture: Future<Void>) {
        println("Initialize Main Verticle...")

        println("Initialize Router...")
        val router = Router.router(vertx)

        router.route("/api/dnsbl*").handler(BodyHandler.create())
        router.post("/api/dnsbl").handler(this::addOneToCsv)
        router.get("/api/dnsbl").handler(this::readFromCsvEndpoint)
        router.delete("/api/dnsbl/:id").handler(this::deleteFromCsv)

        router.get("/api/check-domain").handler(this::checkDomain)

        router.get("/api/scrap").handler(this::scrapDnsblCsv)

        router.route("/*").handler(StaticHandler.create("assets"))

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

    companion object {
        const val CSV_FILE_NAME = "dnsbl.csv"
    }

    private fun scrapDnsblCsv(routingContext: RoutingContext){
        //scrap dnsbl
        val resp = DomainChecker.scrapDnsbl(CSV_FILE_NAME)

        //response
        routingContext.response().setStatusCode(200).end(resp)
    }

    private fun readFromCsvEndpoint(routingContext: RoutingContext){
        //get dnsbl
        val resp = DomainChecker.getDnsbl(CSV_FILE_NAME)

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(resp))
    }

    private fun deleteFromCsv(routingContext: RoutingContext){
        //request path variable
        val id = routingContext.param("id")

        //check path variable if exist
        if (id==null){
            routingContext.response().setStatusCode(400).end()
        }else{
            //delete dnsbl
            DomainChecker.deleteDnsbl(id, CSV_FILE_NAME)

            //get dnsbl
            val resp = DomainChecker.getDnsbl(CSV_FILE_NAME)

            //response
            routingContext.response().setStatusCode(200).end(Json.encodePrettily(resp))
        }
    }

    private fun addOneToCsv(routingContext: RoutingContext){
        //request body
        val dnsbl = Json.decodeValue(routingContext.bodyAsString,
                Dnsbl::class.java)

        //add dnsbl
        val res = DomainChecker.addDnsbl(CSV_FILE_NAME,dnsbl)

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(res))
    }

    private fun checkDomain(routingContext: RoutingContext) {
        //get dnsbl
        val dnsbls = DomainChecker.getDnsbl(CSV_FILE_NAME).map { it.name }.toMutableList()

        //request param
        val domain = routingContext.request().getParam("domain")

        //check param if exist
        if (domain == null) {
            //response bad request if not exist
            routingContext.response().setStatusCode(400).end()
        } else {
            //check domain
            val blockedFrom = DomainChecker
                    .checkDomain(domain,dnsbls)
                    .asSequence().map { Dnsbl(it) }
                    .toMutableList()

            //response success
            routingContext.response().setStatusCode(200).end(Json.encodePrettily(blockedFrom))
        }
    }
}

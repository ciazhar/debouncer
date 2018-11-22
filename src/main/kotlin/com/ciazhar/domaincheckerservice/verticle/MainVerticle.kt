package com.ciazhar.domaincheckerservice.verticle


import com.ciazhar.domaincheckerservice.extension.logger
import com.ciazhar.domaincheckerservice.extension.param
import com.ciazhar.domaincheckerservice.extension.single
import com.ciazhar.domaincheckerservice.lib.domaincheckker.DomainChecker
import com.ciazhar.domaincheckerservice.model.DnsblCsv
import com.ciazhar.domaincheckerservice.service.readFromCsv
import com.ciazhar.domaincheckerservice.service.removeLines
import com.ciazhar.domaincheckerservice.service.writeToCsv
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
        const val CSV_HEADER = "name"
        const val DNSBL_NAME = 0
        const val CSV_FILE_NAME = "dnsbl.csv"
    }

    private var dnsblList : List<DnsblCsv> = listOf()

    private fun scrapDnsblCsv(routingContext: RoutingContext){
        val resp = DomainChecker.scrapDnsbl(CSV_FILE_NAME)

        //response
        routingContext.response().setStatusCode(200).end(resp)
    }

    private fun readFromCsvEndpoint(routingContext: RoutingContext){
        //read from csv
        val resp =  readFromCsv()

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(resp))
    }

    private fun deleteFromCsv(routingContext: RoutingContext){
        val id = routingContext.param("id")
        var idInt = 0
        try {
            idInt = id!!.toInt()
        } catch (nfe: NumberFormatException) {
            // not a valid int, handle this as you wish
        }

        //delete line
        removeLines(CSV_FILE_NAME,idInt+1,1)

        //read from csv
        val resp =  readFromCsv()

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(resp))
    }

    private fun addOneToCsv(routingContext: RoutingContext){
        //request body
        val dnsbl = Json.decodeValue(routingContext.bodyAsString,
                DnsblCsv::class.java)

        //read from csv
        dnsblList = readFromCsv()
        dnsblList += dnsbl
        dnsblList = dnsblList.distinctBy { it.name }

        //write to csv
        writeToCsv(dnsblList)

        //response
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(dnsblList))
    }

    private fun checkDomain(routingContext: RoutingContext) {
        val dnsbls = readFromCsv().map { it.name }.toMutableList()

        val domain = routingContext.request().getParam("domain")
        if (domain == null) {
            routingContext.response().setStatusCode(400).end()
        } else {
            val blockedFrom = DomainChecker.checkDomain(domain,dnsbls)
            val blockerFromJson = blockedFrom.asSequence().map { DnsblCsv(it) }.toMutableList()
            routingContext.response().setStatusCode(200).end(Json.encodePrettily(blockerFromJson))
        }
    }
}

package com.ciazhar.domaincheckerservice.verticle


import com.ciazhar.domaincheckerservice.extension.logger
import com.ciazhar.domaincheckerservice.extension.param
import com.ciazhar.domaincheckerservice.extension.single
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
import org.jsoup.Jsoup


class MainVerticle : AbstractVerticle() {

    private val log = logger(MainVerticle::class)

    private val config by lazy { config() }

    override fun start(startFuture: Future<Void>) {
        println("Initialize Main Verticle...")

        println("Initialize Router...")
        val router = Router.router(vertx)

        // Bind "/" to our hello message.
        router.route("/").handler { routingContext ->
            val response = routingContext.response()
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>")
        }

        router.route("/assets/*").handler(StaticHandler.create("assets"))

        router.route("/api/dnsbl*").handler(BodyHandler.create())
        router.post("/api/dnsbl").handler(this::addOneToCsv)
        router.get("/api/dnsbl").handler(this::readFromCsvEndpoint)
        router.delete("/api/dnsbl/:id").handler(this::deleteFromCsv)

//        router.get("/api/check-domain").handler(this::checkDomain)

        router.get("/api/scrap").handler(this::scrapDnsblCsv)

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
        val CSV_HEADER = "name"
        val DNSBL_NAME = 0
        val CSV_FILE_NAME = "dnsbl.csv"
    }

    var dnsblList : List<DnsblCsv> = listOf()

    private fun scrapDnsblCsv(routingContext: RoutingContext){
        //scrap
        val doc = Jsoup.connect("https://www.dnsbl.info/dnsbl-list.php").get()
        val dnsbls : MutableList<DnsblCsv> = mutableListOf()
        doc.select("td[width='33%']").forEach {
            dnsbls.add(DnsblCsv(
                    name = it.select("a").text()
            ))
        }

        //read from csv and update
        dnsblList = readFromCsv()
        dnsblList += dnsbls
        dnsblList = dnsblList.distinctBy { it.name }

        //write to csv
        val resp = writeToCsv(dnsblList)

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
}

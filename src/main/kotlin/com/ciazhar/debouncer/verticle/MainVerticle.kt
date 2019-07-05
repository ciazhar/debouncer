package com.ciazhar.debouncer.verticle


import com.ciazhar.debouncer.extension.logger
import com.ciazhar.debouncer.extension.param
import com.ciazhar.debouncer.extension.single
import com.ciazhar.debouncer.lib.dnsblcheckker.DomainChecker
import com.ciazhar.debouncer.lib.dnsblcheckker.model.Dnsbl
import com.ciazhar.debouncer.lib.emailsender.EmailSender
import com.ciazhar.debouncer.lib.emailsender.model.Mail
import com.ciazhar.debouncer.lib.svmchecker.SpamChecker
import com.ciazhar.debouncer.lib.svmchecker.service.SVMCheckerService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import java.lang.Exception

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

        router.route("/api/email*").handler(BodyHandler.create())
        router.post("/api/email").handler(this::sendEmail)

        router.route("/api/spam*").handler(BodyHandler.create())
        router.post("/api/spam").handler(this::checkContentSpam)

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
     * DNSBL
     */

    companion object {
        const val CSV_FILE_NAME = "dnsbl.csv"
    }

    private fun scrapDnsblCsv(routingContext: RoutingContext){
        //scrap dnsbl
        val resp = DomainChecker.scrapDnsbl(CSV_FILE_NAME)

        //response
        routingContext.response()
                .setStatusCode(200)
                .end(Json.encodePrettily(resp))
    }

    private fun readFromCsvEndpoint(routingContext: RoutingContext){
        //get dnsbl
        val resp = DomainChecker.getDnsbl(CSV_FILE_NAME)

        //response
        routingContext.response()
                .putHeader("Content-Type","application/json")
                .setStatusCode(200)
                .end(Json.encodePrettily(resp))
    }

    private fun deleteFromCsv(routingContext: RoutingContext){
        //request path variable
        val id = routingContext.param("id")

        //check path variable if exist
        if (id==null){
            routingContext.response().setStatusCode(400).end()
        }
        else{
            //delete dnsbl
            DomainChecker.deleteDnsbl(id, CSV_FILE_NAME)

            //get dnsbl
            val resp = DomainChecker.getDnsbl(CSV_FILE_NAME)

            //response
            routingContext.response()
                    .putHeader("Content-Type","application/json")
                    .setStatusCode(200)
                    .end(Json.encodePrettily(resp))
        }
    }

    private fun addOneToCsv(routingContext: RoutingContext){
        //request body
        val dnsbl = Json.decodeValue(routingContext.bodyAsString,
                Dnsbl::class.java)

        //add dnsbl
        val res = DomainChecker.addDnsbl(CSV_FILE_NAME,dnsbl)

        //response
        routingContext.response()
                .setStatusCode(200)
                .end(Json.encodePrettily(res))
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
            routingContext.response()
                    .putHeader("Content-Type","application/json")
                    .setStatusCode(200)
                    .end(Json.encodePrettily(blockedFrom))
        }
    }

    /**
     * Spam
     */

    private fun checkContentSpam(routingContext: RoutingContext){
        //request body
        val mail = Json.decodeValue(routingContext.bodyAsString,
                Mail::class.java)

        val classifier = SVMCheckerService(SpamChecker.trainOrLoadModel())
        val result = SpamChecker.predict(classifier, mail.body)

        //response
        val map = hashMapOf("message" to "ok", "data" to result)
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(map))
    }

    /**
     * Email
     * */

    private fun sendEmail(routingContext: RoutingContext){
        //request body
        val mail = Json.decodeValue(routingContext.bodyAsString,
                Mail::class.java)
        var bodyRes = "success"
        val classifier = SVMCheckerService(SpamChecker.trainOrLoadModel())

        //check domain email sender
        //because of the domain sender default is gmail, so i skip this step

        //check domain email recipient
        mail.recipient.forEach {
            val arr = it.split("@")
            if (arr.isNotEmpty()){
                try {
                    val res = DomainChecker.checkDomain(it)
                    if (res.size!=0) {
                        bodyRes="$it is blocked in $res"
                        val map = hashMapOf("message" to "ok", "data" to bodyRes)
                        routingContext.response().setStatusCode(500).end(Json.encodePrettily(map))
                        return
                    }
                }catch (e : Exception){
                    bodyRes= e.message.toString()
                    val map = hashMapOf("message" to "ok", "data" to bodyRes)
                    routingContext.response().setStatusCode(500).end(Json.encodePrettily(map))
                    return
                }
            }
        }

        //check spam subject email
        var res = SpamChecker.predict(classifier,mail.subject)
        if (res=="spam"){
            bodyRes="Email subject considered as spam"
            val map = hashMapOf("message" to "ok", "data" to bodyRes)
            routingContext.response().setStatusCode(500).end(Json.encodePrettily(map))
            return
        }

        //check spam body email
        res = SpamChecker.predict(classifier,mail.body)
        if (res=="spam"){
            bodyRes="Email body considered as spam"
            val map = hashMapOf("message" to "ok", "data" to bodyRes)
            routingContext.response().setStatusCode(500).end(Json.encodePrettily(map))
            return
        }

        //send email
        res = EmailSender.sendFromGMail(mail)
        if (res!="success"){
            bodyRes=res
            val map = hashMapOf("message" to "ok", "data" to bodyRes)
            routingContext.response().setStatusCode(500).end(Json.encodePrettily(map))
            return
        }

        //response
        val map = hashMapOf("message" to "ok", "data" to bodyRes)
        routingContext.response().setStatusCode(200).end(Json.encodePrettily(map))
    }
}

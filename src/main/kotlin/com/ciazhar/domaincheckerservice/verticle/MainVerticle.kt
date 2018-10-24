package com.ciazhar.domaincheckerservice.verticle


import com.ciazhar.domaincheckerservice.extension.logger
import com.ciazhar.domaincheckerservice.extension.param
import com.ciazhar.domaincheckerservice.extension.single
import com.ciazhar.domaincheckerservice.model.DnsblCsv
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import org.jsoup.Jsoup
import java.io.*


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
        router.get("/api/dnsbl").handler(this::readFromCsv)
//        router.get("/api/dnsbl/:id").handler(this::getOneFromFile)
//        router.put("/api/dnsbl").handler(this::updateOne)
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

    private val CSV_HEADER = "name"
    private val DNSBL_NAME = 0
    private val CSV_FILE_NAME = "dnsbl.csv"

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

    private fun readFromCsv(routingContext: RoutingContext){
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
        removeLines(CSV_FILE_NAME,idInt,idInt)

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

    fun writeToCsv(dnsbls : List<DnsblCsv>) : String{
        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(CSV_FILE_NAME)

            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')

            for (dnsbl in dnsbls) {
                fileWriter.append(dnsbl.name)
                fileWriter.append('\n')
            }

            return "Write CSV successfully!"
        } catch (e: Exception) {
            e.printStackTrace()
            return "Writing CSV error!"
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Flushing/closing error!"
            }
        }
    }

    fun readFromCsv() : MutableList<DnsblCsv>{
        var fileReader: BufferedReader? = null
        val dnsbls = mutableListOf<DnsblCsv>()

        try {
            var line: String?

            fileReader = BufferedReader(FileReader(CSV_FILE_NAME))

            // Read CSV header
            fileReader.readLine()

            // Read the file line by line starting from the second line
            line = fileReader.readLine()
            while (line != null) {
                val tokens = line.split(",")
                if (tokens.isNotEmpty()) {
                    val dnsbl = DnsblCsv(
                            tokens[DNSBL_NAME])
                    dnsbls.add(dnsbl)
                }

                line = fileReader.readLine()
            }
        } catch (e: Exception) {
            println("Reading CSV Error!")
            e.printStackTrace()
        } finally {
            try {
                fileReader!!.close()
            } catch (e: IOException) {
                println("Closing fileReader Error!")
                e.printStackTrace()
            }
        }
        return dnsbls
    }

    fun removeLines(fileName: String, startLine: Int, numLines: Int) {
        require(!fileName.isEmpty() && startLine >= 1 && numLines >= 1)
        val f = File(fileName)
        if (!f.exists()) {
            println("$fileName does not exist")
            return
        }
        var lines = f.readLines()
        val size = lines.size
        if (startLine > size) {
            println("The starting line is beyond the length of the file")
            return
        }
        var n = numLines
        if (startLine + numLines - 1 > size) {
            println("Attempting to remove some lines which are beyond the end of the file")
            n = size - startLine + 1
        }
        lines = lines.take(startLine - 1) + lines.drop(startLine + n - 1)
        val text = lines.joinToString(System.lineSeparator())
        f.writeText(text)
    }
}

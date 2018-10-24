package com.ciazhar.domaincheckerservice.controller

import com.ciazhar.domaincheckerservice.extension.param
import com.ciazhar.domaincheckerservice.lib.domaincheckker.DomainChecker
import com.ciazhar.domaincheckerservice.model.Dnsbl
import com.google.common.io.Resources
import com.google.gson.Gson
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.RoutingContext
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.util.stream.Collectors


/**
 * JSON
 */

var resource = Resources.getResource("dnsbl.json")
var dnsblListJson = resource.file

private fun getAllFromFile(routingContext: RoutingContext) {
    val gson = Gson()
    val bufferedReader: BufferedReader = File(dnsblListJson).bufferedReader()
    val inputString = bufferedReader.use { it.readText() }

    val dnsbl = gson.fromJson(inputString, Array<Dnsbl>::class.java)

    routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(dnsbl))
}

private fun getOneFromFile(routingContext: RoutingContext) {
    val id = routingContext.param("id")
    if (id ==null){
        routingContext.response().setStatusCode(400).end()
    }

    val gson = Gson()
    val bufferedReader: BufferedReader = File(dnsblListJson).bufferedReader()
    val inputString = bufferedReader.use { it.readText() }

    val dnsbl = gson.fromJson(inputString, Array<Dnsbl>::class.java)
    dnsbl.forEach {
        if (it.id==id){
            routingContext.response()
                    .setStatusCode(201)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(it))
        }
    }
}

fun scrapDnsbl(routingContext: RoutingContext){
    val doc = Jsoup.connect("https://www.dnsbl.info/dnsbl-list.php").get()

    val dnsbls : MutableList<Dnsbl> = mutableListOf()
    doc.select("td[width='33%']").forEach {
        dnsbls.add(Dnsbl(
                name = it.select("a").text(),
                url = "https://www.dnsbl.info"+it.select("a").attr("href"))
        )
    }

    val gson = Gson()
    val jsonString:String = gson.toJson(dnsbls)
    val file= File(dnsblListJson)
    file.writeText(jsonString)

    routingContext.response().setStatusCode(200).end(Json.encodePrettily(dnsbls))
}


/**
 * MONGODB
 */

private var dnsblCollectionName = "dnsbl"
private var Mongo : MongoClient? = null

private fun addOne(routingContext: RoutingContext) {
    val dnsbl = Json.decodeValue(routingContext.bodyAsString,
            Dnsbl::class.java)

    Mongo?.insert(dnsblCollectionName, dnsbl?.toJson()) {res ->
        dnsbl.id=res.result()
        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(dnsbl))
    }
}

private fun getAll(routingContext : RoutingContext) {
    Mongo?.find(dnsblCollectionName, JsonObject()) { res ->
        val objects = res.result()
        val whiskies = objects.stream().map{ Dnsbl(it) }.collect(Collectors.toList())

        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(whiskies))
    }
}

private fun getOne(routingContext : RoutingContext) {
    val id = routingContext.param("id")
    if (id ==null){
        routingContext.response().setStatusCode(400).end()
    }
    Mongo?.find(dnsblCollectionName, JsonObject().put("_id", id)) { res ->
        if (res.succeeded()) {
            if (res.result() == null) {
                routingContext.response().setStatusCode(404).end()
            }
            print("size"+res.result().size)
            if (res.result().size>0){
                val dnsbl = Dnsbl(res.result()[0])
                routingContext.response()
                        .setStatusCode(200)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(dnsbl))
            }else{
                routingContext.response()
                        .setStatusCode(404)
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily("id not found"))
            }

        } else {
            routingContext.response().setStatusCode(404).end()
        }
    }
}

private fun updateOne(routingContext: RoutingContext) {
    val json = routingContext.bodyAsJson
    if (json == null) {
        routingContext.response().setStatusCode(400).end()
    } else {
        Mongo?.update(dnsblCollectionName,
                JsonObject().put("_id", json.getString("id")), // Select a unique document
                // The update syntax: {$set, the json object containing the fields to update}
                JsonObject()
                        .put("\$set", json)
        ) { v ->
            if (v.failed()) {
                routingContext.response().setStatusCode(404).end()
            } else {
                val dnsbl = Dnsbl(json)
                dnsbl.id=json.getString("id")
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(dnsbl))
            }
        }
    }
}

private fun deleteOne(routingContext: RoutingContext) {
    val id = routingContext.request().getParam("id")
    if (id == null) {
        routingContext.response().setStatusCode(400).end()
    } else {
        Mongo?.removeOne(dnsblCollectionName, JsonObject().put("_id", id)
        ) { routingContext.response().setStatusCode(204).end() }
    }
}

/**
 * DOMAIN
 */

private fun checkDomain(routingContext: RoutingContext) {
    val gson = Gson()
    val bufferedReader: BufferedReader = File(dnsblListJson).bufferedReader()
    val inputString = bufferedReader.use { it.readText() }

    val dnsbl = gson.fromJson(inputString, Array<Dnsbl>::class.java)
    val dnsblMutabl = dnsbl.map {
        it.name
    }.toMutableList()

    val domain = routingContext.request().getParam("domain")
    if (domain == null) {
        routingContext.response().setStatusCode(400).end()
    } else {
        val blockedFrom = DomainChecker.check(domain,dnsblMutabl)
        routingContext.response().setStatusCode(200).end(blockedFrom.toString())
    }
}
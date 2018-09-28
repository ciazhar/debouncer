package com.ciazhar.domaincheckerservice.model

import io.vertx.core.json.JsonObject
import java.util.*

class Dnsbl {
    var id: String? = null
//    var createdAt: Date? = Date()
    var name: String = ""
    var url: String = ""

    constructor(){
        this.id = ""
//        this.createdAt = Date()
        this.name = ""
        this.url = ""
    }
    constructor(name: String,url :String,id :String, createdAt :String) {
        this.id = id
//        this.createdAt = Date()
        this.name = name
        this.url = url
    }

    constructor(json: JsonObject) {
        this.id =json.getString("_id")
//        this.createdAt = Date.from(json.getInstant("createdAt"))
        this.name = json.getString("name")
        this.url = json.getString("url")
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
                .put("id",id)
                .put("name", name)
                .put("url", url)
//                .put("createdAt", Date(createdAt!!.time).toInstant())
        return json
    }
}
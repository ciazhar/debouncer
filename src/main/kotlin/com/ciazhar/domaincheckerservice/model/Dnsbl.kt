package com.ciazhar.domaincheckerservice.model

import io.vertx.core.json.JsonObject
import java.util.*

class Dnsbl {
    var id: String? = null
    var name: String = ""
    var url: String = ""

    constructor(){
        this.id = ""
        this.name = ""
        this.url = ""
    }
    constructor(name: String,url :String) {
        this.id = UUID.randomUUID().toString()
        this.name = name
        this.url = url
    }

    constructor(json: JsonObject) {
        this.id =json.getString("_id")
        this.name = json.getString("name")
        this.url = json.getString("url")
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
                .put("id",id)
                .put("name", name)
                .put("url", url)
        return json
    }
}
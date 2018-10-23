package com.ciazhar.domaincheckerservice.model

import io.vertx.core.json.JsonObject

class DnsblCsv{
    var name: String = ""

    constructor(){}
    constructor(name: String) {
        this.name = name
    }

    constructor(json: JsonObject) {
        this.name = json.getString("name")
    }

    fun toJson(): JsonObject {
        val json = JsonObject()
                .put("name", name)
        return json
    }
}
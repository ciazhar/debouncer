package com.ciazhar.domaincheckerservice

import com.ciazhar.domaincheckerservice.extension.propertiesConfiguration
import com.ciazhar.domaincheckerservice.extension.retrieveConfig
import com.ciazhar.domaincheckerservice.extension.useLogBack
import com.ciazhar.domaincheckerservice.verticle.MainVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.ext.mongo.MongoClient

/**
 * Created by ciazhar on 8/29/17.
 */

class Application {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            useLogBack()

            println("Inisialisasi Vertx")
            val vertex = Vertx.vertx()
            val configurationProperties = propertiesConfiguration("application-config.properties")
            val configuration = vertex.retrieveConfig(configurationProperties).toBlocking().first()

            println("Inisialisasi MongoDB")
            val mongo = MongoClient.createShared(vertex,configuration)

            println("Deploy Main Verticle")
            val mainVerticle = MainVerticle(mongo)
            vertex.deployVerticle(mainVerticle, DeploymentOptions().apply {
                this.config = configuration
            })
            println("Application Success Running")
        }
    }
}
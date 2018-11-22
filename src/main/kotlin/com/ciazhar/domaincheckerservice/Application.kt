package com.ciazhar.domaincheckerservice

import com.ciazhar.domaincheckerservice.extension.propertiesConfiguration
import com.ciazhar.domaincheckerservice.extension.retrieveConfig
import com.ciazhar.domaincheckerservice.extension.useLogBack
import com.ciazhar.domaincheckerservice.verticle.MainVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx

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

            println("Deploy Main Verticle")
            val mainVerticle = MainVerticle()
            vertex.deployVerticle(mainVerticle, DeploymentOptions().apply {
                this.config = configuration
            })
            println("Application Success Running")
        }
    }
}
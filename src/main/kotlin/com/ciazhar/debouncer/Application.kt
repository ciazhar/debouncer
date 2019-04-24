package com.ciazhar.debouncer

import com.ciazhar.debouncer.extension.propertiesConfiguration
import com.ciazhar.debouncer.extension.retrieveConfig
import com.ciazhar.debouncer.extension.useLogBack
import com.ciazhar.debouncer.verticle.DNSBLVerticle
import com.ciazhar.debouncer.verticle.EmailVerticle
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
            val mainVerticle = DNSBLVerticle()
            vertex.deployVerticle(mainVerticle, DeploymentOptions().apply {
                this.config = configuration
            })
            val mainVerticle2 = EmailVerticle()
            vertex.deployVerticle(mainVerticle2, DeploymentOptions().apply {
                this.config = configuration
            })
            println("Application Success Running")
        }
    }
}
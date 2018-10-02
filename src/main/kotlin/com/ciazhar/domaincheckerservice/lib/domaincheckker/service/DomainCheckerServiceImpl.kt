package com.ciazhar.domaincheckerservice.lib.domaincheckker.service

import org.xbill.DNS.*
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by ciazhar on 05/02/18.
 * [ Documentatiion Here ]
 */
class DomainCheckerServiceImpl : DomainCheckerService {

    override fun checkDomain(domain: String, dnsbl: String): Observable<Boolean> {
        return Observable.from(Lookup(domain, Type.A).run()).subscribeOn(Schedulers.newThread()).flatMap {
            //println("check $domain on $dnsbl")
            Observable.just(it).subscribeOn(Schedulers.newThread()).map {
                var result = false
                it as ARecord
                val ip = reverseIp(it.address.hostAddress)
                val lookup2 = Lookup("$ip.$dnsbl", Type.TXT)
                val resolver = SimpleResolver()
                lookup2.setResolver(resolver)
                lookup2.setCache(null)
                lookup2.run()

                println("check $domain on $dnsbl. Lookup $ip.$dnsbl. Result ${lookup2.errorString}")

                ///blocked
                if (lookup2.result == Lookup.SUCCESSFUL) {
                    result = true
                }
                ///not blocked
                else if (lookup2.result == Lookup.HOST_NOT_FOUND) {
                    result = false
                }

                //println("check $domain on $dnsbl $result")

                return@map result
            }
        }.toList().map {
            return@map !it.joinToString().contains("false")
        }
    }

    private fun reverseIp(content: String) : String {
        return content.split(".").reversed().joinToString(".")
    }
}
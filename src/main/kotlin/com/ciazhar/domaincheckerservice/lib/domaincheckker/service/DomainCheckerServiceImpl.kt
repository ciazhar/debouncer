package com.ciazhar.domaincheckerservice.lib.domaincheckker.service

import com.ciazhar.domaincheckerservice.model.DnsblCsv
import com.ciazhar.domaincheckerservice.service.readFromCsv
import com.ciazhar.domaincheckerservice.service.writeToCsv
import org.jsoup.Jsoup
import org.xbill.DNS.*
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by ciazhar on 05/02/18.
 * [ Documentatiion Here ]
 */
class DomainCheckerServiceImpl : DomainCheckerService {

    override fun scrapDnsbl(fileName : String) : String {
        //init dnsbl list
        var dnsblList : List<DnsblCsv> = readFromCsv()

        //scrap
        val doc = Jsoup.connect("https://www.dnsbl.info/dnsbl-list.php").get()
        val dnsbls : MutableList<DnsblCsv> = mutableListOf()
        doc.select("td[width='33%']").forEach {
            dnsbls.add(DnsblCsv(
                    name = it.select("a").text()
            ))
        }

        //read from csv and update
        dnsblList += dnsbls
        dnsblList = dnsblList.distinctBy { it.name }

        //write to csv
        return writeToCsv(dnsblList)
    }

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
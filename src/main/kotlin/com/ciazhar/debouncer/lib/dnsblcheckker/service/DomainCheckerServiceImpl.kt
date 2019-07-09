package com.ciazhar.debouncer.lib.dnsblcheckker.service

import com.ciazhar.debouncer.lib.dnsblcheckker.model.Dnsbl
import com.ciazhar.debouncer.lib.dnsblcheckker.util.readFromCsv
import com.ciazhar.debouncer.lib.dnsblcheckker.util.removeLines
import com.ciazhar.debouncer.lib.dnsblcheckker.util.writeToCsv
import org.jsoup.Jsoup
import org.xbill.DNS.*
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by ciazhar on 05/02/18.
 * [ Documentatiion Here ]
 */
class DomainCheckerServiceImpl : DomainCheckerService {

    fun html2text(html: String): String {
        return Jsoup.parse(html).text()
    }

    override fun scrapDnsbl(fileName : String) : String {
        //init dnsbl list
        var dnsblList : List<Dnsbl> = readFromCsv(fileName)
        val dnsbls : MutableList<Dnsbl> = mutableListOf()

        //scrap
        val doc = Jsoup.connect("https://en.m.wikipedia.org/wiki/Comparison_of_DNS_blacklists").get()
        val table = doc.select("table").get(2)
        val rows = table.select("tr")

        for (i in 1 until rows.size) { //first row is the col names so skip it.
            val row = rows.get(i)
            val cols = row.select("td")
            val dnsbl = html2text(cols.get(0).html()).split(" ")[0]
            if (    !dnsbl.contains("ahbl.org")&&
                    !dnsbl.contains("orbitrbl.com")&&
                    !dnsbl.contains("Paid")&&
                    !dnsbl.contains("proxybl.org")&&
                    !dnsbl.contains("spamcannibal.org")&&
                    !dnsbl.contains("drand.net")&&
                    !dnsbl.contains("surgate.net")&&
                    !dnsbl.contains("quorum.to")
            ){
                dnsbls.add(Dnsbl(
                        name = dnsbl
                ))
            }
        }

        //read from csv and update
        dnsblList += dnsbls
        dnsblList = dnsblList.distinctBy { it.name }

        //write to csv
        return writeToCsv(fileName, dnsblList)
    }

    override fun deletednsbl(id : String, fileName: String) {
        var idInt = 0
        try {
            idInt = id.toInt()
        } catch (nfe: NumberFormatException) {
            // not a valid int, handle this as you wish
        }

        //delete line
        removeLines(fileName, idInt + 1, 1)
    }

    override fun getDnsbl(fileName : String) : MutableList<Dnsbl>{
        return readFromCsv(fileName)
    }

    override fun addDnsbl(fileName : String, dnsbl : Dnsbl) : List<Dnsbl> {
        //read from csv
        var dnsblList = readFromCsv(fileName).toList()
        dnsblList += dnsbl
        dnsblList = dnsblList.distinctBy { it.name }

        //write to csv
        writeToCsv(fileName, dnsblList)

        return dnsblList
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
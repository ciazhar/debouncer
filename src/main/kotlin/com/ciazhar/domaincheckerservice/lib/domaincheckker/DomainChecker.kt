package com.ciazhar.domaincheckerservice.lib.domaincheckker

import com.ciazhar.domaincheckerservice.lib.domaincheckker.service.DomainCheckerServiceImpl
import rx.Observable
import rx.schedulers.Schedulers

/**
 * Created by ciazhar on 05/02/18.
 * [ Documentatiion Here ]
 */
object DomainChecker {
    private val service by lazy { DomainCheckerServiceImpl() }

    val dnsblList = mutableListOf(
            "aspews.ext.sorbs.net",
            "b.barracudacentral.org",
            "bad.psky.me",
            "bl.deadbeef.com",
//            "bl.emailbasura.org",
            "bl.mailspike.net",
            "bl.score.senderscore.com",
//            "bl.spamcannibal.org",
//            "bl.spameatingmonkey.net",
            "bl.spamcop.net",
            "blackholes.five-ten-sg.com",
            "blacklist.woody.ch",
            "bogons.cymru.com",
            "cbl.abuseat.org",
            "cdl.anti-spam.org.cn",
            "combined.abuse.ch",
            "combined.rbl.msrbl.net",
            "db.wpbl.info",
            "dnsbl-1.uceprotect.net",
            "dnsbl-2.uceprotect.net",
            "dnsbl-3.uceprotect.net",
//            "dnsbl.ahbl.org",
            "dnsbl.cyberlogic.net",
            "dnsbl.inps.de",
//            "dnsbl.njabl.org",
            "dnsbl.sorbs.net",
            "drone.abuse.ch",
            "duinv.aupads.org",
            "dul.dnsbl.sorbs.net",
            "dul.ru",
            "dyna.spamrats.com",
            "dynip.rothen.com",
            "http.dnsbl.sorbs.net",
            "images.rbl.msrbl.net",
            "ips.backscatterer.org",
            "ix.dnsbl.manitu.net",
            "korea.services.net",
            "misc.dnsbl.sorbs.net",
            "noptr.spamrats.com",
            "ohps.dnsbl.net.au",
            "omrs.dnsbl.net.au",
            "orvedb.aupads.org",
            "osps.dnsbl.net.au",
            "osrs.dnsbl.net.au",
            "owfs.dnsbl.net.au",
            "owps.dnsbl.net.au",
            "phishing.rbl.msrbl.net",
            "probes.dnsbl.net.au",
            "proxy.bl.gweep.ca",
            "proxy.block.transip.nl",
            "psbl.surriel.com",
            "rbl.interserver.net",
            "rdts.dnsbl.net.au",
            "relays.bl.gweep.ca",
            "relays.bl.kundenserver.de",
            "relays.nether.net",
            "residential.block.transip.nl",
            "ricn.dnsbl.net.au",
            "rmst.dnsbl.net.au",
//            "short.rbl.jp",
            "smtp.dnsbl.sorbs.net",
            "socks.dnsbl.sorbs.net",
            "spam.abuse.ch",
            "spam.dnsbl.sorbs.net",
            "spam.rbl.msrbl.net",
            "spam.spamrats.com",
            "spamlist.or.kr",
            "spamrbl.imp.ch",
            "t3direct.dnsbl.net.au",
//            "tor.ahbl.org",
            "tor.dnsbl.sectoor.de",
            "torserver.tor.dnsbl.sectoor.de",
            "ubl.lashback.com",
//            "ubl.unsubscore.com",
            "virbl.bit.nl",
            "virus.rbl.jp",
            "virus.rbl.msrbl.net",
            "web.dnsbl.sorbs.net",
            "wormrbl.imp.ch",
            "xbl.spamhaus.org",
            "zen.spamhaus.org",
            "zombie.dnsbl.sorbs.net"
    )

    @JvmStatic
    fun checkDomain(domain: String): MutableList<String> {

        println("Start Checking $domain ...")
        println("Please wait for some seconds ...")

        val blockedList = mutableListOf<String>()

        Observable.from(dnsblList).subscribeOn(Schedulers.newThread()).flatMap { dnsbl ->
            service.checkDomain(domain, dnsbl).map { it to dnsbl }
        }.filter {
            it.first
        }.map {
            blockedList.add(it.second)
        }.doOnCompleted {
            println("Done")
        }.toBlocking().subscribe()

        return blockedList
    }

    @JvmStatic
    fun checkDomain(domain: String, newDnsblList: MutableList<String>): MutableList<String> {

        println("Start Checking $domain ...")
        println("Please wait for some seconds ...")

        dnsblList.addAll(newDnsblList)

        val blockedList = mutableListOf<String>()

        Observable.from(dnsblList).flatMap { dnsbl ->
            service.checkDomain(domain, dnsbl).map { it to dnsbl }
        }.filter {
            it.first
        }.map {
            blockedList.add(it.second)
        }.doOnCompleted {
            println("Done")
        }.toBlocking().subscribe()

        return blockedList
    }

    @JvmStatic
    fun scrapDnsbl(fileName : String):String {
        return service.scrapDnsbl(fileName)
    }
}
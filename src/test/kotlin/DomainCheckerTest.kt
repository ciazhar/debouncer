package com.ciazhar.domainchecker

import com.ciazhar.domaincheckerservice.lib.domaincheckker.DomainChecker
import org.junit.Test

/**
 * Created by ciazhar on 13/02/18.
 * [ Documentatiion Here ]
 */
class DomainCheckerTest {

    private val vulnerableDomain = "spgdt.id"
    private val secureDomain = "dinus.ac.id"
    private val CSV_FILE_NAME = "dnsbl.csv"

    @Test
    fun scrapDnsbl() {
        println("Running scrapDnsbl ...")
        val dnsbls = DomainChecker.scrapDnsbl(CSV_FILE_NAME)
        println(dnsbls)
        assert(dnsbls.isNotEmpty())
    }

    @Test
    fun checkSecureDomainSuccess() {
        println("Running checkSecureDomainSuccess ...")
        val dnsbls = DomainChecker.getDnsbl(CSV_FILE_NAME).map { it.name }.toMutableList()
        val listResult = DomainChecker.checkDomain(secureDomain,dnsbls)
        println(listResult)
        assert(listResult.isEmpty())
    }

    @Test
    fun checkVulnerableDomainSuccess() {
        println("Running checkVulnerableDomainSuccess ...")
        val dnsbls = DomainChecker.getDnsbl(CSV_FILE_NAME).map { it.name }.toMutableList()
        val listResult = DomainChecker.checkDomain(vulnerableDomain,dnsbls)
        println(listResult)
        assert(listResult.isNotEmpty())
    }
}
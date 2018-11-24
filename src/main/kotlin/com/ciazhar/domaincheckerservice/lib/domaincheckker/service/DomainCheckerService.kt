package com.ciazhar.domaincheckerservice.lib.domaincheckker.service

import com.ciazhar.domaincheckerservice.lib.domaincheckker.model.Dnsbl
import rx.Observable

/**
 * Created by ciazhar on 05/02/18.
 * [ Documentatiion Here ]
 */
interface DomainCheckerService {
    fun checkDomain(domain: String, dnsbl: String): Observable<Boolean>
    fun scrapDnsbl(fileName : String) : String
    fun getDnsbl(fileName : String) : MutableList<Dnsbl>
    fun addDnsbl(fileName : String, dnsbl : Dnsbl) : List<Dnsbl>
    fun deletednsbl(id : String, fileName: String)
}
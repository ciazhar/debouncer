package com.ciazhar.debouncer.lib.dnsemailaddresschecker.service

import com.ciazhar.debouncer.lib.dnsemailaddresschecker.model.AddressStatus

interface EmailAddressCheckerService {
    fun validate(mailAddress : String) : AddressStatus
}
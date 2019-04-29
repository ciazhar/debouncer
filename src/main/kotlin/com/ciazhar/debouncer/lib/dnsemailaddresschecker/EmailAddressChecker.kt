package com.ciazhar.debouncer.lib.dnsemailaddresschecker

import com.ciazhar.debouncer.lib.dnsemailaddresschecker.model.AddressStatus
import com.ciazhar.debouncer.lib.dnsemailaddresschecker.service.EmailAddressCheckerServiceImpl

object MailAddressVerifier {

    private val service by lazy {EmailAddressCheckerServiceImpl()}

    @JvmStatic
    fun validate(content : String) : AddressStatus {
        return service.validate(content)
    }

}
package com.ciazhar.debouncer.lib.emailaddresschecker

import com.ciazhar.debouncer.lib.emailaddresschecker.model.AddressStatus
import com.ciazhar.debouncer.lib.emailaddresschecker.service.EmailAddressCheckerServiceImpl

object MailAddressVerifier {

    private val service by lazy {EmailAddressCheckerServiceImpl()}

    @JvmStatic
    fun validate(content : String) : AddressStatus {
        return service.validate(content)
    }

}
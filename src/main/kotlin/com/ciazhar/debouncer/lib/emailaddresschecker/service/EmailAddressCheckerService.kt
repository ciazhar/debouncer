package com.ciazhar.debouncer.lib.emailaddresschecker.service

import com.ciazhar.debouncer.lib.emailaddresschecker.model.AddressStatus

interface EmailAddressCheckerService {
    fun validate(mailAddress : String) : AddressStatus
}
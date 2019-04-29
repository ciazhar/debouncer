package com.ciazhar.debouncer

import com.ciazhar.debouncer.lib.dnsemailaddresschecker.MailAddressVerifier
import org.junit.Test

class EmailAddressChecker{

    @Test
    fun validateEmailAddress(){
        val str = "rully.arifin@ibsolutions.co.id"
        val result = MailAddressVerifier.validate(str)
        print(result)
    }

}
package com.ciazhar.debouncer.lib.dnsemailaddresschecker.service

import com.ciazhar.debouncer.lib.dnsemailaddresschecker.model.AddressStatus
import org.xbill.DNS.Lookup
import org.xbill.DNS.MXRecord
import org.xbill.DNS.TextParseException
import org.xbill.DNS.Type
import java.net.IDN

class EmailAddressCheckerServiceImpl : EmailAddressCheckerService{

    init {
        Lookup.getDefaultResolver().setTimeout(1)
    }

    override fun validate(mailAddress: String) :AddressStatus {
        val domain = getDomain(mailAddress) ?: return AddressStatus.wrongSchema.setMailAddress(mailAddress)

        return try {
            if (!isDnsValid(domain)) {
                AddressStatus.noMxRecord.setMailAddress(mailAddress)
            }
            else{
                AddressStatus.valid.setMailAddress(mailAddress)
            }
        }
        catch (e: IllegalStateException) {
            AddressStatus.unknown.setMailAddress(mailAddress)
        }
    }

    private fun getDomain(mail: String): String? {
        val part = mail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (part.size != 2) {
            print("Syntax error on : $mail")
            return null
        }
        return IDN.toASCII(part[1])
    }

    private fun getUser(mail: String): String? {
        val part = mail.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return IDN.toASCII(part[0])
    }

    private fun isDnsValid(domain: String): Boolean {
        try {
            val l = Lookup(domain, Type.MX)
            val result = l.run()
            if (l.result == Lookup.TRY_AGAIN) {
                throw IllegalStateException()
            }
            if (result != null && result.isNotEmpty()) {
                result
                        .map { it as MXRecord }
                        .map { Lookup(it.target, Type.A).run() }
                        .forEach {
                            if (it != null && it.isNotEmpty()) {
                                return true
                            }
                        }
            }
        } catch (e: TextParseException) {
            e.printStackTrace()
        }
        return false
    }
}
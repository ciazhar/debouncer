package com.ciazhar.debouncer.lib.emailsender

import com.ciazhar.debouncer.lib.emailsender.model.Mail
import com.ciazhar.debouncer.lib.emailsender.service.EmailSenderServiceImpl

object EmailSender {
    private val service by lazy { EmailSenderServiceImpl() }

    @JvmStatic
    fun sendFromGMail(mail : Mail) : String {
        return service.sendFromGMail(mail)
    }
}
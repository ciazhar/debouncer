package com.ciazhar.debouncer.lib.emailsender.service

import com.ciazhar.debouncer.lib.emailsender.model.Mail

interface EmailSenderService {
    fun sendFromGMail(mail : Mail)
}
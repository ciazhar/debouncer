package com.ciazhar.debouncer

import com.ciazhar.debouncer.lib.emailsender.EmailSender
import com.ciazhar.debouncer.lib.emailsender.model.Mail
import org.junit.Test

class EmailSenderTest{
    @Test
    fun sendEmail() {
        val mail =  Mail(
                "smtp.gmail.com",
                "daisetsunakama",
                "x",
                arrayOf("ciazhar.id@gmail.com", "penguinoflostatlantica@gmail.com"),
                "MSG",
                "ini body"
        )
        EmailSender.sendFromGMail(mail)
    }
}
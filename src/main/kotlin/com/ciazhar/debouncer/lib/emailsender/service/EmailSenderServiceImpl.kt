package com.ciazhar.debouncer.lib.emailsender.service

import com.ciazhar.debouncer.lib.emailsender.model.Mail
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSenderServiceImpl : EmailSenderService {
    override fun sendFromGMail(mail : Mail) {
        val props = System.getProperties()
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = mail.host
        props["mail.smtp.user"] = mail.username
        props["mail.smtp.password"] = mail.password
        props["mail.smtp.port"] = "587"
        props["mail.smtp.auth"] = "true"

        val session = Session.getDefaultInstance(props)
        val message = MimeMessage(session)

        try {
            message.setFrom(InternetAddress(mail.username))
            val toAddress = arrayOfNulls<InternetAddress>(mail.recipient.size)

            // To get the array of addresses
            for (i in mail.recipient.indices) {
                toAddress[i] = InternetAddress(mail.recipient[i])
            }

            for (toAddres in toAddress) {
                message.addRecipient(Message.RecipientType.TO, toAddres)
            }

            message.subject = mail.subject
            message.setText(mail.body)
            val transport = session.getTransport("smtp")
            transport.connect(mail.host, mail.username, mail.password)
            transport.sendMessage(message, message.allRecipients)
            transport.close()
        } catch (me: MessagingException) {
            me.printStackTrace()
        }

    }

}
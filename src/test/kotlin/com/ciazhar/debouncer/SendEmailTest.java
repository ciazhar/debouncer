package com.ciazhar.debouncer;

import com.ciazhar.debouncer.lib.emailsender.UsableSendMail;

public class SendEmailTest {
    public static void main(String[] args) {
        UsableSendMail sendMail =
                new UsableSendMail("smtp.gmail.com",
                        "daisetsunakama", //dont write @gmail.com part in it. Just write front part.
                        "x", //your gmail password
                        new String[]{"ciazhar.id@gmail.com", "penguinoflostatlantica@gmail.com"}, //emails of receiver
                        "MSG", //message you want to send,
                        "ini body"
                );
        sendMail.sendFromGMail();
    }
}

package com.ciazhar.debouncer.lib.emailsender.model

class Mail{
    var host: String = ""
    var username: String = ""
    var password: String = ""
    var recipient: Array<String> = emptyArray()
    var subject: String = ""
    var body: String = ""

    constructor()
    constructor(host: String, username: String, password: String, recipient: Array<String>, subject: String, body: String) {
        this.host = host
        this.username = username
        this.password = password
        this.recipient = recipient
        this.subject = subject
        this.body = body
    }

}
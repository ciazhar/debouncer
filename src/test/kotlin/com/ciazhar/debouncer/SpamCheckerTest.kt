package com.ciazhar.debouncer

import com.ciazhar.debouncer.lib.svmchecker.SpamChecker
import com.ciazhar.debouncer.lib.svmchecker.service.SVMCheckerService
import org.junit.Test

class SpamCheckerTest {
    @Test
    fun spamCheck() {

        val text = "Return-Path: <tqznmzkkdnayh@getherbalnow.info>\n" +
                "Delivered-To: rait@bruce-guenter.dyndns.org\n" +
                "Received: (qmail 26496 invoked from network); 1 Feb 2005 00:25:05 -0000\n" +
                "Received: from localhost (localhost [127.0.0.1])\n" +
                "  by bruce-guenter.dyndns.org ([192.168.1.3]); 01 Feb 2005 00:25:05 -0000\n" +
                "Received: from zak.futurequest.net ([127.0.0.1])\n" +
                "  by localhost ([127.0.0.1])\n" +
                "  with SMTP via TCP; 01 Feb 2005 00:25:05 -0000\n" +
                "Received: (qmail 30245 invoked from network); 1 Feb 2005 00:25:04 -0000\n" +
                "Received: from dsl-200-95-90-28.prod-infinitum.com.mx (unknown [200.95.90.28])\n" +
                "  by zak.futurequest.net ([69.5.6.152])\n" +
                "  with SMTP via TCP; 01 Feb 2005 00:25:01 -0000\n" +
                "X-Message-Info: STRQ+pfol/442+u/C+48/15683577152822\n" +
                "Received: from smtp-brassiere.abalone.tqznmzkkdnayh@getherbalnow.info ([200.95.90.28]) by ko37-r05.tqznmzkkdnayh@getherbalnow.info with Microsoft SMTPSVC(5.0.8987.8828);\n" +
                "\t Mon, 31 Jan 2005 21:22:57 -0300\n" +
                "Received: from flop75.biometry.tqznmzkkdnayh@getherbalnow.info (dose75.tqznmzkkdnayh@getherbalnow.info [200.95.90.28])\n" +
                "\tby smtp-altair.utterance.tqznmzkkdnayh@getherbalnow.info (Postfix) with SMTP id 5CBU8A33VUEQ\n" +
                "\tfor <rait@bruce-guenter.dyndns.org>; Mon, 31 Jan 2005 20:23:57 -0400\n" +
                "X-Message-Info: FYZYN+%ND_LC_CHAR[1-3]65+nmt+R+7/4132393387359\n" +
                "Received: (qmail 35145 invoked by uid 138); Tue, 01 Feb 2005 01:18:57 +0100\n" +
                "Date: Mon, 31 Jan 2005 21:18:57 -0300\n" +
                "Message-Id: <35655717.33376@tqznmzkkdnayh@getherbalnow.info>\n" +
                "From: Jamar Toney <tqznmzkkdnayh@getherbalnow.info>\n" +
                "To: Rait <rait@bruce-guenter.dyndns.org>\n" +
                "Subject: Cheapest V.IA.G.R.A!! 70% Discount! \n" +
                "MIME-Version: 1.0 (produced by cretannagy 7.6)\n" +
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"--5706099938399901743\"\n" +
                "Content-Length: 423\n" +
                "Lines: 27\n" +
                "\n" +
                "----5706099938399901743\n" +
                "Content-Type: text/plain;\n" +
                "\tcharset=\"iso-2816-8\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "Content-Description: what crisscross poetry\n" +
                "\n" +
                "Most places charge $18,\n" +
                "\n" +
                "we charge just $1\n" +
                "\n" +
                "You will never get cheaper v.i.a.g.r.a.! \n" +
                "\n" +
                "Order today before offer expires. \n" +
                "\n" +
                "Delivered world wide!\n" +
                "\n" +
                "http://dfbeky-wr.com/free/?sash99 \n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "optout link\n" +
                "http://dfbeky-wr.com/rm.php?sash99 \n" +
                "\n" +
                "\n" +
                "----5706099938399901743--\n"

        val classifier = SVMCheckerService(SpamChecker.trainOrLoadModel())
        SpamChecker.predict(classifier, text)
    }

}
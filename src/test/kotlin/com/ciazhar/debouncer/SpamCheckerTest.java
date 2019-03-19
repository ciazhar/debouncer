package com.ciazhar.debouncer;

import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVMClassifier;
import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVMModel;
import com.hankcs.hanlp.classification.classifiers.IClassifier;

import java.io.*;

import static com.hankcs.hanlp.utility.Predefine.logger;

public class SpamCheckerTest {
    public static final String CORPUS_FOLDER = "data/dataset";
    /**
     * Model save path
     */
    public static final String MODEL_PATH = "data/svm-classification-model.ser";

    public static void main(String[] args) throws IOException
    {
        IClassifier classifier = new LinearSVMClassifier(trainOrLoadModel());
        predict(classifier, "This helps the smooth muscles in ");
        predict(classifier, "From andraallisonhuaa@email.com  Thu Sep 19 17:51:47 2002\n" +
                "Return-Path: <andraallisonhuaa@email.com>\n" +
                "Delivered-To: zzzz@localhost.spamassassin.taint.org\n" +
                "Received: from localhost (jalapeno [127.0.0.1])\n" +
                "\tby zzzzason.org (Postfix) with ESMTP id EE11116F03\n" +
                "\tfor <zzzz@localhost>; Thu, 19 Sep 2002 17:51:45 +0100 (IST)\n" +
                "Received: from jalapeno [127.0.0.1]\n" +
                "\tby localhost with IMAP (fetchmail-5.9.0)\n" +
                "\tfor zzzz@localhost (single-drop); Thu, 19 Sep 2002 17:51:45 +0100 (IST)\n" +
                "Received: from webnote.net (mail.webnote.net [193.120.211.219]) by\n" +
                "    dogma.slashnull.org (8.11.6/8.11.6) with ESMTP id g8JEuLC24080 for\n" +
                "    <zzzz@jmason.org>; Thu, 19 Sep 2002 15:56:31 +0100\n" +
                "Received: from dev-mail1.netsgo.com ([211.39.34.61]) by webnote.net\n" +
                "    (8.9.3/8.9.3) with ESMTP id PAA02126 for <zzzz@spamassassin.taint.org>;\n" +
                "    Thu, 19 Sep 2002 15:56:51 +0100\n" +
                "From: andraallisonhuaa@email.com\n" +
                "Received: from email-com.mr.outblaze.com (203.145.4.117) by\n" +
                "    dev-mail1.netsgo.com (6.5.007) id 3D6CAB5A0016F30F; Thu, 19 Sep 2002\n" +
                "    23:40:18 +0900\n" +
                "Message-Id: <000072146bff$00001431$000010db@email-com.mr.outblaze.com>\n" +
                "To: <Undisclosed.Recipients@webnote.net>\n" +
                "Subject: Hgh: safe and effective release of your own growth hormone!30543\n" +
                "Date: Thu, 19 Sep 2002 21:45:31 -0500\n" +
                "MIME-Version: 1.0\n" +
                "Reply-To: 100074c213aaa002@hotmail.com\n" +
                "1: X-Mailer: Microsoft Outlook Express 5.50.4522.1200\n" +
                "Content-Type: text/html; charset=\"iso-8859-1\"\n" +
                "Content-Transfer-Encoding: quoted-printable\n" +
                "\n" +
                "<html>\n" +
                "<head>\n" +
                "   <meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Diso-8=\n" +
                "859-1\">\n" +
                "   <meta name=3D\"GENERATOR\" content=3D\"Mozilla/4.77 [en] (Windows NT 5.0; =\n" +
                "U) [Netscape]\">\n" +
                "   <meta name=3D\"ProgId\" content=3D\"FrontPage.Editor.Document\">\n" +
                "   <title>Lose weight while building lean muscle mass  and reversing the\n" +
                "ravages of aging all at once</title>\n" +
                "</head>\n" +
                "<body>\n" +
                " \n" +
                "<center><table BORDER WIDTH=3D\"52%\" >\n" +
                "<tr>\n" +
                "<td WIDTH=3D\"90%\" BGCOLOR=3D\"#CCCCFF\"><b><font face=3D\"Arial\"><font color=3D=\n" +
                "\"#FFFFFF\"><font size=3D+2></font></font></font></b> \n" +
                "<center><table BORDER=3D0 CELLSPACING=3D10 CELLPADDING=3D0 COLS=3D1 WIDTH=3D=\n" +
                "\"100%\" >\n" +
                "<tr>\n" +
                "<td WIDTH=3D\"100%\" BGCOLOR=3D\"#000000\">\n" +
                "<center><b><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><font size=3D+2>Hu=\n" +
                "man\n" +
                "Growth Hormone Therapy</font></font></font></b></center>\n" +
                "</td>\n" +
                "</tr>\n" +
                "</table></center>\n" +
                "\n" +
                "<center>\n" +
                "<p><b><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><font size=3D+1>Lose we=\n" +
                "ight\n" +
                "while building lean muscle mass</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><font size=3D+1> and\n" +
                "reversing the ravages of aging all at once.</font></font></font></b>\n" +
                "<p><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><font size=3D+0>Remarkable=\n" +
                " discoveries\n" +
                "about Human Growth Hormones (<b>HGH</b>) </font></font></font>\n" +
                "<br><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><font size=3D+0>are chang=\n" +
                "ing\n" +
                "the way we think about aging and weight loss.</font></font></font></center=\n" +
                ">\n" +
                "\n" +
                "<br> \n" +
                "<center><table WIDTH=3D\"442\" BGCOLOR=3D\"#FFFFFF\" >\n" +
                "<tr>\n" +
                "<td WIDTH=3D\"229\" HEIGHT=3D\"2\"><b><font face=3D\"Arial, Helvetica, sans-ser=\n" +
                "if\"><font color=3D\"#000000\"><font size=3D+0>Lose\n" +
                "Weight</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "><font size=3D+0>Build\n" +
                "Muscle Tone</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "><font size=3D+0>Reverse\n" +
                "Aging</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "><font size=3D+0>Increased\n" +
                "Libido</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "><font size=3D+0>Duration\n" +
                "Of Penile Erection</font></font></font></b></td>\n" +
                "\n" +
                "<td WIDTH=3D\"199\" HEIGHT=3D\"2\"><b><font face=3D\"Arial, Helvetica, sans-ser=\n" +
                "if\"><font color=3D\"#000000\"><font size=3D+0> New\n" +
                "Hair Growth</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "><font size=3D+0> Improved\n" +
                "Memory</font></font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "> Improved\n" +
                "skin</font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "> New\n" +
                "Hair Growth</font></font></b>\n" +
                "<br><b><font face=3D\"Arial, Helvetica, sans-serif\"><font color=3D\"#000000\"=\n" +
                "> Wrinkle\n" +
                "Disappearance </font></font></b></td>\n" +
                "</tr>\n" +
                "</table></center>\n" +
                "\n" +
                "<center>\n" +
                "<p><b><font face=3D\"Arial\"><font color=3D\"#FFFFFF\"><a href=3D\"http://marke=\n" +
                "t.businessonlinenow.com/hgh/\">Visit\n" +
                "Our Web Site and Lean The Facts: Click Here</a></font></font></b></center>\n" +
                "\n" +
                "<p> </td>\n" +
                "</tr>\n" +
                "</table></center>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n");
    }

    private static void predict(IClassifier classifier, String text)
    {
        System.out.printf("《%s》 属于分类 【%s】\n", text, classifier.classify(text));
    }

    private static LinearSVMModel trainOrLoadModel() throws IOException
    {
        LinearSVMModel model = (LinearSVMModel) readObjectFrom(MODEL_PATH);
        if (model != null) return model;

        File corpusFolder = new File(CORPUS_FOLDER);
        if (!corpusFolder.exists() || !corpusFolder.isDirectory())
        {
            System.err.println("Tanpa corpus teks, harap baca format corpus dan unduhan corpus yang didefinisikan dalam IClassifier.train (java.lang.String)：" +
                    "https://github.com/hankcs/HanLP/wiki/%E6%96%87%E6%9C%AC%E5%88%86%E7%B1%BB%E4%B8%8E%E6%83%85%E6%84%9F%E5%88%86%E6%9E%90");
            System.exit(1);
        }

        IClassifier classifier = new LinearSVMClassifier();  // Buat classifier. Untuk fungsi lebih lanjut, silakan merujuk ke definisi antarmuka IClassifier.
        classifier.train(CORPUS_FOLDER);                     // Model yang terlatih mendukung ketekunan dan tidak harus dilatih lain kali.
        model = (LinearSVMModel) classifier.getModel();
        saveObjectTo(model, MODEL_PATH);
        return model;
    }

    /**
     * 序列化对象
     *
     * @param o
     * @param path
     * @return
     */
    public static boolean saveObjectTo(Object o, String path)
    {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
            oos.close();
        }
        catch (IOException e)
        {
            logger.warning("Pengecualian terjadi saat menyimpan objek "+ o +" ke "+ path +"" + e);
            return false;
        }

        return true;
    }

    /**
     * 反序列化对象
     *
     * @param path
     * @return
     */
    public static Object readObjectFrom(String path)
    {
        ObjectInputStream ois = null;
        try
        {
            ois = new ObjectInputStream(new FileInputStream(path));
            Object o = ois.readObject();
            ois.close();
            return o;
        }
        catch (Exception e)
        {
            logger.warning("Dari" + path + "Pengecualian terjadi saat membaca objek" + e);
        }

        return null;
    }
}

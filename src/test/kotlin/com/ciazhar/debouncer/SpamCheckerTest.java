package com.ciazhar.debouncer;

import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVMClassifier;
import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVMModel;
import com.hankcs.hanlp.classification.classifiers.IClassifier;

import java.io.*;

import static com.hankcs.hanlp.utility.Predefine.logger;

public class SpamCheckerTest {
    public static final String CORPUS_FOLDER = "data/搜狗文本分类语料库微型版";
    /**
     * Model save path
     */
    public static final String MODEL_PATH = "data/svm-classification-model.ser";

    public static void main(String[] args) throws IOException
    {
        IClassifier classifier = new LinearSVMClassifier(trainOrLoadModel());
        predict(classifier, "如果真想用食物解压,建议可以食用燕麦");
        predict(classifier, "美国财政部将就其主要贸易伙伴的外汇政策发布半年度报告");
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

package com.ciazhar.debouncer.lib.svmchecker.model;

import com.hankcs.hanlp.classification.classifiers.AbstractClassifier;
import com.hankcs.hanlp.classification.corpus.Document;
import com.hankcs.hanlp.classification.corpus.IDataSet;
import com.hankcs.hanlp.classification.features.*;
import com.hankcs.hanlp.classification.models.AbstractModel;
import com.hankcs.hanlp.classification.tokenizers.ITokenizer;
import com.hankcs.hanlp.classification.utilities.MathUtility;
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie;
import de.bwaldvogel.liblinear.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static com.hankcs.hanlp.classification.utilities.Predefine.logger;


/**
 * @author hankcs
 */
public class LinearSVMClassifier extends AbstractClassifier
{
    LinearSVMModel model;

    public LinearSVMClassifier()
    {
    }

    public LinearSVMClassifier(LinearSVMModel model)
    {
        this.model = model;
    }

    public Map<String, Double> predict(String text) throws IllegalArgumentException, IllegalStateException
    {
        if (model == null)
        {
            throw new IllegalStateException("Model yang tidak terlatih! Tidak dapat menjalankan perkiraan!");
        }
        if (text == null)
        {
            throw new IllegalArgumentException("Parameter teks == null");
        }

        //Segmentasi kata, buat dokumen
        Document document = new Document(model.wordIdTrie, model.tokenizer.segment(text));

        return predict(document);
    }

    @Override
    public double[] categorize(Document document) throws IllegalArgumentException, IllegalStateException
    {
        FeatureNode[] x = buildDocumentVector(document, model.featureWeighter);
        double[] probs = new double[model.svmModel.getNrClass()];
        Linear.predictProbability(model.svmModel, x, probs);
        return probs;
    }

    @Override
    public void train(IDataSet dataSet)
    {
        if (dataSet.size() == 0) throw new IllegalArgumentException("Kumpulan data pelatihan kosong dan tidak dapat melanjutkan pelatihan");
        // Fitur seleksi menggunakan
        DfFeatureData featureData = selectFeatures(dataSet);
        // Logika perhitungan berat konstruksi
        IFeatureWeighter weighter = new TfIdfFeatureWeighter(dataSet.size(), featureData.df);
        // Membangun masalah SVM
        Problem problem = createLiblinearProblem(dataSet, featureData, weighter);
        // Memori bebas
        BinTrie<Integer> wordIdTrie = featureData.wordIdTrie;
        featureData = null;
        ITokenizer tokenizer = dataSet.getTokenizer();
        String[] catalog = dataSet.getCatalog().toArray();
        dataSet = null;
        System.gc();
        // Memecahkan masalah SVM
        Model svmModel = solveLibLinearProblem(problem);
        // Assign variable
        model = new LinearSVMModel();
        model.tokenizer = tokenizer;
        model.wordIdTrie = wordIdTrie;
        model.catalog = catalog;
        model.svmModel = svmModel;
        model.featureWeighter = weighter;
    }

    public AbstractModel getModel()
    {
        return model;
    }

    private Model solveLibLinearProblem(Problem problem) {
        Parameter lparam = new Parameter(SolverType.L1R_LR,500.,0.01);
        return Linear.train(problem, lparam);
    }

    private Problem createLiblinearProblem(IDataSet dataSet, BaseFeatureData baseFeatureData, IFeatureWeighter weighter) {
        Problem problem = new Problem();
        int n = dataSet.size();
        problem.l = n;
        problem.n = baseFeatureData.featureCategoryJointCount.length;
        problem.x = new FeatureNode[n][];
        problem.y = new double[n];  // Versi terbaru dari liblinear's y array adalah angka floating point
        Iterator<Document> iterator = dataSet.iterator();
        for (int i = 0; i < n; i++)
        {
            // Membangun vektor dokumen
            Document document = iterator.next();
            problem.x[i] = buildDocumentVector(document, weighter);
            // Tetapkan nilai y sampel
            problem.y[i] = document.category;
        }

        return problem;
    }

    private FeatureNode[] buildDocumentVector(Document document, IFeatureWeighter weighter) {
        int termCount = document.tfMap.size();  // Jumlah kata
        FeatureNode[] x = new FeatureNode[termCount];
        Iterator<Map.Entry<Integer, int[]>> tfMapIterator = document.tfMap.entrySet().iterator();
        for (int j = 0; j < termCount; j++)
        {
            Map.Entry<Integer, int[]> tfEntry = tfMapIterator.next();
            int feature = tfEntry.getKey();
            int frequency = tfEntry.getValue()[0];
            x[j] = new FeatureNode(feature + 1,  // liblinear Membutuhkan subskrip untuk bertambah dari 1
                                   weighter.weight(feature, frequency));
        }
        // Menormalkan vektor
        double normalizer = 0;
        for (int j = 0; j < termCount; j++)
        {
            double weight = x[j].getValue();
            normalizer += weight * weight;
        }
        normalizer = Math.sqrt(normalizer);
        for (int j = 0; j < termCount; j++)
        {
            double weight = x[j].getValue();
            x[j].setValue(weight / normalizer);
        }

        return x;
    }

    protected DfFeatureData selectFeatures(IDataSet dataSet) {

        //Membuat extractor
        ChiSquareFeatureExtractor chiSquareFeatureExtractor = new ChiSquareFeatureExtractor();

        //Mengubah dataset mentah menjadi fitur data (yang mengandung inverted DF) dengan menghitung statistiknya
        DfFeatureData featureData = new DfFeatureData(dataSet);

        logger.start("Gunakan deteksi chi-square untuk memilih fitur ...\n");
        //Meneruskan statistik ini ke algoritme pemilihan fitur untuk mendapatkan fitur dan nilainya.
        Map<Integer, Double> selectedFeatures = chiSquareFeatureExtractor.chi_square(featureData);

        //Menghapus fitur yang tidak berguna dari data pelatihan dan rekonstruksi peta fitur
        String[] wordIdArray = dataSet.getLexicon().getWordIdArray();
        int[] idMap = new int[wordIdArray.length];
        Arrays.fill(idMap, -1);
        featureData.wordIdTrie = new BinTrie<Integer>();
        featureData.df = new int[selectedFeatures.size()];
        int p = -1;
        for (Integer feature : selectedFeatures.keySet())
        {
            ++p;
            featureData.wordIdTrie.put(wordIdArray[feature], p);
            featureData.df[p] = MathUtility.sum(featureData.featureCategoryJointCount[feature]);
            idMap[feature] = p;
        }
        logger.finish("Jumlah fitur yang dipilih:%d / %d = %.2f%%\n", selectedFeatures.size(),
                      featureData.featureCategoryJointCount.length,
                      MathUtility.percentage(selectedFeatures.size(), featureData.featureCategoryJointCount.length));
        logger.start("Kurangi data pelatihan...");
        int n = dataSet.size();
        dataSet.shrink(idMap);
        logger.finish("Mengurangi% d sampel,% d sampel yang tersisa\n", n - dataSet.size(), dataSet.size());

        return featureData;
    }
}

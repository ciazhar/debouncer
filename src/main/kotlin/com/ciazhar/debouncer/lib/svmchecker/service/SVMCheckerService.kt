package com.ciazhar.debouncer.lib.svmchecker.service

import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVM
import com.hankcs.hanlp.classification.classifiers.AbstractClassifier
import com.hankcs.hanlp.classification.corpus.Document
import com.hankcs.hanlp.classification.corpus.IDataSet
import com.hankcs.hanlp.classification.features.*
import com.hankcs.hanlp.classification.models.AbstractModel
import com.hankcs.hanlp.classification.utilities.MathUtility
import com.hankcs.hanlp.collection.trie.bintrie.BinTrie
import de.bwaldvogel.liblinear.*
import java.util.*

class SVMCheckerService : AbstractClassifier {
    internal var model: LinearSVM? = null

    constructor() {}

    constructor(model: LinearSVM?) {
        this.model = model
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun predict(text: String?): Map<String, Double> {
        if (model == null) {
            throw IllegalStateException("Model yang tidak terlatih! Tidak dapat menjalankan perkiraan!")
        }
        if (text == null) {
            throw IllegalArgumentException("Parameter teks == null")
        }

        //Segmentasi kata, buat dokumen
        val document = Document(model!!.wordIdTrie, model!!.tokenizer.segment(text))

        return predict(document)
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun categorize(document: Document): DoubleArray {
        val x = buildDocumentVector(document, model!!.featureWeighter)
        val probs = DoubleArray(model!!.svmModel.nrClass)
        Linear.predictProbability(model!!.svmModel, x, probs)
        return probs
    }

    override fun train(dataSet: IDataSet?) {
        var dataSet = dataSet
        if (dataSet!!.size() == 0) throw IllegalArgumentException("Kumpulan data pelatihan kosong dan tidak dapat melanjutkan pelatihan")
        // Fitur seleksi menggunakan
        var featureData: DfFeatureData? = selectFeatures(dataSet)
        // Logika perhitungan berat konstruksi
        val weighter = TfIdfFeatureWeighter(dataSet.size(), featureData!!.df)
        // Membangun masalah SVM
        val problem = createLiblinearProblem(dataSet, featureData, weighter)
        // Memori bebas
        val wordIdTrie = featureData.wordIdTrie
        featureData = null
        val tokenizer = dataSet.tokenizer
        val catalog = dataSet.catalog.toArray()
        dataSet = null
        System.gc()
        // Memecahkan masalah SVM
        val svmModel = solveLibLinearProblem(problem)
        // Assign variable
        model = LinearSVM()
        model!!.tokenizer = tokenizer
        model!!.wordIdTrie = wordIdTrie
        model!!.catalog = catalog
        model!!.svmModel = svmModel
        model!!.featureWeighter = weighter
    }

    override fun getModel(): AbstractModel? {
        return model
    }

    private fun solveLibLinearProblem(problem: Problem): Model {
        val lparam = Parameter(SolverType.L1R_LR, 500.0, 0.01)
        return Linear.train(problem, lparam)
    }

    private fun createLiblinearProblem(dataSet: IDataSet, baseFeatureData: BaseFeatureData, weighter: IFeatureWeighter): Problem {
        val problem = Problem()
        val n = dataSet.size()
        problem.l = n
        problem.n = baseFeatureData.featureCategoryJointCount.size
        problem.x = arrayOfNulls<Array<FeatureNode>>(n)
        problem.y = DoubleArray(n)  // Versi terbaru dari liblinear's y array adalah angka floating point
        val iterator = dataSet.iterator()
        for (i in 0 until n) {
            // Membangun vektor dokumen
            val document = iterator.next()
            problem.x[i] = buildDocumentVector(document, weighter)
            // Tetapkan nilai y sampel
            problem.y[i] = document.category.toDouble()
        }

        return problem
    }

    private fun buildDocumentVector(document: Document, weighter: IFeatureWeighter): Array<FeatureNode?> {
        val termCount = document.tfMap.size  // Jumlah kata
        val x = arrayOfNulls<FeatureNode>(termCount)
        val tfMapIterator = document.tfMap.entries.iterator()
        for (j in 0 until termCount) {
            val tfEntry = tfMapIterator.next()
            val feature = tfEntry.key
            val frequency = tfEntry.value[0]
            x[j] = FeatureNode(feature + 1, // liblinear Membutuhkan subskrip untuk bertambah dari 1
                    weighter.weight(feature, frequency))
        }
        // Menormalkan vektor
        var normalizer = 0.0
        for (j in 0 until termCount) {
            val weight = x[j]!!.getValue()
            normalizer += weight * weight
        }
        normalizer = Math.sqrt(normalizer)
        for (j in 0 until termCount) {
            val weight = x[j]!!.getValue()
            x[j]!!.setValue(weight / normalizer)
        }

        return x
    }

    protected fun selectFeatures(dataSet: IDataSet): DfFeatureData {

        //Membuat extractor
        val chiSquareFeatureExtractor = ChiSquareFeatureExtractor()

        //Mengubah dataset mentah menjadi fitur data (yang mengandung inverted DF) dengan menghitung statistiknya
        val featureData = DfFeatureData(dataSet)

        println("Gunakan deteksi chi-square untuk memilih fitur ...\n")
        //Meneruskan statistik ini ke algoritme pemilihan fitur untuk mendapatkan fitur dan nilainya.
        val selectedFeatures = chiSquareFeatureExtractor.chi_square(featureData)

        //Menghapus fitur yang tidak berguna dari data pelatihan dan rekonstruksi peta fitur
        val wordIdArray = dataSet.lexicon.wordIdArray
        val idMap = IntArray(wordIdArray.size)
        Arrays.fill(idMap, -1)
        featureData.wordIdTrie = BinTrie()
        featureData.df = IntArray(selectedFeatures.size)
        var p = -1
        for (feature in selectedFeatures.keys) {
            ++p
            featureData.wordIdTrie.put(wordIdArray[feature], p)
            featureData.df[p] = MathUtility.sum(*featureData.featureCategoryJointCount[feature])
            idMap[feature] = p
        }
        println("Jumlah fitur yang dipilih : "+ selectedFeatures.size +
                "/"+ featureData.featureCategoryJointCount.size +
                "= "+MathUtility.percentage(
                    selectedFeatures.size.toDouble(),
                    featureData.featureCategoryJointCount.size.toDouble())+ "\n")
        println("Kurangi data pelatihan...")
        val n = dataSet.size()
        dataSet.shrink(idMap)
        var datasetYangDikurangi = n - dataSet.size()

        println("Mengurangi"+ datasetYangDikurangi + "sampel, "+ dataSet.size() + "sampel yang tersisa\n")

        return featureData
    }
}
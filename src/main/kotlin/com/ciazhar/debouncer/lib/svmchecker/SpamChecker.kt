package com.ciazhar.debouncer.lib.svmchecker

import com.ciazhar.debouncer.lib.svmchecker.model.LinearSVM
import com.ciazhar.debouncer.lib.svmchecker.service.SVMCheckerService
import com.hankcs.hanlp.classification.classifiers.IClassifier

object SpamChecker {
    private val service by lazy { SVMCheckerService() }

    @JvmStatic
    fun predict(classifier: IClassifier, text: String){
        return service.predict(classifier,text)
    }

    @JvmStatic
    fun trainOrLoadModel(): LinearSVM?{
        return service.trainOrLoadModel()
    }
}
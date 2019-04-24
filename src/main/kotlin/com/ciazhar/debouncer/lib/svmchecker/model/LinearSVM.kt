package com.ciazhar.debouncer.lib.svmchecker.model

import com.hankcs.hanlp.classification.features.IFeatureWeighter
import com.hankcs.hanlp.classification.models.AbstractModel
import de.bwaldvogel.liblinear.Model

class LinearSVM : AbstractModel() {
    lateinit var featureWeighter : IFeatureWeighter
    lateinit var svmModel : Model
}
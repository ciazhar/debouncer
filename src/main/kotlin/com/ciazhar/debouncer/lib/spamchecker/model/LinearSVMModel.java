/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>me@hankcs.com</email>
 * <create-date>16/2/15 AM9:07</create-date>
 *
 * <copyright file="LinearSVMModel.java" company="码农场">
 * Copyright (c) 2008-2016, 码农场. All Right Reserved, http://www.hankcs.com/
 * This source is subject to Hankcs. Please contact Hankcs to get more information.
 * </copyright>
 */
package com.ciazhar.debouncer.lib.spamchecker.model;

import com.hankcs.hanlp.classification.features.IFeatureWeighter;
import com.hankcs.hanlp.classification.models.AbstractModel;
import de.bwaldvogel.liblinear.Model;

/**
 * 线性SVM模型
 *
 * @author hankcs
 */
public class LinearSVMModel extends AbstractModel
{
    /**
     * Jumlah sampel pelatihan
     */
    public int n = 0;
    /**
     * Jumlah kategori
     */
    public int c = 0;
    /**
     * Jumlah fitur
     */
    public int d = 0;
    /**
     * Alat penghitung berat fitur
     */
    public IFeatureWeighter featureWeighter;
    /**
     * Model klasifikasi SVM
     */
    public Model svmModel;
}
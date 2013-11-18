/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.stats;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A <code>MultivariateConstant</code> provides a multinomial
 * distribution with constant probabilities and labels.  A range
 * of constructors allow a constant multivariate to be constructed
 * from integer counts, floating point probability ratios, with
 * an optional set of labels.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.0
 */
public class MultivariateConstant extends MultivariateDistribution {

    final String[] mLabels;
    final double[] mEstimates;
    final Map<String,Long> mLabelToIndex;

    /**
     * Construct a uniform constant multinomial distribution with the
     * specified number of outcomes.  The outcomes will be labeled
     * as defined in {@link MultivariateDistribution}.
     *
     * @param numOutcomes Number of outcomes.
     * @throws IllegalArgumentException If the number of outcomes is
     * less than one.
     */
    public MultivariateConstant(int numOutcomes) {
        this(allOnes(numOutcomes));
    }

    /**
     * Construct a constant multinomial distribution with
     * probabilities proportional to the specified counts.  The
     * outcomes will be labeled as defined in {@link
     * MultivariateDistribution}.
     *
     * @param counts Counts for each outcome.
     * @throws IllegalArgumentException If any of the counts are less
     * than zero.
     */
    public MultivariateConstant(long[] counts) {
        this(toDouble(counts));
    }

    /**
     * Construct a constant multinomial distribution with
     * probabilities proportional to the specified ratios.  The
     * outcomes will be labeled as defined in {@link
     * MultivariateDistribution}.
     *
     * @param probabilityRatios Probability ratios of outcomes.
     * @throws IllegalArgumentException If any of the ratios are less
     * than zero.
     */
    public MultivariateConstant(double[] probabilityRatios) {
        this(probabilityRatios,null,null);
    }


    /**
     * Construct a uniform constant multinomial distribution with the
     * specified labels.  Indices are asisgned to labels in order, and
     * the labels must not be duplicated.
     *
     * @param labels Labels of outcomes in order of their indices.
     * @throws IllegalArgumentException If any of the labels are
     * duplicated.
     */
    public MultivariateConstant(String[] labels) {
        this(allOnes(labels.length),labels);
    }

    /**
     * Construct a constant multinomial distribution with the
     * specified labels and probabilities proportional to the
     * specified counts.  All counts must be non-negative and there
     * must be the same number of labels as counts with no ducplicate
     * labels.
     *
     * @param counts Counts for outcomes.
     * @param labels Labels of outcomes.
     * @throws IllegalArgumentException If any count is negative,
     * if there are not the same number of counts and labels, or
     * if there are duplicate labels.
     */
    public MultivariateConstant(long[] counts,
                                String[] labels) {
        this(toDouble(counts),labels);
    }

    /**
     * Construct a constant multinomial distribution with the
     * specified labels and probabilities proportional to the
     * specified counts.  All counts must be non-negative and there.
     *
     * @param probabilityRatios Counts for outcomes.
     * @param labels Labels of outcomes.
     * @throws IllegalArgumentException If any count is negative or if
     * there are not the same number of counts and labels.
     */
    public MultivariateConstant(double[] probabilityRatios,
                                String[] labels) {
        this(probabilityRatios, labels.clone(), new HashMap<String,Long>());
        validateEqualLengths(probabilityRatios,labels);
        for (int i = 0; i < labels.length; ++i) {
            if (mLabelToIndex.put(labels[i],Long.valueOf(i)) != null) {
                String msg = "Duplicate labels=" + labels[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }

    private MultivariateConstant(double[] probabilityRatios,
                                 String[] labels,
                                 Map<String,Long> labelToIndex) {
        if (probabilityRatios.length < 1) {
            String msg = "Require at least one count or probability ratio.";
            throw new IllegalArgumentException(msg);
        }
        mLabels = labels;
        mLabelToIndex = labelToIndex;
        validateRatios(probabilityRatios);
        double z = 0;
        for (int i = 0; i < probabilityRatios.length; ++i)
            z += probabilityRatios[i];
        mEstimates = new double[probabilityRatios.length];
        for (int i = 0; i < probabilityRatios.length; ++i)
            mEstimates[i] = probabilityRatios[i] / z;
    }

    @Override
    public int numDimensions() {
        return mEstimates.length;
    }

    @Override
    public long outcome(String outcomeLabel) {
        if (mLabels == null) return super.outcome(outcomeLabel);
        Long result = mLabelToIndex.get(outcomeLabel);
        if (result == null) {
            String msg="Unknown outcome label=" + outcomeLabel;
            throw new IllegalArgumentException(msg);
        }
        return result.longValue();
    }

    @Override
    public String label(long outcome) {
        if (mLabels == null) return super.label(outcome);
        checkOutcome(outcome);
        return mLabels[(int)outcome];
    }

    @Override
    public double probability(long outcome) {
        return outcomeOutOfRange(outcome)
            ? 0.0
            : mEstimates[(int)outcome];
    }

    private void validateEqualLengths(double[] probabilityRatios, String[] labels) {
        if (probabilityRatios.length != labels.length) {
            String msg = "Require same number of ratios and labels."
                + " Found #ratios=" + probabilityRatios.length
                + " # labels=" + labels.length;
            throw new IllegalArgumentException(msg);
        }
    }
    private void validateRatios(double[] probabilityRatios) {
        for (int i = 0; i < probabilityRatios.length; ++i) {
            if (probabilityRatios[i] < 0) {
                String msg = "All probability ratios must be >= 0."
                    + " Found probabilityRatios[" + i + "]="
                    + probabilityRatios[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }

    static double[] toDouble(long[] counts) {
        double[] result = new double[counts.length];
        for (int i = 0; i < counts.length; ++i) {
            if (counts[i] < 0l) {
                String msg = "All counts must be positive."
                    + " Found counts[" + i + "]=" + counts[i];
                throw new IllegalArgumentException(msg);
            }
            result[i] = counts[i];
        }
        return result;
    }

    static double[] allOnes(int length) {
        if (length < 0) {
            String msg="Number of outcomes must be positive."
                + "Found number of outcomes=" + length;
            throw new IllegalArgumentException(msg);
        }
        double[] result = new double[length];
        Arrays.fill(result,1.0);
        return result;
    }

    static String[] numberedLabels(int length) {
        String[] labels = new String[length];
        for (int i = 0; i < length; ++i)
            labels[i] = Integer.toString(i);
        return labels;
    }




}


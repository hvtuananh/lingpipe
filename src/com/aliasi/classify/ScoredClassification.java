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

package com.aliasi.classify;

import com.aliasi.util.ScoredObject;

import java.util.Arrays;
import java.util.List;

/**
 * A <code>ScoredClassification</code> is a ranked classification
 * where each category also has a score that determines the ranking.
 * The category with the highest score is the value returned as the
 * best category.  Thus if scores are costs or distances, then they
 * should be inverted before being used to rank results in a scored
 * classification.
 *
 * <P>Subclasses are available that interpret the scores as
 * conditional or joint probabilities.
 *
 * @author  Bob Carpenter
 * @version 4.1.0
 * @since   LingPipe2.0
 */
public class ScoredClassification extends RankedClassification {

    private final double[] mScores;

    /**
     * Construct a scored classification from parallel arrays of
     * categories and scores.  The scores must be in non-ascending
     * numerical order.
     *
     * <p>Note that the categories and scores are merely saved
     * in a scored classification, so that subsequent changes to
     * the arrays will affect the contructed classification.
     *
     * @param categories Array of categories.
     * @param scores Array of scores.
     * @throws IllegalArgumentException If the category and score
     * arrays are of different lengths, or if a score later in the
     * array is larger than one earlier in the array.
     */
    public ScoredClassification(String[] categories, double[] scores) {
        super(categories);
        if (categories.length != scores.length) {
            String msg = "Categories and scores must be of same length."
                + " Categories length=" + categories.length
                + " Scores length=" + scores.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 1; i < scores.length; ++i)
            if (scores[i-1] < scores[i]) {
                String msg = "Array of scores must be in order."
                    + " scores[" + (i-1) + "]=" + scores[i-1]
                    + " < scores[" + i + "]=" + scores[i];
                throw new IllegalArgumentException(msg);
            }
        mScores = scores;
    }

    /**
     * Factory method which returns a scored classification from the
     * array of scored categories.  The category score array does not
     * need to be sorted.  The categories are taken to be the objects
     * in the scored objects and the scores are the scores.
     *
     * @param categoryScores Array of scored categories.
     * @return Scored classification corresponding to the input..
     * @deprecated Use {@link #create(List)} instead.
     */
    @Deprecated
    public static ScoredClassification create(ScoredObject<String>[] categoryScores) {
        Arrays.sort(categoryScores,ScoredObject.reverseComparator());
        String[] categories = new String[categoryScores.length];
        double[] scores = new double[categoryScores.length];
        for (int i = 0; i < categoryScores.length; ++i) {
            categories[i] = categoryScores[i].getObject();
            scores[i] = categoryScores[i].score();
        }
        return new ScoredClassification(categories,scores);
    }


    /**
     * Factory method which returns a scored classification from the
     * list of scored categories.  The category score list does not
     * need to be sorted.  The categories are taken to be the objects
     * in the scored objects and the scores are the scores.
     *
     * @param categoryScores List of scored categories.
     * @return Scored classification corresponding to the input..
     */
    public static ScoredClassification create(List<ScoredObject<String>> categoryScores) {
        @SuppressWarnings("unchecked")
        ScoredObject<String>[] sos 
            = (ScoredObject<String>[]) new ScoredObject[categoryScores.size()];
        categoryScores.toArray(sos);
        return create(sos);
    }

    /**
     * Returns the score of the category with the specified rank in
     * the classification.  Categories are numbered from zero and thus
     * will be greater than or equal to zero and less than the size of
     * this classification.  Scores are in decreasing order by rank,
     * so that for ranks <code>i</code> and <code>i+1</code>:
     *
     * <blockquote><code>
     * score(i) >= score(i+1)</code>.
     * </code></blockquote>
     *
     * @param rank Rank of result category.
     * @return The score of the category with the specified rank.
     * @throws IllegalArgumentException If the rank is out of range.
     */
    public double score(int rank) {
        checkRange(rank);
        return mScores[rank];
    }

    /**
     * Returns a string-based representation of this scored
     * classification.
     *
     * @return A string-based representation of this classification.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank  Category  Score\n");
        for (int i = 0; i < size(); ++i)
            sb.append(i + "=" + category(i) + " " + score(i) + '\n');
        return sb.toString();
    }


}


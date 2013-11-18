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

/**
 * A <code>RankedClassification</code> provides a classification with
 * an ordered n-best list of category results.  Subclasses of this
 * class add scores with various interpretations to the ranked n-best
 * results.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public class RankedClassification extends Classification {

    private final String[] mCategories;

    /**
     * Construct a ranked classification with the specified results
     * ordered from best to worst.  The best category will be the
     * first category in the array, or <code>null</code> if the
     * specfied array of categories is empty.
     *
     * <p>Note that the categories are simply stored as part of this
     * ranked classification, not copied, so any subsequent change to
     * the category array will affect this ranked classification.
     *
     * @param categories Result categories, in order from best to worst.
     */
    public RankedClassification(String[] categories) {
        super(categories.length == 0
              ? null
              : categories[0]);
        mCategories = categories;
    }

    /**
     * Returns the number of categories in this classification.
     *
     * @return The number of categories in this classification.
     */
    public int size() {
        return mCategories.length;
    }

    /**
     * Returns the category with the specified rank in the
     * classification.  Ranks are numbered from zero and thus will be
     * greater than or equal to zero and less than the size of this
     * classification.
     *
     * @param rank Rank of result the category of which is to be returned.
     * @return The category with the specified rank.
     * @throws IllegalArgumentException If the rank is out of range.
     */
    public String category(int rank) {
        checkRange(rank);
        return mCategories[rank];
    }

    /**
     * Returns a string-based representation of this ranked
     * classification.
     *
     * @return A string-based representation of this classification.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rank  Category\n");
        for (int i = 0; i < size(); ++i)
            sb.append("rank " + i + "=" + category(i) + '\n');
        return sb.toString();
    }


    void checkRange(int rank) {
        if (rank < 0 || rank >= size()) {
            String msg = "Rank out of bounds."
                + " Rank=" + rank
                + " size()=" + size();
            throw new IllegalArgumentException(msg);
        }
    }

}

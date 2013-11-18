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
package com.aliasi.tag;

import com.aliasi.util.Scored;

import java.util.List;

/**
 * A {@code ScoredTagging<E>} represents a tagging of
 * type {@code E} along with a real-valued score.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public class ScoredTagging<E>
    extends Tagging<E>
    implements Scored {

    private final double mScore;

    /**
     * Construct a scored tagging with the specified tokens, tags, and
     * score.  The token and tag lists are copied so that subsequent
     * changes to the list will not affect the constructed tagging.
     *
     * @param tokens Tokens for the tagging.
     * @param tags Tags for the tagging.
     * @param score Score for the tagging.
     * @throws IllegalArgumentException If the lists are not of the
     * same size.
     */
    public ScoredTagging(List<E> tokens,
                         List<String> tags,
                         double score) {
        this(tokens,tags,score,true);
    }

    // no checks, copies
    ScoredTagging(List<E> tokens,
                  List<String> tags,
                  double score,
                  boolean ignore) {
        super(tokens,tags,true); // no checks, copies
        mScore = score;
    }

    /**
     * Return the score for this tagging.
     *
     * @return Score for this tagging.
     */
    public double score() {
        return mScore;
    }

    /**
     * Returns a string-based representation of this scored tagging.
     *
     * @return String-based representation of this scored tagging.
     */
    @Override
    public String toString() {
        return score() + " " + super.toString();
    }
}
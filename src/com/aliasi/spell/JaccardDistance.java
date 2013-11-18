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

package com.aliasi.spell;

import com.aliasi.tokenizer.TokenizerFactory;

import java.util.Set;

/**
 * The <code>JaccardDistance</code> class implements a notion of
 * distance based on token overlap.  The tokens are generated
 * from the character sequences being compared by a tokenizer
 * factory that is supplied at construction time.  A distance of
 * zero (<code>0</code>) is a perfect match, a distance of
 * one (<code>1</code>0 a perfect mismatch.
 *
 * <p>Suppose <code>termSet(cs)</code> is the set of tokens extracted from
 * the character sequence <code>cs</code>.  With these terms,
 * the proximity underlying Jaccard distance is defined
 * as the percentage of tokens that appear in both
 * character sequences:
 *
 * <blockquote><pre>
 * proximity(cs1,cs2)
 *   = size(termSet(cs1) INTERSECT termSet(cs2))
 *     / size(termSet(cs1) UNION termSet(cs2))</pre></blockquote>
 *</pre></blockquote>
 *
 * Proximities run between 0 and 1.  A proximity of 0 means the
 * character sequences share no terms in common and a proximity of 1
 * means the character sequences share all of their terms.
 *
 * <p>Distance is then defined in terms of proximity by subtraction.
 *
 * <blockquote><pre>
 * distance(cs1,cs2) = 1 - proximity(cs1,cs2)
 * </pre></blockquote>
 *
 * Distances also run between 0 and 1.  A distance of 0 means the
 * character sequences share all of their terms, whereas a distance of
 * 1 means they have no terms in common.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.4
 */
public class JaccardDistance extends TokenizedDistance {

    /**
     * Construct an instance of Jaccard string distance using
     * the specified tokenizer factory.
     *
     * @param factory Tokenizer factory for distance.
     */
    public JaccardDistance(TokenizerFactory factory) {
        super(factory);
    }

    /**
     * Returns the Jaccard distance between the specified character
     * sequence.  See the class definition above for a definition.
     *
     * @param cSeq1 First character sequence.
     * @param cSeq2 Second character sequence.
     * @return Jaccard distance between the sequences.
     */
    public double distance(CharSequence cSeq1, CharSequence cSeq2) {
        return 1.0 - proximity(cSeq1,cSeq2);
    }

    /**
     * Returns the proximity between the specified character
     * sequences.
     *
     * @param cSeq1 First character sequence.
     * @param cSeq2 Second character sequence.
     * @return Jaccard proximity between the sequences.
     */
    public double proximity(CharSequence cSeq1, CharSequence cSeq2) {
        Set<String> s1 = tokenSet(cSeq1);
        Set<String> s2 = tokenSet(cSeq2);
        if (s1.size() < s2.size()) {
            Set<String> temp = s2;
            s2 = s1;
            s1 = temp;
        }
        int numMatch = 0;
        for (String x : s1)
            if (s2.contains(x))
                ++numMatch;
        int numTotal = s1.size() + s2.size() - numMatch;
        return ((double) numMatch) / ((double) numTotal);

    }



}

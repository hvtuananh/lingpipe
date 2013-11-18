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

package com.aliasi.coref.matchers;

import com.aliasi.coref.BooleanMatcherAdapter;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionChain;

/**
 * Implements a matching function that returns the score specified in
 * the constructor if there is a token-wise match between the normal
 * tokens of the mention and one of the mentions in the mention chain
 * that is within a specified edit distance.  Subclasses of this
 * class may redefine the basic edit distances provided by
 * {@link #deleteCost(String)}, {@link #insertCost(String)},
 * and {@link #substituteCost(String,String)}, which are defined in this
 * class to be <code>1</code> in the case of insertion or deletion,
 * and <code>0</code> for an exact substitution and <code>2</code> for
 * a mismatch substitution.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public final class SequenceSubstringMatch extends BooleanMatcherAdapter {

    /**
     * Construct a sequence substring matcher that returns the
     * specified score in the case of a match.
     *
     * @param score Score to return in the case of a match.
     */
    public SequenceSubstringMatch(int score) {
        super(score);
    }

    /**
     * Returns <code>true</code> if the normal tokens in the mention
     * are within a threshold edit distance of the normal tokens in
     * one of the mentions in the chain.
     *
     * @param mention Mention to test.
     * @param chain Mention chain to test.
     * @return <code>true</code> if there is a sequence substring
     * match between the mention and chain.
     */
    @Override
    public boolean matchBoolean(Mention mention, MentionChain chain) {
        if (mention.isPronominal()) return false;
        String[] mentionTokens = mention.normalTokens();
        for (Mention chainMention : chain.mentions()) {
            String[] chainMentionTokens = chainMention.normalTokens();
            if (withinEditDistance(mentionTokens,chainMentionTokens))
                return true;
        }
        return false;
    }

    /**
     * Returns the edit distance threshold required to satisfy the
     * sequence substring match.  Based on the number of tokens in
     * each sequence.
     *
     * @param tokens1 First array of tokens.
     * @param tokens2 Second array of tokens.
     * @return Threshold edit distance required to match the two
     * arrays of tokens.
     */
    private static int threshold(String[] tokens1, String[] tokens2) {
        // subtract #ignorable tokens for more agressive matching
        int max = Math.max(tokens1.length, tokens2.length);
        switch (max) {
        case 1: return 0;
        case 2: return 1;
        case 3: return 1;
        case 4: return 1;
        default: return (max + 1)/3;
        }
    }

    /**
     * Returns <code>true</code> if the specified arrays of tokens
     * have an edit distance within the distance specified internally.
     *
     * @param tokens1 First array of tokens to test.
     * @param tokens2 Second array of tokens to test.
     * @return <code>true</code> if the edit distance between the
     * arrays of tokens is within the threshold.
     */
    public boolean withinEditDistance(String[] tokens1, String[] tokens2) {
        return withinEditDistance(tokens1,tokens2,
                                  threshold(tokens1,tokens2));
    }

    /**
     * Returns <code>true</code> if the specified arrays of tokens are
     * within the specified maximum distance, allowing for deletion,
     * insertion and substitution costs as specified by {@link
     * #deleteCost(String)}, {@link #insertCost(String)}, and {@link
     * #substituteCost(String,String)}.  To support pairs of tokens
     * from different sets, as well as asymmetric primitive edit
     * distances, insertions and deletions are separated, and
     * substitution may be order sensitive.  Deletions are from the
     * first array of tokens, and insertions into the second array.
     * Substitution costs will be computed with the first argument
     * drawn from the first array of tokens and the second argument
     * drawn from the second array.
     *
     * @param tokens1 First array of tokens to match.
     * @param tokens2 Second array of tokens to match.
     * @param maximumDistance Maximum edit distance allowed between
     * token arrays.
     * @return <code>true</code> if the edit distance between the
     * arrays is less than or equal to the specified maximum distance.
     */
    public boolean withinEditDistance(String[] tokens1,
                                      String[] tokens2,
                                      int maximumDistance) {
        int distances[][]
            = new int[tokens2.length+1][tokens1.length+1];
        distances[0][0] = 0;
        for (int i = 1; i <= tokens1.length; ++i)
            distances[0][i] = ( distances[0][i-1]
                                + deleteCost(tokens1[i-1]) );
        for (int j = 1; j <= tokens2.length; ++j) {
            distances[j][0] = ( distances[j-1][0]
                                + deleteCost(tokens2[j-1]) );
            boolean keep = distances[j][0] <= maximumDistance;
            for (int i = 1; i <= tokens1.length; ++i) {
                distances[j][i] =
                    Math.min(distances[j-1][i-1]
                             + substituteCost(tokens1[i-1],tokens2[j-1]),
                             Math.min(distances[j-1][i]
                                      + deleteCost(tokens2[j-1]),
                                      distances[j][i-1]
                                      + deleteCost(tokens1[i-1])));
                if (!keep && distances[j][i] <= maximumDistance)
                    keep = true;
            }
            if (!keep) return false;
        }
        return distances[tokens2.length][tokens1.length]
            <= maximumDistance;
    }

    /**
     * Returns the cost to delete the specified token.
     *
     * @param token Token to measure for deletion cost.
     * @return Cost to delete the specified token.
     */
    protected int deleteCost(String token) {
        return 1;
    }
    /**
     * Returns the cost to insert the specified token.
     *
     * @param token Token to measure for insertion cost.
     * @return Cost to insert the specified token.
     */
    protected int insertCost(String token) {
        return 1;
    }

    /**
     * Returns the cost to substitute the new token for the original token.
     *
     * @param originalToken Original token.
     * @param newToken New token.
     * @return Cost to substitute the new token for the original token.
     */
    protected int substituteCost(String originalToken, String newToken) {
        return originalToken.equalsIgnoreCase(newToken)
            ? 0
            : 2;
    }

}

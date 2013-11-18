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

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Distance;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Proximity;
import com.aliasi.util.Strings;


import java.util.HashSet;
import java.util.Set;

/**
 * The <code>TokenizedDistance</code> class provides an underlying
 * implementation of string distance based on comparing sets of
 * tokens.  It holds a tokenizer factory and provides convenience
 * methods for extracting tokens from the input.
 *
 * <p>The method {@link #tokenSet(CharSequence)} provides the set of
 * tokens derived by tokenizing the specified character sequence.  The
 * method {@link #termFrequencyVector(CharSequence)} provides a
 * mapping from tokens extracted by a tokenizer to integer counts.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.4.0
 */
public abstract class TokenizedDistance 
    implements Distance<CharSequence>,
               Proximity<CharSequence> {

    /**
     * The underlying tokenizer factory, which is fixed at
     * construction time.
     */
    final TokenizerFactory mTokenizerFactory;

    /**
     * Construct a tokenized distance from the specified tokenizer
     * factory.
     *
     * @param tokenizerFactory Tokenizer for this distance.
     */
    public TokenizedDistance(TokenizerFactory tokenizerFactory) {
        mTokenizerFactory = tokenizerFactory;
    }

    /**
     * Return the tokenizer factory for this tokenized distance.
     *
     * @return This distance's tokenizer factory.
     */
    public TokenizerFactory tokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Return the set of tokens produced by the specified character
     * sequence using the tokenizer for this distance measure.
     *
     * @param cSeq Character sequence to tokenize.
     * @return The token set for the character sequence.
     */
    public Set<String> tokenSet(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return tokenSet(cs,0,cs.length);
    }

    /**
     * Return the set of tokens produced by the specified character
     * slice using the tokenizer for this distance measure.
     *
     * @param cs Underlying array of characters.
     * @param start Index of first character in slice.
     * @param length Length of slice.
     * @return The token set for the character sequence.
     * @throws IndexOutOfBoundsException If the start index is
     * not within the underlying array, or if the start index
     * plus the length minus one is not within the underlying
     * array.
     */
    public Set<String> tokenSet(char[] cs, int start, int length) {
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,length);
        Set<String> tokenSet = new HashSet<String>();
        String token;
        while ((token = tokenizer.nextToken()) != null)
            tokenSet.add(token);
        return tokenSet;
    }    

    /**
     * Return the mapping from terms to their counts derived from
     * the specified character sequence using the tokenizer factory
     * in th is class.
     *
     * @param cSeq Character sequence to tokenize.
     * @return Counts of tokens in character sequence.
     */
    public ObjectToCounterMap<String> termFrequencyVector(CharSequence cSeq) {
        ObjectToCounterMap<String> termFrequency = new ObjectToCounterMap<String>();
        char[] cs = Strings.toCharArray(cSeq);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        String token;
        while ((token = tokenizer.nextToken()) != null)
            termFrequency.increment(token);
        return termFrequency;
    }

}

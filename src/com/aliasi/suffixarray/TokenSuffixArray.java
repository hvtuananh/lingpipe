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

package com.aliasi.suffixarray;

import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A {@code TokenSuffixArray} implements a suffix array of tokens.
 *
 * See {@link CharSuffixArray} for a description of suffix arrays
 * and their applications.
 *
 * <h3>Constructing a Token Suffix Array</h3>
 *
 * A suffix array is constructed from a list of tokens.  These may be
 * provided directly, or as a character array and tokenizer factory.
 * 
 * <p>If the maximum length is less than the length of the array,
 * strings are truncated to be at most this length before comparison.
 * The result isn't a standard, fully sorted suffix array, but can be
 * faster to create and will suffice for many applications. The
 * indexes will be sorted relative to the truncated strings, so they
 * will be in order up to the specified length.
 *
 * <h3>Document Boundary Token</h3>
 *
 * The document boundary token is used to separate documents. 
 * When the document  boundary token is found when comparing 
 * tokens, it's considered smaller than any other token (no matter
 * how it would sort as a string) and also as a string terminator.
 *
 * <p>Thus if the tokenization corresponds to multiple documents,
 * the boundary token should be used to separate them.
 *
 * <h3>Tokenization Normalization for Comparison</h3>
 *
 * In order to do comparisons that are case insensitive, or ignore
 * punctuation, the tokenizer should perform the normalization.
 * 
 * <h3>Using Suffix Arrays</h3>
 *
 * Token suffix arrays are used in exactly the same way as character
 * suffix arrays; see {@link CharSuffixArray} for details and an
 * example.
 * 
 * <h3>Thread Safety</h3>
 *
 * Once constructed, a tokenized suffix array is thread safe.
 * 
 * @author Bob Carpenter
 * @version 4.1.0
 * @since 4.0.2
 */
public class TokenSuffixArray {

    private final Tokenization mTokenization;
    private final int[] mSuffixArray;
    private final String mDocumentBoundaryToken;
    private final int mMaxSuffixLength;

    /**
     * The default boundary token for documents.
     */
    public static final String DEFAULT_DOCUMENT_BOUNDARY_TOKEN = "\u0000";

    /**
     * Construct at token suffix array with no limit on suffix length
     * and the default document-boundary token.
     *
     * @param tokenization Tokenization on which to base the suffix
     * array.
     */
    public TokenSuffixArray(Tokenization tokenization) {
        this(tokenization,Integer.MAX_VALUE);
    }

    /**
     * Construct a suffix array from the specified tokenization, comparing
     * suffixes using up the specified maximum suffix length using the
     * default document-boundary token.
     *
     * @param tokenization Tokenization on which to base suffix array.
     * @param maxSuffixLength Maximum length of token sequences to compare.
     */
    public TokenSuffixArray(Tokenization tokenization, int maxSuffixLength) {
        this(tokenization,maxSuffixLength,DEFAULT_DOCUMENT_BOUNDARY_TOKEN);
    }

    /**
     * Construct a suffix array from the specified tokenization, comparing
     * suffixes using up the specified maximum suffix length using the
     * default document-boundary token.
     *
     * @param tokenization Tokenization on which to base suffix array.
     * @param maxSuffixLength Maximum length of token sequences to compare.
     * @param documentBoundaryToken Token used to separate documents.
     */
    public TokenSuffixArray(Tokenization tokenization, 
                            int maxSuffixLength,
                            String documentBoundaryToken) {
        mTokenization = tokenization;
        mDocumentBoundaryToken = documentBoundaryToken;
        mMaxSuffixLength = maxSuffixLength;
        Integer[] is = new Integer[tokenization.numTokens()];
        for (int i = 0; i < is.length; ++i)
            is[i] = i;
        Arrays.sort(is,new TokenIndexComparator());
        int[] suffixArray = new int[is.length];
        for (int i = 0; i < is.length; ++i)
            suffixArray[i] = is[i];
        mSuffixArray = suffixArray;
    }


    /**
     * Returns the token used to separate documents in this suffix
     * array.
     *
     * @return Separator token.
     */
    public String documentBoundaryToken() {
        return mDocumentBoundaryToken;
    }

    /**
     * Returns the maximum suffix length for this token suffix array.
     *
     * @return Maximum length of suffixes.
     */
    public int maxSuffixLength() {
        return mMaxSuffixLength;
    }

    /**
     * Returns the tokenization underlying this suffix array.
     * The tokenization may be used to retrieve the processed tokens,
     * the underlying text, as well as the positions of the tokens
     * in the text.
     *
     * @return The tokenization for this suffix array.
     */
    public Tokenization tokenization() {
        return mTokenization;
    }

    /**
     * Returns the value of the suffix array at the specified index.
     * This value is an index into the underlying list of tokens.
     *
     * @param idx Suffix array index.
     * @return Index of the first token of the suffix at the
     * specified index.
     */
    public int suffixArray(int idx) {
        return mSuffixArray[idx];
    }

    /**
     * Returns the number of tokens in the suffix array.
     *
     * @return Number of tokens in the suffix array.
     */
    public int suffixArrayLength() {
        return mSuffixArray.length;
    }

    /**
     * Returns the substring of the original string that's spanned
     * by the tokens starting at the specified suffix array index
     * and running the specified maximum number of tokens (or until
     * the token sequence ends).
     *
     * @param idx Index in suffix array of first token.
     * @param maxTokens Maximum number of tokens to include
     * in string.  
     * @return Substring starting at the specified index and
     * running the maximum number of tokens or until the end of
     * the tokenization.
     */
    public String substring(int idx, int maxTokens) {
        int start = suffixArray(idx);
        // must be int because numTokens() is int and taking min
        int end = (int) Math.min((long)start + (long)maxTokens, 
                                 mTokenization.numTokens());
        int text_start = mTokenization.tokenStart(start);
        int text_end = mTokenization.tokenEnd(end-1);
        return mTokenization.text().substring(text_start, text_end);
    }

    /**
     * Returns a list of maximal spans of suffix array indexes
     * which refer to suffixes that share a prefix of at least
     * the specified minimum match length.
     *
     * @param minMatchLength Minimum number of tokens required to
     * match.
     * @return The list of pairs of start (inclusive) and end
     * (exclsuive) positions in the suffix array that match up
     * to the specified minimum number of tokens.
     */
    public List<int[]> prefixMatches(int minMatchLength) {
        List<int[]> matches = new ArrayList<int[]>();
        for (int i = 0; i < mSuffixArray.length; ) {
            int j = suffixesMatchTo(i,minMatchLength,mTokenization.tokenList());
            if (i + 1 != j) {
                matches.add(new int[] { i, j });
                i = j;
            } else {
                ++i;
            }
        }
        return matches;
    }

    private int suffixesMatchTo(int i, int minMatchLength, List<String> tokens) {
        int index1 = mSuffixArray[i];
        int j = i+1;
        for (; j < mSuffixArray.length; ++j) {
            int index2 = mSuffixArray[j];
            if (!matchTokens(index1,index2,minMatchLength,tokens))
                break;
        }
        return j;
    }

    private boolean matchTokens(int index1, int index2, int minMatches, List<String> tokens) {
        if (index1 + minMatches > mSuffixArray.length)
            return false; // not enough toks
        if (index2 + minMatches > mSuffixArray.length)
            return false; // not enough toks
        for (int k = 0; k < minMatches; ++k) {
            String tok1 = tokens.get(index1 + k);
            if (tok1.equals(mDocumentBoundaryToken))
                return false;
            String tok2 = tokens.get(index2 + k);
            if (tok2.equals(mDocumentBoundaryToken))
                return false;
            if (!tokens.get(index1 + k).equals(tokens.get(index2 + k)))
                return false;
        }
        return true;
    }

    class TokenIndexComparator implements Comparator<Integer> {
        public int compare(Integer i, Integer j) {
            List<String> tokens = mTokenization.tokenList();
            for (int m = i, n = j, k = 0; k < mMaxSuffixLength; ++m, ++n, ++k) {
                if (m == tokens.size() || tokens.get(m).equals(mDocumentBoundaryToken)) {
                    if (n == tokens.size() || tokens.get(n).equals(mDocumentBoundaryToken))
                        return 0;
                    return -1;
                }
                if (n == tokens.size() || tokens.get(n).equals(mDocumentBoundaryToken))
                    return 1;
                int c = tokens.get(m).compareTo(tokens.get(n));
                if (c != 0)
                    return c;
            }
            return 0;
        }
    }


}
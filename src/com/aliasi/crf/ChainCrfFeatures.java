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

package com.aliasi.crf;

import java.util.List;
import java.util.Map;

/**
 * The {@code ChainCrfFeatures} interface specifies methods for
 * extracting node and edge features for a conditional random field.
 *
 * <h3>Use by CRFs</h3>
 *
 * <p>Typically, these features will be created by an implementation
 * of {@link ChainCrfFeatureExtractor} in the context of CRF training
 * or tagging.  In this case, the previous tag is guaranteed to fall
 * in the set of tags provided at construction time.
 *
 * <h3>Caching High Cost Features</h3>
 *
 * <p>During construction, the features implementation may cache
 * values, such as part-of-speech tags for CRF chunker features.
 *
 * <h3>Thread Safety</h3>
 *
 * After safely publishing the constructed features, the feature
 * extraction methods should be thread safe.  The read methods
 * implemented by this class are all thread safe.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public abstract class ChainCrfFeatures<E> {

    //
    private final List<E> mTokens;
    private final List<String> mTags;

    /**
     * Construct a chain CRF feature set for the specified lists of
     * input tokens and possible output tags.
     *
     * @param tokens Input tokens.
     * @param tags Possible output tags.
     */
    public ChainCrfFeatures(List<E> tokens, List<String> tags) {
        mTokens = tokens;
        mTags = tags;
    }

    /**
     * Returns the number of tokens for this feature set.
     *
     * @return Number of tokens for this feature set.
     */
    public int numTokens() {
        return mTokens.size();
    }

    /**
     * Return the token at the specified input position.
     *
     * @param n Input position.
     * @return Token at specified index position.
     * @throws IndexOutOfBoundsException If the specified input
     * position is less than 0 or greater than or equal to the number
     * of tokens.
     */
    public E token(int n) {
        return mTokens.get(n);
    }

    /**
     * Returns the number of possible output tags for this feature
     * set.
     *
     * @return Number of possible output tags.
     */
    public int numTags() {
        return mTags.size();
    }

    /**
     * Return the output tag with the specified index.
     *
     * @return Output tag for index.
     * @throws IndexOutOfBoundsException If the specified index is less than 0
     * or greater than or equal to the number of tokens.
     */
    public String tag(int k) {
        return mTags.get(k);
    }

    /**
     * Return the node features for the specified input position.
     *
     * @param n Position in input token sequence.
     * @return Features for the node at the specified position.
     * @throws IndexOutOfBoundsException If the specifieid token position
     * is out of bounds.
     */
    public abstract Map<String,? extends Number> nodeFeatures(int n);

    /**
     * Return the edge features for the specified input position
     * and index of the previous tag.
     *
     * @param n Position in input token sequence.
     * @param previousTagIndex Index of previous tag in list of tags.
     * @throws IndexOutOfBoundsException If the specifieid token position or
     * tag index are out of bounds.
     */
    public abstract Map<String,? extends Number> edgeFeatures(int n, int previousTagIndex);

}
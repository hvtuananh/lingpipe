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

package com.aliasi.features;

import com.aliasi.util.FeatureExtractor;

import java.util.Map;

/**
 * A {@code FeatureExtractorFilter} contains a reference to another
 * feature extractor.  The typical use is as a superclass of a feature
 * extractor that modifies a base feature extractor in some way.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 * @param <E> The type of objects whose features are extracted.
 */
public class FeatureExtractorFilter<E> implements FeatureExtractor<E> {

    final FeatureExtractor<? super E> mExtractor;

    /**
     * Construct a feature extractor filter from the specified
     * feature extractor.
     *
     * @param extractor Contained feature extractor.
     */
    public FeatureExtractorFilter(FeatureExtractor<? super E> extractor) {
        mExtractor = extractor;
    }

    /**
     * Return the features extracted by the contained feature
     * extractor.
     *
     * @param in Input to be converted to a feature vector.
     * @return The map representing the feature vector for the
     * input object.
     */
    public Map<String, ? extends Number> features(E in) {
        return mExtractor.features(in);
    }

    /**
     * Returns the contained feature extractor.
     *
     * @return The contained feature extractor.
     */
    public FeatureExtractor<? super E> baseExtractor() {
        return mExtractor;
    }

}

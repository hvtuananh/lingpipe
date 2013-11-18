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
import java.util.HashMap;

/**
 * A {@code ModifiedFeatureExtractor} allows feature values to be
 * modified in a feature-specific fashion.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 * @param <E> The type of objects whose features are extracted.
 */
public abstract class ModifiedFeatureExtractor<E>
    extends FeatureExtractorFilter<E>
    implements FeatureExtractor<E> {

    /**
     * Construct a modified feature extractor with the specified
     * base extractor.
     *
     * @param extractor Base feature extractor.
     */
    public ModifiedFeatureExtractor(FeatureExtractor<? super E> extractor) {
        super(extractor);
    }

    /**
     * Return the modified form of the feature vector produced by
     * the base feature extractor.  Each feature/value pair will
     * be passed through the method {@link #filter(String,Number)} to
     * produce a new value, which if non-null, is added to the
     * result.
     *
     * @return The modified feature vector.
     */
    @Override
    public Map<String,? extends Number> features(E in) {
        Map<String,? extends Number> featureMap = baseExtractor().features(in);
        Map<String,Number> result = new HashMap<String,Number>();
        for (Map.Entry<String,? extends Number> entry : featureMap.entrySet()) {
            String feature = entry.getKey();
            Number originalValue = entry.getValue();
            Number value = filter(feature,originalValue);
            if (value == null) continue;
            result.put(feature,value);
        }
        return result;
    }

    /**
     * Return the value for the specified original feature and value,
     * or {@code null} to remove the feature altogether.
     *
     * <p>This implementation passes through the value.
     * 
     * @param feature Feature corresponding to the value.
     * @param value Value to filter.
     * @return The modified value or {@code null}.
     */
    public Number filter(String feature, Number value) {
        return value;
    }
            
}

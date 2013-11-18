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

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BinaryMap;
import com.aliasi.util.FeatureExtractor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;

/**
 * A {@code StringLengthFeatureExtractor} implements a feature
 * extractor that provides string length features based on
 * a specified set of string lengths.  
 *
 * <p>Each specified length will become a feature with value 1.0 if
 * the string is greater than or equal to the specified length.  For
 * example, if the specified lengths were <code>{1,2,3}</code>, the
 * string {@code "ab"} would produce feature map {@code LEN>=1:1.0,
 * LEN>=2:1.0}.
 *
 * <p>A length of 0 will always produce the feature mapping {@code
 * LEN>=0:1.0}, which is redundant if there is an intercept in
 * (constant feature) in the relevant problem.  If not, it is tantamount
 * to adding one.  Note that intercept features added this way are
 * subject to priors and not treated separately like an intercept
 * always added as the first feature.
 * 
 * <h3>Thread Safety</h3>
 *
 * A string-length feature extractor is thread safe.
 * 
 * <h3>Serialization</h3>
 *
 * A string-length feature extractor may be serialized.  The deserialized
 * extractor will be an instance of this class.
 * 
 * @author Bob Carpenter
 * @version 3.9.2
 * @since Lingpipe3.9.2
 */
public class StringLengthFeatureExtractor
    implements FeatureExtractor<CharSequence>, 
               Serializable {

    static final long serialVersionUID = 4390057742097519384L;

    private final int[] mLengths;
    private final String[] mFeatureNames;

    /**
     * Construct a string-length feature extractor based on
     * the specified lengths.
     * 
     * @param lengths Array (or varargs) of lengths.
     * @throws IllegalArgumentException If there is not at least one
     * length or if any of the lengths are less than zero.
     */
    public StringLengthFeatureExtractor(int... lengths) {
        if (lengths.length < 1) {
            String msg = "Require non-empty array of lengths.";
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < lengths.length; ++i) {
            if (lengths[i] < 0) {
                String msg = "Lengths must be non-negative."
                    + " Found lengths[" + i + "]=" + lengths[i];
                throw new IllegalArgumentException(msg);
            }
        }
        TreeSet<Integer> lengthSet = new TreeSet<Integer>();
        for (int length : lengths)
            lengthSet.add(length);
        mLengths = new int[lengthSet.size()];
        int pos = 0;
        for (Integer length : lengthSet) {
            mLengths[pos] = length;
            ++pos;
        }
        mFeatureNames = new String[mLengths.length];
        for (int i = 0; i < mLengths.length; ++i)
            mFeatureNames[i] = "LEN>=" + mLengths[i];
    }
 
    public Map<String,? extends Number> features(CharSequence in) {
        int len = in.length();
        int end = 0;
        while (end < mLengths.length && len >= mLengths[end])
            ++end;
        if (end == 0)
            return Collections.<String,Number>emptyMap();
        BinaryMap<String> features = new BinaryMap<String>();
        for (int i = 0; i < end; ++i)
            features.add(mFeatureNames[i]);
        return features;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 5726292832004631457L;
        StringLengthFeatureExtractor mExtractor;
        public Serializer() {
            this(null);
        }
        public Serializer(StringLengthFeatureExtractor extractor) {
            mExtractor = extractor;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            writeInts(mExtractor.mLengths,out);
        }
        @Override
        public Object read(ObjectInput in) throws IOException {
            int[] lengths = readInts(in);
            return new StringLengthFeatureExtractor(lengths);
        }
    }
   
}


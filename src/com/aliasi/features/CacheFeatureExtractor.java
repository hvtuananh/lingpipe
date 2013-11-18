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
import com.aliasi.util.FeatureExtractor;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Map;

/**
 * A {@code CacheFeatureExtractor} uses a cache to store a mapping from
 * objects to their feature vector maps.  
 *
 * <p>Elements will be stored in the cache and looked up in the cache
 * using the standard map methods {@code put()} and {@code get()}.  It
 * is up to the map to remove elements.  
 * 
 * <p><b>Thread Safety</b></p>
 *
 * <p>A cached feature extractor will be thread safe if its contained
 * cache is thread safe.  A map may be made thread safe by wrapping it
 * with synchronization.  There are more live thread-safe maps in Java
 * from the {@code util.concurrent} package.  The {@link
 * com.aliasi.util.FastCache} and {@link
 * com.aliasi.util.HardFastCache} are also both thread safe.  The only
 * methods of the map accessed by this class are the put and get
 * methods.
 * 
 * <p><b>Serialization</b></p>
 *
 * <p>A cached feature extractor will be serializable if its contained
 * map and contained feature extractor are serializable.  The current
 * values in the cache will be included in the serialized value.  To
 * prevent serializing values, clear the cache before serializing it
 * (the cache is accessible through the {@link #cache()} method.  The
 * fast cache and most of Java's map implementations are serializable.
 * 
 * @author Bob Carpenter
 * @version 3.8
 * @since Lingpipe3.8
 * @param <E> Type of object whose features are extracted
 */
public class CacheFeatureExtractor<E> 
    extends FeatureExtractorFilter<E>
    implements Serializable {

    static final long serialVersionUID = -3991123544605867490L;

    final Map<E, Map<String, ? extends Number>> mCache;

    /**
     * Construct a cached feature extractor that caches the
     * results of the specified extractor using the specified cache.
     *
     * @param extractor Extractor to use to extract feature vectors.
     * @param cache Cache in which to store extracted vectors.
     */
    public CacheFeatureExtractor(FeatureExtractor<? super E> extractor,
                                 Map<E, Map<String, ? extends Number>> cache) {
        super(extractor);
        mCache = cache;
    }

    /**
     * Returns the cache for this cached feature extractor.
     * This is the actual cache used by this extractor, so
     * changes to it affect this extractor.
     *
     * @return The cache for this feature extractor.
     */
    public Map<E,Map<String,? extends Number>> cache() {
        return mCache;
    }

    @Override
    public Map<String, ? extends Number> features(E in) {
        Map<String, ? extends Number> features = mCache.get(in);
        if (features == null) {
            features = baseExtractor().features(in);
            mCache.put(in, features);
        }
        return features;
    }

    Object writeReplace() {
        return new Serializer<E>(this);
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = 4597111698841666424L;
        final CacheFeatureExtractor<F> mExtractor;
        public Serializer(CacheFeatureExtractor<F> extractor) {
            mExtractor = extractor;
        }
        public Serializer() {
            this(null);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mExtractor.mExtractor);
            out.writeObject(mExtractor.mCache);
        }
        @Override
        public Object read(ObjectInput in) throws ClassNotFoundException,
                                                      IOException {
            @SuppressWarnings("unchecked")
            FeatureExtractor<? super F> extractor 
                = (FeatureExtractor<? super F>) in.readObject();
            @SuppressWarnings("unchecked")
            Map<F,Map<String,? extends Number>> cache
                = (Map<F,Map<String,? extends Number>>) in.readObject();
            return new CacheFeatureExtractor<F>(extractor,cache);
        }
    }


}


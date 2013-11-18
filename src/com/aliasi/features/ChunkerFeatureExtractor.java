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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * A {@code ChunkerFeatureExtractor} implements a feature extractor
 * for character sequences based on a specified chunker.  Feature
 * names are derived from the chunk types optionally concatenated to
 * the phrase making up the chunk.  Feature values are the count of
 * their occurrences.
 *
 * <p>For instance, if a chunker were to return a chunk of type {@code
 * PER} spanning the phrase {@code John} and a chunk of type {@code
 * LOC} spanning the phrase {@code New York}, then the features will
 * be {@code PER:1, LOC:1} if the phrases are not included and
 * {@code PER_John:1, LOC_New York:1}.  If the phrase {@code John}
 * had shown up three times, the value for {@code PER_John} would
 * be {@code 3} (assuming types are included).
 *
 * <h3>Serialization</h3>
 *
 * A chunker-based feature extractor will be serializable if its
 * underlying chunker is serializable.
 * 
 * <h3>Thread Safety</h3>
 *
 * Upon safe publishing, a chunker feature extractor will be
 * thread safe if its underlying chunker is thread safe.
 * 
 * @author Bob Carpenter
 * @version 3.9.2
 * @since Lingpipe3.9.2
 */
             
public class ChunkerFeatureExtractor
    implements FeatureExtractor<CharSequence>,
               Serializable {

    static final long serialVersionUID = 6331037082723097532L;

    private final Chunker mChunker;
    private final boolean mIncludePhrase;

    /**
     * Construct a new chunker feature extractor based on the
     * specified chunker, including the phrases extracted if the
     * specified flag is true.
     *
     * @param chunker Base chunker for the extractor.
     * @param includePhrase Set to {@code true} to append the
     * phrase derived from the chunk to the feature name.
     */
    public ChunkerFeatureExtractor(Chunker chunker,
                                   boolean includePhrase) {
        mChunker = chunker;
        mIncludePhrase = includePhrase;
    }

    public Map<String,? extends Number> features(CharSequence in) {
        Chunking chunking = mChunker.chunk(in);
        Set<Chunk> chunkSet = chunking.chunkSet();
        if (chunkSet.isEmpty())
            return Collections.<String,Number>emptyMap();
        ObjectToDoubleMap<String> features = new ObjectToDoubleMap<String>();
        CharSequence text = chunking.charSequence();
        for (Chunk chunk : chunkSet) {
            String chunkType = chunk.type();
            if (!mIncludePhrase) {
                features.increment(chunkType,1.0);
            } else {
                StringBuilder sb = new StringBuilder(chunkType);
                sb.append('_');
                sb.append(text,chunk.start(),chunk.end());
                features.increment(sb.toString(),1.0);
            }
        }
        return features;
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        static final long serialVersionUID = 1943280252018121446L;
        final ChunkerFeatureExtractor mExtractor;
        public Serializer() {
            this(null);
        }
        public Serializer(ChunkerFeatureExtractor extractor) {
            mExtractor = extractor;
        }
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            
            @SuppressWarnings("unchecked")
            Chunker chunker = (Chunker) in.readObject();
            boolean includePhrase = in.readBoolean();
            return new ChunkerFeatureExtractor(chunker,includePhrase);
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mExtractor.mChunker);
            out.writeBoolean(mExtractor.mIncludePhrase);
        }
    }
}
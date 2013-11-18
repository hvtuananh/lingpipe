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

package com.aliasi.dict;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToSet;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Iterator;

import java.io.Serializable;

/**
 * A <code>MapDictionary</code> uses an underlying map from phrases to
 * their set of dictionary entries.  Map-based dictionaries are
 * compilable if their underlying entries are compilable, which
 * requires every category object to implement either the LingPipe
 * interface {@link Compilable} or the Java interface {@link java.io.Serializable}
 *
 * <p>The result is a fast
 * implementation of {@link #addEntry(DictionaryEntry)}, {@link
 * #iterator()} and {@link #phraseEntryIt(String)}.
 *
 * <h3>Serialization and Compilation</h3>
 *
 * Serializing or compiling a dictionary writes out a compact
 * representation of the dictionary.  When read back in, the
 * dictionary will be the same as the one written.  
 *
 * <p>The dictionary entries must be serializable or compilable.
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.3.1
 * @param <C> the type of object stored in this dictionary
 */
public class MapDictionary<C>
    extends AbstractDictionary<C>
    implements Compilable, Serializable {

    static final long serialVersionUID = 3296124888445414454L;

    final ObjectToSet<String, DictionaryEntry<C>> mPhraseToEntrySet;

    /**
     * Construct an empty map-based dictionary.
     */
    public MapDictionary() {
        this(new ObjectToSet<String,DictionaryEntry<C>>());
    }

    private MapDictionary(ObjectToSet<String,DictionaryEntry<C>> phraseToEntrySet) {
        mPhraseToEntrySet = phraseToEntrySet;
    }

    @Override
    public void addEntry(DictionaryEntry<C> entry) {
        mPhraseToEntrySet.addMember(entry.phrase(),entry);
    }

    /**
     * Return an iterator over the dictionary entries in this dictionary.
     *
     * @return Iterator over entries.
     */
    @Override
    public Iterator<DictionaryEntry<C>> iterator() {
        return mPhraseToEntrySet.memberIterator();
    }

    @Override
    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase) {
        return mPhraseToEntrySet.getSet(phrase).iterator();
    }
    
    /**
     * Serializes the object to the specified output.
     */
    @Override
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer<C>(this));
    }

    Object writeReplace() {
        return new Externalizer<C>(this);
    }

    private static class Externalizer<D> extends AbstractExternalizable {
        private static final long serialVersionUID = -9136273040574611243L;
        final MapDictionary<D> mDictionary;
        public Externalizer() { this(null); }
        public Externalizer(MapDictionary<D> dictionary) {
            mDictionary = dictionary;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            MapDictionary<D> dict = new MapDictionary<D>();
            int numEntries = in.readInt();
            for (int i = 0; i < numEntries; ++i) {
                // required for readObject; safe if safe write
                @SuppressWarnings("unchecked")
                DictionaryEntry<D> entry = (DictionaryEntry<D>) in.readObject();
                dict.addEntry(entry);
            }
            return dict;
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeInt(mDictionary.size());
            for (DictionaryEntry<D> entry : mDictionary) {
                entry.compileTo(objOut);
            }
        }
    }

}

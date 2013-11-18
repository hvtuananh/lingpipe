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
import com.aliasi.util.Scored;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A <code>DictionaryEntry</code> provides a phrase as a string, an
 * object-based category for the phrase, and a double-valued score.
 * Equality for dictionary entries involves equality of the
 * phrase and category components; dictionaries should not contain
 * entries with the same phrase and category and different scores.
 *
 * @author Bob Carpenter
 * @version 3.8.3
 * @since   LingPipe2.1
 * @param <C> the type of object stored in the dictionary
 */
public class DictionaryEntry<C> implements Compilable, Scored {

    private final String mPhrase;
    private final C mCategory;
    private final double mScore;
    private final int mCount;

    /**
     * Construct a dictionary entry with the specified
     * phrase, category, count and score.
     *
     * @param phrase Phrase for the constructed entry.
     * @param category Category for the constructed entry.
     * @param count Count for the constructed entry.
     * @param score Score for the constructed entry.
     */
    public DictionaryEntry(String phrase,
                           C category,
                           int count,
                           double score) {
        mPhrase = phrase;
        mCategory = category;
        mCount = count;
        mScore = score;
    }

    /**
     * Construct a dictionary entry with the specified phrase,
     * category and score, with a count of zero.
     *
     * @param phrase Phrase for the constructed entry.
     * @param category Category for the constructed entry.
     * @param score Score for the constructed entry.
     */
    public DictionaryEntry(String phrase,
                           C category,
                           double score) {
        this(phrase,category,0,score);
    }

    /**
     * Construct a dictionary entry with the specified
     * phrase, category and count, with the score set
     * to the count value.
     *
     * @param phrase Phrase for the constructed entry.
     * @param category Category for the constructed entry.
     * @param count Count for the constructed entry.
     */
    public DictionaryEntry(String phrase,
                           C category,
                           int count) {
        this(phrase,category,count,count);
    }

    /**
     * Construct a dictionary entry with the specified
     * phrase and category, with count and score
     * set to <code>1</code>.
     *
     * @param phrase Phrase for the constructed entry.
     * @param category Category for the constructed entry.
     */
    public DictionaryEntry(String phrase,
                           C category) {
        this(phrase,category,1);
    }

    /**
     * Returns the phrase for this dictionary entry.
     *
     * @return The phrase.
     */
    public String phrase() {
        return mPhrase;
    }

    /**
     * Returns the category for this dictionary entry.
     *
     * @return The category.
     */
    public C category() {
        return mCategory;
    }

    /**
     * Returns the score for this dictionary entry.
     *
     * @return The score.
     */
    public double score() {
        return mScore;
    }

    /**
     * Returns the count for this dictionary entry.
     *
     * @return The count for this entry.
     */
    public int count() {
        return mCount;
    }

    /**
     * Returns a string-based representation of this entry.
     *
     * @return A string-based representation of this entry.
     */
    @Override
    public String toString() {
        return phrase() + ":" + category() + " " + score();
    }

    /**
     * Returns <code>true</code> if the specified object is a
     * dictionary object equal to this one.  Equality is
     * defined in terms of equality of phrases, categories,
     * counts and scores; all must be equal for entries
     * to be equal.  Equality is defined consistently with
     * {@link #hashCode()}.
     *
     * @param that Object to compare with this entry.
     * @return <code>true</code> if the sepcified object is equal to
     * this entry.
     */
    @Override
    @SuppressWarnings("rawtypes") // for instanceof
    public boolean equals(Object that) {
        if (!(that instanceof DictionaryEntry)) return false;
        DictionaryEntry<?> thatEntry = (DictionaryEntry<?>) that;
        return mPhrase.equals(thatEntry.mPhrase)
            && mCategory.equals(thatEntry.mCategory)
            && mScore == thatEntry.mScore
            && mCount == thatEntry.mCount;
    }

    /**
     * Returns the hash code for this entry.  Hash codes are defined
     * in terms of phrase and category hashCodes and are consistent
     * with {@link #equals(Object)}.
     *
     * @return The hash code for this entry.
     */
    @Override
    public int hashCode() {
        return mPhrase.hashCode() + 31*mCategory.hashCode();
    }

    /**
     * Compiles this dictionary entry to the specified object output.
     * This method will throw a class-cast exception if it encounters
     * a dictionary entry whose category object is not serializable.
     *
     * @param objOut Output to which object is written.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer<C>(this));
    }


    private static class Externalizer<C2> extends AbstractExternalizable {
        private static final long serialVersionUID = -863015530144283246L;
        private final DictionaryEntry<C2> mEntry;
        public Externalizer() { this(null); }
        public Externalizer(DictionaryEntry<C2> entry) {
            mEntry = entry;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            String phrase = in.readUTF();
            // required for read object
            @SuppressWarnings("unchecked")
            C2 category = (C2) in.readObject();
            int count = in.readInt();
            double score = in.readDouble();
            return new DictionaryEntry<C2>(phrase,category,count,score);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            objOut.writeUTF(mEntry.phrase());
            Object category = mEntry.category();

            AbstractExternalizable.compileOrSerialize(category,objOut);
            objOut.writeInt(mEntry.count());
            objOut.writeDouble(mEntry.score());
        }
    }

}

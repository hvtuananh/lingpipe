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

import com.aliasi.util.Compilable;
import com.aliasi.util.Iterators;

import java.io.IOException;
import java.io.ObjectOutput;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An <code>AbstractDictionary</code> is a dictionary with convenience
 * implementations of most methods.  Like the {@link AbstractSet}
 * class it extends, the {@link #iterator()} method must be defined by
 * concrete implementations to return an iterator over dictionary
 * entries.  Unlike <code>AbstractSet</code>, even the {@link #size()}
 * method is implemented in terms of the iterator.
 *
 * <P>The implementation of the mutable methods in this class all
 * throw unsupported operation exceptions.  This includes the method
 * {@link #addEntry(DictionaryEntry entry)} in the dictionary
 * interface, and the methods {@link #remove(Object)} and {@link
 * #add(Object)} and their relatives in the set interface.  Subclasses
 * may override any or all of these methods.
 *
 * @author Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.1
 * @param <C> the type of entries in the dictionary
 */
public abstract class AbstractDictionary<C> extends AbstractSet<DictionaryEntry<C>>
    implements Dictionary<C>, Compilable {

    /**
     * Construct an abstract dictionary.
     */
    protected AbstractDictionary() {
        /* do nothing */
    }

    /**
     * Returns an iterator over the dictionary entries with the
     * specified phrase.
     *
     * <P><i>Implementation Note:</i> This implementation filters the
     * result of {@link #iterator()} for entries with a matching
     * phrase.
     *
     * @param phrase The phrase to look up.
     * @return Iterator over the entries with the specified phrase.
     */
    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase) {
        return new PhraseIterator(phrase);
    }

    /**
     * Returns the dictionary entries with the specified phrase.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #phraseEntryIt(String)} in a collection and
     * then converts it to an array.
     *
     * @param phrase The phrase to look up.
     * @return The entries with the specified phrase.
     */
    DictionaryEntry<C>[] phraseEntries(String phrase) {
        return itToEntries(phraseEntryIt(phrase));
    }

    /**
     * Returns the dictionary entries with the specified phrase.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #phraseEntryIt(String)} in a list.
     *
     * @param phrase The phrase to look up.
     * @return The entries with the specified phrase.
     */
    public List<DictionaryEntry<C>> phraseEntryList(String phrase) {
        return itToEntryList(phraseEntryIt(phrase));
    }

    /**
     * Returns an iterator over the dictionary entries with the
     * specified category.
     *
     * <P><i>Implementation Note:</i> This implementation filters the
     * result of {@link #iterator()} for entries with a matching
     * category.
     *
     * @param category Category of entries.
     * @return Iterator over entries with specified category.
     */
    public Iterator<DictionaryEntry<C>> categoryEntryIt(C category) {
        return new CategoryIterator(category);
    }

    /**
     * Returns the dictionary entries with the specified category.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #categoryEntryIt(Object)} in a collection
     * and then converts it to an array.
     *
     * @param category Category of entries.
     * @return Entries with specified category.
     */
    DictionaryEntry<C>[] categoryEntries(C category) {
        return itToEntries(categoryEntryIt(category));
    }

    /**
     * Returns the dictionary entries with the specified category.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #categoryEntryIt(Object)} in a list.
     *
     * @param category Category of entries.
     * @return Entries with specified category.
     */
    public List<DictionaryEntry<C>> categoryEntryList(C category) {
        return itToEntryList(categoryEntryIt(category));
    }

    /**
     * Returns an iterator over all of the dictionary entries for this
     * dictionary.  This is the only method that a concrete
     * implementation needs to provide, but overriding other methods
     * may lead to increased efficiency.
     *
     * @return An iterator over all of this dictionary's entries.
    public abstract Iterator<DictionaryEntry<C>> iterator();
     */

    /**
     * Returns the size of this dictionary as measured by number
     * of dictionary entries.
     *
     * <P><i>Implementation Note:</i> The implementation here just
     * takes the length of {@link #entries()}.  Subclasses may
     * override this method for greater efficiency.
     */
    @Override
    public int size() {
        return entries().length;
    }

    /**
     * Returns all of the dictionary entries for this dictionary.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #categoryEntryIt(Object)} in a collection
     * and then converts it to an array.
     *
     * @return This dictionary's entries.
     */
    DictionaryEntry<C>[] entries() {
        return itToEntries(iterator());
    }


    /**
     * Returns all of the dictionary entries for this dictionary.
     *
     * <P><i>Implementation Note:</i> This implementation buffers the
     * results of {@link #categoryEntryIt(Object)} in a list.
     *
     * @return This dictionary's entries.
     */
    public List<DictionaryEntry<C>> entryList() {
        return itToEntryList(iterator());
    }



    /**
     * Adds the specified dictionary entry to the dictionary.  This
     * method must be overridden by subclasses that allow the addition
     * of dictionary entries.
     *
     * @param entry Dictionary entry to add.
     * @throws UnsupportedOperationException If this operation is not
     * supported by a subclass implementation.
     */
    public void addEntry(DictionaryEntry<C> entry) {
        unsupported("addEntry(DictionaryEntry)");
    }

    /**
     * Compiles this dictionary to the specified object output.  This
     * method must be overriden by subclasses that allow the compilation
     * of dictionaries; the implementation here throws an unsupported
     * operation exception.
     *
     * @param out Object output to which this dictionary is compiled.
     * @throws UnsupportedOperationException If this operation is not
     * supported by a subclass implementation.
     * @throws IOException If there is an I/O error writing the
     * object.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        unsupported("compileTo(ObjectOut)");
    }

    private void unsupported(String op) {
        String msg = "Unsupported operation=" + op
            + " Class=" + getClass();
        throw new UnsupportedOperationException(msg);
    }

    private List<DictionaryEntry<C>> itToEntryList(Iterator<DictionaryEntry<C>> it) {
        List<DictionaryEntry<C>> entryList
            = new ArrayList<DictionaryEntry<C>>();
        while (it.hasNext())
            entryList.add(it.next());
        return entryList;
    }

    private DictionaryEntry<C>[] itToEntries(Iterator<DictionaryEntry<C>> it) {
        List<DictionaryEntry<C>> entryList = itToEntryList(it);
        @SuppressWarnings({"unchecked","rawtypes"})  // required for array
        DictionaryEntry<C>[] entries
            = (DictionaryEntry<C>[]) new DictionaryEntry[entryList.size()];
        entryList.toArray(entries);
        return entries;
    }

    private class PhraseIterator
        extends Iterators.Filter<DictionaryEntry<C>> {

        private final String mPhrase;
        public PhraseIterator(String phrase) {
            super(iterator());
            mPhrase = phrase;
        }
        @Override
        public boolean accept(DictionaryEntry<C> entry) {
            return entry.phrase().equals(mPhrase);
        }
    }

    private class CategoryIterator
        extends Iterators.Filter<DictionaryEntry<C>> {

        private final C mCategory;
        public CategoryIterator(C category) {
            super(iterator());
            mCategory = category;
        }
        @Override
        public boolean accept(DictionaryEntry<C> entry) {
            return entry.category().equals(mCategory);
        }
    }

}

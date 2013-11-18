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

// import com.aliasi.util.Arrays;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Iterators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A <code>TrieDictionary</code> stores a dictionary using a character
 * trie structure.  This requires a constant amount of space for each
 * entry and each prefix of an entry's string.  Lookups take an amount
 * of time proportional to the length of the string being looked up,
 * with each character requiring a lookup in a map.  The lookup is
 * done with binary search in this implementation in time proportional
 * to the log of the number of characters, for a total lookup time
 * of <code><b>O</b>(n log c)</code> where <code>n</code> is the
 * number of characters in the string being looked up and <code>c</code>
 * is the number of charactes.
 *
 * <P>Tries are a popular data structure; see the <a
 * href="http://en.wikipedia.org/wiki/Trie">Wikipedia Trie</a> topic for
 * examples and references.  Tries are also used in the language model
 * classes {@link com.aliasi.lm.TrieCharSeqCounter} and {@link
 * com.aliasi.lm.TrieIntSeqCounter} and the compiled forms of all of
 * the language models.
 *
 * <h4>Compilation and Serialization</h4>
 *
 * The trie dictionary implements both the Java {@link Serializable}
 * and LingPipe {@link Compilable} interfaces to write the contents
 * of a trie dictionary to an object output.  Both approaches produce
 * the same result and the dictionary read back in will be an instance
 * of <code>TrieDictionary</code> and equivalent to the dictionary that
 * was serialized or compiled.
 *
 * @author Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.1
 * @param <C> the type of object stored in this dictionary
 */
public class TrieDictionary<C>
    extends AbstractDictionary<C>
    implements Serializable, Compilable {

    static final long serialVersionUID = -6772406715071883449L;

    Node<C> mRootNode = new Node<C>();

    /**
     * Construct a trie-based dictionary.
     */
    public TrieDictionary() {
        /* do ntohing */
    }

    @Override
    DictionaryEntry<C>[] phraseEntries(String phrase) {
        Node<C> node = mRootNode;
        for (int i = 0; i < phrase.length(); ++i) {
            node = node.getDtr(phrase.charAt(i));
            if (node == null) return Node.<C>emptyEntries();
        }
        return node.mEntries;
    }

    @Override
    public Iterator<DictionaryEntry<C>> phraseEntryIt(String phrase) {
        return Iterators.<DictionaryEntry<C>>array(phraseEntries(phrase));
    }

    /**
     * Equal entries will be ignored.
     */
    @Override
    public void addEntry(DictionaryEntry<C> entry) {
        String phrase = entry.phrase();
        Node<C> node = mRootNode;
        for (int i = 0; i < phrase.length(); ++i)
            node = node.getOrAddDtr(phrase.charAt(i));
        node.addEntry(entry);
    }

    /**
     * Returns an iterator over all of the dictionary entries
     * for this dictioniary.  This is the implementation of the iterator
     * view of this dictionary as a collection (set of entries).
     *
     * @return An iterator over all of the dictionary entries for this
     * dictioniary.
     */
    @Override
    public Iterator<DictionaryEntry<C>> iterator() {
        return new TrieIterator<C>(mRootNode);
    }

    private Object writeReplace() {
        return new Externalizer<C>(this);
    }

    /**
     * Compile the entries in this dictionary to the specified object output.
     *
     * @param out Object output to which to write the dictionary.
     * @throws IOException If there is an underlying I/O error during
     * the write.
     */
    @Override
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer<C>(this));
    }

    private static class Externalizer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -6351978792499636468L;
        private final TrieDictionary<F> mDictionary;
        public Externalizer(TrieDictionary<F> dict) {
            mDictionary = dict;
        }
        public Externalizer() {
            this(null);
        }
        @Override
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            TrieDictionary<F> dict = new TrieDictionary<F>();
            int numEntries = in.readInt();
            for (int i = 0; i < numEntries; ++i) {
                // required for readObject; safe with good serialization
                @SuppressWarnings("unchecked")
                DictionaryEntry<F> entry = (DictionaryEntry<F>) in.readObject();
                dict.addEntry(entry);
            }
            return dict;
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            int count = mDictionary.size();
            out.writeInt(count);
            for (DictionaryEntry<F> entry : mDictionary)
                entry.compileTo(out);
        }
    }

    private static class TrieIterator<D>
        extends Iterators.Buffered<DictionaryEntry<D>> {
        LinkedList<Node<D>> mQueue = new LinkedList<Node<D>>();
        DictionaryEntry<D>[] mEntries;
        int mNextEntry = -1;
        TrieIterator(Node<D> root) {
            mQueue.add(root);
        }
        @Override
        protected DictionaryEntry<D> bufferNext() {
            while (mEntries == null && !mQueue.isEmpty()) {
                Node<D> node = mQueue.removeFirst();
                addDtrs(node.mDtrNodes);
                if (node.mEntries.length > 0) {
                    mEntries = node.mEntries;
                    mNextEntry = 0;
                }
            }
            if (mEntries == null) return null;
            DictionaryEntry<D> result = mEntries[mNextEntry++];
            if (mNextEntry >= mEntries.length) mEntries = null;
            return result;
        }
        void addDtrs(Node<D>[] dtrs) {
            for (int i = dtrs.length; --i >= 0; ) {
                if (dtrs[i] == null) System.out.println("ADDING=" + i);
                mQueue.addFirst(dtrs[i]);
            }
        }
    }


}

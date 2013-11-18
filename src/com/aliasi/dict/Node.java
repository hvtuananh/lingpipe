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

import java.util.Arrays;

/**
 * @author Bob Carpenter
 * @version 3.8.3
 * @since   LingPipe2.1
 */
class Node<C> {

    @SuppressWarnings({"unchecked","rawtypes"}) // ok because empty
    static final DictionaryEntry[] EMPTY_ENTRIES = new DictionaryEntry[0];
    static final char[] EMPTY_CHARS = new char[0];
    @SuppressWarnings({"unchecked","rawtypes"}) // ok because empty
    static final Node[] EMPTY_NODES = new Node[0];

    static <D> DictionaryEntry<D>[] emptyEntries() {
        // required for array; safe
        @SuppressWarnings("unchecked")
        DictionaryEntry<D>[] entries = (DictionaryEntry<D>[]) EMPTY_ENTRIES;
        return entries;
    }

    static <D> Node<D>[] emptyNodes() {
        // required for array; safe
        @SuppressWarnings("unchecked")
        Node<D>[] nodes = (Node<D>[]) EMPTY_NODES;
        return nodes;
    }

    DictionaryEntry<C>[] mEntries = Node.<C>emptyEntries();

    char[] mDtrChars = EMPTY_CHARS;

    Node<C>[] mDtrNodes = Node.<C>emptyNodes();

    Node<C> getDtr(char c) {
        int i = Arrays.binarySearch(mDtrChars,c);
        return i < 0 ? null : mDtrNodes[i];
    }
    Node<C> getOrAddDtr(char c) {
        Node<C> dtr = getDtr(c);
        if (dtr != null) return dtr;
        Node<C> result = new Node<C>();
        char[] oldDtrChars = mDtrChars;
        Node<C>[] oldDtrNodes = mDtrNodes;
        mDtrChars = new char[mDtrChars.length+1];
        // required for array creation, including local var
        @SuppressWarnings({"unchecked","rawtypes"})
        Node<C>[] dtrNodes = (Node<C>[]) new Node[mDtrNodes.length+1];
        mDtrNodes = dtrNodes;
        int i = 0;
        for (; i < oldDtrChars.length; ++i) {
            if (oldDtrChars[i] > c) break;
            mDtrChars[i] = oldDtrChars[i];
            mDtrNodes[i] = oldDtrNodes[i];
        }
        mDtrChars[i] = c;
        mDtrNodes[i] = result;
        for (; i < oldDtrChars.length; ++i) {
            mDtrChars[i+1] = oldDtrChars[i];
            mDtrNodes[i+1] = oldDtrNodes[i];
        }
        return result;

    }
    void addEntry(DictionaryEntry<C> entry) {
        DictionaryEntry<C>[] oldEntries = mEntries;
        for (int i = 0; i < oldEntries.length; ++i)
            if (oldEntries[i].equals(entry)) return;
        // required for array alloc
        @SuppressWarnings({"unchecked","rawtypes"})
        DictionaryEntry<C>[] entries
            = (DictionaryEntry<C>[]) new DictionaryEntry[oldEntries.length+1];
        mEntries = entries;
        mEntries[0] = entry;
        System.arraycopy(oldEntries,0,mEntries,1,oldEntries.length);
    }
}


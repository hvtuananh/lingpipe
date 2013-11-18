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

package com.aliasi.lm;

import com.aliasi.io.BitInput;

import java.io.IOException;

/**
 * A <code>BitTrieReader</code> provides a trie reader that wraps a
 * bit-level input.  
 *
 * <p>The encoding is discussed in the class
 * documentation for the corresponding writer, {@link BitTrieWriter}.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public class BitTrieReader extends BitTrie implements TrieReader {

    private final BitInput mBitInput;

    /**
     * Construct a bit trie reader from the specified bit input.
     *
     * @param bitInput Bit input from which to read the trie.
     */
    public BitTrieReader(BitInput bitInput) {
    mBitInput = bitInput;
    }

    /**
     * Read and return the next count from the underlying bit input.
     *
     * @return The next count.
     * @throws IOException If there is an underlying I/O error.
     */
    public long readCount() throws IOException {
    long count =  mBitInput.readDelta();
    // System.out.println("CODE:" + count);
    pushValue(-1L);
    // System.out.println("Read count=" + count);
    return count;
    }

    /**
     * Read and return the next symbol from the underlying bit input.  
     * Returns <code>-1</code> if there are no more daughters to
     * read in the current trie node.
     *
     * @return The next count.
     * @throws IOException If there is an underlying I/O error.
     */
    public long readSymbol() throws IOException {
    long n = mBitInput.readDelta();
    // System.out.println("CODE:" + n);
    if (n == 1L) {
        popValue();
        // System.out.println("Read symbl=-1");
        return -1L;
    }
    long sym = n + popValue() - 1L;
    // System.out.println("Read symbol=" + sym);
    pushValue(sym);
    return sym;
    }

}
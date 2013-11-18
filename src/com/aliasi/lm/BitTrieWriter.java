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

import com.aliasi.io.BitOutput;

import java.io.IOException;

/**
 * A <code>BitTrieWriter</code> provides a trie writer that wraps a
 * bit-level output.  
 *
 * <p>The reader for the output of a bit trie writer is {@link
 * BitTrieReader}.
 *
 * <p>Counts of trie nodes and differences between
 * successive symbols on transitions are delta coded for compression
 * (see {@link BitOutput#writeDelta(long)}).  
 *
 * <p>The method {@link #copy(TrieReader,TrieWriter)} is available to
 * copy the contents of a reader to a writer.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public class BitTrieWriter extends BitTrie implements TrieWriter {

    private final BitOutput mBitOutput;

    /**
     * Construct a bit trie writer from the specified bit output
     * with the specified maximum n-gram.
     *
     * @param bitOutput Underlying bit output.
     */
    public BitTrieWriter(BitOutput bitOutput) {
	mBitOutput = bitOutput;
    }

    public void writeCount(long count) throws IOException {
	checkCount(count);
	// System.out.println(" TrieWriter.writeCount(" + count + ")");
	// System.out.println("CODE: " + count);
	mBitOutput.writeDelta(count);
	pushValue(-1L);
    }
    
    public void writeSymbol(long symbol) throws IOException {
	if (symbol == -1L) {
	    // encode -1 as 1L
	    mBitOutput.writeDelta(1L);
	    // System.out.println("TrieWriter.writeSymbol(-1)");
	    // System.out.println("CODE: " + 1L);
	    popValue();
	} else {
	    long code = symbol - popValue() + 1L; // >= 2
	    mBitOutput.writeDelta(code);
	    // System.out.println("TrieWriter.writeSymbol(" + symbol + ") char=" + ((char)symbol));
	    // System.out.println("CODE: " + code);
	    pushValue(symbol);
	}
    }

    /**
     * Copies the content of the specified trie reader to the specified
     * trie writer.
     *
     * @param reader Reader from which to read.
     * @param writer Writer to which to write.
     */
    public static void copy(TrieReader reader, TrieWriter writer) 
	throws IOException {

	long count = reader.readCount();
	// System.out.println("count=" + count);
	writer.writeCount(count);
	long symbol;
	while ((symbol = reader.readSymbol()) != -1L) {
	    // System.out.println("symbol=" + symbol + "[" + ((char)symbol) + "]");
	    writer.writeSymbol(symbol);
	    copy(reader,writer);
	}
	// System.out.println("symbol=-1");
	writer.writeSymbol(-1L);
    }


}
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

import java.io.IOException;

/**
 * The <code>TrieWriter</code> interface provides a means
 * to write an arbitrary trie structure with positive node counts.
 *
 * <p>A trie is encoded depth-first according to the following
 * recursive definition.  First, the the count of the tree
 * (<code>count</code>) is encoded.  Then, for each daughter
 * <code>K</code>, <code>1&nbsp;<=&nbsp;K&nbsp;<=&nbsp;N</code>, in
 * increasing symbol order, the daughter symbol (<code>dtrSymK</code>)
 * is encoded followed by the encoding of the daughter tree
 * (<code>dtrTreeK</code>).  Finally, after the last daughter tree is
 * encoded, the number <code>-1</code> is encoded.
 *
 * <blockquote><pre>
 * encode(tree) =
 *   count
 *   dtrSym1
 *   encode(dtrTree1)
 *   ...
 *   dtrSymN
 *   encode(dtrTreeN)
 *   -1
 * </pre></blockquote>
 *
 * <p>This results in a unique depth-first encoding of an entire tree
 * as a sequence of <code>long</code> values.
 *
 * @author  Bob Carpenter
 * @version 2.3
 * @since   LingPipe2.3
 */
public interface TrieWriter {

    /**
     * Writes the specified count for the next sub-trie.
     *
     * @param count Count to write.
     * @throws IOException If there is an underlying I/O error.
     * @throws IllegalStateException If it is not legal to write a count
     * into the current sequence.
     */
    public void writeCount(long count) throws IOException;

    /**
     * Writes the specified symbol for the next daughter. 
     *
     * <p>The symbol <code>-1</code> must be written following the last
     * daughter of a tree. 
     * 
     * @param symbol Symbol to write.
     * @throws IOException If there is an underlying I/O error.
     * @throws IllegalStateException If it is not legal to write a
     * symbol into the current sequence.
     */
    public void writeSymbol(long symbol) throws IOException;

}
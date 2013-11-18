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

package com.aliasi.sentences;

import java.util.Collection;

/**
 * The <code>SentenceModel</code> interface specifies a means of doing
 * sentence segmentation from arrays of tokens and whitespaces.  
 * 
 * <P>The sentence model operates over aligned arrays of tokens and
 * whitespaces, as derived from a {@link
 * com.aliasi.tokenizer.Tokenizer}.  There are two methods in the
 * interface.  The standard external interface is {@link
 * #boundaryIndices(String[],String[])}, which returns an array of
 * token indices that are sentence-final.  For instance, with tokens
 * <code>{&quot;John&quot;, &quot;ran&quot;, &quot;.&quot;, &quot;He&quot;, &quot;also&quot;, &quot;jumped&quot;, &quot;!&quot;}</code>, and
 * whitespaces <code>{&quot;&quot;, &quot;&nbsp;&quot;, &quot;&quot;, &quot;&nbsp;&nbsp;&quot;, &quot;&nbsp;&quot;, &quot;&nbsp;&quot;, &quot;&nbsp;&quot;, &quot;&quot;, &quot;&quot;}</code>.
 * the return result from the Indo-European model would be
 * <code>{2,6}</code>, because the token indexed 2 is a period
 * (<code>.</code>) and the token indexed 6 is an exclamation point
 * (<code>!</code>).  The return result will often depend on the
 * whitespaces as well as the tokens.
 *
 * <P>The second method is {@link
 * #boundaryIndices(String[],String[],int,int,Collection)}, which adds
 * the boundary indexes as <code>Integer</code>s to the specified
 * collection for the slice determined by the start and end plus one
 * indices.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe1.0
 */
public interface SentenceModel {

    /**
     * Returns an array of indices of sentence-final tokens.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @return Array of integers indicating indices of tokens that
     * are sentence final.
     * @throws IllegalArgumentException If the array of whitespaces is
     * not one longer than the array of tokens.
     */
    public int[] boundaryIndices(String[] tokens, String[] whitespaces);


    /**
     * Adds the sentence final token indices as <code>Integer</code>
     * instances to the specified collection, only considering tokens
     * between index <code>start</code> and <code>end-1</code>
     * inclusive.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param end Index one beyond the last token to annotate.
     * @param indices Collection into which to write the boundary
     * indices.
     * @throws IllegalArgumentException If the array of tokens is 
     * not at least as long as <code>start+end</code> and the
     * array of whitespaces at least as long as <code>start+end+1</code>.
     */
    public void boundaryIndices(String[] tokens, String[] whitespaces,
                                int start, int end,
                                Collection<Integer> indices);


}

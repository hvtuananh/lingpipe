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

package com.aliasi.tokenizer;

/**
 * A <code>TokenizerFactory</code> constructors tokenizers from
 * subsequences of character arrays.  
 *
 * <p>Tokenizer factories are typically implemented to be serializable
 * so that they may be serialized along with the models that depend on
 * them.
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 */
public interface TokenizerFactory {

    /**
     * Returns a tokenizer for the specified subsequence
     * of characters.
     *
     * @param ch Characters to tokenize.
     * @param start Index of first character to tokenize.
     * @param length Number of characters to tokenize.
     */
    public Tokenizer tokenizer(char[] ch, int start, int length);

}

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

package com.aliasi.spell;

/**
 * The <code>SpellChecker</code> interface specifies a single method
 * for first-best spelling correction.
 *
 * @author  Bob Carpenter
 * @version 2.0
 * @since   LingPipe2.0
 */
public interface SpellChecker {

    /**
     * Returns a first-best hypothesis of the intended message given
     * the received message.  The returned result will be
     * <code>null</code> if the received message itself is the best
     * hypothesis.
     *
     * @param receivedMsg Input string to correct.
     * @return The first-best hypotheses of what was meant.
     */
    public String didYouMean(String receivedMsg);

}

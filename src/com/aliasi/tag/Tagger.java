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

package com.aliasi.tag;

import java.util.List;

/**
 * The {@code Tagger<E>} interface for objects that tag a list of
 * tokens of type {@code E}.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public interface Tagger<E> {

    /**
     * Return the tagging for the specified list of tokens.
     *
     * @param tokens Input tokens to tag.
     * @return Tagging for the specified input tokens.
     */
    public Tagging<E> tag(List<E> tokens);

}
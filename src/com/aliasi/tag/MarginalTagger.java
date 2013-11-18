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
 * The {@code MarginalTagger<E>} interface is for objects that tag a
 * list of tokens with marginal per-tag and transition probabilities.
 * The result of tagging is a tag lattice that allows the efficient
 * evaluation of the marginal probabilities of tags or tag sequences
 * at specified positions.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public interface MarginalTagger<E> {

    /**
     * Return the marginal tagging for the specified list of
     * input tokens.
     *
     * @param tokens Input tokens to tag.
     * @return The lattice of tags for the specified tokens.
     */
    public TagLattice<E> tagMarginal(List<E> tokens);

}
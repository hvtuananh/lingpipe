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

package com.aliasi.crf;

import java.util.List;

/**
 * The {@code ChainCrfFeatureExtractor} interface specifies a method
 * for conditional random fields to extract the necessary node and
 * edge features for estimation and tagging.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public interface ChainCrfFeatureExtractor<E> {

    /**
     * Return the chain CRF features for the specified list of input
     * tokens and specified list of possible tags.

     * <p>When called from LingPipe's CRF class, the list of tags
     * is guaranteed to be unique.
     *
     * @param tokens List of token objects.
     * @param tags List of possible output tags.
     * @return The features for the specified tokens and tags.
     */
    public ChainCrfFeatures<E> extract(List<E> tokens, List<String> tags);

}
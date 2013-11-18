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

import com.aliasi.util.ScoredObject;

import java.util.Iterator;
import java.util.List;

/**
 * The {@code NBestTagger<E>} interface for objects that tag a list of
 * objects with multiple tagged results.  The number of results may be
 * limited, which may improve efficiency for large result sets.  The
 * n-best scored taggings are returned in order through an iterator.
 *
 * <p>An optional method returns the same n-best list with scores
 * normalized to conditional probabilities.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public interface NBestTagger<E> extends Tagger<E> {

    /**
     * Return an iterator over the {@code n}-best scored taggings for
     * the specified input tokens up to a specified maximum {@code n}.
     *
     * @param tokens Input tokens to tag.
     * @param maxResults Maximum number of results to return.
     * @return Iterator over the n-best scored taggings for the
     * specified tokens.
     */
    public Iterator<ScoredTagging<E>> tagNBest(List<E> tokens, int maxResults);

    /**
     * Return an iterator over the {@code n}-best scored taggings for
     * the specified input tokens up to a specified maximum {@code n},
     * with scores normalized to conditional probabilities.
     *
     * <p>Optional operation.
     *
     * @param tokens Input tokens to tag.
     * @param maxResults Maximum number of results to return.
     * @return Iterator over the n-best scored taggings for the
     * specified tokens.
     * @throws UnsupportedOperationException If this method is not
     * supported.
     */
    public Iterator<ScoredTagging<E>> tagNBestConditional(List<E> tokens, int maxResults);

}
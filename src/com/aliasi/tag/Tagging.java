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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Tagging<E>} represents a first-best assignment of a sequence
 * of tags to a sequence of tokens of type {@code E}. 
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 * @param <E> Type of tokens in the tagging.
 */
public class Tagging<E> {

    private final List<E> mTokens;
    private final List<String> mTags;

    /**
     * Construct a tagging with the specified list of tokens and tags.
     * The lists are copied, so subsequent changes to the lists
     * themselves will not matter.
     *
     * @param tokens List of tokens for the tagging.
     * @param tags List of tags for the tagging.
     * @throws IllegalArgumentException If the lists are not of the
     * same size.
     */
    public Tagging(List<E> tokens, List<String> tags) {
        this(new ArrayList<E>(tokens),new ArrayList<String>(tags),true);
        if (tokens.size() != tags.size()) {
            String msg = "Tokens and tags must be same size."
                + " Found tokens.size()=" + tokens.size()
                + " tags.size()=" + tags.size();
            throw new IllegalArgumentException(msg);
        }
    }

    Tagging(List<E> tokens, List<String> tags, boolean ignore) {
        mTokens = tokens;
        mTags = tags;
    }

    /**
     * Returns the number of tokens and tags in this
     * tagging.
     *
     * @return The length of this tagging.
     */
    public int size() {
        return mTokens.size();
    }

    /**
     * Return the token in the specified position for this
     * tagging.
     *
     * @param n Position of token.
     * @return Token at specified position.
     * @throws IndexOutOfBoundsException If the position is out of
     * range ({@code n < 0 || n >= size()}).
     */
    public E token(int n) {
        return mTokens.get(n);
    }

    /**
     * Return the tag in the specified position for this
     * tagging.
     *
     * @param n Position of tag.
     * @return Tag at specified position.
     * @throws IndexOutOfBoundsException If the position is out of
     * range ({@code n < 0 || n >= size()}).
     */
    public String tag(int n) {
        return mTags.get(n);
    }

    /**
     * Return an immutable view of the tokens for this tagging.
     *
     * @return The tokens for this tagging.
     */
    public List<E> tokens() {
        return Collections.unmodifiableList(mTokens);
    }

    /**
     * Return an immutable list of tags for this tagging.
     */
    public List<String> tags() {
        return Collections.unmodifiableList(mTags);
    }

    /**
     * Return a string-based representation of this tagging.
     *
     * @return String-based representation of this tagging.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size(); ++i) {
            if (i > 0) sb.append(" ");
            sb.append(token(i) + "/" + tag(i));
        }
        return sb.toString();
    }

}

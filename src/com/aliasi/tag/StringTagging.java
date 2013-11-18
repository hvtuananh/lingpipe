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
 * A {@code StringTagging} is a tagging over string-based tokens
 * that indexes each token to a position in an underlying character
 * sequence.
 *
 * <p>Because tokenizers may normalize inputs, the underlying
 * characters between a token's start and end are not necessarily
 * equivalent to the token itself.  That is, {@code token(n)} does not
 * need to be equal to {@code
 * characters().substring(tokenStart(n),tokenEnd(n))}.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe3.9
 */
public class StringTagging extends Tagging<String> {

    private final String mCs;
    private final int[] mTokenStarts;
    private final int[] mTokenEnds;

    /**
     * Construct a string tagging from the specified string-based
     * tokens and tags, an underlying character sequence, and arrays
     * representing the position at which each token starts and ends.
     *
     * <p>The lists and arrays are copied, and the character sequence
     * converted to a string.  Subsequent changes to these arguments
     * will not affect the constructed tagging.
     *
     * @param tokens List of strings representing token inputs.
     * @param tags List of strings representing tag outputs, parallel to {@code tags}.
     * @param cs Underlying character sequence.
     * @param tokenStarts Starting positions of tokens, parallel to {@code tokens}.
     * @param tokenEnds Ending positions of tokens, parallel to {@code tokens}.
     * @throws IllegalArgumentException If the list of tokens, list of tags,
     * token starts, and token ends are not all the same length, or if a token
     * start/end index is not possible for the underlying characters.
     */
    public StringTagging(List<String> tokens,
                         List<String> tags,
                         CharSequence cs,
                         int[] tokenStarts,
                         int[] tokenEnds) {
        super(tokens,tags);
        if (tokenStarts.length != tokens.size()) {
            String msg = "Token starts must be same length as tokens."
                + " Found tokens.size()=" + tokens.size()
                + " tokenStarts.length=" + tokenStarts.length;
            throw new IllegalArgumentException(msg);
        }
        if (tokenEnds.length != tokens.size()) {
            String msg = "Token ends must be same length as tokens."
                + " Found tokens.size()=" + tokens.size()
                + " tokenEnds.length=" + tokenEnds.length;
            throw new IllegalArgumentException(msg);
        }
        for (int n = 0; n < tokenStarts.length; ++n) {
            if (tokenStarts[n] > tokenEnds[n]) {
                String msg = "Tokens must start before they end."
                    + " tokenStarts[" + n + "]=" + tokenStarts[n]
                    + " tokenEnds[n" + n + "]=" + tokenEnds[n];
                throw new IllegalArgumentException(msg);
            }
        }
        for (int n = 1; n < tokenStarts.length; ++n) {
            if (tokenStarts[n-1] > tokenStarts[n]) {
                String msg = "Token starts must be in ascending order."
                    + " Found tokenStarts[" + (n-1) + "]=" + tokenStarts[n-1]
                    + " tokenStarts[" + n + "]=" + tokenStarts[n];
                throw new IllegalArgumentException(msg);
            }
            if (tokenEnds[n-1] > tokenEnds[n]) {
                String msg = "Token ends must be in ascending order."
                    + " Found tokenEnds[" + (n-1) + "]=" + tokenEnds[n-1]
                    + " tokenEnds[" + n + "]=" + tokenEnds[n];
                throw new IllegalArgumentException(msg);
            }
        }
        if (tokenStarts.length > 0) {
            if (tokenStarts[0] < 0) {
                String msg = "Token starts must be >= 0."
                    + " Found tokenStarts[" + 0 + "]=" + tokenStarts[0];
                throw new IllegalArgumentException(msg);
            }
            if (tokenEnds[tokenEnds.length-1] > cs.length()) {
                String msg = "Tokens must fall within span of chars."
                    + " Found cs=" + cs
                    + " cs.length()=" + cs.length()
                    + " tokenEnds[" + (tokenEnds.length-1) + "]=" 
                    + tokenEnds[tokenEnds.length-1];
                throw new IllegalArgumentException(msg);
            }
        }
        mCs = cs.toString();
        mTokenStarts = tokenStarts.clone();
        mTokenEnds = tokenEnds.clone();
    }

    /**
     * Construct a string tagging from the specified string-based
     * tokens and tags, an underlying character sequence, and lists
     * representing the position at which each token starts and ends.
     *
     * <p>The lists are copied, and the character sequence converted
     * to a string.  Subsequent changes to these arguments will not
     * affect the constructed tagging.
     *
     * @param tokens List of strings representing token inputs.
     * @param tags List of strings representing tag outputs, parallel to {@code tags}.
     * @param cs Underlying character sequence.
     * @param tokenStarts Starting positions of tokens, parallel to {@code tokens}.
     * @param tokenEnds Ending positions of tokens, parallel to {@code tokens}.
     * @throws IllegalArgumentException If the list of tokens, list of tags,
     * token starts, and token ends are not all the same length, or if a token
     * start/end index is not possible for the underlying characters.
     */
    public StringTagging(List<String> tokens,
                         List<String> tags,
                         CharSequence cs,
                         List<Integer> tokenStarts,
                         List<Integer> tokenEnds) {
        // duplicate copy of token starts and ends
        this(tokens,tags,cs,toArray(tokenStarts),toArray(tokenEnds));
    }

    static int[] toArray(List<Integer> xs) {
        int[] result = new int[xs.size()];
        for (int i = 0; i < xs.size(); ++i)
            result[i] = xs.get(i);
        return result;
    }

    // no checks
    StringTagging(String s,
                  List<String> tokens,
                  List<String> tags,
                  int[] tokenStarts,
                  int[] tokenEnds,
                  boolean ignore) {
        super(tokens,tags,ignore);
        mCs = s;
        mTokenStarts = tokenStarts;
        mTokenEnds = tokenEnds;
    }

    /**
     * Return the character offfset of the start of the token in
     * the specified input position in the underlying characters.
     *
     * @param n Position of token in input token list.
     * @return Character offset of first character in the token
     * in the underlying characters.
     * @throws IndexOutOfBoundsException If the position is not between 0
     * (inclusive) and the number of tokens (exclusive).
     */
    public int tokenStart(int n) {
        return mTokenStarts[n];
    }

    /**
     * Return the character offset of the end of the token in the
     * specified input position in the underlying characters.
     *
     * @param n Position of token in input token list.
     * @return Character offset of last character plus 1 in the token
     * in the underlying characters.
     * @throws IndexOutOfBoundsException If the position is not between 0
     * (inclusive) and the number of tokens (exclusive).
     */
    public int tokenEnd(int n) {
        return mTokenEnds[n];
    }

    /**
     * Return the string underlying the token in the specified
     * position.
     *
     * @param n Token input position.
     * @return Underlying token string.
     * @throws IndexOutOfBoundsException If the position is not between 0
     * (inclusive) and the number of tokens (exclusive).
     */
    public String rawToken(int n) {
        return mCs.substring(tokenStart(n),tokenEnd(n));
    }

    /**
     * Returns the characters underlying this string tagging.
     * 
     * @return Underlying character string.
     */
    public String characters() {
        return mCs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mCs);
        sb.append('\n');
        for (int i = 0; i < size(); ++i) {
            if (i > 0) sb.append(" ");
            sb.append(token(i) + "/" + tag(i) + "//" + rawToken(i) + "///@(" + tokenStart(i) + "," + tokenEnd(i) + ")");
        }
        return sb.toString();
    }

    /**
     * Returns {@code true} if the specified object is a string
     * tagging that's structurally identical to this tagging.
     * For taggings to be identical, their underlying strings must
     * be equal, all tags and tokens must be equal, and all token
     * starts and ends must be equal.
     *
     * @param that Object to compare to this tagging.
     * @return {@code true} if the specified object is a string
     * tagging equal to this tagging.
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof StringTagging)) 
            return false;
        StringTagging thatTagging = (StringTagging) that;
        if (!characters().equals(thatTagging.characters())) 
            return false;
        if (size() != thatTagging.size())
            return false;
        for (int n = 0; n < size(); ++n) {
            if (!token(n).equals(thatTagging.token(n)))
                return false;
            if (!tag(n).equals(thatTagging.tag(n)))
                return false;
            if (tokenStart(n) != thatTagging.tokenStart(n))
                return false;
            if (tokenEnd(n) != thatTagging.tokenEnd(n))
                return false;
        }
        return true;
    }

    /**
     * Returns a hash code computed from the underlying string and
     * tags.
     *
     * The hash code is computed for a size N tagging as:
     *
     * <blockquote><pre>
     * 31**N * characters().hashCode()
     *   + 31**(N-1) * token(N-1).hashCode()
     *   + 31**(N-2) * token(N-2).hashCode()
     *   + ...
     *   + 31**1 * token(1).hashCode()
     *   + 31**0 * token(0).hashCode()
     * </pre></blockquote>
     *
     * <p>This hash code is consistent with equality.
     *
     * @return Hash code for this string tagging.
     */
    @Override
    public int hashCode() {
        int c = characters().hashCode();
        for (int n = 0; n < size(); ++n)
            c = 31 * c + tag(n).hashCode();
        return c;
    }

 

}
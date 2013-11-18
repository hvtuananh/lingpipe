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

import com.aliasi.util.Iterators;
import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The abstract class {@code Tokenizer} serves as a base for tokenizer
 * implementations, which provide streams of tokens, whitespaces,
 * and positions.
 *
 * <p> A tokenizer acts as an iterator over both space and token
 * streams.  The next space is returned through {@link
 * #nextWhitespace()}, and the next token through {@link
 * #nextToken()}.  Some tokenizers may implement {@link
 * #lastTokenStartPosition()}, which returns the offset of the
 * previous token's first character in an underlying character stream.
 *
 * <p>Tokenizers implement the {@link Iterable} interface to allow
 * easy iteration over just the tokens using for-each loops.
 *
 * <p> The entire underlying character sequence may be reconstructed by
 * alternating the next whitespace and next token, beginning with the
 * first whitespace, until the end of both are reached.  Offsets
 * returned by {@link #lastTokenStartPosition} are not guaranteed to
 * be into this sequence of characters.
 * </p>
 *
 * <p> Concrete subclasses must implement {@link #nextToken()} to
 * return the next token.  They may override {@link #nextWhitespace()}
 * to return the next space string; it is implemented in this class to
 * return a single space {@link Strings#SINGLE_SPACE_STRING}.
 * Subclasses may also implement {@link #lastTokenStartPosition()},
 * which otherwise will throw an
 * <code>UnsupportedOperationException</code>.
 * </p>
 *
 * @author  Bob Carpenter
 * @version 3.8.1
 * @since   LingPipe1.0
 */
public abstract class Tokenizer implements Iterable<String> {

    /**
     * Construct a tokenizer.
     */
    public Tokenizer() {
        /* do nothing */
    }

    /**
     * Returns an iterator over the tokens remaining in this
     * tokenizer.
     *
     * <p>The returned iterator is not thread safe with respect to the
     * underlying tokenizer.  Specifically, it maintains a handle to
     * this tokenizer.  Calls to the iterators <code>hasNext()</code> and
     * <code>nextToken()</code> methods call this tokenizers
     * <code>nextToken()</code> method.
     *
     * @return An iterator over the tokens remaining in this
     * tokenizer.
     */
    public Iterator<String> iterator() {
        return new TokenIterator();
    }

    /**
     * Returns the next token in the stream, or <code>null</code> if
     * there are no more tokens.  Flushes any whitespace that has
     * not been returned.
     *
     * @return The next token, or <code>null</code> if there are no
     * more tokens.
     */
    public abstract String nextToken();

    /**
     * Returns the next whitespace.  Returns the same result for
     * subsequent calls without a call to <code>nextToken</code>.
     *
     * <p>The default implementation in this class is to return
     * a single space, {@link Strings#SINGLE_SPACE_STRING}.
     *
     * @return The next space.
     */
    public String nextWhitespace() {
        return Strings.SINGLE_SPACE_STRING;
    }

    /**
     * Returns the offset of the first character of the most recently
     * returned token (optional operation).  A tokenizer should return
     * <code>-1</code> if no token has been returned yet.
     *
     * <p>The position returned is relative to the beginning of the
     * slice of the character array being tokenized, not the beginning
     * of the array itself.
     *
     * <P>The implementation here simply throws an unsupported
     * operation exception. Subclasses should override this method if
     * they support character offset indexing.
     *
     * @return The character offset of the first character of the most
     * recently returned token, or {@code -1} if not token has yet
     * been returned.
     * @throws UnsupportedOperationException If this method is not
     * supported.
     */
    public int lastTokenStartPosition() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the offset of one position past the last character of
     * the most recently returned token (optional operation).  A
     * tokenizer should return <code>-1</code> if no token has been
     * returned yet.
     *
     * <p>The position returned is relative to the beginning of the
     * slice of the character array being tokenized, not the beginning
     * of the array itself.
     *
     * <p>The implementation here throws an unsupported operation
     * exception.  Subclasses should override this method to
     * support offset indexing.
     *
     * @return One plus the offset of the last character of the most
     * recently returned token, or {@code -1} if not token has yet
     * been returned.
     * @throws UnsupportedOperationException If the method is not supported.
     */
    public int lastTokenEndPosition() {
        throw new UnsupportedOperationException();
    }


    /**
     * Adds the remaining tokens and whitespaces to the specified
     * lists.
     *
     * @param tokens List to which tokens are added.
     * @param whitespaces List to which whitespaces are added.
     */
    public void tokenize(List<? super String> tokens,
                         List<? super String> whitespaces) {
        whitespaces.add(nextWhitespace());
        String token;
        while ((token = nextToken()) != null) {
            tokens.add(token.toString());
            whitespaces.add(nextWhitespace().toString());
        }
    }

    /**
     * Returns the remaining tokens in an array of strings.  If called
     * first, this returns all of the tokens produced by this
     * tokenizer. Flushes all remaining whitespace.
     *
     * @return Array of tokens remaining in this tokenizer.
     */
    public String[] tokenize() {
        List<String> tokenList = new ArrayList<String>();
        String token;
        while ((token = nextToken()) != null)
            tokenList.add(token);
        return tokenList.<String>toArray(Strings.EMPTY_STRING_ARRAY);
    }

    class TokenIterator extends Iterators.Buffered<String> {
        @Override
        public String bufferNext() {
            return nextToken();
        }
    }

}

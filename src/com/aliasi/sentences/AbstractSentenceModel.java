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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An <code>AbstractSentenceModel</code> implements a sentence model
 * in terms of the method that adds indices to the collection.
 * Subclasses must only implement the method {@link
 * #boundaryIndices(String[],String[],int,int,Collection)}, which adds
 * an <code>Integer</code> to the collection for each token index
 * which is sentence final.
 *
 * <P>The abstract model also adds
 * the method {@link #boundaryIndices(String[],String[],int,int)} for
 * operating over slices of parallel arrays of tokens and whitespaces.
 *
 * <P>The static utility methods {@link #verifyBounds(String[],String[],int,int)}
 * and {@link #verifyTokensWhitespaces(String[],String[])} may be used
 * by subclasses to verify that the inputs to the sentence detection
 * methods are legal.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
public abstract class AbstractSentenceModel implements SentenceModel {

    /**
     * Construct an abstract sentence model.  Because this class is
     * abstract, this constructor may only be called by subclasses.
     */
    protected AbstractSentenceModel() {
        /* do nothing */
    }

    /**
     * Returns an array of indices of sentence-final tokens.
     *
     * <P>If this method is overridden in a subclass, the static
     * method {@link #verifyTokensWhitespaces(String[],String[])} should
     * be called to verify that the arguments are valid.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @return Array of integers indicating indices of tokens that
     * are sentence final.
     * @throws IllegalArgumentException If the array of whitespaces is
     * not one element longer than the array of tokens.
     */
    public int[] boundaryIndices(String[] tokens, String[] whitespaces) {
    verifyTokensWhitespaces(tokens,whitespaces);
        return boundaryIndices(tokens,whitespaces,0,tokens.length);
    }

    /**
     * Returns an array of indices of sentence-final tokens
     * for the slice of the token and whitespace arrays specified.
     *
     * <P>If this method is overridden in a subclass, the static
     * method {@link #verifyBounds(String[],String[],int,int)} should
     * be called by implementations to verify that the arguments are
     * valid.
     *
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param length Number of tokens to annotate.
     * @return Array of integers indicating indices of tokens that
     * are sentence final.
     * @throws IllegalArgumentException If the array of whitespaces is
     * not one element longer than the array of tokens.
     */
    public int[] boundaryIndices(String[] tokens, String[] whitespaces,
                                 int start, int length) {
    verifyBounds(tokens,whitespaces,start,length);
        List<Integer> boundaries = new ArrayList<Integer>();
        boundaryIndices(tokens,whitespaces,start,length,boundaries);
        int[] result = new int[boundaries.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = boundaries.get(i).intValue();
        return result;
    }

    /**
     * Adds the sentence final token indices as <code>Integer</code>
     * instances to the specified collection, only considering tokens
     * between index <code>start</code> and <code>end-1</code>
     * inclusive.
     *
     * <P>The abstract implementations provide an array list for
     * the collection of indices, and results will be returned in
     * the order they are added to the collection.
     *
     * <P>The static method {@link
     * #verifyBounds(String[],String[],int,int)} should be called by
     * implementations to verify that the arguments are valid.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param length Number of tokens to annotate.
     * @param indices Collection into which to write the boundary
     * indices.
     * @throws IllegalArgumentException If the array of whitespaces
     * and tokens is not long enough for the start and length parameters.
     */
    public abstract void boundaryIndices(String[] tokens,
                                         String[] whitespaces,
                                         int start, int length,
                                         Collection<Integer> indices);


    /**
     * Throws an illegal argument exception if the arrays of tokens is
     * not as long as the start plus length, or if the array of
     * whitespaces is not at least one element longer than the start
     * plus length.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @param start Index of first token to annotate.
     * @param length Number of tokens to annotate.
     * @throws IllegalArgumentException If the array of whitespaces
     * and tokens is not long enough for the start and length parameters.
     */
    protected static void verifyBounds(String[] tokens, String[] whitespaces,
                       int start, int length) {
    if (tokens.length < start+length) {
        String msg = "Token array must be at least as long as start+length."
        + " tokens.length=" + tokens.length
        + " start=" + start
        + " length=" + length;
        throw new IllegalArgumentException(msg);
    }
    if (whitespaces.length <= start+length) {
        String msg = "Whitespace array must be longer than start+length."
        + " whitespaces.length=" + whitespaces.length
        + " start=" + start
        + " length=" + length;
        throw new IllegalArgumentException(msg);
    }
    }


    /**
     * Throws an illegal argument exception if the array of whitespaces
     * is not one longer than the array of tokens.
     *
     * @param tokens Array of tokens to annotate.
     * @param whitespaces Array of whitespaces to annotate.
     * @throws IllegalArgumentException If the array of whitespaces
     * is not one longer than the array of tokens.
     */
    protected static void verifyTokensWhitespaces(String[] tokens,
                          String[] whitespaces) {
    if (whitespaces.length == tokens.length+1) return;
    String msg = "Whitespaces array must be one longer than tokens."
        + " Found tokens.length=" + tokens.length
        + " whitespaces.length=" + whitespaces.length;
    throw new IllegalArgumentException(msg);
    }



}

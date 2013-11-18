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

package com.aliasi.chunk;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.TokenCategorizer;

import java.io.IOException;
import java.io.ObjectInput;

import java.util.ArrayList;
import java.util.List;

/**
 * A compiled estimator is constructed by reading a binary model
 * compiled by a trainable estimator from a data input stream.  The
 * estimator may then be used to estimate instances of <code>log
 * P(Tag,Token|Tag-1,Token-1,Token-2)</code> using the {@link
 * #estimate(int,int,int,int,int)} method, where the integer values
 * are identifers of the associated symbols in the appropriate (tag or
 * token) symbol table.  The symbol tables are stored in the compiled
 * estimator.  Various operations on tags as identifiers are
 * precomputed and supplied by methods in this class.
 *
 * <p> The components of a compiled estimator is stored in the
 * following order.
 *
 * <br/><br/>
 * <table cellpadding="5" border="1">
 * <tr><td width="25%"><b>Variable</b></td>
 *     <td width="25%"><b>Type</b></td>
 *     <td width="50%"><b>Description</b></td></tr>
 * <tr><td><code>tagSymbolTable</code></td>
 *     <td><code>SymbolTable</code></td>
 *     <td>Symbol table for tags.</td></tr>
 * <tr><td><code>tokenSymbolTable</code></td>
 *     <td><code>SymbolTable</code></td>
 *     <td>Symbol table for tokens.</td></tr>
 * <tr><td><code>tagTrie</code></td>
 *     <td><code>EstimatorTrie</code></td>
 *     <td>Estimator trie for tags.</td></tr>
 * <tr><td><code>tokenTrie</code></td>
 *     <td><code>EsitmatorTrie</code></td>
 *     <td>Estimator trie for tokens.</td></tr>
 * <tr><td><code>logVocabEstimate</code></td>
 *     <td><code>double</code></td>
 *     <td>Estimate of log likelihood of a token.</td></tr>
 * </table>
 * <br/>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 */
final class CompiledEstimator {

    /**
     * A trie of estimates and backoffs for <code>log
     * P(Tag|Tag-1,Token-1,Token-2)</code>.
     */
    private final EstimatorTrie mTagTrie;

    /**
     * A trie of estimates and backoffs for <code>log
     * P(Token|Tag,Tag-1,Token-1)</code>.
     */
    private final EstimatorTrie mTokenTrie;

    /**
     * A symbol table for tags.
     */
    private final SymbolTable mTagSymbolTable;

    /**
     * The symbol table for tokens.
     */
    private final SymbolTable mTokenSymbolTable;

    /**
     * <code>mCannotFollow[tagID][tagMinus1ID]</code> is true
     * if and only if a tag with identifier <code>tagID</code> can follow
     * a tag of identifier <code>tagMinus1ID</code>.
     */
    private final boolean[][] mCannotFollow;

    /**
     * <code>mConvertToInterior[tagID]</code> is the interior
     * tag ID with the same base tag as <code>tagID</code>.
     */
    private final int[] mConvertToInterior;

    /**
     * Array of start tag identifiers; tags are all prefixed by "ST_".
     * Does not include "OUT" tag.
     */
    private final int[] mStart;

    /**
     * Array of interior tag identifiers; tags not prefixed by "ST_".
     */
    private final int[] mInterior;

    /**
     * Natural log of the uniform vocabulary estimate for this
     * estimator.
     */
    private final double mLogUniformVocabEstimate;

    /**
     * Categorizer to provide token categories for smoothed
     * estimates and unknown estimates.
     */
    private final TokenCategorizer mTokenCategorizer;

    /**
     * Construct a compiled estimator from a data input stream and
     * sets the log estimate of the uniform vocabulary likelihood for
     * smoothing the token model.
     *
     * @param in Data input stream from which to read the estimator.
     * @param categorizer Token categorizer to classify tokens.
     *
     * @throws IOException If there is an I/O exception reading from
     * the data input stream.
     */
    public CompiledEstimator(ObjectInput in)
        throws ClassNotFoundException, IOException {

        mTokenCategorizer = (TokenCategorizer) in.readObject();

        mTagSymbolTable = (SymbolTable) in.readObject();
        mTokenSymbolTable = (SymbolTable) in.readObject();

        // read from model & put in training
        mTagTrie = new EstimatorTrie(in);
        mTokenTrie = new EstimatorTrie(in);
        mLogUniformVocabEstimate = in.readDouble();

        int numSymbols = mTagSymbolTable.numSymbols();
        mConvertToInterior = new int[numSymbols];
        mCannotFollow = new boolean[numSymbols][numSymbols];
        int numTags = mTagSymbolTable.numSymbols();
        List<Integer> starts = new ArrayList<Integer>();
        List<Integer> interiors = new ArrayList<Integer>();
        for (int tagID = 0; tagID < numTags; ++tagID) {
            String tag = idToTag(tagID);
            mConvertToInterior[tagID] = tagToInteriorID(tag);
            if (tagID != mConvertToInterior[tagID]) {
                interiors.add(Integer.valueOf(mConvertToInterior[tagID]));
                starts.add(Integer.valueOf(tagID));
            }
            for (int tagMinus1ID = 0; tagMinus1ID < numTags; ++tagMinus1ID)
                mCannotFollow[tagID][tagMinus1ID]
                    = Tags.illegalSequence(idToTag(tagMinus1ID),tag);
        }
        mStart = convertToIntArray(starts);
        mInterior = convertToIntArray(interiors);
    }

    /**
     * Returns the array of start tag IDs.  The array returned is
     * aligned with the interior tag IDs returned by {@link
     * #interiorTagIDs()}.
     *
     * @return Array of identifiers for start tags.
     */
    public int[] startTagIDs() {
        return mStart;
    }

    /**
     * Returns the array of interior tag IDs.  The array returned is
     * aligned with the start tag IDs returned by {@link
     * #startTagIDs()}.
     *
     * @return Array of identifiers for interior tags.
     */
    public int[] interiorTagIDs() {
        return mInterior;
    }

    /**
     * Returns number of possible tags produced by this estimator,
     * including both base and start forms of tags and the
     * distinguished out tag.
     *
     * @return Number of possible tags produced by this estimator.
     */
    public int numTags() {
        return mTagSymbolTable.numSymbols();
    }

    /** Maps a tag to its integer identifier or <code>-1</code> if it
     * is not in the table.
     * @param tag Name of tag.
     * @return Integer identifier for the specified tag or <code>-1</code> if it is not in the table.
     */
    public int tagToID(String tag) {
        return mTagSymbolTable.symbolToID(tag);
    }

    /**
     * Maps a tag identifier to the name of that tag.  Throws an array out of
     * bounds exception if the identifier does not exist in the table.
     *
     * @param id Identifier of the tag to return.
     * @return Name of the tag identified by the specified identifier.
     */
    public String idToTag(int id) {
        return mTagSymbolTable.idToSymbol(id);
    }

    /**
     * Maps a token to its integer identifier or <code>-1</code> if it
     * is not in the table.
     *
     * @param token Name of token.
     * @return Integer identifier for the specified token or <code>-1</code> if it is not in the table.
     */
    public int tokenToID(String token) {
        return mTokenSymbolTable.symbolToID(token);
    }

    /**
     * Maps a token to its integer identifier if it is in the symbol
     * table, or to the identifier of its token category.
     *
     * @param token Token to compute ID for.
     * @return Identifier of token if it exists, or identifier of its
     * category if nothing is known about the token.
     */
    public int tokenOrCategoryToID(String token) {
        int id = tokenToID(token);
        if (id < 0) {
            id = tokenToID(mTokenCategorizer.categorize(token));
            if (id < 0) {
                System.err.println("No id for token category: " + token);
            }
        }
        return id;
    }

    /**
     * Maps an integer identifier to the token it represents
     * in the token symbol table.
     *
     * @param id Identifier of the token.
     * @return Token with specified identifier in the token symbol
     * table.
     */
    public String idToToken(int id) {
        return mTokenSymbolTable.idToSymbol(id);

    }

    /**
     * Returns <code>true</code> if the tag identified by the first
     * identifier cannot follow the tag identified by the second
     * identifier.
     *
     * @param tagID Identifier of tag.
     * @param tagMinus1ID Identifier of preceding tag.
     * @return <code>true</code> if the tag for <code>tagID</code>
     * cannot follow the tag for <code>tagMinus1ID</code>.
     */
    public boolean cannotFollow(int tagID, int tagMinus1ID) {
        return mCannotFollow[tagID][tagMinus1ID];
    }

    /**
     * Returns the identifier for the base tag of
     * the tag picked out by the specified identifier.
     *
     * @param tagID Identifier of tag to convert to base form.
     * @return Identifier of the base form of the tag picked out by
     * the specified identifier.
     */
    private int idToInteriorID(int tagID) {
        return mConvertToInterior[tagID];
    }


    /**
     * Returns <code>log P(tag,token|tag-1,token-1,token-2)</code>,
     * where information about the tags and tokens are supplied
     * through symbol table identifiers.
     *
     * @param tagID Identifier of outcome tag to estimate along with
     * the token.
     * @param tokenID Identifier of outcome token to estimate along
     * with the tag.
     * @param tagMinus1ID Identifier of the previous tag.
     * @param tokenMinus1ID Identifier of the previous token.
     * @param tokenMinus2ID Token two back from token.
     * @return <code>log P(tag,token|tag-1,token-1,token-2)</code>.
     */
    public double estimate(int tagID, int tokenID,
                           int tagMinus1ID,
                           int tokenMinus1ID,
                           int tokenMinus2ID) {
        if (cannotFollow(tagID,tagMinus1ID)) return Double.NaN;
        int tagMinus1IDInterior = idToInteriorID(tagMinus1ID);
        return estimateTag(tagID,tagMinus1IDInterior,
                           tokenMinus1ID,tokenMinus2ID)
            + estimateToken(tokenID,tagID,tagMinus1IDInterior,tokenMinus1ID);
    }

    /**
     * Return <code>log P(tag|tag-1,token-1,token-2)</code>.  Returns
     * <code>Double.NaN</code> when nothing is known about
     * <code>tag-1</code>.
     *
     * @param tagID Identifier of outcome tag to estimate along with
     * the token.
     * @param tagMinus1ID Identifier of the previous tag.
     * @param tokenMinus1ID Identifier of the previous token.
     * @param tokenMinus2ID Token two back from token.
     * @return <code>log P(tag|tag-1,token-1,token-2)</code>.
     */
    private double estimateTag(int tagID,
                               int tagMinus1ID,
                               int tokenMinus1ID,
                               int tokenMinus2ID) {
        // find most specific node matching context,
        // then lookup estimate from there
        // estimating from node follows backoffs,
        // adding 1-lambda from current context as necessary
        int nodeTag1Index = mTagTrie.lookupChild(tagMinus1ID,0);
        if (nodeTag1Index == -1) {
            // no outcomes for simple tag -- really an error
            return Double.NaN;
        }
        int nodeTag1W1Index
            = mTagTrie.lookupChild(tokenMinus1ID,nodeTag1Index);
        if (nodeTag1W1Index == -1) {
            return mTagTrie.estimateFromNode(tagID,nodeTag1Index);
        }
        int nodeTag1W1W2Index
            = mTagTrie.lookupChild(tokenMinus2ID,nodeTag1W1Index);
        if (nodeTag1W1W2Index == -1) {
            return mTagTrie.estimateFromNode(tagID,nodeTag1W1Index);
        }
        return mTagTrie.estimateFromNode(tagID,nodeTag1W1W2Index);
    }

    /**
     * Return <code>log P(token|tag,tag-1,token-1)</code>, where
     * information about the tags and tokens are supplied through
     * symbol table identifiers.  Return <code>Double.NaN</code> if
     * nothign is known about <code>tag</code>.
     *
     * @param tokenID Identifier of outcome token to estimate along
     * with the tag.
     * @param tagID Identifier of outcome tag to estimate along with
     * the token.
     * @param tagMinus1ID Identifier of the previous tag.
     * @param tokenMinus1ID Identifier of the previous token.
     * @return  <code>log P(token|tag,tag-1,token-1)</code>.
     */
    private double estimateToken(int tokenID,
                                 int tagID, int tagMinus1ID,
                                 int tokenMinus1ID) {
        int nodeTagIndex = mTokenTrie.lookupChild(tagID,0);
        if (nodeTagIndex == -1)
            return Double.NaN;
        int nodeTagTag1Index = mTokenTrie.lookupChild(tagMinus1ID,nodeTagIndex);
        if (nodeTagTag1Index == -1) {
            return
                mTokenTrie.estimateFromNodeUniform(tokenID,
                                                   nodeTagIndex,
                                                   mLogUniformVocabEstimate);
        }
        int nodeTagTag1W1Index
            = mTokenTrie.lookupChild(tokenMinus1ID,nodeTagTag1Index);
        if (nodeTagTag1W1Index != -1) {
            return
                mTokenTrie.estimateFromNodeUniform(tokenID,
                                                   nodeTagTag1W1Index,
                                                   mLogUniformVocabEstimate);
        }
        return mTokenTrie.estimateFromNodeUniform(tokenID,
                                                  nodeTagTag1Index,
                                                  mLogUniformVocabEstimate);
    }

    /**
     * Return the identifier for the base tag corresponding
     * to the specified tag.
     *
     * @param tag Tag whose base tag ID is returned.
     * @return Identifier for base tag of specified tag.
     */
    private int tagToInteriorID(String tag) {
        return tagToID(Tags.toInnerTag(tag));
    }

    /**
     * Convert the array list of <code>Integer</code> objects to an
     * array of their integer values.
     *
     * @param xs Arraylist of Integer objects.
     * @return Array of integer values for the specified array of
     * objects.
     */
    private static int[] convertToIntArray(List<Integer> xs) {
        int[] result = new int[xs.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = xs.get(i).intValue();
        return result;
    }

}

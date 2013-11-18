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

package com.aliasi.suffixarray;

import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A {@code DocumentTokenSuffixArray} implements a suffix array over a
 * collection of named documents. 
 *
 * <h3>How it Works</h3>
 *
 * The basic idea is that the documents are concatenated and then
 * stored in a token suffix array.  This class provides methods for
 * extracting the document given a position in the suffix array.
 *
 * <p>The documents are concatenated with a specified distinguished
 * token as a separator.  The separator acts as an end-of-document
 * marker that terminates comparisons.  
 *
 * <p>A document suffix array is constructed from a mapping of
 * identifiers to documents.  A tokenizer factory and separator are
 * also provided. 
 *
 * <p>The underlying suffix array may be retrieved using {@link
 * #suffixArray()} and manipulated as any other token-based suffix
 * array.  The method {@link #textPositionToDocId(int)} provides
 * the means to map a position in the underlying token array to
 * the document that spans the positions.
 *
 * @author Bob Carpenter
 * @version 4.1.0
 * @since LingPipe 4.0.2
 */
public class DocumentTokenSuffixArray {

    private final TokenSuffixArray mTsa;
    private final int[] mDocStarts;
    private final String[] mDocIds;
    private final Map<String,Integer> mDocIdToIndex;

    /**
     * Construct a suffix array from the specified identified document
     * collection using the specified tokenizer factory, limiting comparisons
     * to the specified maximum suffix length and separating documents with
     * the specified boundary token.
     *
     * <p>For this class to work properly, the tokenizer factory must
     * tokenize the document boundary token into a single token when
     * surrounded by spaces.
     *
     * @param idToDocMap Mapping from document identifiers to document
     * texts.
     * @param tf Tokenizer factory to use for matching.
     * @param maxSuffixLength Maximum suffix length (in tokens) for
     * comparsions.
     * @param documentBoundaryToken Distinguished token used to separate
     * documents.
     * @throws IllegalArgumentException If the tokenizer factory does not
     * tokenize the document boundary token surrounded by single whitespaces
     * into a single token consisting of the boundary token.
     * // raise exception if find boundary in tokens of doc?
     */
    public DocumentTokenSuffixArray(Map<String,String> idToDocMap,
                                    TokenizerFactory tf,
                                    int maxSuffixLength,
                                    String documentBoundaryToken) {
        String test = " " + documentBoundaryToken + " ";
        String[] test_tokens = tf.tokenizer(test.toCharArray(),0,test.length()).tokenize();
        if (test_tokens.length != 1 || !test_tokens[0].equals(documentBoundaryToken)) {
            String msg = "Tokenizer factory must convert boundary token to self."
                + " Found documentBoundaryToken=|" + documentBoundaryToken + "|"
                + " tokenizerFactory=" + tf
                + " result of tokenizing boundary token=|" + Arrays.asList(test_tokens) + "|";
            throw new IllegalArgumentException(msg);
        }

        mDocIds = idToDocMap.keySet().toArray(Strings.EMPTY_STRING_ARRAY);
        Arrays.sort(mDocIds);

        List<Integer> docStarts = new ArrayList<Integer>(idToDocMap.size());
        mDocIdToIndex = new HashMap<String,Integer>(idToDocMap.size());
        Set<Map.Entry<String,String>> entrySet = idToDocMap.entrySet();
        int token_pos = 0;
        int total_chars = 0;
        int count = 0;
        for (String id : mDocIds) {
            String text = idToDocMap.get(id);
            mDocIdToIndex.put(id,count++);
            docStarts.add(token_pos);
            token_pos += tokenCount(tf,text) + 1; 
            total_chars += text.length() + documentBoundaryToken.length() + 2;
        }

        mDocStarts = new int[docStarts.size()];
        for (int i = 0; i < mDocStarts.length; ++i)
            mDocStarts[i] = docStarts.get(i);

        // block uses less memory than StringBuilder w. append().
        char[] cs = new char[total_chars];
        int char_pos = 0;
        for (String id : mDocIds) {
            String text = idToDocMap.get(id);
            for (int i = 0; i < text.length(); ++i)
                cs[char_pos++] = text.charAt(i);
            cs[char_pos++] = ' ';
            for (int i = 0; i < documentBoundaryToken.length(); ++i)
                cs[char_pos++] = documentBoundaryToken.charAt(i);
            cs[char_pos++] = ' ';
        }

        Tokenization tokenization
            = new Tokenization(cs,0,cs.length,tf);
        mTsa = new TokenSuffixArray(tokenization,maxSuffixLength,
                                    documentBoundaryToken);
    }

    /**
     * Return the token suffix array backing this document suffix
     * array.
     *
     * @return Underlying suffix array.
     */
    public TokenSuffixArray suffixArray() {
        return mTsa;
    }

    /**
     * Return the identifier of the document that contains
     * the specified position in the underlying text.
     *
     * @param textPosition Position in underlying list of concatenated
     * documents.
     * @return Position
     */
    public String textPositionToDocId(int textPosition) {
        if (textPosition < 0 || textPosition > mTsa.tokenization().text().length()) {
            String msg = "Position must be >= 0 and <= text.length="
                + mTsa.tokenization().text().length()
                + " Found textPosition=" + textPosition;
            throw new IndexOutOfBoundsException(msg);
        }
        return mDocIds[largestWithoutGoingOver(mDocStarts,textPosition)];
    }


    /**
     * Return the text of the document with the specified name.
     * 
     * @param docName Name of document.
     * @return Text for that document.
     * @throws NullPointerException If the document name is not known.
     */
    public String documentText(String docName) {
        String boundaryToken = mTsa.documentBoundaryToken();
        String text = mTsa.tokenization().text();
        int idx = mDocIdToIndex.get(docName);
        int start = mDocStarts[idx];
        int boundaryEnd 
            = ((idx + 1) == mDocStarts.length)
            ? text.length() 
            : mDocStarts[idx+1];
        int end = boundaryEnd - boundaryToken.length() - 2;
        return text.substring(start,end);
    }

    /**
     * Returns the number of documents in the collection.
     *
     * @return Number of documents in the collection.
     */
    public int numDocuments() {
        return mDocStarts.length;
    }

    /**
     * Returns an unmodifiable view of the set of document names in
     * the collection.
     *
     * @return The set of document names.
     */
    public Set<String> documentNames() {
        return Collections.unmodifiableSet(mDocIdToIndex.keySet());
    }


    /**
     * Returns the starting token position in the underlying token
     * suffix array of the document with the specified identifier in
     * the overall set of documents.  Returns {@code -1} if the
     * document is not part of the collection.
     *
     * @param docId Document identifier.
     * @return Position of first token in document in the underlying
     * token suffix array.
     */
    public int docStartToken(String docId) {
        int idx = Arrays.binarySearch(mDocIds,docId);
        return idx < 0 ? -1 : mDocStarts[idx];
    }

    /**
     * Returns the index of the next token past the last token of the
     * specified document.  Returns {@code -1} if the document is not
     * part of the collection.
     *
     * @param docId Document identifier.
     * @return Position of first token in document in the underlying
     * token suffix array.
     */
    public int docEndToken(String docId) {
        int idx = Arrays.binarySearch(mDocIds,docId);
        if (idx < 0)
            return -1;
        int next_idx = idx + 1;
        if (next_idx == mDocIds.length)
            return Math.max(1,mTsa.suffixArrayLength() - 1);
        return Math.max(1,mDocStarts[idx+1] - 1);
    }
        

    /**
     * Given an increasing array of values and a specified value,
     * return the largest index into the array such that the array's
     * value at the index is smaller than or equal to the specified
     * value.  Returns -1 if there are no entries in the array less
     * than the specified value.
     *
     * <p><b>Warning:</b> No test is made that the values are in
     * increasing order.  If they are not, the behavior of this
     * method is not specified.
     * 
     * @param vals Array of values, sorted in ascending order.
     * @param val Specified value to search.
     */
    public static int largestWithoutGoingOver(int[] vals, 
                                              int val) {
        int start = 0;
        int end = vals.length;
        if (vals.length == 0)
            return -1;
        if (val < vals[start])
            return -1;
        if (val >= vals[end-1]) 
            return end - 1;
        // invariant: vals[start] <= val <= vals[end-1] 
        while (start + 1 < end) {
            int mid = (start + end) / 2;
            // invariant: start < mid < end
            if (val < vals[mid]) 
                end = mid;
            else if (val > vals[mid])
                start = mid;
            else
                return mid;
        }
        return start;
    }

    static int tokenCount(TokenizerFactory tf, String text) {
        int count = 0;
        for (String token : tf.tokenizer(text.toCharArray(),0,text.length()))
             ++count;
        return count;
    }



}
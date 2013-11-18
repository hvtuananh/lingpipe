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

package com.aliasi.lm;

import com.aliasi.stats.Model;

import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.IOException;

import java.util.Arrays;

/**
 * A <code>CompiledNGramProcessLM</code> implements a conditional
 * process language model.  Instances are constructed by reading a
 * serialized version of an instance of {@link NGramProcessLM} through
 * data input.
 *
 * <P>Compiled models contain precompulted estimates and smoothing
 * parameters.  For instance, consider an 3-gram process language model
 * trained on the string "abracadabra", which yields the following counts:
 *
 * <blockquote>
 * <pre>
 *    11
 *     a 5
 *       br 2
 *       ca 1
 *       da 1
 *    bra 2
 *    cad 1
 *    dab 1
 *    r 2
 *      a 2
 *         c 1
 * </pre>
 * </blockquote>
 *
 * For instance, <code>count("")=11</code>,
 * <code>count("a")=5</code>,
 * <code>count("ab")=2</code>,
 * <code>count("abr")=2</code>,
 * <code>count("br")=2</code>,
 * <code>count("bra")=2</code>,
 * <code>count("r")=2</code>, and
 * <code>count("ra")=2</code>, and
 * <code>count("rac")=1</code>.
 *
 * Assuming a lambda factor hyperparameter set to 4.0, the compiled
 * model consists of the following five parallel arrays:
 *
 * <blockquote>
 * <table border='1' cellpadding='5'>
 * <tr><td>&nbsp;</td>
 *     <td>&nbsp;</td>
 *     <td><tt>char (2)</tt></td>
 *     <td><tt>int (4)</tt></td>
 *     <td><tt>float (4)</tt></td>
 *     <td><tt>float (4)</tt></td>
 *     <td><tt>int (4)</tt></td></tr>
 * <tr><td><i>Index</i></td>
 *     <td><i>Context</i></td>
 *     <td><i>Char</i></td>
 *     <td><i>Suffix</i></td>
 *     <td><i>log<sub><sub>2</sub></sub> P(char|context)</i></td>
 *     <td><i>log<sub><sub>2</sub></sub> (1 - &lambda;(context.char))</i></td>
 *     <td><i>First Dtr</i></td></tr>
 <tr><td>0</td><td>n/a</td><td>n/a</td><td>n/a</td><td>n/a</td>
 <td>-0.6322682</td><td>1</td></tr>
 <tr><td>1</td><td>""</td><td>a</td><td>0</td><td>-2.6098135</td>
 <td>-0.4150375</td><td>6</td></tr>
 <tr><td>2</td><td>""</td><td>b</td><td>0</td><td>-3.8987012</td>
 <td>-0.5849625</td><td>9</td></tr>
 <tr><td>3</td><td>""</td><td>c</td><td>0</td><td>-4.845262</td>
 <td>-0.32192808</td><td>10</td></tr>
 <tr><td>4</td><td>""</td><td>d</td><td>0</td><td>-4.845262</td>
 <td>-0.32192808</td><td>11</td></tr>
 <tr><td>5</td><td>""</td><td>r</td><td>0</td><td>-3.8987012</td>
 <td>-0.5849625</td><td>12</td></tr>
 <tr><td>6</td><td>"a"</td><td>b</td><td>2</td><td>-2.5122285</td>
 <td>-0.5849625</td><td>13</td></tr>
 <tr><td>7</td><td>"a"</td><td>c</td><td>3<td>-3.4966948</td>
 <td>-0.32192808</td><td>14</td></tr>
 <tr><td>8</td><td>"a"</td><td>d</td><td>4</td><td>-3.4966948</td>
 <td>-0.32192808</td><td>15</td></tr>
 <tr><td>9</td><td>"b"</td><td>r</td><td>5</td><td>-1.4034244</td>
 <td>-0.5849625</td><td>16</td></tr>
 <tr><td>10</td><td>"c"</td><td>a</td><td>1</td><td>-1.5948515</td>
 <td>-0.32192808</td><td>17</td></tr>
 <tr><td>11</td><td>"d"</td><td>a</td><td>1</td><td>-1.5948515</td>
 <td>-0.32192808</td><td>18</td></tr>
 <tr><td>12</td><td>"r"</td><td>a</td><td>1</td><td>-1.1760978</td>
 <td>-0.32192808</td><td>19</td></tr>
 <tr><td>13</td><td>"ab"</td><td>r</td><td>9</td><td>-0.77261907</td>
 <td colspan='2' rowspan='7'>
 <i><small>This space intentionally left left blank: 
 maximum length n-grams are not contexts.</small</i></td></tr>
 <tr><td>14</td><td>"ac"</td><td>a</td><td>10</td><td>-1.1051782</td></tr>
 <tr><td>15</td><td>"ad"</td><td>a</td><td>11</td><td>-1.1051782</td></tr>
 <tr><td>16</td><td>"br"</td><td>a</td><td>12</td><td>-0.6703262</td></tr>
 <tr><td>17</td><td>"ca"</td><td>d</td><td>8</td><td>-1.8843123</td></tr>
 <tr><td>18</td><td>"da"</td><td>b</td><td>6</td><td>-1.5554274</td></tr>
 <tr><td>19</td><td>"ra"</td><td>c</td><td>7</td><td>-1.8843123</td></tr>
 * </table>
 * </blockquote>
 *
 * The actual data is contained in the last five columns of the table.
 * Thus internal nodes require 10 bytes and terminal nodes require 18
 * bytes.  The indices in the first column and the implicit context
 * represented by the node are for convenience.  Each of these indices
 * picks out a row in the table that corresponds to a node in the trie
 * structure representing the counts.  The nodes are arranged
 * according to a unicode-order breadth-first traversal of the count
 * trie.  Each non-terminal node stores a character, the index of the
 * suffix node (as in a suffix tree), a probability estimate for the
 * character given the context of characters that led to the
 * character, an interpolation value for the context represented by
 * the context leading to the character plus the character itself, and
 * finally a pointer to the first daughter of the node in the array.
 * Note that the last daughter of a node is thus picked out by the
 * first daughter of the next node minus one.  For instance, the
 * daughters of node 1 are 6, 7 and 8, and the daguther of node 9 is
 * 16.  The terminal nodes have no daughters; note that memory is not
 * allocated for these terminal cells.
 *
 * <P>The second column indicates the context derived by walking
 * from the root node (index 0) down to the node.  For instance, the
 * path "bra" leads from the root node 0 ("") to the node 2 ("b"), to
 * node 9 ("br"), and finally to node 16 ("bra").  The tree is
 * traversed by starting at the root node and looking up daughters by
 * binary search.
 *
 *
 * <P>If a full context and node are in the tree, the estimate can
 * simply be looked up.  For instance,
 * <code>log<sub><sub>2</sub></sub> P(b) = -3.898</code>,
 * <code>log<sub><sub>2</sub></sub> P(r|b) = -1.403</code>, and
 * <code>log<sub><sub>2</sub></sub> P(a|br) = -0.670</code>.  If the
 * context is available, but not the outcome, the result is just the
 * addition of the log one minus lambda estimates down to the first
 * context for which the outcome exists.  These contexts to explore
 * are all suffixes of the context currently being explored and may be
 * looked up in the suffix array. For instance, consider:
 *
 * <blockquote><code>
 * P(c|br) 
 * = &lambda;(br) P<sub><sub>ML</sub></sub>(c|br) 
 * + (1-&lambda;(br)) P(c|b)
 * </code></blockquote>
 *
 * In this case, the outcome <code>c</code> is not available for the
 * context <code>br</code> (the only daughter is <code>a</code>),
 * hence the maximum likelihood estimate is zero, and the result
 * reduces to the second term:
 *
 * <blockquote><code>
 *  P(c|br) = (1-&lambda;(br)) P(c|b)
 * </code></blockquote>
 *
 * and hence
 *
 * <blockquote><code>
 *  log<sub><sub>2</sub></sub> P(c|br)
 *  = log<sub><sub>2</sub></sub>((1-&lambda;(br)) P(c|b))
 *  = log<sub><sub>2</sub></sub>(1-&lambda;(br))
 *    + log<sub><sub>2</sub></sub> P(c|b)
 * </code></blockquote>
 *
 * This continues until the outcome is found.  In this case,
 * we continue with the next term in the same way:
 *
 * <blockquote><code>
 *  = log<sub><sub>2</sub></sub> (1-&lambda;(br))
 *    + log<sub><sub>2</sub></sub> (1- &lambda;(b))
 *    + log<sub><sub>2</sub></sub> P(c)
 * <br>
 * = -0.584 + -0.584 + -0.4845
 * </code></blockquote>
 *
 * Because 3-grams are the upper bound on context length, and terminal
 * nodes have no daughters, we conserve length at the end of the
 * smoothing and daughter arrays.
 *
 * <P>In practice, this smoothing may require going all the way
 * down to the uniform model.  The uniform model estimate is
 * stored separately.  The interpolation parameter for the root
 * node works as for any other node.
 *
 * <P>For estimates of sequences, the final node used will act
 * as the first potential context for the next estimate.  This
 * replaces a number of lookups equal to the n-gram length by
 * binary character search with a simple array lookup.
 *
 * <P><i>Implementation Note:</i> The suffix indices are not included
 * in the binary serialization format. Instead, they are initialized
 * right after the binary data is read in.  This requires walking the
 * trie-structure and computing each node's suffix by doing a walk
 * from the root.  For instance, a million node 8-gram model will
 * require at most 8 million binary character searches during
 * initialization.
 *
 * @author  Bob Carpenter
 * @version 3.6
 * @since   LingPipe2.0
 */
public class CompiledNGramProcessLM
    implements LanguageModel.Process,
               LanguageModel.Conditional,
               Model<CharSequence> {

    private final int mMaxNGram;
    private final float mLogUniformEstimate;
    private final char[] mChars;
    private final float[] mLogProbs;
    private final float[] mLogOneMinusLambdas;
    private final int[] mFirstChild;
    private final int[] mSuffix;
    private final int mLastContextIndex;

    // Data Format
    // -------------------------------------------------
    // maxNGram:int
    // logUniformEstimate:float
    // numTotalNodes:int
    // numInternalNodes:int
    // (c:char,
    //  logProb:float,
    //  logOneMinusLambda:float,
    //  firstChild:int)^numInternalNodes
    // (c:char,
    //  logProb:float)^(numTotalNodes-numInternalNodes)

    // public CompiledNGramProcessLM() { }

    /**
     * Construct a compiled n-gram process language model by reading
     * it from the specified data input.  The data should have been
     * constructed by serializing an instance of {@link NGramProcessLM}.
     *
     * @param dataIn Data input from which to read the model.
     * @throws IOException If there is an exception reading from the
     * input.
     */
    CompiledNGramProcessLM(ObjectInput dataIn) throws IOException {
        mMaxNGram = dataIn.readInt();
        mLogUniformEstimate = dataIn.readFloat();
        int numTotalNodes = dataIn.readInt();
        int lastInternalNodeIndex = dataIn.readInt();
        mLastContextIndex = lastInternalNodeIndex;
        mChars = new char[numTotalNodes];
        mLogProbs = new float[numTotalNodes];
        mSuffix = new int[numTotalNodes];
        Arrays.fill(mSuffix,CACHE_NOT_COMPUTED_VALUE);
        mLogOneMinusLambdas = new float[lastInternalNodeIndex+1];
        mFirstChild = new int[lastInternalNodeIndex+2];
        mFirstChild[lastInternalNodeIndex+1] = numTotalNodes;
        for (int i = 0; i <= lastInternalNodeIndex; ++i) {
            mChars[i] = dataIn.readChar();
            mLogProbs[i] = dataIn.readFloat();
            mLogOneMinusLambdas[i] = dataIn.readFloat();
            mFirstChild[i] = dataIn.readInt();
        }
        for (int i = lastInternalNodeIndex+1; i < numTotalNodes; ++i) {
            mChars[i] = dataIn.readChar();
            mLogProbs[i] = dataIn.readFloat();
        }
        compileSuffixes("",ROOT_NODE_INDEX);
    }

    /**
     * Returns the array of characters that have been observed for this
     * language model.  These are returned in increasing unicode order.
     *
     * <P>It is assumed that the observed characters are
     * those stored unigram probabilities in the trie.
     *
     * @return The array of characters that have been observed
     * for this language model.
     */
    public char[] observedCharacters() {
        if (mFirstChild.length < 2) return new char[0];
        char[] result = new char[mFirstChild[1]-1];
        for (int i = 0; i < result.length; ++i)
            result[i] = mChars[i+1];
        return result;
    }

    /**
     * Returns the maximum length n-gram used for this compiled
     * language model.  The maximum amount of context used for
     * estimates will be one less than this.
     *
     * @return The maximum length n-gram counted in this language
     * model.
     */
    public int maxNGram() {
        return mMaxNGram;
    }

    /**
     * Returns the total number of nodes in this language model's trie
     * structure.  This represents the total number of sequences for
     * which there are precompiled conditional probability estimates.
     *
     * @return The total number of nodes in the underlying trie
     * structure.
     */
    public int numNodes() {
        return mChars.length;
    }

    /**
     * Return the index in the parallel array structure underlying of
     * the maximum length suffix of the specified string that is
     * defined as a context.
     *
     * @param context String of context.
     * @return int Index of maximum length suffix of the specified
     * context.
     */
    public int longestContextIndex(String context) {
        char[] cs = context.toCharArray();
        int length = cs.length;
        for (int i = 0; i < length; ++i) {
            int k = getIndex(cs,i,length);
            if (k >= 0) {
                while (k >= mLogOneMinusLambdas.length)
                    k = mSuffix[k];
                return k;
            }
        }
        return 0; // back off all the way
    }

    int numInternalNodes() {
        return mFirstChild.length;
    }

    private void compileSuffixes(String context, int index) {
        mSuffix[index] = suffixIndex(context);
        if (index >= mFirstChild.length) return;
        int firstChildIndex = mFirstChild[index];
        int lastChildIndex =
            index+1 < mFirstChild.length
            ? mFirstChild[index+1]
            : mChars.length;
        for (int i = firstChildIndex; i < lastChildIndex; ++i)
            compileSuffixes(context + mChars[i], i);
    }

    private int suffixIndex(String context) {
        int suffixLength = context.length()-1;
        if (suffixLength < 0) return -1;
        char[] cs = new char[suffixLength];
        for (int i = 0; i < suffixLength; ++i)
            cs[i] = context.charAt(i+1);
        return getIndex(cs,0,suffixLength);
    }

    /**
     * This method is a convenience impelementation of the {@link
     * Model} interface which delegates the call to {@link
     * #log2Estimate(CharSequence)}.
     *
     * @param cSeq Character sequence whose probability is returned.
     * @return The log (base 2) probability of the specified character sequence.
     */
    public double log2Prob(CharSequence cSeq) {
        return log2Estimate(cSeq);
    }

    /**
     * This method is a convenience implementation of the {@link Model}
     * interface which returns the result of raising 2.0 to the 
     * power of the result of a call to {@link #log2Estimate(CharSequence)}.
     *
     * @param cSeq Character sequence whose probability is returned.
     * @return The log probability of the specified character sequence.
     */
    public double prob(CharSequence cSeq) {
        return java.lang.Math.pow(2.0,log2Estimate(cSeq));
    }


    public final double log2Estimate(CharSequence cSeq) {
        char[] cs = Strings.toCharArray(cSeq);
        return log2Estimate(cs,0,cs.length);
    }

    /**
     * Returns the log (base 2) estimate of the specified character in
     * the context with the specified index.  This corresponds to
     * values returned by the conditional estimates when the context
     * and outcome character are specified in a singlecharacter sequence or
     * slice.
     * 
     * 
     * <P>The main use of this method is to incrementally compute
     * conditional estimates and contexts, in conjunction with the
     * method {@link #nextContext(int,char)}.
     *
     * @param contextIndex Index of context of estimate.
     * @param nextChar Character being estimated.
     * @return Log (base 2) estimate of character in context.
     */
    public final double log2Estimate(int contextIndex, 
                                     char nextChar) {
        double sum = 0.0;
        int outcomeIndex;
        for (int currentContextIndex = contextIndex;
             (outcomeIndex = getIndex(currentContextIndex,nextChar)) < 0;
             currentContextIndex = mSuffix[currentContextIndex]) {
            if (currentContextIndex < mLogOneMinusLambdas.length)
                sum += mLogOneMinusLambdas[currentContextIndex];
            if (currentContextIndex == ROOT_NODE_INDEX) {
                return sum + mLogUniformEstimate;
            }
        }
        return sum + mLogProbs[outcomeIndex];    
    }

    /**
     * Returns the index of the context formed by appending the
     * specified character to the context of the specified index.  The
     * main use of this method is to incrementally compute conditional
     * estimates and contexts, in conjunction with the method {@link
     * #log2Estimate(int,char)}.  
     *
     * <P>Note that the index of the root node is always <code>0</code>.
     *
     * @param contextIndex Index of present context.
     * @param nextChar Next character.
     * @return Index of context formed by appending next character to
     * the present context.
     * @throws IllegalArgumentException If the context index is less
     * than zero or greater than the last context index.
     */
    public int nextContext(int contextIndex, char nextChar) {
        if (contextIndex < 0 
            || contextIndex > mLastContextIndex) {
            String msg = "Context must be greater than zero."
                + " Context must be less than last index=" + mLastContextIndex
                + " Context=" + contextIndex;
            throw new IllegalArgumentException(msg);
        }
        for (int currentContextIndex = contextIndex;
             true;
             currentContextIndex = mSuffix[currentContextIndex]) {         
            int outcomeIndex = getIndex(currentContextIndex,nextChar);
            if (outcomeIndex < mLogOneMinusLambdas.length
                && outcomeIndex >= 0) return outcomeIndex;
            if (currentContextIndex == ROOT_NODE_INDEX) 
                return ROOT_NODE_INDEX; // can't go back further
        }
    }

    public final double log2Estimate(char[] cs, int start, int end) {
        int len = mLogOneMinusLambdas.length;
        Strings.checkArgsStartEnd(cs,start,end);
        double sum = 0.0;
        int contextIndex = ROOT_NODE_INDEX;
        NEXT_CHAR:
        for (int i = start; i < end; ++i) {
            char nextChar = cs[i];
            int outcomeIndex;
            while ((outcomeIndex = getIndex(contextIndex,nextChar)) < 0) {
                if (contextIndex < len)
                    sum += mLogOneMinusLambdas[contextIndex];
                if (contextIndex == ROOT_NODE_INDEX) {
                    sum += mLogUniformEstimate;
                    contextIndex = ROOT_NODE_INDEX;
                    continue NEXT_CHAR;
                }
                contextIndex = mSuffix[contextIndex]; // backoff until end
            }
            sum += mLogProbs[outcomeIndex];
            contextIndex
                = outcomeIndex < len
                ? outcomeIndex
                : mSuffix[outcomeIndex];
        }
        return sum;

    }

    public double log2ConditionalEstimate(CharSequence cSeq) {
        char[] cs = cSeq.toString().toCharArray();
        return log2ConditionalEstimate(cs,0,cs.length);
    }

    public double log2ConditionalEstimate(char[] cs, int start, int end) {
        Strings.checkArgsStartEnd(cs,start,end);
        double total = 0.0;
        int contextEnd = end - 1;
        char c = cs[contextEnd]; // last char
        int maxContextLength = Math.min(contextEnd-start,mMaxNGram-1);
        for (int contextLength = maxContextLength;
             contextLength >= 0;
             --contextLength) {

            int contextStart = contextEnd - contextLength;
            int contextIndex = getIndex(cs,contextStart,contextEnd);
            if (contextIndex == -1) continue; // no ctx, try shorter context
            while (contextIndex > mLastContextIndex)
                contextIndex = mSuffix[contextIndex];  // no outcomes, 
            // go to shortest w. outcomes
            int outcomeIndex = getIndex(contextIndex,c);
            if (outcomeIndex != -1)
                return total + mLogProbs[outcomeIndex];
            total += mLogOneMinusLambdas[contextIndex];
        }
        return total + mLogUniformEstimate;
    }

    /**
     * Returns a string-based representation of this compiled n-gram.
     * It writes one row per line of the parallel indices.  It should
     * probably not be called with very large models, as the resulting
     * string will be much larger than the model itself.
     *
     * @return String-based representation of this model.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Max NGram=" + mMaxNGram);
        sb.append('\n');
        sb.append("Log2 Uniform Estimate=" + mLogUniformEstimate);
        sb.append('\n');
        sb.append("i c suff prob 1-lambda firstChild");
        sb.append('\n');
        for (int i = 0; i < mChars.length; ++i) {
            sb.append(i);
            sb.append(" ");
            sb.append(mChars[i]);
            sb.append(" ");
            sb.append(mSuffix[i]);
            sb.append(" ");
            sb.append(mLogProbs[i]);
            if (i < mLogOneMinusLambdas.length) {
                sb.append(" ");
                sb.append(mFirstChild[i]);
                sb.append(" ");
                sb.append(mLogOneMinusLambdas[i]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private int getIndex(int fromIndex, char c) {
        if (fromIndex+1 >= mFirstChild.length) return -1;
        int low = mFirstChild[fromIndex];
        int high = mFirstChild[fromIndex+1]-1;
        while (low <= high) {
            int mid = (high + low)/2;
            if (mChars[mid] == c) return mid;
            else if (mChars[mid] < c)
                low = (low == mid) ? mid+1 : mid;
            else
                high = (high == mid) ? mid-1 : mid;
        }
        return -1;
    }

    private int getIndex(char[] cs, int start, int end) {
        int index = 0;
        for (int currentStart = start;
             currentStart < end;
             ++currentStart) {
            index = getIndex(index,cs[currentStart]);
            if (index == -1) return -1;
        }
        return index;
    }

    /**
     * The index of the root node, namely <code>0</code>.
     */
    public static final int ROOT_NODE_INDEX = 0;

    private static final int CACHE_NOT_COMPUTED_VALUE = -1;
}

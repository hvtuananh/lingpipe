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

import java.io.IOException;
import java.io.ObjectInput;

/**
 * An estimator trie is a data structure for compactly representing
 * the contexts and outcomes in an estimator at varying levels
 * of detail to allow for the computation of smoothed outcomes at
 * runtime.
 *
 * <p> The data file format for serialization of an estimator trie, as
 * read by the constructor {@link #EstimatorTrie(ObjectInput)} and
 * as written, for example, by {@link
 * com.aliasi.ne.TrainableEstimator#writeTo(java.io.DataOutputStream)}.
 *
 * <br/><br/>
 * <table cellpadding="5" border="1">
 *   <tr>
 *     <td width="15%"><b>Count</b></td>
 *     <td width="15%"><b>Type</b></td>
 *     <td width="30%"><b>Variable</b></td>
 *     <td width="40%"><b>Content</b></td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>int</td>
 *     <td>numNodes</td>
 *     <td>Number of nodes in trie</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="5">numNodes</td>
 *     <td>int</td>
 *     <td>nodeSymbol</td>
 *     <td>Symbol identifier for node</td>
 *   </tr>
 *   <tr>
 *     <td>int</td>
 *     <td>nodeFirstOutcomeIndex</td>
 *     <td>Index in outcome array of first outcome</td>
 *   </tr>
 *   <tr>
 *     <td>int</td>
 *     <td>nodeFirstChild</td>
 *     <td>Index in this array of first child node</td>
 *   </tr>
 *   <tr>
 *     <td>float</td>
 *     <td>nodeLogOneMinusLambda</td>
 *     <td>log (1-lambda) for this node</td>
 *   </tr>
 *   <tr>
 *     <td>int</td>
 *     <td>nodeBackoff</td>
 *     <td>Index in this array of backoff node, or -1 if none</td>
 *   </tr>
 *   <tr>
 *     <td>1</td>
 *     <td>int</td>
 *     <td>numOutcomes</td>
 *     <td>Number of outcomes in trie</td>
 *   </tr>
 *   <tr>
 *     <td rowspan="3">numOutcomes</td>
 *     <td>int</td>
 *     <td>outcomeSymbol</td>
 *     <td>Symbol identifier for outcome</td>
 *   </tr>
 *   <tr>
 *     <td>int</td>
 *     <td>outcomeLogEstimate</td>
 *     <td>log probability estimate for this outcome</td>
 *   </tr>
 * </table>
 * </br>
 *
 * The parallel arrays are encoded one index at a time rather than one
 * whole array at a time.  All values are stored in local variables
 * with the given names, with the context node and outcome array
 * entries being indexed by the node number and outcome number
 * respectively.
 * </p>
 *
 * <p> The parallel arrays are used for lookup as follows.  A context
 * for estimating outcomes consists of a sequence of events,
 * represented here as symbols.  The events to be kept the longest in
 * smoothing are at the front of the sequence and are looked up first.
 * The first entry (index <code>0</code>) in the nodes array
 * constitutes the root node or null context.  It may or may not have
 * any outcomes on it.  The algorithm for evaluation follows the
 * sequence of context events, stopping only when extending the
 * context is not possible.  That node represents the most specific
 * context for which estimates can be made, and may not actually use
 * all of the contextual events if they haven't been seen, or seen
 * frequently enough, during training.  From this most specific node,
 * an estimate is made for the outcome event in question.  The
 * outcomes for the most specific context node are searched for the
 * outcome event being estimated.  If it is found, the log estimate on
 * that outcome is returned.  If it is not found, the backoff pointer
 * from the most specific node is followed, and the evaluation is done
 * again from the more general node, adding the <code>log
 * (1-lambda)</code> value as required by the smoothing model.  If a
 * backoff is attempted from a most general node without a backoff
 * node, an estimate of <code>log 0.0 = Double.NaN</code> is returned,
 * or in the case of estimation with a specified uniform distribution,
 * the log of the uniform estimate is added to the <code>log
 * (1-lambda)</code> and returned.
 * </p>
 *
 * <p> Lookup in the arrays is carried out by binary search over the
 * arrays of symbol identifiers.  For searching children in thre trie,
 * <code>nodeFirstChild[i]</code> is the index of the first child node
 * of node <code>i</code>, and <code>nodeFirstChild[i+1]</code> is the
 * index of the first child of node <code>i+1</code>, and so on.
 * Therefore, the binary search for children of <code>i</code> is
 * carried out over indices <code>j</code> such that <code>
 * nodeFirstChild[i] <= j < nodeFirstChild[i+1]</code>.  This requires
 * the node symbol ids to be in sorted order in these ranges.  The
 * outcomes are searched similarly, with outcomes for node
 * <code>i</code> searched on indices <code>j</code> such that
 * <code>nodeFirstOutcomeIndex[i] <= j <
 * nodeFirstOutcomeIndex[i+1]</code>.
 * </p>
 *
 * <p> The methods provided for estimation require the callers of
 * these classes to walk the trie.  This avoids unnecessary
 * intermediate object create to call the trie, and allows the tries
 * themselves to be flexible with respect to their geometry.

 * Given a context node (identified by index) and a symbol (by integer
 * identifier) for an event, the (index of a) more specific context
 * node that includes the symbol can be retrieved if it exists.  Given
 * a particular context node (identified by index), and a symbol
 * representing an outcome (identified by an integer from a symbol
 * table), the log estimate from the given context is returned by
 * {@link #estimateFromNode(int,int)}.  If the outcome doesn't exist
 * at any level of backoff, <code>Double.NaN</code> is returned.  The
 * method {@link #estimateFromNodeUniform(int,int,double)} includes a
 * double value to return instead of <code>Double.NaN</code> as a
 * uniform estimate.  </p>
 *
 * @author  Bob Carpenter
 * @version 1.0
 * @since   LingPipe1.0
 * @see OutcomeCounter
 * @see Node
 */
class EstimatorTrie {

    // Parallel arrays for trie nodes.

    /** Number of nodes in the trie, indexing the parallel trie
     * arrays.
     */
    private final int _numNodes;

    /** Elements hold the identifiers for the symbol at the
     * element.
     */
    private final int[] _nodeSymbol;

    /** Elements hold the index in the outcome arrays of the first
     * outcome.  One longer than other arrays, with final element
     * holding the length for symmetry in all the bounded loops.
     */
    private final int[] _nodeFirstOutcome;

    /** Elements hold the index in these arrays of the node that
     * is the first child of this node in the trie structure.  One
     * longer than other arrays, with final element holding the
     * length for symmetry in all the bounded loops.
     */
    private final int[] _nodeFirstChild;

    /** Elements hold the smoothing constant, <code>log 1 -
     * lambda(context)</code>.
     */
    private final float[] _nodeLogOneMinusLambda;

    /** Elements hold the index of the backoff node to check next
     * for the outcome saught.  Will hold <code>-1</code> if there
     * is no backoff node for the node.
     */
    private final int[] _nodeBackoff;

    // Parallel arrays for outcomes given contexts.

    /** Number of outcomes total, indexing the parallel outcome
     * arrays.
     */
    private final int _numOutcomes;

    /** Elements hold the identifier of the symbol represented by
     * the outcome.
     */
    private final int[] _outcomeSymbol;

    /** Elements hold the log estimate of the outcome.
     */
    private final float[] _outcomeLogEstimate;

    /** Create an estimator trie from a data input stream.  Does
     * not close the stream so that an estimator trie may be read
     * out of a portion of a stream.
     *
     * @param in Data input stream from which to read this estimator's data.
     * @throws IOException If there is an exception reading from the
     * underlying stream.
     */
    public EstimatorTrie(ObjectInput in) throws IOException {
        _numNodes = in.readInt();
        _nodeSymbol = new int[_numNodes];
        _nodeFirstOutcome = new int[_numNodes+1];
        _nodeFirstChild = new int[_numNodes+1];
        _nodeLogOneMinusLambda = new float[_numNodes];
        _nodeBackoff = new int[_numNodes];
        for (int i = 0; i < _numNodes; ++i) {
            _nodeSymbol[i] = in.readInt();
            _nodeFirstOutcome[i] = in.readInt();
            _nodeFirstChild[i] = in.readInt();
            _nodeLogOneMinusLambda[i] = in.readFloat();
            _nodeBackoff[i] = in.readInt();
        }
        _nodeFirstChild[_numNodes] = _numNodes; // boundary
        _numOutcomes = in.readInt();
        _nodeFirstOutcome[_numNodes] = _numOutcomes; // boundary
        _outcomeSymbol = new int[_numOutcomes];
        _outcomeLogEstimate = new float[_numOutcomes];
        for (int i = 0; i < _numOutcomes; ++i) {
            _outcomeSymbol[i] = in.readInt();
            _outcomeLogEstimate[i] = in.readFloat();
        }
    }

    /**
     * Returns the log estimate of the given outcome symbol from
     * the specified node, or {@link java.lang.Double#NaN} if
     * there is none.
     *
     * @param symbolID Outcome whose estimate is sought.
     * @param nodeIndex Index of node representing context of
     * estimate.
     * @return Log estimate of outcome symbol given node index or
     * <code>Double.NaN</code> if impossible.
     */
    public double estimateFromNode(int symbolID, int nodeIndex) {
        if (symbolID < 0) return Double.NaN;
        double backoffAccumulator = 0.0;
        for(int currentNodeIndex = nodeIndex;
            currentNodeIndex >= 0;
            currentNodeIndex = _nodeBackoff[currentNodeIndex]) {
            int low = _nodeFirstOutcome[currentNodeIndex];
            int high = _nodeFirstOutcome[currentNodeIndex+1]-1;
            while (low <= high) {
                int mid = (high + low)/2;
                if (_outcomeSymbol[mid] == symbolID)
                    return backoffAccumulator + _outcomeLogEstimate[mid];
                else if (_outcomeSymbol[mid] < symbolID)
                    low = (low == mid ? mid+1 : mid);
                else high = (high == mid ? mid-1 : mid);
            }
            backoffAccumulator += _nodeLogOneMinusLambda[currentNodeIndex];
        }
        return Double.NaN; // no more backoff nodes available
    }

    /**
     * Returns the log estimate of the given outcome symbol from
     * the specified node, or {@link java.lang.Double#NaN} if
     * there is none.  Just like {@link #estimateFromNode} except
     * that final backoff will be to a uniform estimate that is
     * supplied as an argument.
     *
     * @param symbolID Outcome whose estimate is sought.
     * @param nodeIndex Index of node representing context of estimate.
     * @return Log estimate of outcome symbol given node index or
     * <code>Double.NaN</code> if impossible.
     */
    public double estimateFromNodeUniform(int symbolID, int nodeIndex,
                                          double uniformEstimate) {

        if (symbolID < 0) return Double.NaN;
        double backoffAccumulator = 0.0;
        for (int currentNodeIndex = nodeIndex;
             currentNodeIndex >= 0;
             currentNodeIndex = _nodeBackoff[currentNodeIndex]) {
            int low = _nodeFirstOutcome[currentNodeIndex];
            int high = _nodeFirstOutcome[currentNodeIndex+1]-1;
            while (low <= high) {
                int mid = (high + low)/2;
                if (_outcomeSymbol[mid] == symbolID)
                    return backoffAccumulator + _outcomeLogEstimate[mid];
                else if (_outcomeSymbol[mid] < symbolID)
                    low = (low == mid ? mid+1 : mid);
                else high = (high == mid ? mid-1 : mid);
            }
            backoffAccumulator += _nodeLogOneMinusLambda[currentNodeIndex];
        }
        return (backoffAccumulator + uniformEstimate);
    }

    /**
     * Returns the index in the trie of the child node of the
     * specified parent node index having the specified symbol ID
     * or <code>-1</code> if it does not exist.
     *
     * @param symbolID Identifier of symbol of the child sought.
     * @param parentNodeIndex Index of parent node from which to
     * search for a child with specified identifier.
     * @return Index of the child node of the specified parent
     * node with specified symbol identifier.
     */
    public int lookupChild(int symbolID, int parentNodeIndex) {
        int low = _nodeFirstChild[parentNodeIndex];
        int high = _nodeFirstChild[parentNodeIndex+1]-1;
        if (symbolID < 0) return -1;
        while (low <= high) {
            int mid = (high + low)/2;
            if (_nodeSymbol[mid] == symbolID) return mid;
            else if (_nodeSymbol[mid] < symbolID)
                low = (low == mid ? mid+1 : mid);
            else high = (high == mid ? mid-1 : mid);
        }
        return -1;
    }


}


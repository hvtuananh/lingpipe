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

import com.aliasi.symbol.SymbolTableCompiler;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

/**
 * A <code>Node</code> represents a context event for evaluation of a
 * conditional probability.  It is the main data structure employed by
 * {@link EstimatorTrie}.  A node represents a context as a string
 * symbol.  A node maps symbols representing event outcomes to a
 * {@link OutcomeCounter}.  A node supports incrementing outcome
 * counters, and tracks counts the total number of outcomes and total
 * number of times all of its outcomes have been incremented.  Nodes
 * are used for smoothed estimation, in support of which they map
 * event symbols to finer-grained context and provide a back-pointer
 * to the next most general context (in general, for a node with
 * symbol <code>"a"</code>, if <code>node.backoffNode()</code> exists,
 * then <code>node == node.backoffNode().getChild("a")</code>.
 *
 * <p> Each node computes estimates based on Witten-Bell-style
 * smoothing, which is a form of linear interpolation smoothing where the
 * interpolation between a finer and coarser contexts is determined by
 * counts on the finer context.  The exact formula used to define
 * the estimate for a given node in a given context, assuming that
 * there is a backoff context, is:
 *
 * <pre>
 *   context.estimate(outcome)
 *       = context.lambda()          * context.directEstimate(outcome)
 *         + ( 1 -context.lambda() ) * context.backoffContext().estimate(outcome)
 *
 *   context.lambda()
 *       = context.totalCount()
 *         / ( context.totalCount()
               + ( LAMBDA_FACTOR * context.numOutcomes() ) )
 * </pre>
 *
 * where <code>context.estimate(outcome)</code> is the estimate
 * provided by the node <code>context</code> to the outcome
 * <code>outcome</code>, <code>context.lambda()</code> is the linear
 * interpolation factor, <code>context.directEstimate(outcome)</code>
 * is a simple frequency-based maximum likelihood estimate of the
 * outcome (number of times the outcome event has been incremented
 * divided by the total number of outcomes that have been
 * incremented), and <code>context.backoffContext()</code> is the next
 * most general context after <code>context</code>.  In cases where
 * the context being the most general context and having no backoff
 * context, the direct estimate is returned.  The definition of
 * <code>lambda()</code> involves <code>context.totalCount())</code>,
 * which is the total number of training events incremented on the
 * node, and <code>context.numOutcomes()</code>, which is the number
 * of distinct outcomes incremented on this node.  The maximum
 * likelihood estimate for an outcome is given by
 * <code>context.directEstimate()</code> which is just the count of a
 * particular outcome (read from its counter) divided by the total
 * count.  The definition of <code>lambda()</code> ensures <code>0.0
 * <= lambda() <= 1.0</code> for a proper linear interpolation factor.
 * <code>lambda()</code> increases as the total number of outcomes
 * increases, meaning that the more data seen by the finer grained
 * estimate, the more weight it is given in interpolation, whereas
 * <code>lambda()</code> decreases as more different events have been
 * seen; the balance between these two factors is determined by the
 * <code>LAMBDA_FACTOR</code>, which is an estimator tuning parameter.
 *
 * <br/><br/>
 * <table cellpadding="5" border="1"><tr><td width="100%">
 *
 * For example, estimating <code>P(a|b,c)</code>, assume the context
 * <code>c1</code> represents <code>b,c</code> and context
 * <code>c2</code> represents context <code>b</code>, so that
 * <code>c1.backoffContext() == c2</code>, and further assume that
 * <code>c2</code> is the most general context available.  Then the
 * estimate of <code>P(a|b,c)</code> is given by:
 *
 * <blockquote><code>
 *   c1.lambda() *
 *   c1.directEstimate(a) + (1-c1.lambda()) * c2.directEstimate(a)
 * </code></blockquote>
 *
 * </td></tr></table>
 * </p>
 *
 * <p>In typical usage, a root node will be used to navigate down to
 * more specific contexts.  The root node may or may not act as a
 * general backoff node; estimators can go even further than the null
 * observed context to a uniform prior estimate.  These nodes will
 * form a trie-structure of event contexts which will be incremented
 * during training.  Then, they may be pruned from a root node to
 * remove all daughter nodes and counters with fewer than a specified
 * number of outcomes.  Finally, estimates may be compiled, which
 * caches the important values, and assigns array indexes to each node
 * where the nodes are implicit in the indexes of a collection of
 * parallel arrays.  The structure of an entire estimator is captured,
 * for example, by {@link com.aliasi.ne.TrainableEstimator}.  </p>
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe1.0
 * @see OutcomeCounter
 * @see EstimatorTrie
 */
class Node {

    /**
     * Cached version of 1-lambda()
     */
    float mOneMinusLambda;

    /**
     * Index in an array used to store nodes.
     */
    private int mIndex = -1;

    /**
     * Total number of outcomes incremented for this node.
     */
    private int mTotalCount = 0;

    /**
     * Number of distinct outcomes for this node.
     */
    private short mNumOutcomes = 0;

    /**
     * A mapping from event symbols to more specific context nodes.
     */
    private final Map<String,Node> mChildren = new TreeMap<String,Node>();

    /**
     * A mapping from event symbols to outcome counters.
     */
    private final Map<String,OutcomeCounter> mOutcomes
        = new TreeMap<String,OutcomeCounter>();

    /**
     * The next most general node in an estimator; may be
     * <code>null</code> for maximally specific contexts.
     */
    private final Node mBackoffNode;

    /**
     * Symbol table compiler for the context symbols of this
     * estimator.
     */
    private final SymbolTableCompiler mSymbolTable;

    /**
     * The symbol representing the event difference between
     * this outcome and the next most general outcome.
     */
    private final String mSymbol;

    /**
     * Construct a node representing an estimation context event with
     * a specified symbol representing an event, a symbol table
     * compiler for the symbol, and a backoff node representing a more
     * general context.
     *
     * @param symbol String representing symbol for this context.
     * @param symbolTable Table compiler for the symbol.
     * @param backoffNode Next more general context.
     */
    public Node(String symbol, SymbolTableCompiler symbolTable,
                Node backoffNode) {

        mSymbol = symbol;
        if (symbolTable == null)
            throw new IllegalArgumentException("Null table.");
        mSymbolTable = symbolTable;
        if (symbol != null) symbolTable.addSymbol(symbol);
        mBackoffNode = backoffNode;
    }

    public void printSymbols() {
        if (mSymbolTable == null) System.out.println("NULL Symbol TABLE");
        System.out.println(mSymbolTable.toString());
    }

    /**
     * Return the identifier for the symbol representing the context
     * event for this node.  The full context is the joint context of
     * this node's context and all of the more general nodes'
     * contexts.
     *
     * @return Identifier for the symbol representing the context
     * event for this node.
     */
    public int getSymbolID() {
        if (mSymbol == null) return -1;
        return mSymbolTable.symbolToID(mSymbol);
    }

    /**
     * Adds the symbol for this node to the specified symbol table,
     * and recursively for each more specific context node and each
     * outcome counter.
     */
    public void generateSymbols() {
        if (mSymbol != null) mSymbolTable.addSymbol(mSymbol);
        for (OutcomeCounter counter : mOutcomes.values())
            counter.addSymbolToTable();
        for (Node child : mChildren.values())
            child.generateSymbols();
    }

    /**
     * Index of this node after compiling all nodes into
     * array indices.  Will be <code>-1</code> if called
     * before {@link #setIndex(int)} is called to set
     * the index.
     *
     * @return Array index for this context.
     */
    public int index() {
        return mIndex;
    }

    /**
     * Set the array index for this node, which will be
     * available through {@link #index()}.
     *
     * @param index Index for this context.
     */
    public void setIndex(int index) {
        mIndex = index;
    }

    /**
     * Prune all daughter nodes and outcomes with fewer than the
     * specified number of outcomes.  Adjusts total outcome counts
     * accordingly for estimation.
     *
     * @param threshold Minimal count for outcomes and child nodes to
     * be preserved.
     */
    public void prune(int threshold) {
        Iterator<String> outcomes = outcomes().iterator();
        while (outcomes.hasNext()) {
            OutcomeCounter counter = getOutcome(outcomes.next());
            if (counter.count() < threshold) {
                mTotalCount -= counter.count();
                --mNumOutcomes;
                outcomes.remove();
            }
        }
        Iterator<String> childrenIt = children().iterator();
        while (childrenIt.hasNext()) {
            Node childNode = getChild(childrenIt.next());
            childNode.prune(threshold);
            if (childNode.totalCount() < threshold)
                childrenIt.remove();
        }
    }

    /**
     * Returns the total number of nodes at least as specific as this
     * node, including this node.
     *
     * @return Total number of nodes at least as specific as this
     * node.
     */
    public int numNodes() {
        int count = 1;
        for (String childString : children())
            count += getChild(childString).numNodes();
        return count;
    }

    /**
     * Returns total number of outcome counters on this node and all
     * more specific nodes.
     *
     * @return Number of outcome counters on nodes at least as
     * specific as this node.
     */
    public int numCounters() {
        int count = mOutcomes.keySet().size();
        for (String childString : children())
            count += getChild(childString).numCounters();
        return count;
    }

    /**
     * Returns <code>true</code> if the specified outcome has a
     * nonzero count for this node.
     *
     * @param outcome Outcome to test.
     * @return <code>true</code> if the specified outcome has a
     * nonzero count for this node.
     */
    public boolean hasOutcome(String outcome) {
        return mOutcomes.containsKey(outcome);
    }

    /**
     * Returns the counter representing the specified outcome symbol
     * or <code>null</code> if it doesn't exist.
     *
     * @param outcome Outcome event symbol.
     * @return Counter representing the specified outcome, or
     * <code>null</code> if it doesn't exist.
     */
    public OutcomeCounter getOutcome(String outcome) {
        return mOutcomes.get(outcome);
    }

    /**
     * Returns <code>true</code> if this node has the more specific
     * outcome specified.
     *
     * @param child Next more specific context event symbol.
     * @return <code>true</code> if this node has the more specific
     * outcome specified.
     */
    public boolean hasChild(String child) {
        return mChildren.containsKey(child);
    }
    /**
     * Returns the node representing the more specific outcome
     * specified, or <code>null</code> if it doesn't exist.
     *
     * @param child Next more specific context event symbol.
     * @return The node representing the more specific outcome
     * specified, or <code>null</code> if it doesn't exist.
     */
    public Node getChild(String child) {
        return mChildren.get(child);
    }

    /**
     * Returns the next more specific context specified by the symbol,
     * or creates one and returns the one created.  The backoff node
     * and symbol table compiler to use for the symbol are also
     * specified.  The initial count and number of outcomes will be
     * <code>0</code>.
     *
     * @param child Next more specific context event symbol.
     * @param backoffNode Backoff node for the returned node in case
     * it needs to be created.
     * @param symbolTable Symbol table for the specified symbol in case the
     * returned node needs to be created.
     * @return The node representing the more specific outcome
     * specified.
     */
    public Node getOrCreateChild(String child, Node backoffNode,
                                 SymbolTableCompiler symbolTable) {
        if (hasChild(child)) return getChild(child);
        Node node = new Node(child, symbolTable, backoffNode);
        mChildren.put(child,node);
        return node;
    }

    /**
     * Returns the complete set of outcomes.
     *
     * @return Set of outcomes.
     */
    public Set<String> outcomes() {
        return mOutcomes.keySet();
    }

    /**
     * Returns the complete set of next more specific contexts.
     *
     * @return Set of next more specific contexts.
     */
    public Set<String> children() {
        return mChildren.keySet();
    }

    /**
     * Returns the number of times the specified outcome
     * has been seen, which may be <code>0</code>.
     *
     * @param outcome Symbol representing outcome event to test.
     * @return Number of times outcome has been incremented from this
     * node.
     */
    public int outcomeCount(String outcome) {
        OutcomeCounter ctr = getOutcome(outcome);
        return ctr == null ? 0 : ctr.count();
    }

    /**
     * Increments the outcome counter specified for this node, using
     * the supplied symbol table to create a new outcome counter
     * if the event has not previously been created.
     *
     * @param outcome Symbol representing outcome to increment.
     * @param symbolTable Symbol table for outcome in case it needs to
     * be created.
     */
    public void incrementOutcome(String outcome,
                                 SymbolTableCompiler symbolTable) {
        ++mTotalCount;
        if (hasOutcome(outcome)) {
            getOutcome(outcome).increment();
        } else {
            ++mNumOutcomes;
            mOutcomes.put(outcome,new OutcomeCounter(outcome,symbolTable,1));
        }
    }

    /**
     * Returns the total count of all outcomes for this node.
     *
     * @return Total count of all outcomes for this node.
     */
    public int totalCount() {
        return mTotalCount;
    }

    /**
     * Returns the compiled value of the natural log of
     * one minus lambda, <code>log (1-lambda)</code>.
     *
     * @return Compiled value of <code>log (1-lambda)</code>.
     */
    public float oneMinusLambda() {
        return mOneMinusLambda;
    }

    /**
     * Compiles all the estimates for this node, its outcomes, and
     * applies recursively to all more specific nodes.  Stores
     * <code>1-lambda</code> for future use on nodes and estimates
     * are stored on counters.
     *
     * @param lambdaFactor Lambda factor used to compute estimates.
     */
    public void compileEstimates(double lambdaFactor) {
        mOneMinusLambda = (float) java.lang.Math.log(1.0 - lambda(lambdaFactor));
        for (String outcome : outcomes()) {
            getOutcome(outcome).setEstimate((float)logEstimate(outcome,
                                                               lambdaFactor));
        }
        for (String childString : children()) {
            Node child = getChild(childString);
            child.compileEstimates(lambdaFactor);
        }
    }

    /**
     * Return the natural log of the estimate for the specified
     * outcome, using the specified lambda factor.
     *
     * @param outcome Outcome to estimate.
     * @param lambdaFactor Lambda factor used to compute estimate.
     * @return Log estimate of the specified outcome.
     */
    public double logEstimate(String outcome, double lambdaFactor) {
        return java.lang.Math.log(estimate(outcome,lambdaFactor));
    }

    /**
     * Returns the next most general node to use for backoff,
     * or <code>null</code> if none is available.
     *
     * @return Next most general node to use for backoff, or
     * <code>null</code> if none is available.
     */
    public Node backoffNode() {
        return mBackoffNode;
    }

    /**
     * Return the estimate for the specified outcome, using the
     * specified lambda factor.
     *
     * @param outcome Outcome to estimate.
     * @param lambdaFactor Lambda factor used to compute estimate.
     * @return Log estimate of the specified outcome.
     */
    public double estimate(String outcome, double lambdaFactor) {
        if (mBackoffNode == null) return maxLikelihoodEstimate(outcome);
        double lambda = lambda(lambdaFactor);
        return lambda * maxLikelihoodEstimate(outcome)
            + (1-lambda) * mBackoffNode.estimate(outcome,lambdaFactor);
    }

    /**
     * Returns the maximal likelihood estimate for the outcome given
     * the counts on this node and the outcome counter.
     *
     * @param outcome Outcome to estimate.
     * @return Maximal likelihood estimate of the specified outcome.
     */
    public double maxLikelihoodEstimate(String outcome) {
        return ((double)outcomeCount(outcome)) / (double)mTotalCount;
    }

    /**
     * Returns the value of <code>lambda</code> given the specified
     * lambda factor.  Returns <code>0.0</code> if there are no
     * outcomes with nonzero count.
     *
     * @param lambdaFactor Factor used to compute lambda value.
     */
    public double lambda(double lambdaFactor) {
        if (mTotalCount == 0) return 0.0;
        return ((double)mTotalCount)
            / (mTotalCount + lambdaFactor * mNumOutcomes);
    }

}

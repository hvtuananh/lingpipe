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

import com.aliasi.tokenizer.TokenCategorizer;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

// used only by TrainTokenShapeChunker

final class TrainableEstimator implements Compilable {

    /**
     * Root of the trie representing next tag contexts and outcomes.
     */
    private Node mRootTagNode;

    /**
     * Root of the trie representing next token contexts and outcomes.
     */
    private Node mRootTokenNode;

    /**
     * The symbol table used for tokens.
     */
    private final SymbolTableCompiler mTokenSymbolTable
        = new SymbolTableCompiler();

    /**
     * The symbol table used for tags.
     */
    private final SymbolTableCompiler mTagSymbolTable
        = new SymbolTableCompiler();


    /**
     * The value of the lambda factor.
     */
    private double mLambdaFactor;

    /**
     * The natural log of the estimate of the likelihood of a lexical
     * item.
     */
    private double mLogUniformVocabEstimate;

    /**
     * The token categorizer for this estimator to compute estimates
     * for unknown tokens.
     */
    private final TokenCategorizer mTokenCategorizer;

    /**
     * Construct a trainable estimator with the specified
     * lambda factor and log uniform vocabulary estimate.
     *
     * @param lambdaFactor Lambda factor to use for this estimator.
     * @param logUniformVocabEstimate Natural log of the uniform
     * vocabulary estimate for smoothing.
     * @param categorizer Token categorizer to categorize unknown tokens.
     */
    public TrainableEstimator(double lambdaFactor,
                              double logUniformVocabEstimate,
                              TokenCategorizer categorizer) {
        mLambdaFactor = lambdaFactor;
        mLogUniformVocabEstimate = logUniformVocabEstimate;
        mTokenCategorizer = categorizer;
        mRootTagNode = new Node(null,mTagSymbolTable,null);
        mRootTokenNode = new Node(null,mTokenSymbolTable,null);
        mTagSymbolTable.addSymbol(Tags.OUT_TAG);
    }

    /**
     * Construct a trainable estimator with default values for the
     * lambda factor of <code>4.0</code> and for the log uniform
     * vocabulary estimate of <code>java.lang.Math.log(1.0/1000000.0)</code>.
     * These may be set before writing the estimator to file with
     * {@link #setLambdaFactor(double)} and {@link
     * #setLogUniformVocabularyEstimate(double)}.
     *
     * @param categorizer Token categorizer to categorize unknown tokens.
     */
    public TrainableEstimator(TokenCategorizer categorizer) {
        this (4.0,
              java.lang.Math.log(1.0/1000000.0),
              categorizer);
    }

    /**
     * Sets the lambda factor to the specified value, which must be a
     * non-negative, non-infinite double.
     *
     * @param lambdaFactor Lambda factor to set.
     * @throws IllegalArgumentException If the specified factor is
     * negative, infinite, or not a number.
     */
    public void setLambdaFactor(double lambdaFactor) {
        if (lambdaFactor < 0.0
            || Double.isNaN(lambdaFactor)
            || Double.isInfinite(lambdaFactor))
            throw new
                IllegalArgumentException("Lambda factor must be > 0."
                                         + " Was=" + lambdaFactor);
        mLambdaFactor = lambdaFactor;
    }

    /**
     * Sets the log uniform vocabulary estimate to the specified
     * value, which must be a negative, non-infinite number.
     *
     * @param estimate Log Uniform vocabulary estimate to set.
     * @throws IllegalArgumentException If the specified factor is not
     * a number or is positive, zero or infinite.
     */
    public void setLogUniformVocabularyEstimate(double estimate) {
        if (estimate >= 0.0
            || Double.isNaN(estimate)
            || Double.isInfinite(estimate))
            throw new
                IllegalArgumentException("Log vocab estimate must be < 0."
                                         + " Was=" + estimate);
        mLogUniformVocabEstimate = estimate;
    }
    /**
     * Train the estimator based on the specified parallel arrays of
     * tokens and tags.
     *
     * @param tokens Array of tokens on which to train.
     * @param tags Array of tags on which to train.
     */
    public void handle(String[] tokens, String[] tags) {

        // System.out.println("tokens(" + java.util.Arrays.asList(tokens) + ")");
        // System.out.println("tags(" + java.util.Arrays.asList(tags) + ")");

        // train first token/tag pair given dummy starts
        if (tokens.length < 1) return;
        trainOutcome(tokens[0],tags[0],
                     Tags.START_TAG,
                     Tags.START_TOKEN,Tags.START_TOKEN);
        if (tokens.length < 2) {
            // train final token/tag pair
            trainOutcome(Tags.START_TOKEN,Tags.START_TAG,
                         tags[0],
                         tokens[0], Tags.START_TOKEN);
            return;
        }

        // train second token/tag pair w extensions
        trainOutcome(tokens[1],tags[1],
                     tags[0],
                     tokens[0],Tags.START_TOKEN);

        // train rest of pairs given
        for (int i = 2; i < tokens.length; ++i)
            trainOutcome(tokens[i],tags[i],
                         tags[i-1],
                         tokens[i-1],tokens[i-2]);

        // train final token/tag pair beyond the last
        trainOutcome(Tags.START_TOKEN, Tags.START_TAG,
                     tags[tags.length-1],
                     tokens[tokens.length-1], tokens[tokens.length-2]);
    }

    /**
     * Write a compiled version of this estimator to the specified
     * data output stream.  Caller is responsible for closing the
     * stream.
     *
     * @param out Data output stream to which to write a compiled
     * version of this estimator.
     * @throws IOException If there is an exception in the writing to
     * data output stream.
     */
    public void compileTo(ObjectOutput out) throws IOException {
        out.writeObject(new Externalizer(this));
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 4179100933315980535L;
        final TrainableEstimator mEstimator;
        public Externalizer() {
            this(null);
        }
        public Externalizer(TrainableEstimator estimator) {
            mEstimator = estimator;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            return new CompiledEstimator(in);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            AbstractExternalizable.compileOrSerialize(mEstimator.mTokenCategorizer,objOut);
            mEstimator.generateSymbols();
            mEstimator.mTagSymbolTable.compileTo(objOut);
            mEstimator.mTokenSymbolTable.compileTo(objOut);
            mEstimator.writeEstimator(mEstimator.mRootTagNode,objOut);
            mEstimator.writeEstimator(mEstimator.mRootTokenNode,objOut);
            objOut.writeDouble(mEstimator.mLogUniformVocabEstimate);
        }
    }



    /**
     * Train the estimator for a specific token and tag outcome
     * given a context.  If specified arguments are <code>null</code>,
     * only the non-<code>null</code> outcomes and contexts are
     * used for training.
     *
     * @param token Token outcome.
     * @param tag Tag outcome.
     * @param tagMinus1 Tag assigned to previous token.
     * @param tokenMinus1 Previous token.
     * @param tokenMinus2 Token occurring two tokens back.
     */
    public void trainOutcome(String token, String tag,
                             String tagMinus1,
                             String tokenMinus1, String tokenMinus2) {
        mTagSymbolTable.addSymbol(tag);
        mTokenSymbolTable.addSymbol(token);
        String tagMinus1Interior
            = (tagMinus1 == null)
            ? null
            : Tags.toInnerTag(tagMinus1);
        trainTokenModel(token,tag,tagMinus1Interior,tokenMinus1);
        trainTagModel(tag,tagMinus1Interior,tokenMinus1,tokenMinus2);
    }

    /**
     * Generates the symbol tables from the trie structures
     * representing the counts.
     */
    private void generateSymbols() {
        mRootTagNode.generateSymbols();
        // mRootTagNode.printSymbols();
        mRootTokenNode.generateSymbols();
        // mRootTokenNode.printSymbols();
        // make sure all token category symbols have token ids
        String[] tokenCategories = mTokenCategorizer.categories();
        for (int i = 0; i < tokenCategories.length; ++i)
            mTokenSymbolTable.addSymbol(tokenCategories[i]);
    }

    /**
     * Train the token half of the model.  If specified tags or
     * tokens are <code>null</code>, only the non-<code>null</code>
     * events and contexts are used for training.
     *
     * @param token Token outcome.
     * @param tag Tag outcome.
     * @param tagMinus1 Tag assigned to previous token.
     * @param tokenMinus1 Previous token.
     */
    public void trainTokenModel(String token,
                                String tag, String tagMinus1,
                                String tokenMinus1) {

        // CONTEXT             NODE            BACKOFF
        // Tag, Tag-1, W-1     nodeTagTag1W1   nodeTagTag1
        // Tag, Tag-1          nodeTagTag1     nodeTag
        // Tag                 nodeTag         -null-

        if (tag == null || token == null) return;
        Node nodeTag
            = mRootTokenNode.getOrCreateChild(tag,null,mTagSymbolTable);
        nodeTag.incrementOutcome(token,mTokenSymbolTable);

        if (tagMinus1 == null) return;
        Node nodeTagTag1
            = nodeTag.getOrCreateChild(tagMinus1,nodeTag,mTagSymbolTable);
        nodeTagTag1.incrementOutcome(token,mTokenSymbolTable);

        if (tokenMinus1 == null) return;
        Node nodeTagTag1W1
            = nodeTagTag1.getOrCreateChild(tokenMinus1,
                                           nodeTagTag1,mTokenSymbolTable);
        nodeTagTag1W1.incrementOutcome(token,mTokenSymbolTable);
    }


    /**
     * Train the tag model with the specified events.  Values
     * supplied may be <code>null</code>, in which case only
     * the non-<code>null</code> events and contexts are trained.
     *
     * @param tag Tag outcome.
     * @param tagMinus1 Tag assigned to previous token.
     * @param tokenMinus1 Previous token.
     * @param tokenMinus2 Token occurring two tokens back.
     */
    public void trainTagModel(String tag,
                              String tagMinus1,
                              String tokenMinus1, String tokenMinus2) {

        // CONTEXT             NODE             BACKOFF
        // Tag-1, W-1, W-2     nodeTag1W1W2     nodeTag1W1
        // Tag-1, W-1          nodeTag1W1       nodeTag1
        // Tag-1               nodeTag1         -null-

        if (tag == null || tagMinus1 == null) return;
        Node nodeTag1
            = mRootTagNode.getOrCreateChild(tagMinus1,null,mTagSymbolTable);
        nodeTag1.incrementOutcome(tag,mTagSymbolTable);

        if (tokenMinus1 == null) return;
        Node nodeTag1W1
            = nodeTag1.getOrCreateChild(tokenMinus1,
                                        nodeTag1,mTokenSymbolTable);
        nodeTag1W1.incrementOutcome(tag,mTagSymbolTable);

        if (tokenMinus2 == null) return;
        Node nodeTag1W1W2
            = nodeTag1W1.getOrCreateChild(tokenMinus2,
                                          nodeTag1W1,mTokenSymbolTable);
        nodeTag1W1W2.incrementOutcome(tag,mTagSymbolTable);
    }

    /**
     * Train the token outcome for the tag in the token model.
     * Only the context for the tag gets incremented; more specific
     * contexts are not affected.
     *
     * @param token Token outcome to train.
     * @param tag Tag outcome to train.
     */
    public void trainTokenOutcome(String token, String tag) {
        trainTokenModel(token,tag,null,null);
    }

    /**
     * Returns the number of nodes in the tag model.
     *
     * @return Number of nodes in the tag model.
     */
    public int numTagNodes() {
        return mRootTagNode.numNodes();
    }

    /**
     * Returns the number of outcomes in the tag model.
     *
     * @return Number of outcomes in the tag model.
     */
    public int numTagOutcomes() {
        return mRootTagNode.numCounters();
    }

    /**
     * Returns the number of nodes in the token model.
     *
     * @return Number of nodes in the token model.
     */
    public int numTokenNodes() {
        return mRootTokenNode.numNodes();
    }

    /**
     * Returns the number of outcomes in the token model.
     *
     * @return Number of outcomes in the token model.
     */
    public int numTokenOutcomes() {
        return mRootTokenNode.numCounters();
    }

    /**
     * Prune the models to the specified thresholds in terms of number
     * of training events for a node required to maintain a node in
     * the model.
     *
     * @param thresholdTag Minimum number of training events to
     * preserve a node in the tag model.
     * @param thresholdToken Minimum number of training events to
     * preserve a node in the token model.
     */
    public void prune(int thresholdTag, int thresholdToken) {
        mRootTagNode.prune(thresholdTag);
        mRootTokenNode.prune(thresholdToken);
    }


    /**
     * Smoothes the tag model by adding the specified count to every
     * legal transition.  That is, the count is addeed to each
     * estimate of <code>P(Tag2|Tag1)</code> where
     * <code>Tag1,Tag2</code> is a legal sequence.  This makes sure
     * that every legal transition is possible in the output, even if
     * it wasn't seen in the input.  A higher count increases the
     * degree of smoothing by moving the estimate of tag sequences
     * closer to uniform; this makes tags for which there was little
     * or no training data more likely than they would be with just
     * the smoothed maximum likelihood estimates.  There will be no
     * transition data for tags added only through a dictionary
     *
     * <P>Legality of a sequence is defined by {@link
     * Tags#illegalSequence(String,String)}.
     *
     * @param countToAdd Count to add to each legal sequence.
     */
    public void smoothTags(int countToAdd) {
        // mTagSymbolTable.add(Tags.OUT);
        String[] tags = mTagSymbolTable.symbols();
        for (int i = 0; i < tags.length; ++i) {
            String tag1 = tags[i];
            for (int j = 0; j < tags.length; ++j) {
                String tag2 = tags[j];
                if (Tags.illegalSequence(tag1,tag2)) continue;
                for (int k = 0; k < countToAdd; ++k) {
                    trainTagModel(tag2,tag1,null,null);
                }
            }
        }
    }

    /**
     * Writes an estimator picked out by a specified root node to
     * the specified data output stream.
     *
     * @param rootNode Node to write to data output stream.
     */
    private void writeEstimator(Node rootNode,
                                ObjectOutput out)
        throws IOException {

        rootNode.compileEstimates(mLambdaFactor);
        indexNodes(rootNode);
        out.writeInt(rootNode.numNodes());
        writeNodes(rootNode,out);
        out.writeInt(rootNode.numCounters());
        writeOutcomes(rootNode,out);
    }

    /**
     * Writes an integer index on each node, following a breadth-first
     * walk of the trie structure.
     *
     */
    private static void indexNodes(Node rootNode) {
        LinkedList<Node> nodeQueue = new LinkedList<Node>();
        nodeQueue.addLast(rootNode);
        int index = 0;
        while (nodeQueue.size() > 0) {
            Node node = nodeQueue.removeFirst();
            node.setIndex(index++);
            for (String childString : node.children())
                nodeQueue.addLast(node.getChild(childString));
        }
    }

    /**
     * Writes the nodes in the estimator to the specified data output
     * stream.
     *
     * @param out Data output stream to which symbol table is written.
     * @throws IOException If there is an exception on the underlying
     * output stream.
     */
    private static void writeNodes(Node rootNode, ObjectOutput out)
        throws IOException {

        LinkedList<Object[]> nodeQueue = new LinkedList<Object[]>();
        nodeQueue.addLast(new Object[] {rootNode,null} );
        int outcomesIndex = 0;
        int index = 0;
        while (nodeQueue.size() > 0) {

            // OUTPUT format per node.  Nodes output in breadth-first
            // search order using a queue.
            // int,      int,               int,
            // symbolID, firstOutcomeIndex, firstChildIndex,

            // float,          int
            // oneMinusLambda, backoffNodeIndex

            Object[] pair = nodeQueue.removeFirst();
            Node node = (Node) pair[0];
            out.writeInt(node.getSymbolID());
            out.writeInt(outcomesIndex);
            outcomesIndex += node.outcomes().size();
            TreeSet<String> children = new TreeSet<String>(node.children());
            if (children.size() == 0) {
                out.writeInt(index);
            } else {
                Iterator<String> childIterator = children.iterator();
                Node firstChild
                    = node.getChild(childIterator.next());
                out.writeInt(firstChild.index());
                index = firstChild.index() + node.children().size();
                childIterator = children.iterator();
                while (childIterator.hasNext()) {
                    String childName =  childIterator.next();
                    Node childNode = node.getChild(childName);
                    nodeQueue.addLast(new Object[] {childNode,childName});
                }
            }
            out.writeFloat(node.oneMinusLambda());
            out.writeInt(node.backoffNode() == null
                         ? -1 : node.backoffNode().index());
        }
    }

    /**
     * Writes the outcomes in the estimator to the specified data
     * output stream.
     *
     * @param out Data output stream to which symbol table is written.
     * @throws IOException If there is an exception on the underlying
     * output stream.
     */
    private static void writeOutcomes(Node rootNode, ObjectOutput out)
        throws IOException {

        LinkedList<Node> nodeQueue = new LinkedList<Node>();
        nodeQueue.addLast(rootNode);
        while (nodeQueue.size() > 0) {
            Node node = nodeQueue.removeFirst();
            for (String outcome : node.outcomes()) {
                OutcomeCounter outcomeCounter = node.getOutcome(outcome);
                out.writeInt(outcomeCounter.getSymbolID());
                out.writeFloat(outcomeCounter.estimate());
            }
            for (String child : node.children())
                nodeQueue.addLast(node.getChild(child));
        }
    }

}

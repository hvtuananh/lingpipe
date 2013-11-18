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

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.hmm.AbstractHmmEstimator;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tag.Tagging;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * A <code>CharLmHmmChunker</code> employs a hidden Markov model
 * estimator and tokenizer factory to learn a chunker.  This
 * estimator used is an instance of {@link AbstractHmmEstimator}
 * for underlying HMM estimation.  It uses a tokenizer factory to
 * break the chunks down into sequences of tokens and tags.
 *
 * <h4>Training</h4>
 *
 * <p>This class implements the {@code ObjectHandler<Chunking>}, which
 * may be used to supply training instances.  Every training event is
 * used to train the underlying HMM.  Training instances are supplied
 * through the chunk handler in the usual way.
 *
 * <p>Training instances for the tag handler
 * require the standard BIO tagging scheme in which the first token in
 * a chunk of type <code><i>X</i></code> is tagged
 * <code>B-<i>X</i></code> (&quot;begin&quot;), with all subsequent
 * tokens in the same chunk tagged <code>I-<i>X</i></code>
 * (&quot;in&quot;).  All tokens not in chunks are tagged
 * <code>O</code>.  For example, the tags required for training are:
 *
 * <blockquote><pre>
 * Yestereday      O
 * afternoon       O
 * ,               O
 * John            B-PER
 * J               I-PER
 * .               I-PER
 * Smith           I-PER
 * traveled        O
 * to              O
 * Washington      O
 * .               O</pre></blockquote>
 *
 * This is the same tagging scheme supplied in several corpora (Penn
 * BioIE, ConNLL, etc.)  Note that this is <i>not</i> the same tag
 * scheme used for the underlying HMM.  This simpler tag scheme shown
 * above is first converted to the more fine-grained tag scheme
 * described in the class documentation for {@link HmmChunker}.
 *
 * <h4>Training with a Dictionary</h4>
 *
 * This chunker may be trained with dictionary entries through the
 * method {@link #trainDictionary(CharSequence cSeq, String type)}.
 * Calling this method trains the emission probabilities for
 * the relevant tags determined by tokenizing the specifid character
 * sequence (after conversion to the underlying tag scheme defined
 * in {@link HmmChunker}).
 *
 * <p><b>Warning:</b>It is not enough to just train with a dictionary.
 * Dictionaries do not train the contexts in which elements show up.
 * Ordinary training data must also be supplied, and this data must have
 * some elements which are not part of chunks in order to train the
 * out tags.  If only a dictionary is used to train, null pointer exceptions
 * will show up at run time.
 *
 * <p>For example, calling
 *
 * <blockquote><pre>
 * charLmHmmChunker.trainDictionary(&quot;Washington&quot;, &quot;LOCATION&quot;);</pre></blockquote>
 *
 * would provide the token &quot;Washington&quot; as a training case
 * for emission from the tag <code>W_LOCATION</code>--the 'W_'
 * annotation is emitted because the trainDictionary uses the richer tag
 * set of {@link HmmChunker}.  Alterantively, calling:
 *
 * <blockquote><pre>
 * charLmHmmChunker.trainDictionary(&quot;John J. Smith&quot;, &quot;PERSON&quot;);</pre></blockquote>
 *
 * would train the tag <code>B_PERSON</code> to be trained
 * with the sequence &quot;John&quot;, the tag <code>I_PERSON</code>
 * to be trained with the tokens &quot;J&quot; and &quot;.&quot;,
 * and the tag <code>E_PERSON</code> to be trained with the
 * token &quot;Smith&quot;.  Furthermore, in this case, the transition
 * probabilities receive training instances for the three
 * transitions: <code>B_PERSON</code> to <code>M_PERSON</code>,
 * <code>M_PERSON</code> to <code>M_PERSON</code>, and finally,
 * <code>M_PERSON</code> to <code>E_PERSON</code>.
 *
 * <p>Note that there is no method to train non-chunk tokens, because
 * the categories assigned to them are context-specific, being
 * determined by the surrounding tokens.  An effective way to train
 * out categories in general is to supply them as part of entire
 * sentences that have no chunks in them.  Note that this only trains
 * the begin-sentence, end-sentence and internal tags for non-chunked
 * tokens.
 *
 * <p>To be useful, the dictionary entries must match the chunks that
 * should be found.  For instance, in the MUC training data, there are
 * many instances of <code>USAir</code>, the name of a United States
 * airline.  It might be thought that stock listings would help the
 * extraction of company names, but in fact, the company is
 * &quot;officially&quot; known as <code>USAirways Group</code>.
 *
 * <p>It is also important that training with dictionaries not be
 * done with huge diffuse dictionaries that wind up smoothing the
 * language models too much.  For example, training just locations
 * with a 2 million location gazzeteer once per entry will leave
 * obscure locations with an estimate close to those of New York
 * or Beijing.
 *
 * <h4>Tag Smoothing</h4>
 *
 * <p>The constructor {@link
 * #CharLmHmmChunker(TokenizerFactory,AbstractHmmEstimator,boolean)})}
 * accepts a flag that determines whether to smooth tag transition
 * probabilities.  If the flag is set to <code>true</code> in the
 * constructor, every time a new symbol is seen in the training data,
 * all of its relevant underlying tags are added to the symbol table
 * and all legal transitions among them and all other tags are
 * incremented by one.
 *
 * <p>If smoothing is turned off, only tag-tag transitions seen in the
 * training data are allowed.
 *
 * <p>The begin-sentence and end-sentence tags are automatically added
 * in the constructor, so that if no training data is provided, a
 * chunking with no chunks is returned.  This smoothing may not be
 * turned off.  Thus there will always be a non-zero probability in
 * the underlying HMM of starting with tag <code>BB_O_BOS</code> and
 * <code>WW_O_BOS</code>, of ending with the tag <code>EE_O_BOS</code>
 * or <code>WW_O_BOS</code>.  There will also always be a non-zero
 * probability of transitioning from
 * <code>BB_O_BOS</code> to <code>MM_O</code> and
 * to <code>EE_O_BOS</code>, and of transitioning from <code>MM_O</code> to
 * <code>MM_O</code> and <code>EE_O_BOS</code>.
 *
 *
 * <h4>Compilation</h4>
 *
 * <P>This class implements the {@link Compilable} interface.  To
 * compile a static model from the current state of training, call the
 * method {@link #compileTo(ObjectOutput)}.  The result of reading an
 * object from the corresponding object input stream will produce a
 * compiled HMM chunker of class {@link HmmChunker}, with the same
 * estimates as the current state of the chunker being compiled.
 *
 * <h4>Caching</h4>
 * <p>
 * Caching is turned off on the HMM decoder for this class by default.
 * If caching is turned on for instances of this class (through the
 * method {@link #getDecoder()} inherited from
 * <code>HmmChunker</code>), then training instances will fail to be
 * reflected in cached estimates and the results may be inconsistent
 * and may lead to exceptions.  Caching may be turned on as long as
 * there are no more training instances, but in this case, it is
 * almost always more efficient to just compile the model and turn
 * caching on for that.
 *
 * <p>After compilation, the returned chunker will have caching turned
 * off by default. To turn on caching for the compiled model, which is
 * highly recommended for efficiency, retrieve the HMM decoder and set
 * its cache.  For instance, to set up caching for both log estimates
 * and linear estimates, use the code:
 *
 * <blockquote><pre>
 * ObjectInput objIn = ...;
 * HmmChunker chunker = (HmmChunker) objIn.readObject();
 * HmmDecoder decoder = chunker.getDecoder();
 * decoder.setEmissionCache(new FastCache(1000000));
 * decoder.setEmissionLog2Cache(new FastCache(1000000));
 * </pre></blockquote>
 *
 * <h3>Reserved Tag</h3>
 *
 * <p>The tag <code>BOS</code> is reserved for use by the system
 * for encoding document start/end positions.  See {@link HmmChunker}
 * for more information.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.2
 */
public class CharLmHmmChunker extends HmmChunker
    implements ObjectHandler<Chunking>, 
               Compilable  {

    private final boolean mValidateTokenizer = false;

    private final AbstractHmmEstimator mHmmEstimator;
    private final TokenizerFactory mTokenizerFactory;
    private final Set<String> mTagSet = new HashSet<String>();

    private final boolean mSmoothTags;

    /**
     * Construct a <code>CharLmHmmChunker</code> from the specified
     * tokenizer factory and hidden Markov model estimator.  Smoothing
     * is turned off by default.  See {@link
     * #CharLmHmmChunker(TokenizerFactory,AbstractHmmEstimator,boolean)})
     * for more information.
     *
     * @param tokenizerFactory Tokenizer factory to tokenize chunks.
     * @param hmmEstimator Underlying HMM estimator.
     */
    public CharLmHmmChunker(TokenizerFactory tokenizerFactory,
                            AbstractHmmEstimator hmmEstimator) {
        this(tokenizerFactory,hmmEstimator,false);
    }

    /**
     * Construct a <code>CharLmHmmChunker</code> from the specified
     * tokenizer factory, HMM estimator and tag-smoothing flag.
     *
     * <p>If smoothing is turned on, then every time a new entity
     * type is seen in the training data, all possible underlying
     * tags involving that object are added to the symbol table,
     * and every legal transition among these tags and all other tags
     * is increment by count 1.
     *
     * <p>The tokenizer factory must be compilable in order for the model
     * to be compiled.  If it is not compilable, then attempting to
     * compile the model will raise an exception.
     *
     * @param tokenizerFactory Tokenizer factory to tokenize chunks.
     * @param hmmEstimator Underlying HMM estimator.
     * @param smoothTags Set to <code>true</code> for tag smoothing.
     */
    public CharLmHmmChunker(TokenizerFactory tokenizerFactory,
                            AbstractHmmEstimator hmmEstimator,
                            boolean smoothTags) {
        super(tokenizerFactory,new HmmDecoder(hmmEstimator));
        mHmmEstimator = hmmEstimator;
        mTokenizerFactory = tokenizerFactory;
        mSmoothTags = smoothTags;
        smoothBoundaries();
    }

    /**
     * Returns the underlying hidden Markov model estimator for this
     * chunker estimator.  This is the actual estimator used by this
     * class, so changes to it will affect wthis class's chunk
     * estimates.
     *
     * @return The underlying HMM estimator.
     */
    public AbstractHmmEstimator getHmmEstimator() {
        return mHmmEstimator;
    }

    /**
     * Return the tokenizer factory for this chunker.
     *
     * @return The tokenizer factory for this chunker.
     */
    @Override
    public TokenizerFactory getTokenizerFactory() {
        return mTokenizerFactory;
    }

    /**
     * Train the underlying hidden Markov model based on the specified
     * character sequence being of the specified type.  As described
     * in the class documentation above, this only trains the emission
     * probabilities and internal transitions for the character
     * sequence, based on the underlying tokenizer factory.
     *
     * <p><b>Warning:</b> Chunkers cannot only be trained with
     * a dictionary.  They require regular training data in order
     * to train the contexts in which dictionary items show up.
     * Attempting to train with only a dictionary will lead to
     * null pointer exceptions when attempting to decode.
     *
     * @param cSeq Character sequence on which to train.
     * @param type Type of chunk.
     */
    public void trainDictionary(CharSequence cSeq, String type) {
        char[] cs = Strings.toCharArray(cSeq);
        Tokenizer tokenizer = getTokenizerFactory().tokenizer(cs,0,cs.length);
        String[] tokens = tokenizer.tokenize();
        if (tokens.length < 1) {
            String msg = "Did not find any tokens in entry."
                + "Char sequence=" + cSeq;
            throw new IllegalArgumentException(msg);
        }
        AbstractHmmEstimator estimator = getHmmEstimator();
        SymbolTable table = estimator.stateSymbolTable();
        smoothBaseTag(type,table,estimator);
        if (tokens.length == 1) {
            estimator.trainEmit("W_" + type, tokens[0]);
            return;
        }
        String initialTag = "B_" + type;
        estimator.trainEmit(initialTag, tokens[0]);
        String prevTag = initialTag;
        for (int i = 1; i+1 < tokens.length; ++i) {
            String tag = "M_" + type;
            estimator.trainEmit(tag, tokens[i]);
            estimator.trainTransit(prevTag,tag);
            prevTag = tag;
        }
        String finalTag = "E_" + type;
        estimator.trainEmit(finalTag, tokens[tokens.length-1]);
        estimator.trainTransit(prevTag,finalTag);
    }

    // copied from old adapter; another copy in TrainTokenShapeChunker

    /**
     * Handle the specified chunking by tokenizing it, assigning tags
     * and training the underlying hidden Markov model.  For a description
     * of how chunkings are broken down into taggings, see the parent
     * class documentation in {@link HmmChunker}.
     *
     * @param chunking Chunking to use for training.
     */
    public void handle(Chunking chunking) {
        CharSequence cSeq = chunking.charSequence();
        char[] cs = Strings.toCharArray(cSeq);

        Set<Chunk> chunkSet = chunking.chunkSet();
        Chunk[] chunks = chunkSet.<Chunk>toArray(EMPTY_CHUNK_ARRAY);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);

        List<String> tokenList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();
        int pos = 0;
        for (Chunk nextChunk : chunks) {
            String type = nextChunk.type();
            int start = nextChunk.start();
            int end = nextChunk.end();
            outTag(cs,pos,start,tokenList,whiteList,tagList,mTokenizerFactory);
            chunkTag(cs,start,end,type,tokenList,whiteList,tagList,mTokenizerFactory);
            pos = end;
        }
        outTag(cs,pos,cSeq.length(),tokenList,whiteList,tagList,mTokenizerFactory);
        String[] toks = tokenList.<String>toArray(Strings.EMPTY_STRING_ARRAY);
        String[] whites = whiteList.toArray(Strings.EMPTY_STRING_ARRAY);
        String[] tags = tagList.toArray(Strings.EMPTY_STRING_ARRAY);
        if (mValidateTokenizer
            && !consistentTokens(toks,whites,mTokenizerFactory)) {
            String msg = "Tokens not consistent with tokenizer factory."
                + " Tokens=" + Arrays.asList(toks)
                + " Tokenization=" + tokenization(toks,whites)
                + " Factory class=" + mTokenizerFactory.getClass();
            throw new IllegalArgumentException(msg);
        }
        handle(toks,whites,tags);
    }

    void handle(String[] tokens, String[] whitespaces, String[] tags) {
        Tagging<String> tagging
            = new Tagging<String>(Arrays.asList(tokens),
                                  Arrays.asList(trainNormalize(tags)));
        getHmmEstimator().handle(tagging);
        smoothTags(tags);
    }


    /**
     * Compiles this model to the specified object output stream.  The
     * model may then be read back in using {@link
     * java.io.ObjectInput#readObject()}; the resulting object will be
     * instance of {@link HmmChunker}.  See the class documentation
     * above for information on setting the cache for a compiled
     * model.
     *
     * @throws IOException If there is an I/O error during the write.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    /**
     * Returns a string representation of the complete topology of the
     * underlying HMM with log2 transition probabilities.  Note that this
     * output does not represent the emission probabilities per category.
     *
     * @return String-based representation of this chunker.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        java.util.Set<String> expandedTagSet = new java.util.TreeSet<String>();
        expandedTagSet.add("MM_O");
        expandedTagSet.add("WW_O_BOS");
        expandedTagSet.add("BB_O_BOS");
        expandedTagSet.add("EE_O_BOS");
        for (Object tag0 : mTagSet) {
            String x = tag0.toString();
            expandedTagSet.add("B_" + x);
            expandedTagSet.add("M_" + x);
            expandedTagSet.add("E_" + x);
            expandedTagSet.add("W_" + x);
            expandedTagSet.add("BB_O_" + x);
            expandedTagSet.add("EE_O_" + x);
            expandedTagSet.add("WW_O_" + x);
        }

        for (Object tag0Obj : expandedTagSet) {
            String tag0 = tag0Obj.toString();
            sb.append("\n");
            sb.append("start(" + tag0 + ")=" + mHmmEstimator.startLog2Prob(tag0));
            sb.append("\n");
            sb.append("  end(" + tag0 + ")=" + mHmmEstimator.endLog2Prob(tag0));
            sb.append("\n");
            for (Object tag1Obj : expandedTagSet) {
                String tag1 = tag1Obj.toString();
                sb.append("trans(" + tag0 + "," + tag1 + ")="
                                   + mHmmEstimator.transitLog2Prob(tag0,tag1));
                sb.append("\n");
            }
        }
        return sb.toString();
    }



    void smoothBoundaries() {
        // mTagSet.add("BOS"); // BOS used for begin and end
        AbstractHmmEstimator hmmEstimator = getHmmEstimator();
        SymbolTable table = hmmEstimator.stateSymbolTable();
        String bbO = "BB_O_BOS";
        String mmO = "MM_O";
        String eeO = "EE_O_BOS";
        String wwO = "WW_O_BOS";

        table.getOrAddSymbol(bbO);
        table.getOrAddSymbol(mmO);
        table.getOrAddSymbol(eeO);
        table.getOrAddSymbol(wwO);

        hmmEstimator.trainStart(bbO);
        hmmEstimator.trainStart(wwO);

        hmmEstimator.trainEnd(eeO);
        hmmEstimator.trainEnd(wwO);

        hmmEstimator.trainTransit(bbO,mmO);
        hmmEstimator.trainTransit(bbO,eeO);
        hmmEstimator.trainTransit(mmO,mmO);
        hmmEstimator.trainTransit(mmO,eeO);
    }

    void smoothTags(String[] tags) {
        if (!mSmoothTags) return;
        AbstractHmmEstimator hmmEstimator = getHmmEstimator();
        SymbolTable table = hmmEstimator.stateSymbolTable();
        for (int i = 0; i < tags.length; ++i)
            smoothTag(tags[i],table,hmmEstimator);
    }

    void smoothTag(String tag, SymbolTable table,
                   AbstractHmmEstimator hmmEstimator) {

        smoothBaseTag(HmmChunker.baseTag(tag), table, hmmEstimator);

    }

    void smoothBaseTag(String baseTag, SymbolTable table,
                       AbstractHmmEstimator hmmEstimator) {

        if (!mTagSet.add(baseTag)) return; // already added
        if ("O".equals(baseTag)) return;  // constructor + other tags smooth "O"

        String b_x = "B_" + baseTag;
        String m_x = "M_" + baseTag;
        String e_x = "E_" + baseTag;
        String w_x = "W_" + baseTag;

        String bb_o_x = "BB_O_" + baseTag;
        // String mm_o = "MM_O"; // no tag modifier, just constant
        String ee_o_x = "EE_O_" + baseTag;
        String ww_o_x = "WW_O_" + baseTag;

        table.getOrAddSymbol(b_x);
        table.getOrAddSymbol(m_x);
        table.getOrAddSymbol(e_x);
        table.getOrAddSymbol(w_x);

        table.getOrAddSymbol(bb_o_x);
        // table.getOrAddSymbol("MM_O");  // in constructor
        table.getOrAddSymbol(ee_o_x);
        table.getOrAddSymbol(ww_o_x);

        hmmEstimator.trainStart(b_x);
        hmmEstimator.trainTransit(b_x,m_x);
        hmmEstimator.trainTransit(b_x,e_x);

        hmmEstimator.trainTransit(m_x,m_x);
        hmmEstimator.trainTransit(m_x,e_x);

        hmmEstimator.trainEnd(e_x);
        hmmEstimator.trainTransit(e_x,bb_o_x);

        hmmEstimator.trainStart(w_x);
        hmmEstimator.trainEnd(w_x);
        hmmEstimator.trainTransit(w_x,bb_o_x);

        hmmEstimator.trainTransit(bb_o_x,"MM_O");

        hmmEstimator.trainTransit("MM_O",ee_o_x); // handles all MM_O to ends

        hmmEstimator.trainTransit(ee_o_x,b_x);
        hmmEstimator.trainTransit(ee_o_x,w_x);

        hmmEstimator.trainStart(ww_o_x);
        hmmEstimator.trainTransit(ww_o_x,b_x);
        hmmEstimator.trainTransit(ww_o_x,w_x);

        hmmEstimator.trainTransit(e_x,"WW_O_BOS");
        hmmEstimator.trainTransit(w_x,"WW_O_BOS");

        hmmEstimator.trainTransit(bb_o_x,"EE_O_BOS");
        hmmEstimator.trainTransit("BB_O_BOS",ee_o_x);

        for (String type : mTagSet) {
            if ("O".equals(type)) continue;
            if ("BOS".equals(type)) continue;
            String bb_o_y = "BB_O_" + type;
            String ww_o_y = "WW_O_" + type;
            String ee_o_y = "EE_O_" + type;
            String b_y = "B_" + type;
            String w_y = "W_" + type;
            String e_y = "E_" + type;
            hmmEstimator.trainTransit(e_x,ww_o_y);
            hmmEstimator.trainTransit(e_x,b_y);
            hmmEstimator.trainTransit(e_x,w_y);
            hmmEstimator.trainTransit(w_x,ww_o_y);
            hmmEstimator.trainTransit(w_x,b_y);
            hmmEstimator.trainTransit(w_x,w_y);
            hmmEstimator.trainTransit(e_y,b_x);
            hmmEstimator.trainTransit(e_y,w_x);
            hmmEstimator.trainTransit(e_y,ww_o_x);
            hmmEstimator.trainTransit(w_y,b_x);
            hmmEstimator.trainTransit(w_y,w_x);
            hmmEstimator.trainTransit(w_y,ww_o_x);
            hmmEstimator.trainTransit(bb_o_x,ee_o_y);
            hmmEstimator.trainTransit(bb_o_y,ee_o_x);
        }

    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 4630707998932521821L;
        final CharLmHmmChunker mChunker;
        public Externalizer() {
            this(null);
        }
        public Externalizer(CharLmHmmChunker chunker) {
            mChunker = chunker;
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            TokenizerFactory tokenizerFactory
                = (TokenizerFactory) in.readObject();
            HiddenMarkovModel hmm
                = (HiddenMarkovModel) in.readObject();
            HmmDecoder decoder = new HmmDecoder(hmm);
            return new HmmChunker(tokenizerFactory,decoder);
        }
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            AbstractExternalizable.compileOrSerialize(mChunker.getTokenizerFactory(),objOut);
            AbstractExternalizable.compileOrSerialize(mChunker.getHmmEstimator(),objOut);
        }
    }

    // copied from old adapter; another copy in TrainTokenShapeChunker

    static final Chunk[] EMPTY_CHUNK_ARRAY = new Chunk[0];

    static void outTag(char[] cs, int start, int end,
                       List<String> tokenList, List<String> whiteList, List<String> tagList,
                       TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        whiteList.add(tokenizer.nextWhitespace());
        String nextToken;
        while ((nextToken = tokenizer.nextToken()) != null) {
            tokenList.add(nextToken);
            tagList.add(ChunkTagHandlerAdapter2.OUT_TAG);
            whiteList.add(tokenizer.nextWhitespace());
        }

    }

    static void chunkTag(char[] cs, int start, int end, String type,
                         List<String> tokenList, List<String> whiteList, List<String> tagList,
                         TokenizerFactory factory) {
        Tokenizer tokenizer = factory.tokenizer(cs,start,end-start);
        String firstToken = tokenizer.nextToken();
        tokenList.add(firstToken);
        tagList.add(ChunkTagHandlerAdapter2.BEGIN_TAG_PREFIX + type);
        while (true) {
            String nextWhitespace = tokenizer.nextWhitespace();
            String nextToken = tokenizer.nextToken();
            if (nextToken == null) break;
            tokenList.add(nextToken);
            whiteList.add(nextWhitespace);
            tagList.add(ChunkTagHandlerAdapter2.IN_TAG_PREFIX + type);
        }
    }

    public static boolean consistentTokens(String[] toks,
                                           String[] whitespaces,
                                           TokenizerFactory tokenizerFactory) {
        if (toks.length+1 != whitespaces.length) return false;
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = tokenizerFactory.tokenizer(cs,0,cs.length);
        String nextWhitespace = tokenizer.nextWhitespace();
        if (!whitespaces[0].equals(nextWhitespace)) {
            return false;
        }
        for (int i = 0; i < toks.length; ++i) {
            String token = tokenizer.nextToken();
            if (token == null) {
                return false;
            }
            if (!toks[i].equals(token)) {
                return false;
            }
            nextWhitespace = tokenizer.nextWhitespace();
            if (!whitespaces[i+1].equals(nextWhitespace)) {
                return false;
            }
        }
        return true;
    }

    List<String> tokenization(String[] toks, String[] whitespaces) {
        List<String> tokList = new ArrayList<String>();
        List<String> whiteList = new ArrayList<String>();
        char[] cs = getChars(toks,whitespaces);
        Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,0,cs.length);
        tokenizer.tokenize(tokList,whiteList);
        return tokList;
    }

    static char[] getChars(String[] toks, String[] whitespaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toks.length; ++i) {
            sb.append(whitespaces[i]);
            sb.append(toks[i]);
        }
        sb.append(whitespaces[whitespaces.length-1]);
        return Strings.toCharArray(sb);
    }

}

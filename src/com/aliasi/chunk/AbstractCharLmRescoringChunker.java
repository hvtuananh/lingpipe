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

import com.aliasi.lm.LanguageModel;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * An <code>AbstractCharLmRescoringChunker</code> provides the basic
 * character language-model rescoring model used by the trainable
 * <code>CharLmRescoringChunker</code> and its compiled version.
 *
 * <h2>Rescoring Model</h2>

 * The per-type language models simply model expressions of that type,
 * both within and across tokens.  The non-chunk model is responsible
 * not only for modeling the text not in chunks, but also in
 * predicting what the next chunk is given the text not in a chunk.
 *
 * <p>The exact model used is most easily described through an
 * example.  Consider the sentence <i>John J. Smith lives in
 * Washington.</i> with <i>John J. Smith</i> as a person-type chunk
 * and <i>Washington</i> as a location-type chunk.  The probablity of
 * this analysis derives from alternating chunk/non-chunk spans,
 * starting and ending with non-chunk spans.
 *
 * <blockquote><pre>
 * P<sub><sub>OUT</sub></sub>(<i>C</i><sub><sub>PER</sub></sub>|<i>C</i><sub><sub>BOS</sub></sub>)
 * * P<sub><sub>PER</sub></sub>(John J. Smith)
 * * P<sub><sub>OUT</sub></sub>( lives in <i>C</i><sub><sub>LOC</sub></sub>|<i>C</i><sub><sub>PER</sub></sub>)
 * * P<sub><sub>LOC</sub></sub>(Washington)
 * * P<sub><sub>OUT</sub></sub>(.<i>C</i><sub><sub>EOS</sub></sub>|<i>C</i><sub><sub>LOC</sub></sub>)
 * </pre></blockquote>
 *
 * Note that the chunk models <code>P<sub><sub>PER</sub></sub></code> and
 * <code>P<sub><sub>LOC</sub></sub></code> are bounded models, and thus predict
 * the first letter given the fact that it's the first letter, and
 * also encodes an end-of-string probability to model the end.  See
 * {@link com.aliasi.lm.NGramBoundaryLM} for more information on bounded models.
 *
 * <p>The non-chunk <code>P<sub><sub>OUT</sub></sub></code> model is a process
 * language model, but uses distinguished characters in much the same
 * way as the bounded models do internally.  In particular, we have
 * distinguished characters for each type
 * (e.g. <code><i>C</i><sub><sub>PER</sub></sub></code>), and for
 * begin-of-sentence and end-of-sentence markers
 * (e.g. <code><i>C</i><sub><sub>BOS</sub></sub></code>).  These must be chosen
 * so as not to conflict with any input characters in training or
 * decoding.  With this encoding, the non-chunk model bears the brunt
 * of the burden in predicting types.  To start, it conditions the
 * text it generates on the previous type, encoded as a character.  To
 * end, it generates the next chunk type, also encoded as a character.
 * This allows the models to be sensitive to the fact that phrases
 * like <i> lives in </i> (including the spaces on either side) are
 * conditioned on following a person.  The following chunk type,
 * location, is generated conditional on following
 * <code><i>C</i><sub><sub>PER</sub></sub> lives in</code>.  The only constraints
 * on the length of these dependencies is the length of the n-gram
 * models (and the size of the chunk/non-chunk spans).
 *
 * <p>The resulting model generates a properly normalized probability
 * distribution over chunkings.
 *
 * <h3>Reserved Tag</h3>
 *
 * <p>The tag <code>BOS</code> is reserved for use by the system
 * for encoding document start/end positions.  See {@link HmmChunker}
 * for more information.
 *
 * @author  Bob Carpenter
 * @version 3.0
 * @since   LingPipe2.3
 * @param <B> the type of the underlying n-best chunker being rescored
 * @param <O> the type of the process language model for non-entities
 * @param <C> the type of the sequence language model for entities
 */
public class AbstractCharLmRescoringChunker<B extends NBestChunker,
                                            O extends LanguageModel.Process,
                                            C extends LanguageModel.Sequence>
    extends RescoringChunker<B> {

    final Map<String,Character> mTypeToChar;
    final Map<String,C> mTypeToLM;

    final O mOutLM;

    final static char UNKNOWN_TYPE_CHAR = 0xFFFF;
    final static char BOS_CHAR = (char)(0xFFFE);
    final static char EOS_CHAR = (char)(BOS_CHAR-1);

    /**
     * Construct a rescoring chunker based on the specified underlying
     * chunker, with the specified number of underlying chunkings
     * rescored, based on the models and type encodings provided in
     * the last three arguments.  See the class documentation for more
     * information on the role of these parameters.
     *
     * @param baseNBestChunker Underlying chunker to rescore.
     * @param numChunkingsRescored Number of underlying chunkings
     * rescored by this chunker.
     * @param outLM The process language model for non-chunks.
     * @param typeToChar A mapping from chunk types to the characters that
     * encode them.
     * @param typeToLM A mapping from chunk types to the language
     * models used to model them.
     */
    public AbstractCharLmRescoringChunker(B baseNBestChunker,
                                          int numChunkingsRescored,
                                          O outLM,
                                          Map<String,Character> typeToChar,
                                          Map<String,C> typeToLM) {
        super(baseNBestChunker,numChunkingsRescored);
        mOutLM = outLM;
        mTypeToChar = typeToChar;
        mTypeToLM = typeToLM;
    }

    /**
     * Returns the character used to encode the specified type
     * in the model.  See the class documentation for more details
     * on the use of this character in the model.
     *
     * @param chunkType Type of chunk.
     * @return The character to code the type in the model.
     * @throws IllegalArgumentException If the specified chunk
     * type does not exist.
     */
    public char typeToChar(String chunkType) {
        Character result = mTypeToChar.get(chunkType);
        if (result == null)
            return UNKNOWN_TYPE_CHAR;
        return result.charValue();
    }

    /**
     * Returns the process language model for non-chunks.  This
     * is the actual language model used, so changes to it affect
     * this chunker.
     *
     * @return The process language model for non-chunks.
     */
    public O outLM() {
        return mOutLM;
    }

    /**
     * Returns the sequence language model for chunks of the
     * specified type.
     *
     * @param chunkType Type of chunk.
     * @return Language model for the specified chunk type.
     */
    public C chunkLM(String chunkType) {
        return mTypeToLM.get(chunkType);
    }

    /**
     * Performs rescoring of the base chunking output using
     * character language models.  See the class documentation
     * above for more information.
     *
     * @param chunking Chunking being rescored.
     * @return New score for chunker.
     */
    @Override
    public double rescore(Chunking chunking) {
        String text = chunking.charSequence().toString();
        double logProb = 0.0;
        int pos = 0;
        char prevTagChar = BOS_CHAR;
        for (Chunk chunk : orderedSet(chunking)) {
            int start = chunk.start();
            int end = chunk.end();
            String chunkType = chunk.type();
            char tagChar = typeToChar(chunkType);
            logProb += outLMEstimate(text.substring(pos,start),
                                     prevTagChar,tagChar);

            if (mTypeToLM.get(chunkType) == null) {
                System.out.println("\nFound null lm for type=" + chunkType
                                   + " Full type set =" + mTypeToLM.keySet());
                System.out.println("Chunking=" + chunking);
            }

            logProb += typeLMEstimate(chunkType,text.substring(start,end));
            pos = end;
            prevTagChar = tagChar;
        }
        logProb += outLMEstimate(text.substring(pos),
                                 prevTagChar,EOS_CHAR);
        return logProb;
    }


    double typeLMEstimate(String type, String text) {
        LanguageModel.Sequence lm = mTypeToLM.get(type);
        if (lm == null) {
            String msg = "Found null lm for type=" + type
                + " Full type set =" + mTypeToLM.keySet();
            System.out.println("TypeLM Estimate:\n" + msg);
            return -16.0 * text.length();
        }
        double estimate = lm.log2Estimate(text);
        return estimate;
    }


    double outLMEstimate(String text,
                         char prevTagChar, char nextTagChar) {
        String seq = prevTagChar + text + nextTagChar;
        String start = seq.substring(0,1);
        double estimate = mOutLM.log2Estimate(seq)
            - mOutLM.log2Estimate(start);
        return estimate;
    }

    static char[] wrapText(String text, char prevTagChar, char nextTagChar) {
        char[] cs = new char[text.length()+2];
        cs[0] = prevTagChar;
        cs[cs.length-1] = nextTagChar;
        for (int i = 0; i < text.length(); ++i)
            cs[i+1] = text.charAt(i);
        return cs;
    }

    static Set<Chunk> orderedSet(Chunking chunking) {
        Set<Chunk> orderedChunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
        orderedChunkSet.addAll(chunking.chunkSet());
        return orderedChunkSet;
    }






}

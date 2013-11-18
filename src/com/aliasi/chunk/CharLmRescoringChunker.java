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

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * A <code>CharLmRescoringChunker</code> provides a long-distance
 * character language model-based chunker that operates by rescoring
 * the output of a contained character language model HMM chunker.

 * <h3>The Underlying Chunker</h3>
 *
 * <p>This model performs rescoring over an underlying chunker.
 * The underlying chunkeris an instance of {@link CharLmHmmChunker},
 * configured with the specified tokenizer factory, n-gram length,
 * number of characters and interpolation ratio provided in the
 * constructor.  The underlying chunker may be configured after
 * retrieving it through the superclass's {@link #baseChunker()}
 * method.  The typical use of this is to configure caching.
 *
 * <h3>The Rescoring Model</h3>
 *
 * <p>The rescoring model used by this chunker is based on a bounded
 * character language model per chunk type with an additional
 * process character language model for text not in chunks.  The
 * remaining details are described in the class documentation for
 * the superclass {@link AbstractCharLmRescoringChunker}.
 *
 * <h3>Training and Compilation</h3>
 *
 * <p>This chunker is trained in the usual way through calls to the
 * appropriate <code>handle()</code> method.  The method {@link
 * #handle(Chunking)} implements the {@code ObjectHandler<Chunking>} interface
 * and allows for training through chunking examples.  
 * A model is compiled by calling the {@link
 * Compilable} interface method {@link #compileTo(ObjectOutput)}.
 * The compiled model is an instance of a <code>AbstractCharLmRescoringChunker</code>,
 * and its underlying chunker may be recovered that way.
 *
 * <h3>Runtime Configuration</h3>
 *
 * <p>The underlying chunker is recoverable as a character language
 * model HMM chunker through {@link #baseChunker()}.  The
 * non-chunk process n-gram character language model is returned by
 * {@link #outLM()}, whereas the chunk models are returned
 * by {@link #chunkLM(String)}.
 *
 * <p>The components of a character LM rescoring chunker are accessible
 * in their training format for methods on this class, as described above.
 *
 * <p>The compiled models are instances of {@link RescoringChunker},
 * which allow their underlying chunker to be retrieved through {@link
 * #baseChunker()} and then configured.  The other run-time models, for
 * may be retrieved through the superclass's
 *
 * <h3>Reserved Tag</h3>
 *
 * <p>The tag <code>BOS</code> is reserved for use by the system
 * for encoding document start/end positions.  See {@link HmmChunker}
 * for more information.
 *
 * @author  Bob Carpenter
 * @version 4.0.0
 * @since   LingPipe2.3
 */
public class CharLmRescoringChunker
    extends AbstractCharLmRescoringChunker<CharLmHmmChunker, // PLEASE IGNORE DEPRECATION WARNING
                                           NGramProcessLM,
                                           NGramBoundaryLM>
    implements ObjectHandler<Chunking>, 
               Compilable {

    final int mNGram;
    final int mNumChars;
    final double mInterpolationRatio;

    char mNextCodeChar = (char) (BOS_CHAR - 2);

    /**
     * Construct a character language model rescoring chunker based on
     * the specified components.  Tags in the underlying model are not
     * smoothed by default (see the full constructor's documentation:
     * {@link
     * #CharLmRescoringChunker(TokenizerFactory,int,int,int,double,boolean)}.
     *
     * @param tokenizerFactory Tokenizer factory for boundaries.
     * @param numChunkingsRescored Number of underlying chunkings rescored.
     * @param nGram N-gram length for all models.
     * @param numChars Number of characters in the training and
     * run-time character sets.
     * @param interpolationRatio Underlying language-model
     * interpolation ratios.
     */
    public CharLmRescoringChunker(TokenizerFactory tokenizerFactory,
                                  int numChunkingsRescored,
                                  int nGram,
                                  int numChars,
                                  double interpolationRatio) {
        super(new CharLmHmmChunker(tokenizerFactory,
                                   new HmmCharLmEstimator(nGram,
                                                          numChars,
                                                          interpolationRatio)),
              numChunkingsRescored,
              new NGramProcessLM(nGram,numChars,interpolationRatio),
              new HashMap<String,Character>(),
              new HashMap<String,NGramBoundaryLM>());
        mNGram = nGram;
        mNumChars = numChars;
        mInterpolationRatio = interpolationRatio;
    }

    /**
     * Construct a character language model rescoring chunker based on
     * the specified components.
     *
     * <p>Whether tags are smoothed in the underlying model is determined
     * by the flag in the constructor.  See {@link CharLmHmmChunker}'s
     * class documentation for more information on the effects of
     * smoothing.
     *
     *
     * @param tokenizerFactory Tokenizer factory for boundaries.
     * @param numChunkingsRescored Number of underlying chunkings rescored.
     * @param nGram N-gram length for all models.
     * @param numChars Number of characters in the training and
     * run-time character sets.
     * @param interpolationRatio Underlying language-model
     * interpolation ratios.
     * @param smoothTags Set to <code>true</code> to smooth tags in underlying
     * chunker.
     */
    public CharLmRescoringChunker(TokenizerFactory tokenizerFactory,
                                  int numChunkingsRescored,
                                  int nGram,
                                  int numChars,
                                  double interpolationRatio,
                                  boolean smoothTags) {
        super(new CharLmHmmChunker(tokenizerFactory,
                                   new HmmCharLmEstimator(nGram,
                                                          numChars,
                                                          interpolationRatio),
                                   smoothTags),
              numChunkingsRescored,
              new NGramProcessLM(nGram,numChars,interpolationRatio),
              new HashMap<String,Character>(),
              new HashMap<String,NGramBoundaryLM>());
        mNGram = nGram;
        mNumChars = numChars;
        mInterpolationRatio = interpolationRatio;
    }

    /**
     * Trains this chunker with the specified chunking.
     *
     * @param chunking Training data.
     */
    public void handle(Chunking chunking) {
        // train underlying
        ObjectHandler<Chunking> handler2 = baseChunker();
        handler2.handle(chunking);

        // train rescorer
        String text = chunking.charSequence().toString();
        char prevTagChar = BOS_CHAR;
        int pos = 0;
        for (Chunk chunk : orderedSet(chunking)) {
            int start = chunk.start();
            int end = chunk.end();
            if (pos > start) {
                String msg = "Chunk overlap for chunk=" + chunk
                    + " in chunking=" + chunking;
                throw new IllegalArgumentException(msg);
            }
            String chunkType = chunk.type();
            createTypeIfNecessary(chunkType);
            char tagChar = typeToChar(chunkType);
            trainOutLM(text.substring(pos,start),
                       prevTagChar,tagChar);
            trainTypeLM(chunkType,text.substring(start,end));
            pos = end;
            prevTagChar = tagChar;
        }
        trainOutLM(text.substring(pos),
                   prevTagChar,EOS_CHAR);
    }


    /**
     * Compiles this model to the specified object output stream.  The
     * model may then be read back in using {@link
     * java.io.ObjectInput#readObject()}; the resulting object will be
     * an instance of {@link AbstractCharLmRescoringChunker}.
     *
     * @throws IOException If there is an I/O error during the write.
     * @throws IllegalArgumentException If the tokenizer factory supplied to
     * the constructor of this class is not compilable.
     */
    public void compileTo(ObjectOutput objOut) throws IOException {
        objOut.writeObject(new Externalizer(this));
    }

    /**
     * Provides the specified character sequence data as training data
     * for the language model of the specfied type.  This method calls
     * the method of the same signature on the trainable base chunker.
     * The language model for the specified type will be created if it
     * has not been seen previously.
     *
     * <p><i>Warning:</i> It is not sufficient to only train a model
     * using this method.  Annotated data with a representative
     * balance of entities and non-entity text is required to train
     * the overall likelihood of entities and the contexts in which
     * they occur.  Use of this method will <i>not</i> bias the
     * likelihoods of entities occurring.  But, it might cause the
     * common entities in the training data to be overwhelmed if a
     * large dictionary is used.  One possibility is to train the
     * basic data multiple times relative to the dictionary (or
     * vice-versa).
     *
     * @param cSeq Character sequence for training.
     * @param type Type of character sequence.
     */
    public void trainDictionary(CharSequence cSeq, String type) {
        baseChunker().trainDictionary(cSeq,type);
        trainTypeLM(type,cSeq);
    }

    /**
     * Trains the language model for non-entities using the specified
     * character sequence.
     *
     * <p><i>Warning</i>: Training using this method biases the
     * likelihood of entities downward, because it does not train the
     * likelihood of a non-entity character sequence ending and being
     * followed by an entity of a specified type.  Thus this method is
     * best used to seed a dictionary of common words that are
     * relatively few in number relative to the entity-annotated
     * training data.
     *
     * @param cSeq Data to train the non-entity (out) model.
     */
    public void trainOut(CharSequence cSeq) {
        outLM().train(cSeq);
    }


    void createTypeIfNecessary(String chunkType) {
        if (mTypeToChar.containsKey(chunkType)) return;
        Character c = Character.valueOf(mNextCodeChar--);
        mTypeToChar.put(chunkType,c);
        NGramBoundaryLM lm
            = new NGramBoundaryLM(mNGram,mNumChars,mInterpolationRatio,
                                  (char) 0xFFFF);
        mTypeToLM.put(chunkType,lm);
    }


    void trainTypeLM(String type, CharSequence text) {
        createTypeIfNecessary(type);
        NGramBoundaryLM lm = mTypeToLM.get(type);
        lm.train(text);
    }

    void trainOutLM(String text,
                    char prevTagChar, char nextTagChar) {
        String trainSeq = prevTagChar + text + nextTagChar;
        outLM().train(trainSeq);
        outLM().substringCounter().decrementUnigram(prevTagChar);
    }

    static class Externalizer extends AbstractExternalizable {
        private static final long serialVersionUID = 3555143657918695241L;
        final CharLmRescoringChunker mChunker;
        public Externalizer() {
            this(null);
        }
        public Externalizer(CharLmRescoringChunker chunker) {
            mChunker = chunker;
        }
        // base chunker:NBestChunker
        // numChunkingsRescored:int
        // numTypes:int
        // (type:UTF,codeChar:char,ngramBoundaryLM:LanguageModel.Process)**numTypes
        // outLM:NGram.Process
        @Override
        public void writeExternal(ObjectOutput objOut) throws IOException {
            mChunker.baseChunker().compileTo(objOut);
            objOut.writeInt(mChunker.numChunkingsRescored());
            String[] types
                = mChunker.mTypeToLM.keySet().<String>toArray(Strings.EMPTY_STRING_ARRAY);
            objOut.writeInt(types.length);
            for (int i = 0; i < types.length; ++i) {
                objOut.writeUTF(types[i]);
                objOut.writeChar(mChunker.typeToChar(types[i]));
                NGramBoundaryLM lm
                    = mChunker.mTypeToLM.get(types[i]);
                lm.compileTo(objOut);
            }
            mChunker.outLM().compileTo(objOut);
        }
        @Override
        public Object read(ObjectInput in)
            throws ClassNotFoundException, IOException {

            NBestChunker baseChunker = (NBestChunker) in.readObject();
            int numChunkingsRescored = in.readInt();
            int numTypes = in.readInt();
            Map<String,Character> typeToChar = new HashMap<String,Character>();
            Map<String,LanguageModel.Sequence> typeToLM = new HashMap<String,LanguageModel.Sequence>();
            for (int i = 0; i < numTypes; ++i) {
                String type = in.readUTF();
                char c = in.readChar();
                LanguageModel.Sequence lm
                    = (LanguageModel.Sequence) in.readObject();
                typeToChar.put(type,Character.valueOf(c));
                typeToLM.put(type,lm);
            }
            LanguageModel.Process outLM
                = (LanguageModel.Process) in.readObject();
            return new AbstractCharLmRescoringChunker<NBestChunker,LanguageModel.Process,LanguageModel.Sequence>(baseChunker,
                                                      numChunkingsRescored,
                                                      outLM,
                                                      typeToChar,
                                                      typeToLM);
        }
    }



}

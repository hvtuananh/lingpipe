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

import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ObjectHandler;

/**
 * A <code>SentenceEvaluator</code> handles reference chunkings by
 * constructing a response chunking and adding them to a sentence
 * evaluation.  The resulting evaluation may be retrieved through the
 * method {@link #evaluation()} at any time. 
 *
 * <P>This evaluator class implements the {@code
 * ObjectHandler<Chunking>} interface.  The chunkings passed to the
 * {@link #handle(Chunking)} method are treated as reference
 * chunkings.  Their character sequence is extracted using {@link
 * Chunking#charSequence()} and the contained sentence chunker is used
 * to produce a response chunking over the character sequence.  The
 * resulting pair of chunkings is passed to the contained sentence
 * evaluation.
 * 
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe2.1
 */
public class SentenceEvaluator 
    implements ObjectHandler<Chunking> {

    private final SentenceChunker mSentenceChunker;
    private final SentenceEvaluation mSentenceEvaluation;

    /**
     * Construct a sentence evaluator using the specified sentence
     * chunker to construct responses.
     *
     * @param sentenceChunker Sentence chunker for responses.
     */
    public SentenceEvaluator(SentenceChunker sentenceChunker) {
    mSentenceEvaluation = new SentenceEvaluation();
    mSentenceChunker = sentenceChunker;
    }

    /**
     * Handle the specified reference chunking by extracting its
     * character sequence, producing a response chunking from the
     * contained sentence chunker, and adding the reference and
     * result to the evaluation.
     *
     * @param refChunking Refernece chunking.
     */
    public void handle(Chunking refChunking) {
    CharSequence cSeq = refChunking.charSequence();
    Chunking responseChunking = mSentenceChunker.chunk(cSeq);
    mSentenceEvaluation.addCase(refChunking,responseChunking);
    }

    /**
     * Return the evaluation for this evaluator.  This may be
     * inspected at any time.  If evaluations are later added to this
     * evaluator, the returned evaluation will reflect them.
     *
     * @return Sentence evaluation for this evaluator.
     */
    public SentenceEvaluation evaluation() {
    return mSentenceEvaluation;
    }

}


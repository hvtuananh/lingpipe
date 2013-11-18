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

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;

import com.aliasi.util.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>SentenceEvaluation</code> stores and reports the results of
 * evaluating a set of reference sentence chunkings and response
 * sentence chunkings. Evaluation results are available through the
 * method {@link #chunkingEvaluation()}, which returns the evaluation
 * of the sentences as chunkings, and the method {@link
 * #endBoundaryEvaluation()}, which returns the evaluation of the
 * sentences chunkings solely on the basis of the end boundary index of the sentence.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.1
 */
public class SentenceEvaluation {

    private final ChunkingEvaluation mChunkingEvaluation;
    private final PrecisionRecallEvaluation mEndBoundaryEvaluation;
    private final Set<ChunkAndCharSeq> mTPBoundaries = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFPBoundaries = new HashSet<ChunkAndCharSeq>();
    private final Set<ChunkAndCharSeq> mFNBoundaries = new HashSet<ChunkAndCharSeq>();
    private final Chunking[] mLastCase = new Chunking[2];
    /**
     * Construct a sentence evaluation.
     */
    public SentenceEvaluation() {
        mChunkingEvaluation = new ChunkingEvaluation();
        mEndBoundaryEvaluation = new PrecisionRecallEvaluation();
    }

    /**
     * Add the case corresponding to the specified reference and
     * response chunkings.  The chunkings should only contain sentence
     * chunks with chunk type {@link
     * SentenceChunker#SENTENCE_CHUNK_TYPE}.
     *
     * @param referenceChunking The reference chunking.
     * @param responseChunking The response chunking.
     * @throws IllegalArgumentException If the reference chunking and
     * response chunking are not over the same characters or if either
     * contains chunks of type other than
     * <code>SentenceChunker.SENTENCE_CHUNK_TYPE</code>.
     */
    public void addCase(Chunking referenceChunking, Chunking responseChunking) {
        if (!Strings.equalCharSequence(referenceChunking.charSequence(),
                                       responseChunking.charSequence())) {
            String msg = "Underlying char sequences must have same characters."
                + " Found referenceChunking.charSequence()="
                + referenceChunking.charSequence()
                + " responseChunking.charSequence()="
                + responseChunking.charSequence();
            throw new IllegalArgumentException(msg);
        }
        verifySentenceTypes("reference",referenceChunking);
        verifySentenceTypes("response",responseChunking);

        mChunkingEvaluation.addCase(referenceChunking,responseChunking);

        mLastCase[0] = referenceChunking;
        mLastCase[1] = responseChunking;

        // evaluate chunkings solely on basis of end indices.
        Map<Integer,Chunk> endChunkMap = new HashMap<Integer,Chunk>();
        CharSequence cSeq = referenceChunking.charSequence();
        for (Chunk refChunk : referenceChunking.chunkSet()) {
            Integer end = Integer.valueOf(refChunk.end());
            endChunkMap.put(end,refChunk);
        }
        for (Chunk respChunk : responseChunking.chunkSet()) {
            Integer end = Integer.valueOf(respChunk.end());
            boolean inRef = endChunkMap.containsKey(end);
            ChunkAndCharSeq ccs = new ChunkAndCharSeq(respChunk,cSeq);
            if (inRef) {
                mTPBoundaries.add(ccs);
                mEndBoundaryEvaluation.addCase(true,true);
                endChunkMap.remove(end);
            } else {
                mFPBoundaries.add(ccs);
                mEndBoundaryEvaluation.addCase(false,true);
            }
        }
        //    Vector falseNegatives = endChunkMap.values();
        for (Chunk refChunk : endChunkMap.values()) {
            mFNBoundaries.add(new ChunkAndCharSeq(refChunk,cSeq));
            mEndBoundaryEvaluation.addCase(true,false);
        }
    }

    /**
     * Return the chunking evaluation for this sentence evaluation.
     * This is the evaluation based purely on the chunks and their
     * matching, not on any sentence-specific evaluation
     *
     * @return The chunking evaluation for this sentence evaluation.
     */
    public ChunkingEvaluation chunkingEvaluation() {
        return mChunkingEvaluation;
    }

    /**
     * Returns a precision/recall evaluation based on the end
     * boundaries in the reference and response cases.  End boundaries
     * in the response and the reference are true positives, those in
     * the response but not the reference are false positives, and those
     * in the reference but not the response are false negatives.
     */
    public PrecisionRecallEvaluation endBoundaryEvaluation() {
        return mEndBoundaryEvaluation;
    }

    /**
     * Return the set of chunks and character sequences whose end
     * boundaries are in both the reference and response.
     *
     * @return The set of true-positive end-boundary chunks.
     */
    public Set<ChunkAndCharSeq> truePositiveEndBoundaries() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mTPBoundaries);
    }

    /**
     * Return the set of chunks and character sequences whose end
     * boundaries are in response but not the reference chunking.
     *
     * @return The set of false-positive end-boundary chunks.
     */
    public Set<ChunkAndCharSeq> falsePositiveEndBoundaries() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFPBoundaries);
    }

    /**
     * Return the set of chunks and character sequences whose end
     * boundaries are in reference but not the response chunking.
     *
     * @return The set of false-negative end-boundary chunks.
     */
    public Set<ChunkAndCharSeq> falseNegativeEndBoundaries() {
        return Collections.<ChunkAndCharSeq>unmodifiableSet(mFNBoundaries);
    }

    static void verifySentenceTypes(String input, Chunking chunking) {
        for (Chunk chunk : chunking.chunkSet()) {
            if (!chunk.type().equals(SentenceChunker.SENTENCE_CHUNK_TYPE)) {
                String msg = "Chunk must have sentence type."
                    + " Found type=" + chunk.type()
                    + " in input type=" + input;
                throw new IllegalArgumentException(msg);
            }
        }
    }

    /**
     * Returns a formatted listing for the last case that was
     * evaluated, showing the underlying character sequence for the
     * chunkings, annotated with the sentence boundaries from the
     * reference and response chunking, with linebreaks inserted every
     * lineLength characters.  The lineLength must be a positive
     * integer.
     *
     * @param lineLength The line length of the formatted string.
     * @throws IllegalArgumentException If the lineLength is < 1.
     * @throws IllegalStateException If no cases have been evaluated.
     */
    public String lastCaseToString(int lineLength) {
        if (lineLength < 1) {
            String msg = "Line length must be greater than 0.";
            throw new IllegalArgumentException(msg);
        }
        if (mLastCase[0] == null || mLastCase[1] == null) {
            String msg = "No cases have been evaluated.";
            throw new IllegalStateException(msg);
        }
        return sentenceCaseToString(mLastCase[0],mLastCase[1],lineLength);
    }

    /**
     * Given a pair of reference and response chunkings, returns a
     * string showing the underlying character sequence
     * chunking pair, annotated with the
     * sentence boundaries from each chunking, with linebreaks
     * inserted every lineLength characters.  The chunkings should
     * only contain sentence chunks with chunk type {@link
     * SentenceChunker#SENTENCE_CHUNK_TYPE}. The lineLength must be a
     * positive integer.
     *
     * @param referenceChunking The reference chunking.
     * @param responseChunking The response chunking.
     * @param lineLength The line length of the formatted string.
     * @throws IllegalArgumentException If the reference chunking and
     * response chunking are not over the same characters or if either
     * contains chunks of type other than
     * <code>SentenceChunker.SENTENCE_CHUNK_TYPE</code>, or if the
     * lineLength is < 1.
     */
    public static String sentenceCaseToString(Chunking referenceChunking, Chunking responseChunking, int lineLength) {
        if (lineLength < 1) {
            String msg = "Line length must be greater than 0.";
            throw new IllegalArgumentException(msg);
        }
        if (!Strings.equalCharSequence(referenceChunking.charSequence(),
                                       responseChunking.charSequence())) {
            String msg = "Underlying char sequences must have same characters."
                + " Found referenceChunking.charSequence()="
                + referenceChunking.charSequence()
                + " responseChunking.charSequence()="
                + responseChunking.charSequence();
            throw new IllegalArgumentException(msg);
        }
        SentenceEvaluation.verifySentenceTypes("reference",referenceChunking);
        SentenceEvaluation.verifySentenceTypes("response",responseChunking);

        CharSequence cSeq = referenceChunking.charSequence();
        int[] refEnds = new int[referenceChunking.chunkSet().size()];
        int iRef = 0;
        for (Chunk refChunk : referenceChunking.chunkSet())
            refEnds[iRef++]= refChunk.end()-1;
        int[] respEnds = new int[responseChunking.chunkSet().size()];
        int iResp = 0;
        for (Chunk respChunk : responseChunking.chunkSet())
            respEnds[iResp++]= respChunk.end()-1;
        StringBuilder sbOut = new StringBuilder();
        StringBuilder refLine =  new StringBuilder();
        StringBuilder textLine = new StringBuilder();
        StringBuilder respLine = new StringBuilder();
        refLine.append("ref:  ");
        textLine.append("text: ");
        respLine.append("resp: ");
        int cLen = cSeq.length();
        int refIndex = 0;
        int respIndex = 0;
        for (int i = 0; i < cLen; i++) {
            textLine.append(cSeq.charAt(i));
            if (refIndex < refEnds.length && respIndex < respEnds.length
                && refEnds[refIndex]==i && respEnds[respIndex]==i ) {
                refLine.append("+");
                respLine.append("+");
                refIndex++;
                respIndex++;
            }
            else if (refIndex < refEnds.length && refEnds[refIndex]==i) {
                refLine.append("X");
                respLine.append("-");
                refIndex++;
            }
            else if (respIndex < respEnds.length && respEnds[respIndex]==i) {
                refLine.append("-");
                respLine.append("X");
                respIndex++;
            }
            else {
                refLine.append("-");
                respLine.append("-");
            }
            if (i > 0 && i%lineLength == 0) {
                sbOut.append(refLine+"\n");
                sbOut.append(textLine+"\n");
                sbOut.append(respLine+"\n");
                sbOut.append("\n");
                refLine.setLength(0);
                textLine.setLength(0);
                respLine.setLength(0);
                refLine.append("ref:  ");
                textLine.append("text: ");
                respLine.append("resp: ");
            }
        }
        sbOut.append(refLine+"\n");
        sbOut.append(textLine+"\n");
        sbOut.append(respLine+"\n");
        sbOut.append("\n\n");
        return sbOut.toString();
    }


}

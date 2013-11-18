import java.io.File;

import com.aliasi.chunk.*;
import com.aliasi.hmm.*;
import com.aliasi.corpus.*;
import com.aliasi.corpus.parsers.*;
import com.aliasi.tokenizer.*;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import java.io.*;
import java.util.*;

public class EvalGeneTagChunker {

    static final int MAX_N_GRAM = 8;
    static final int NUM_CHARS = 256;
    static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior

    static EvalChunker sEvalChunker;
    static ChunkerEvaluator sEvaluator;

    // java EvalGeneTagChunker <goldFile> <corpusFile> <numFolds>
    @SuppressWarnings("deprecation")
    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        File goldFile = new File(args[0]);
        File corpusFile = new File(args[1]);
        int numFolds = Integer.valueOf(args[2]);

        System.out.println("Parsing Training Data");
        ChunkingAccumulator accum = new ChunkingAccumulator();
        @SuppressWarnings("deprecation")
        com.aliasi.corpus.parsers.GeneTagChunkParser parser 
            = new com.aliasi.corpus.parsers.GeneTagChunkParser(goldFile);
        parser.setHandler(accum);
        parser.parse(corpusFile);
        Chunking[] chunkings = accum.getChunkings();
        System.out.println("Found " + chunkings.length + " chunkings.");

        sEvalChunker = new EvalChunker();
        sEvaluator = new ChunkerEvaluator(sEvalChunker);
        sEvaluator.setVerbose(false);
        sEvaluator.setMaxNBest(128);
        sEvaluator.setMaxNBestReport(8);
        sEvaluator.setMaxConfidenceChunks(16);
        for (int i = 0; i < numFolds; ++i)
            eval(chunkings,i,numFolds);

        System.out.println("Base Evaluation\n" + sEvaluator);
        double[][] prCurve = sEvaluator.confidenceEvaluation().prCurve(true);
        System.out.println("PR Curve");
        // r, p
        for (int i = 0; i < prCurve.length; ++i)
            System.out.println(prCurve[i][0] + " " + prCurve[i][1]);
    }
    static void eval(Chunking[] chunkings, int fold, int numFolds)
        throws IOException, ClassNotFoundException {

        System.out.println("Evaluating fold=" + (fold+1)
                           + " of " + numFolds);
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        HmmCharLmEstimator hmmEstimator
            = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION);
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,hmmEstimator);

        for (int i = 0; i < numFolds; ++i) {
            if (i != fold) {
                System.out.println("     train, fold=" + (i+1));
                visitFold(chunkings,i,numFolds,chunkerEstimator);
            }
        }

        System.out.println("     compiling");
        sEvalChunker.mChunker
            = (HmmChunker) AbstractExternalizable.compile(chunkerEstimator);
        System.out.println("     evaluating, fold=" + (fold+1));
        visitFold(chunkings,fold,numFolds,sEvaluator);
    }

    static void visitFold(Chunking[] chunkings, int fold, int numFolds,
                          ObjectHandler<Chunking> handler) {
        int start = startFold(chunkings.length, fold, numFolds);
        int end = startFold(chunkings.length, fold+1, numFolds);
        for (int i = start; i < end; ++i)
            handler.handle(chunkings[i]);
    }

    static int startFold(double n, double fold, double numFolds) {
        return (int) (n * fold / numFolds);
    }

    static class ChunkingAccumulator 
        implements ObjectHandler<Chunking> {
        final Set<Chunking> mChunkingSet = new HashSet<Chunking>();
        public void handle(Chunking chunking) {
            mChunkingSet.add(chunking);
        }
        Chunking[] getChunkings() {
            return mChunkingSet.toArray(new Chunking[0]);
        }
    }

    static class EvalChunker implements Chunker, NBestChunker, ConfidenceChunker {
        HmmChunker mChunker;
        public Chunking chunk(CharSequence in) {
            return mChunker.chunk(in);
        }
        public Chunking chunk(char[] cs, int start, int end) {
            return mChunker.chunk(cs,start,end);
        }
        public Iterator<Chunk> nBestChunks(char[] cs, int start, int end, int max) {
            return mChunker.nBestChunks(cs,start,end,max);
        }
        public Iterator<ScoredObject<Chunking>> nBest(char[] cs, int start, int end, int max) {
            return mChunker.nBest(cs,start,end,max);
        }
    }


    }

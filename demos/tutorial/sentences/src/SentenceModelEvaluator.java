import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkAndCharSeq;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.sentences.HeuristicSentenceModel;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceEvaluation;
import com.aliasi.sentences.SentenceEvaluator;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Iterator;
import java.util.Set;

import org.xml.sax.SAXException;

public class SentenceModelEvaluator {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException, SAXException {

        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        SentenceModel sentenceModel  = new MedlineSentenceModel();
        SentenceChunker sentenceChunker = new SentenceChunker(tokenizerFactory,sentenceModel);
        SentenceEvaluator sentenceEvaluator = new SentenceEvaluator(sentenceChunker);

        @SuppressWarnings("deprecation")
        com.aliasi.corpus.parsers.GeniaSentenceParser parser = new com.aliasi.corpus.parsers.GeniaSentenceParser();
        parser.setHandler(sentenceEvaluator);

        File inFile = new File(args[0]);
        parser.parse(inFile);

        SentenceEvaluation sentenceEvaluation = sentenceEvaluator.evaluation();

        PrecisionRecallEvaluation chunkingStats = 
            sentenceEvaluation.chunkingEvaluation().precisionRecallEvaluation();
        System.out.println("Chunking Evaluation statistics");
        System.out.println(chunkingStats.toString());
        System.out.println("");

        PrecisionRecallEvaluation endBoundaryStats = 
            sentenceEvaluation.endBoundaryEvaluation();
        System.out.println("Sentence Evaluation end boundary statistics");
        System.out.println(endBoundaryStats.toString());

        // print error diagnostics to files
        int i = 0;
        Set<ChunkAndCharSeq> falseNegatives = sentenceEvaluation.falseNegativeEndBoundaries();
        PrintStream falseNegOut = 
            new PrintStream(new FileOutputStream("EvaluatorFalseNegatives.txt"));
        for (Iterator<ChunkAndCharSeq> it = falseNegatives.iterator(); it.hasNext(); ++i ) {
            ChunkAndCharSeq sentence = it.next();
            falseNegOut.println(i + ". " + sentence.spanEndContext(34));
        }
        falseNegOut.close();
        int j = 0;
        Set<ChunkAndCharSeq> falsePositives = sentenceEvaluation.falsePositiveEndBoundaries();
        PrintStream falsePosOut = 
            new PrintStream(new FileOutputStream("EvaluatorFalsePositives.txt"));
        for (Iterator<ChunkAndCharSeq> it = falsePositives.iterator(); it.hasNext(); ++j ) {
            ChunkAndCharSeq sentence = it.next();
            falsePosOut.println(j + ". " + sentence.spanEndContext(34));
        }
        falsePosOut.close();

    }
}


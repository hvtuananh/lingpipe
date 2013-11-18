import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;
import com.aliasi.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class ChineseTokens05 {

    CompiledSpellChecker mSpellChecker;

    PrecisionRecallEvaluation mBreakEval = new PrecisionRecallEvaluation();
    PrecisionRecallEvaluation mChunkEval = new PrecisionRecallEvaluation();

    ObjectToCounterMap<Integer> mReferenceLengthHistogram = new ObjectToCounterMap<Integer>();
    ObjectToCounterMap<Integer> mResponseLengthHistogram = new ObjectToCounterMap<Integer>();

    Set<Character> mTrainingCharSet = new HashSet<Character>();
    Set<Character> mTestCharSet = new HashSet<Character>();
    Set<String> mTrainingTokenSet = new HashSet<String>();
    Set<String> mTestTokenSet = new HashSet<String>();

    // parameter values
    File mZipFile;
    String mCorpusName;
    File mOutputFile;

    File mKnownToksFile;
    Writer mOutputWriter;
    int mMaxNGram;
    double mLambdaFactor;
    int mNumChars;
    int mMaxNBest;

    String mCharEncoding = "UTF-8";

    public String mModelFileName = null;

    public ChineseTokens05(String[] args) {
        System.out.println("CHINESE TOKENS 2005");

        mZipFile = new File(args[0],"icwb2-data.zip");
        mCorpusName = args[1];
        mOutputFile = new File(mCorpusName + ".segments");
        mKnownToksFile = new File(mCorpusName + ".knownWords");
        mMaxNGram = Integer.valueOf(args[2]);
        mLambdaFactor = Double.valueOf(args[3]);
        mNumChars = Integer.valueOf(args[4]);
        mMaxNBest = Integer.valueOf(args[5]);

        System.out.println("    Data Zip File=" + mZipFile);
        System.out.println("    Corpus Name=" + mCorpusName);
        System.out.println("    Output File Name=" + mOutputFile);
        System.out.println("    Known Tokens File Name=" + mKnownToksFile);
        System.out.println("    Max N-gram=" + mMaxNGram);
        System.out.println("    Lambda factor=" + mLambdaFactor);
        System.out.println("    Num chars=" + mNumChars);
        System.out.println("    Max n-best=" + mMaxNBest);
    }

    void run() throws ClassNotFoundException, IOException {
        compileSpellChecker();
        testSpellChecker();
        printResults();
    }

    void compileSpellChecker() throws IOException, ClassNotFoundException {
        NGramProcessLM lm
            = new NGramProcessLM(mMaxNGram,mNumChars,mLambdaFactor);
        WeightedEditDistance distance
            = new ChineseTokens.ChineseTokenizing(0.0,0.0);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm, distance,null);

        String trainingEntryName = mCorpusName + "_training.utf8";
        FileInputStream fileIn = new FileInputStream(mZipFile);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        ZipEntry entry = null;
        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            if (!name.endsWith(trainingEntryName)) continue;
            System.out.println("Reading Data from entry=" + name);
            String[] lines = ChineseTokens.extractLines(zipIn,mTrainingCharSet,
                                                        mTrainingTokenSet,mCharEncoding);
            System.out.println("  Found " + lines.length + " sentences.");
            System.out.println("  Found " + mTrainingCharSet.size()
                               + " distinct characters.");
            System.out.println("  Found " + mTrainingTokenSet.size()
                               + " distinct tokens.");
            for (int i = 0; i < lines.length; ++i)
                trainer.handle(lines[i]);
        }
        Streams.closeQuietly(zipIn);


        mSpellChecker = (CompiledSpellChecker) AbstractExternalizable.compile(trainer);

        mSpellChecker.setAllowInsert(true);
        mSpellChecker.setAllowMatch(true);
        mSpellChecker.setAllowDelete(false);
        mSpellChecker.setAllowSubstitute(false);
        mSpellChecker.setAllowTranspose(false);
        mSpellChecker.setNumConsecutiveInsertionsAllowed(1);
        mSpellChecker.setNBest(mMaxNBest);
    }

    void testSpellChecker() throws IOException {
        OutputStream out = new FileOutputStream(mOutputFile);
        mOutputWriter = new OutputStreamWriter(out,Strings.UTF8);

        String testEntryName = mCorpusName + "_test_gold.utf8";
        String testEntryName2 = mCorpusName + "_testing_gold.utf8";
        // fudge for inconsistency of as test file in .zip file
        FileInputStream fileIn = new FileInputStream(mZipFile);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        ZipEntry entry = null;
        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            if ((!name.endsWith(testEntryName))
                && !name.endsWith(testEntryName2)) continue;
            System.out.println("Testing Results. Zip Entry=" + name);
            String[] lines
                = ChineseTokens.extractLines(zipIn,mTestCharSet,mTestTokenSet,mCharEncoding);
            System.out.println("    Found " + lines.length + " test sentences.");
            for (int i = 0; i < lines.length; ++i)
                test(lines[i]);
        }
        Streams.closeQuietly(zipIn);
    }

    void printResults() throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = mTrainingTokenSet.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            sb.append('\n');
        }
        Files.writeStringToFile(sb.toString(),mKnownToksFile,
                                Strings.UTF8);

        System.out.println("  Found " + mTestTokenSet.size() + " test tokens.");
        System.out.println("  Found "
                           + ChineseTokens.sizeOfDiff(mTestTokenSet,mTrainingTokenSet)
                           + " unknown test tokens.");
        System.out.print("    Found " + mTestCharSet.size() + " test characterss.");
        System.out.println("  Found "
                           + ChineseTokens.sizeOfDiff(mTestCharSet,mTrainingCharSet)
                           + " unknown test characters.");

        System.out.println("\nReference/Response Token Length Histogram");
        System.out.println("Length, #REF, #RESP, Diff");
        for (int i = 1; i < 10; ++i) {
            Integer iObj = Integer.valueOf(i);
            int refCount = mReferenceLengthHistogram.getCount(iObj);
            int respCount = mResponseLengthHistogram.getCount(iObj);
            int diff = respCount-refCount;
            System.out.println("    " + i
                               + ", " + refCount
                               + ", " + respCount
                               + ", " + diff);
        }

        System.out.println("Scores");
        System.out.println("  EndPoint:"
                           + " P=" + mBreakEval.precision()
                           + " R=" + mBreakEval.recall()
                           + " F=" + mBreakEval.fMeasure());
        System.out.println("     Chunk:"
                           + " P=" + mChunkEval.precision()
                           + " R=" + mChunkEval.recall()
                           + " F=" + mChunkEval.fMeasure());
    }

    void test(String reference) throws IOException {
        String testInput = reference.replaceAll(" ","");

        String response = mSpellChecker.didYouMean(testInput);
        response += ' ';

        mOutputWriter.write(response);
        mOutputWriter.write("\n");

        Set<Integer> refSpaces = ChineseTokens.getSpaces(reference);
        Set<Integer> responseSpaces = ChineseTokens.getSpaces(response);
        ChineseTokens.prEval("Break Points",refSpaces,responseSpaces,mBreakEval);

        Set<Tuple<Integer>> refChunks
            = ChineseTokens.getChunks(reference,mReferenceLengthHistogram);
        Set<Tuple<Integer>> responseChunks
            = ChineseTokens.getChunks(response,mResponseLengthHistogram);
        ChineseTokens.prEval("Chunks",refChunks,responseChunks,mChunkEval);
    }

    public static void main(String[] args) {
        try {
            new ChineseTokens05(args).run();
        } catch (Throwable t) {
            System.out.println("EXCEPTION IN RUN:");
            t.printStackTrace(System.out);
        }
    }

}

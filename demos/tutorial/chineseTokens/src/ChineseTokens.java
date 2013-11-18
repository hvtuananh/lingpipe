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
import com.aliasi.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


public class ChineseTokens {

    CompiledSpellChecker mSpellChecker;

    PrecisionRecallEvaluation mBreakEval = new PrecisionRecallEvaluation();
    PrecisionRecallEvaluation mChunkEval = new PrecisionRecallEvaluation();

    ObjectToCounterMap<Integer> mReferenceLengthHistogram
        = new ObjectToCounterMap<Integer>();
    ObjectToCounterMap<Integer> mResponseLengthHistogram
        = new ObjectToCounterMap<Integer>();

    Set<Character> mTrainingCharSet = new HashSet<Character>();
    Set<Character> mTestCharSet = new HashSet<Character>();
    Set<String> mTrainingTokenSet = new HashSet<String>();
    Set<String> mTestTokenSet = new HashSet<String>();

    // parameter values
    File mDataDir;
    String mTrainingCorpusName;
    String mTestCorpusName;
    File mOutputFile;
    File mKnownToksFile;
    Writer mOutputWriter;
    String mCharEncoding;
    int mMaxNGram;
    double mLambdaFactor;
    int mNumChars;
    int mMaxNBest;
    double mContinueWeight;
    double mBreakWeight;

    public ChineseTokens(String[] args) {
        System.out.println("CHINESE TOKENS");

        mDataDir = new File(args[0]);
        mTrainingCorpusName = args[1];
        mTestCorpusName = args[2];
        mOutputFile = new File(mDataDir,args[3]+".segments");
        mKnownToksFile = new File(mDataDir,args[3]+".knownWords");
        mCharEncoding = args[4];
        mMaxNGram = Integer.valueOf(args[5]);
        mLambdaFactor = Double.valueOf(args[6]);
        mNumChars = Integer.valueOf(args[7]);
        mMaxNBest = Integer.valueOf(args[8]);
        mContinueWeight = Double.valueOf(args[9]);
        mBreakWeight = Double.valueOf(args[10]);

        System.out.println("    Data Directory=" + mDataDir);
        System.out.println("    Train Corpus Name=" + mTrainingCorpusName);
        System.out.println("    Test Corpus Name=" + mTestCorpusName);
        System.out.println("    Output File Name=" + mOutputFile);
        System.out.println("    Known Tokens File Name=" + mKnownToksFile);
        System.out.println("    Char Encoding=" + mCharEncoding);
        System.out.println("    Max N-gram=" + mMaxNGram);
        System.out.println("    Lambda factor=" + mLambdaFactor);
        System.out.println("    Num chars=" + mNumChars);
        System.out.println("    Max n-best=" + mMaxNBest);
        System.out.println("    Continue weight=" + mContinueWeight);
        System.out.println("    Break weight=" + mBreakWeight);
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
            = new ChineseTokenizing(mContinueWeight,mBreakWeight);
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm, distance,null);
        File trainingFile
            = new File(mDataDir,mTrainingCorpusName+"_training.zip");
        System.out.println("Training Zip File=" + trainingFile);
        FileInputStream fileIn = new FileInputStream(trainingFile);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        ZipEntry entry = null;
        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            String[] lines = extractLines(zipIn,mTrainingCharSet,
                                          mTrainingTokenSet,mCharEncoding);
            for (int i = 0; i < lines.length; ++i)
                trainer.handle(lines[i]);
        }
        Streams.closeQuietly(zipIn);

        System.out.println("Compiling Spell Checker");
        mSpellChecker
            = (CompiledSpellChecker) AbstractExternalizable.compile(trainer);

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
        mOutputWriter = new OutputStreamWriter(out,mCharEncoding);
        File file = new File(mDataDir,mTestCorpusName+"-testref.txt");
        System.out.println("Testing Results. File=" + file);
        FileInputStream fileIn = new FileInputStream(file);
        String[] lines = extractLines(fileIn,mTestCharSet,mTestTokenSet,mCharEncoding);
        for (int i = 0; i < lines.length; ++i)
            test(lines[i]);
        Streams.closeQuietly(fileIn);
        Streams.closeQuietly(mOutputWriter);
    }

    void printResults() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String token : mTrainingTokenSet) {
            sb.append(token);
            sb.append('\n');
        }
        Files.writeStringToFile(sb.toString(),mKnownToksFile,mCharEncoding);

        System.out.print("  # Training Toks=" + mTrainingTokenSet.size());
        System.out.println("  # Unknown Test Toks="
                           + sizeOfDiff(mTestTokenSet,mTrainingTokenSet));

        System.out.print("  # Training Chars=" + mTrainingCharSet.size());
        System.out.println("  # Unknown Test Chars="
                           + sizeOfDiff(mTestCharSet,mTrainingCharSet));

        System.out.println("Token Length, #REF, #RESP, Diff");
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

        Set<Integer> refSpaces = getSpaces(reference);
        Set<Integer> responseSpaces = getSpaces(response);
        prEval("Break Points",refSpaces,responseSpaces,mBreakEval);

        Set<Tuple<Integer>> refChunks
            = getChunks(reference,mReferenceLengthHistogram);
        Set<Tuple<Integer>> responseChunks
            = getChunks(response,mResponseLengthHistogram);
        prEval("Chunks",refChunks,responseChunks,mChunkEval);
    }

    static void addTokChars(Set<Character> charSet,
                            Set<String> tokSet,
                            String line) {
        if (line.indexOf("  ") >= 0) {
                String msg = "Illegal double space.\n"
                    + "    line=/" + line + "/";
                throw new RuntimeException(msg);
        }
        String[] toks = line.split("\\s+");
        for (int i = 0; i < toks.length; ++i) {
            String tok = toks[i];
            if (tok.length() == 0) {
                String msg = "Illegal token length= 0\n"
                    + "    line=/" + line + "/";
                throw new RuntimeException(msg);
            }
            tokSet.add(tok);
            for (int j = 0; j < tok.length(); ++j) {
                charSet.add(Character.valueOf(tok.charAt(j)));
            }
        }
    }

    static <E> void prEval(String evalName,
                           Set<E> refSet,
                           Set<E> responseSet,
                           PrecisionRecallEvaluation eval) {
        for (E e : refSet)
            eval.addCase(true,responseSet.contains(e));

        for (E e : responseSet)
            if (!refSet.contains(e))
                eval.addCase(false,true);
    }

    public static void main(String[] args) {
        try {
            new ChineseTokens(args).run();
        } catch (Throwable t) {
            System.out.println("EXCEPTION IN RUN:");
            t.printStackTrace(System.out);
        }
    }

    // size of (set1 - set2)
    static <E> int sizeOfDiff(Set<E> set1, Set<E> set2) {
        HashSet<E> diff = new HashSet<E>(set1);
        diff.removeAll(set2);
        return diff.size();
    }

    static String[] extractLines(InputStream in, Set<Character> charSet, Set<String> tokenSet,
                                 String encoding)
        throws IOException {

        ArrayList<String> lineList = new ArrayList<String>();
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader bufReader = new BufferedReader(reader);
        String refLine;
        while ((refLine = bufReader.readLine()) != null) {
            String trimmedLine = refLine.trim() + " ";
            String normalizedLine = trimmedLine.replaceAll("\\s+"," ");
            lineList.add(normalizedLine);
            addTokChars(charSet,tokenSet,normalizedLine);
        }
        return lineList.toArray(new String[0]);
    }

    static Set<Integer> getSpaces(String xs) {
        Set<Integer> breakSet = new HashSet<Integer>();
        int index = 0;
        for (int i = 0; i < xs.length(); ++i)
            if (xs.charAt(i) == ' ')
                breakSet.add(Integer.valueOf(index));
            else
                ++index;
        return breakSet;
    }

    static Set<Tuple<Integer>>
        getChunks(String xs,
                  ObjectToCounterMap<Integer> lengthCounter) {
        Set<Tuple<Integer>> chunkSet = new HashSet<Tuple<Integer>>();
        String[] chunks = xs.split(" ");
        int index = 0;
        for (int i = 0; i < chunks.length; ++i) {
            int len = chunks[i].length();
            Tuple<Integer> chunk
                = Tuple.create(Integer.valueOf(index),
                               Integer.valueOf(index+len));
            chunkSet.add(chunk);
            index += len;
            lengthCounter.increment(Integer.valueOf(len));
        }
        return chunkSet;
    }

    public static final class ChineseTokenizing
        extends FixedWeightEditDistance
        implements Compilable {

        static final long serialVersionUID = -756371L;

        private final double mBreakWeight;
        private final double mContinueWeight;

        public ChineseTokenizing(double breakWeight, double continueWeight) {
            mBreakWeight = breakWeight;
            mContinueWeight = continueWeight;
        }

        public double insertWeight(char cInserted) {
            return cInserted == ' ' ? mBreakWeight : Double.NEGATIVE_INFINITY;
        }
        public double matchWeight(char cMatched) {
            return mContinueWeight;
        }
        public void compileTo(ObjectOutput objOut) throws IOException {
            objOut.writeObject(new Externalizable(this));
        }
        private static class Externalizable extends AbstractExternalizable {
            static final long serialVersionUID = -756373L;
            final ChineseTokenizing mDistance;
            public Externalizable() { this(null); }
            public Externalizable(ChineseTokenizing distance) {
                mDistance = distance;
            }
            public void writeExternal(ObjectOutput objOut) throws IOException {
                objOut.writeDouble(mDistance.mBreakWeight);
                objOut.writeDouble(mDistance.mContinueWeight);
            }
            public Object read(ObjectInput objIn)
                throws IOException, ClassNotFoundException {

                double breakWeight = objIn.readDouble();
                double continueWeight = objIn.readDouble();
                return new ChineseTokenizing(breakWeight,continueWeight);
            }
        }
    }

}

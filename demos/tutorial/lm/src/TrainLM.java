import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TrieCharSeqCounter;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TrainLM extends AbstractCommand {

    TrainingHandler mTextHandler;
    NGramProcessLM mLM;
    PrintWriter mPrinter = null;
    long mMaxTrainingCharCount;
    Parser<ObjectHandler<CharSequence>> mTextParser;
    FileFilter mFileExtensionFilter;

    int mNGram;
    int mNumChars;

    double[] mLambdas;

    int mSampleFrequency;
    double[][] mSamples;
    int mSampleIndex = 0;
    long mCharCount = 0;

    Runtime mRuntime;
    long mStartTime;

    public TrainLM(String[] args) 
        throws ClassNotFoundException, NoSuchMethodException, 
               InstantiationException, IllegalAccessException, InvocationTargetException {

        super(args,DEFAULT_PARAMS);
        File outFile = getArgumentFile(REPORT_FILE_PARAM);
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(outFile);
            OutputStreamWriter osWriter = new OutputStreamWriter(fileOut);
            mPrinter = new PrintWriter(osWriter);
        } catch (IOException e) {
            throw new IllegalArgumentException("IOException=" + e);
        }
        printParams();

        mNGram = getArgumentInt(MAX_NGRAM_PARAM);
        mNumChars = getArgumentInt(NUM_CHARS_PARAM);
        String[] lambdaNames = getArgument(LAMBDA_FACTORS_PARAM).split(",");
        if (lambdaNames.length < 1)
            illegalArgument(LAMBDA_FACTORS_PARAM,
                            "Must supply at least one lambda factor.");
        mLambdas = new double[lambdaNames.length];
        for (int i = 0; i < mLambdas.length; ++i)
            mLambdas[i] = Double.valueOf(lambdaNames[i]);
        mLM = new NGramProcessLM(mNGram,mNumChars);

        mMaxTrainingCharCount = getArgumentLong(MAX_TRAINING_CHAR_COUNT);

        mSamples = new double[mLambdas.length][getArgumentInt(SAMPLE_SIZE)];
        mSampleIndex = 0;
        mSampleFrequency = getArgumentInt(SAMPLE_FREQUENCY);

        mTextHandler = new TrainingHandler();
        String textParserClassName = getExistingArgument(TEXT_PARSER_PARAM);
        @SuppressWarnings({"unchecked","rawtypes"})
        Parser<ObjectHandler<CharSequence>> textParser
            = (Parser<ObjectHandler<CharSequence>>)
            Class
            .forName(textParserClassName)
            .getConstructor(new Class[0])
            .newInstance(new Object[0]);
        mTextParser = textParser;
        mTextParser.setHandler(mTextHandler);
        String fileExtension = getExistingArgument(FILE_EXTENSION_PARAM);
        mFileExtensionFilter = new FileExtensionFilter(fileExtension,false);

        mRuntime = Runtime.getRuntime();
        mStartTime = System.currentTimeMillis();
    }

    public void run() {
        try {
            train();
            printTotals();
            printTopNGrams();
        } catch (Exception e) {
            println("Exception=" + e);
            e.printStackTrace(System.out);
            e.printStackTrace(mPrinter);
        } finally {
            Streams.closeQuietly(mPrinter);
        }
    }

    void train() throws IOException {
        println("");
        println("LEARNING CURVE");
        print("#CHARS, ELAPSED(s), TOTAL_MEM(MB), FREE_MEM(MB), TOT-FREE(MB)");
        for (int i = 0; i < mLambdas.length; ++i)
            print(", MEAN(" + mLambdas[i] + "), DEV(" + mLambdas[i] + ")");
        println("");

        for (int i = 0; i < numBareArguments(); ++i) {
            File dir = new File(getBareArgument(i));
            if (!dir.isDirectory()) {
                String msg = "Arguments must be directories."
                    + "Found arg " + i + "=" + dir;
                throw new IllegalArgumentException(msg);
            }
            println("# Visiting directory=" + dir);
            File[] files = dir.listFiles(mFileExtensionFilter);
            for (int j = 0; j < files.length; ++j)
                trainFile(files[j]);
        }
    }

    void trainFile(File file) throws IOException {
        String fileName = file.getName();
        if (fileName.endsWith(".gz"))
            trainGZipFile(file);
        else
            trainTextFile(file);
    }

    void trainTextFile(File file) throws IOException {
        String url = file.toURI().toURL().toString();
        InputSource in = new InputSource(url);
        mTextParser.parse(in);
    }

    void trainGZipFile(File file) throws IOException {
        System.out.println("# Training gzip file=" + file + " [char count so far=" + mCharCount + "]");
        FileInputStream fileIn = null;
        BufferedInputStream bufIn = null;
        GZIPInputStream gzipIn = null;
        try {
            fileIn = new FileInputStream(file);
            bufIn = new BufferedInputStream(fileIn);
            gzipIn = new GZIPInputStream(bufIn);
            InputSource in = new InputSource(gzipIn);
            mTextParser.parse(in);
        } finally {
            Streams.closeQuietly(gzipIn);
            Streams.closeQuietly(bufIn);
            Streams.closeQuietly(fileIn);
        }
    }


    void print(String msg) {
        System.out.print(msg);
        mPrinter.print(msg);
        mPrinter.flush();
    }

    void println(String msg) {
        print(msg);
        print("\n");
    }

    void printParams() {
        println("RUN PARAMETERS");
        println("CORPUS NAME=" + getExistingArgument(CORPUS_NAME_PARAM));
        println("FILE EXTENSION=" + getExistingArgument(FILE_EXTENSION_PARAM));
        println("TEXT PARSER CLASS=" + getExistingArgument(TEXT_PARSER_PARAM));
        println("MAX TRAINING CHARS=" + getArgumentLong(MAX_TRAINING_CHAR_COUNT));
        println("MAX NGRAM=" + getArgument(MAX_NGRAM_PARAM));
        println("NUM CHARS=" + getArgumentInt(NUM_CHARS_PARAM));
        println("LAMBDA FACTORS="
                + Arrays.asList(getArgument(LAMBDA_FACTORS_PARAM).split(",")));
        println("SAMPLE SIZE="
              + getArgumentInt(SAMPLE_SIZE));
        println("SAMPLE FREQUENCY="
              + getArgumentInt(SAMPLE_FREQUENCY));
        println("PRINT FREQUENCY="
                + (getArgumentInt(SAMPLE_FREQUENCY)
                   * getArgumentInt(SAMPLE_SIZE)));
        println("INPUT DIRECTORIES=" + Arrays.asList(bareArguments()));
        println("REPORT WRITTEN TO FILE="
              + getExistingArgument(REPORT_FILE_PARAM));
    }

    void exit() {
        println("Hard stop at character=" + mCharCount);
        System.exit(0);
    }

    class TrainingHandler implements ObjectHandler<CharSequence> {
        public void handle(CharSequence cSeq) {
            char[] cs = cSeq.toString().toCharArray();
            int start = 0;
            int length = cs.length;
            for (int i = 1; i <= length; ++i) {
                ++mCharCount;
                if (mCharCount > mMaxTrainingCharCount) exit();
                if ((mCharCount % mSampleFrequency) != 0) continue;
                for (int j = 0; j < mLambdas.length; ++j)
                    mSamples[j][mSampleIndex]
                        = -mLM.log2ConditionalEstimate(cs,start,i,
                                                       mNGram,
                                                       mLambdas[j]);
                ++mSampleIndex;
                if (mSampleIndex == mSamples[0].length) {
                    report();
                    mSampleIndex = 0;
                }
            }
            mLM.train(cSeq);
        }
    }

    void report() {
        print(Long.toString(mCharCount));
        print(", " + ((System.currentTimeMillis() - mStartTime)/1000l));
        long totalMem = mRuntime.totalMemory();
        long freeMem = mRuntime.freeMemory();
        print(", " + totalMem/1000000l);
        print(", " + freeMem/1000000l);
        print(", " + (totalMem-freeMem)/1000000l);
        for (int i = 0; i < mLambdas.length; ++i) {
            double xEntropy = Statistics.mean(mSamples[i]);
            double dev = Statistics.standardDeviation(mSamples[i]);
            print(",   " + decimalFormat(xEntropy) + "," + decimalFormat(dev));
        }
        println("");
    }

    void printTotals() {
        TrieCharSeqCounter counter = mLM.substringCounter();
        long[][] uniqueTotals = counter.uniqueTotalNGramCount();
        println("");
        println("N-GRAM COUNTS");
        println("N, #Unique, #Total, %");
        for (int i = 0; i < uniqueTotals.length; ++i) {
            long unique = uniqueTotals[i][0];
            long total = uniqueTotals[i][1];
            double avg = 1.0 - ((double)unique)/(double)total;
            println(i + ", " + unique + ", " + total + ", " + decimalFormat(avg));
        }
    }

    void printTopNGrams() {
        println("");
        println("TOP N-GRAMS");
        println("N, (N-GRAM,Count)*");
        TrieCharSeqCounter seqCounter = mLM.substringCounter();
        for (int i = 0; i <= mNGram; ++i) {
            print(i + ",");
            ObjectToCounterMap<String> topNGrams = seqCounter.topNGrams(i,5);
            Object[] keysByCount = topNGrams.keysOrderedByCount();
            for (int j = 0; j < keysByCount.length; ++j) {
                String nGram = keysByCount[j].toString();
                int count = topNGrams.getCount(nGram);
                String csvNGram = '"' + nGram.replaceAll("\"","\\\"") + '"';
                print("  \"" + nGram + "\"," + count);
            }
            println("");
        }
    }

    static String decimalFormat(double x) {
        return String.format("%6.3f",x);
    }

    static final String CORPUS_NAME_PARAM = "corpusName";
    static final String FILE_EXTENSION_PARAM = "fileExtension";
    static final String TEXT_PARSER_PARAM = "textParser";

    static final String MAX_TRAINING_CHAR_COUNT = "maxTrainingChars";

    static final String MAX_NGRAM_PARAM = "maxNGram";
    static final String NUM_CHARS_PARAM = "numChars";

    static final String LAMBDA_FACTORS_PARAM = "lambdaFactors";

    static final String SAMPLE_SIZE = "sampleSize";
    static final String SAMPLE_FREQUENCY = "sampleFreq";
    static final String REPORT_FILE_PARAM = "reportFile";

    static final Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MAX_NGRAM_PARAM,"5");
        DEFAULT_PARAMS.setProperty(SAMPLE_SIZE,"1000");
        DEFAULT_PARAMS.setProperty(SAMPLE_FREQUENCY,"1000");
        DEFAULT_PARAMS.setProperty(LAMBDA_FACTORS_PARAM,"1.4,5.6,16");
        String reportFileName
            = "TrainLM-Report" + System.currentTimeMillis() + ".txt";
        DEFAULT_PARAMS.setProperty(REPORT_FILE_PARAM,reportFileName);
        DEFAULT_PARAMS.setProperty(NUM_CHARS_PARAM,"256");
        DEFAULT_PARAMS.setProperty(FILE_EXTENSION_PARAM,".txt");
        DEFAULT_PARAMS.setProperty(CORPUS_NAME_PARAM,"unk");
        DEFAULT_PARAMS.setProperty(MAX_TRAINING_CHAR_COUNT,
                                   Long.toString(Long.MAX_VALUE));
    }

    public static void main(String[] args) 
        throws IllegalAccessException, 
               ClassNotFoundException, 
               NoSuchMethodException, 
               InstantiationException, 
               InvocationTargetException {

        new TrainLM(args).run();
    }



}

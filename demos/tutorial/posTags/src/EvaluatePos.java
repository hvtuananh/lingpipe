import com.aliasi.classify.ConfusionMatrix;

import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.hmm.HmmDecoder;

import com.aliasi.tag.MarginalTaggerEvaluator;
import com.aliasi.tag.NBestTaggerEvaluator;
import com.aliasi.tag.TaggerEvaluator;
import com.aliasi.tag.Tagging;

import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.InputSource;

public class EvaluatePos {

    final int mSentEvalRate;
    final int mToksBeforeEval;
    final int mMaxNBest;
    final int mNGram;
    final int mNumChars;
    final double mLambdaFactor;
    final PosCorpus mCorpus;

    final Set<String> mTagSet = new HashSet<String>();
    HmmCharLmEstimator mEstimator;
    TaggerEvaluator<String> mTaggerEvaluator;
    NBestTaggerEvaluator<String> mNBestTaggerEvaluator;
    MarginalTaggerEvaluator<String> mMarginalTaggerEvaluator;

    int mTrainingSentenceCount = 0;
    int mTrainingTokenCount = 0;

    public EvaluatePos(String[] args) throws Exception {
        mSentEvalRate = Integer.valueOf(args[0]);
        mToksBeforeEval = Integer.valueOf(args[1]);
        mMaxNBest = Integer.valueOf(args[2]);
        mNGram = Integer.valueOf(args[3]);
        mNumChars = Integer.valueOf(args[4]);
        mLambdaFactor = Double.valueOf(args[5]);
        String constructorName = args[6];
        File corpusFile = new File(args[7]);
        Object[] consArgs = new Object[] { corpusFile };
        @SuppressWarnings("rawtypes") // req 2 step
        PosCorpus corpus 
            = (PosCorpus) 
            Class
            .forName(constructorName)
            .getConstructor(new Class[] { File.class })
            .newInstance(consArgs);
        mCorpus = corpus;
    }

    void run() throws IOException {
        System.out.println("\nCOMMAND PARAMETERS:");
        System.out.println("  Sent eval rate=" + mSentEvalRate);
        System.out.println("  Toks before eval=" + mToksBeforeEval);
        System.out.println("  Max n-best eval=" + mMaxNBest);
        System.out.println("  Max n-gram=" + mNGram);
        System.out.println("  Num chars=" + mNumChars);
        System.out.println("  Lambda factor=" + mLambdaFactor);

        CorpusProfileHandler profileHandler = new CorpusProfileHandler();
        parseCorpus(profileHandler);
        String[] tags = mTagSet.toArray(Strings.EMPTY_STRING_ARRAY);
        Arrays.sort(tags);
        Set<String> tagSet = new HashSet<String>();
        for (String tag : tags)
            tagSet.add(tag);

        System.out.println("\nCORPUS PROFILE:");
        System.out.println("  Corpus class=" + mCorpus.getClass().getName());
        System.out.println("  #Sentences="
                           + mTrainingSentenceCount);
        System.out.println("  #Tokens=" + mTrainingTokenCount);
        System.out.println("  #Tags=" + tags.length);
        System.out.println("  Tags=" + Arrays.asList(tags));

        System.out.println("\nEVALUATION:");
        mEstimator
            = new HmmCharLmEstimator(mNGram,mNumChars,mLambdaFactor);
        for (int i = 0; i < tags.length; ++i)
            mEstimator.addState(tags[i]);

        HmmDecoder decoder
            = new HmmDecoder(mEstimator); // no caching
        boolean storeTokens = true;
        mTaggerEvaluator
            = new TaggerEvaluator<String>(decoder,storeTokens);
        mNBestTaggerEvaluator
            = new NBestTaggerEvaluator<String>(decoder,mMaxNBest,mMaxNBest);
        mMarginalTaggerEvaluator
            = new MarginalTaggerEvaluator<String>(decoder,tagSet,storeTokens);

        LearningCurveHandler evaluationHandler
            = new LearningCurveHandler();
        parseCorpus(evaluationHandler);

        System.out.println("\n\n\nFINAL REPORT");

        System.out.println("\n\nFirst Best Evaluation");
        System.out.println(mTaggerEvaluator.tokenEval());

        System.out.println("\n\nN Best Evaluation");
        System.out.println(mNBestTaggerEvaluator.nBestHistogram());

    }

    void parseCorpus(ObjectHandler<Tagging<String>> handler) throws IOException {
        Parser<ObjectHandler<Tagging<String>>> parser = mCorpus.parser();
        parser.setHandler(handler);
        Iterator<InputSource> it = mCorpus.sourceIterator();
        while (it.hasNext()) {
            InputSource in = it.next();
            parser.parse(in);
        }
    }

    class CorpusProfileHandler implements ObjectHandler<Tagging<String>> {
        public void handle(Tagging<String> tagging) {
            ++mTrainingSentenceCount;
            mTrainingTokenCount += tagging.size();
            for (int i = 0; i < tagging.size(); ++i)
                mTagSet.add(tagging.tag(i));
        }
    }

    class LearningCurveHandler implements ObjectHandler<Tagging<String>> {
        Set<String> mKnownTokenSet = new HashSet<String>();
        int mUnknownTokensTotal = 0;
        int mUnknownTokensCorrect = 0;
        public void handle(Tagging<String> tagging) {
            if (mEstimator.numTrainingTokens() > mToksBeforeEval
                && mEstimator.numTrainingCases() % mSentEvalRate == 0) {

                mTaggerEvaluator.handle(tagging);
                mNBestTaggerEvaluator.handle(tagging);
                mMarginalTaggerEvaluator.handle(tagging);
                System.out.println("\nTest Case "
                                   + mTaggerEvaluator.numCases());
                System.out.println("First Best Last Case Report");
                System.out.println(mTaggerEvaluator.lastCaseToString(mKnownTokenSet));
                System.out.println("N-Best Last Case Report");
                System.out.println(mNBestTaggerEvaluator.lastCaseToString(5));
                System.out.println("Marginal Last Case Report");
                System.out.println(mMarginalTaggerEvaluator.lastCaseToString(5));
                System.out.println("Cumulative Evaluation");
                System.out.print("    Estimator:  #Train Cases="
                                 + mEstimator.numTrainingCases());
                System.out.println(" #Train Toks="
                                   + mEstimator.numTrainingTokens());
                ConfusionMatrix tokenEval = mTaggerEvaluator.tokenEval().confusionMatrix();
                System.out.println("    First Best Accuracy (All Tokens) = "
                                   + tokenEval.totalCorrect() 
                                   + "/" + tokenEval.totalCount()
                                   + " = " + tokenEval.totalAccuracy());
                ConfusionMatrix unkTokenEval = mTaggerEvaluator.unknownTokenEval(mKnownTokenSet).confusionMatrix();
                mUnknownTokensTotal += unkTokenEval.totalCount();
                mUnknownTokensCorrect += unkTokenEval.totalCorrect();
                System.out.println("    First Best Accuracy (Unknown Tokens) = "
                                   + mUnknownTokensCorrect
                                   + "/" + mUnknownTokensTotal
                                   + " = " + (mUnknownTokensCorrect/(double)mUnknownTokensTotal));
            }
            // train after eval
            mEstimator.handle(tagging);
            for (int i = 0; i < tagging.size(); ++i)
                mKnownTokenSet.add(tagging.token(i));
        }
    }

    public static void main(String[] args)
        throws Exception {

        new EvaluatePos(args).run();
    }



}

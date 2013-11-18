import com.aliasi.util.Files;

import com.aliasi.classify.BinaryLMClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.JointClassifierEvaluator;
import com.aliasi.classify.NaiveBayesClassifier;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;


import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PolarityWhole {

    File mPolarityDir;
    List<List<String>> mPosReviewLists;
    List<List<String>> mNegReviewLists;
    ObjectToCounterMap<String> mTokenCount = new ObjectToCounterMap<String>();

    PolarityWhole(String[] args) {
        mPolarityDir = new File(args[0]);
    }

    void run() throws ClassNotFoundException, IOException {
        System.out.println("POLARITY DEMO");
        System.out.println("  Data Directory=" + mPolarityDir);
        readData();
        evaluate(new NGramFactory(8));
        evaluate(new NaiveBayesFactory(5));
    }

    <L extends LanguageModel.Dynamic>
    void evaluate(DynamicLMClassifierFactory<L> factory)
        throws ClassNotFoundException, IOException {

        JointClassifierEvaluator<CharSequence> evaluator
            = new JointClassifierEvaluator<CharSequence>(null,CATEGORIES,false);
        for (int i = 0; i < NUM_FOLDS; ++i)
            evaluate(i,factory,evaluator);
    }

    <L extends LanguageModel.Dynamic> 
    void evaluate(int fold,
                  DynamicLMClassifierFactory<L> factory,
                  JointClassifierEvaluator<CharSequence> evaluator)
        throws ClassNotFoundException, IOException {

        System.out.println("  Evaluating fold=" + fold);

        DynamicLMClassifier<L> classifier 
            = factory.create();

        System.out.print("    Training. Fold=");
        for (int i = 0; i < NUM_FOLDS; ++i) {
            if (i != fold) {
                System.out.print(" " + i);
                train(i,classifier);
            }
        }
        System.out.println();

        System.out.println("    Compiling.");

        factory.tweak(classifier);

        @SuppressWarnings("unchecked")
        JointClassifier<CharSequence> compiledClassifier
            = (JointClassifier<CharSequence>) 
            AbstractExternalizable.compile(classifier);

        System.out.println("    Testing.");
        test(fold,compiledClassifier,evaluator);

        System.out.println("EVALUATION");

        System.out.println("CLASSIFIER=" + factory.toString());
        System.out.println(evaluator.toString());
    }

    <L extends LanguageModel.Dynamic> 
    void train(int fold, DynamicLMClassifier<L> classifier) {
        train(mPosReviewLists.get(fold),POSITIVE,classifier);
        train(mNegReviewLists.get(fold),NEGATIVE,classifier);
    }

    <L extends LanguageModel.Dynamic>
    void train(List<String> reviewList, 
               String category,
               DynamicLMClassifier<L> classifier) {
        Iterator<String> it = reviewList.iterator();
        while (it.hasNext()) {
            String text = it.next();
            Classification classification
                = new Classification(category);
            Classified<CharSequence> classified
                = new Classified<CharSequence>(text,classification);
            classifier.handle(classified);
        }
    }

    void test(int fold, JointClassifier<CharSequence> classifier,
              JointClassifierEvaluator<CharSequence> evaluator) {
        test(mPosReviewLists.get(fold),POSITIVE,classifier,evaluator);
        test(mNegReviewLists.get(fold),NEGATIVE,classifier,evaluator);
    }

    void test(List<String> reviewList, String category,
              JointClassifier<CharSequence> classifier, 
              JointClassifierEvaluator<CharSequence> evaluator) {
        Iterator<String> it = reviewList.iterator();
        while (it.hasNext()) {
            String review = it.next().toString();
            JointClassification classification
                = classifier.classify(review);
            evaluator.addClassification(category,classification,null);
        }
    }

    void readData() throws IOException {
        mPosReviewLists = readData(new File(mPolarityDir,"pos"));
        mNegReviewLists = readData(new File(mPolarityDir,"neg"));
        List<String> keyList = mTokenCount.keysOrderedByCountList();
        System.out.println("    #Tokens=" + keyList.size());
        for (int i = 0; i < keyList.size() && i < 200; ++i)
            System.out.println("      " + keyList.get(i) 
                               + "=" + mTokenCount.getCount(keyList.get(i)));
    }

    List<List<String>> readData(File dir) throws IOException {
        List<List<String>> foldReviewLists = new ArrayList<List<String>>();
        for (int i = 0; i < NUM_FOLDS; ++i)
            foldReviewLists.add(new ArrayList<String>());
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            String review = Files.readFromFile(files[i],"ISO-8859-1");
            addTokens(review);
            int k = fileToFold(files[i]);
            foldReviewLists.get(k).add(review);
        }
        return foldReviewLists;
    }

    void addTokens(String review) {
        String[] tokens = review.split("\\s+");
        for (int i = 0; i < tokens.length; ++i) {
            mTokenCount.increment(tokens[i]);
        }
    }

    int fileToFold(File file) {
        String name = file.getName();
        char foldChar = name.charAt(2);
        if (foldChar == '0') return 0;
        else return (foldChar - '1') + 1;
    }

    static final int NUM_FOLDS = 10;

    static final String POSITIVE = BinaryLMClassifier.DEFAULT_ACCEPT_CATEGORY;
    static final String NEGATIVE = BinaryLMClassifier.DEFAULT_REJECT_CATEGORY;
    static final String[] CATEGORIES = new String[] {
        BinaryLMClassifier.DEFAULT_ACCEPT_CATEGORY,
        BinaryLMClassifier.DEFAULT_REJECT_CATEGORY
    };

    static abstract class DynamicLMClassifierFactory<L extends LanguageModel.Dynamic> {
        abstract DynamicLMClassifier<L> create();
        void tweak(DynamicLMClassifier<L> classifier) { }
    }

    class NGramFactory extends DynamicLMClassifierFactory<NGramProcessLM> {
        int mMaxNGram;
        public NGramFactory(int maxNGram) {
            mMaxNGram = maxNGram;
        }
        public DynamicLMClassifier<NGramProcessLM> create() {
            return
                DynamicLMClassifier
                .createNGramProcess(CATEGORIES,mMaxNGram);
        }
        public void tweak(DynamicLMClassifier<NGramProcessLM> classifier) {
            NGramProcessLM lmPos
                = classifier.languageModel(POSITIVE);
            NGramProcessLM lmNeg
                = classifier.languageModel(NEGATIVE);
            Object[] keys = mTokenCount.keysOrderedByCount();
            for (int i = 0; i < keys.length; ++i) {
                String token = keys[i].toString();
                lmPos.train(token);
                lmNeg.train(token);
            }
            // lmPos.substringCounter().prune(2);
            // lmNeg.substringCounter().prune(2);
        }
        public String toString() {
            return mMaxNGram + "-gram Character LM Classifier";
        }
    }

    class NaiveBayesFactory extends DynamicLMClassifierFactory<TokenizedLM> {
        int mMaxNGram;
        public NaiveBayesFactory(int nGram) {
            mMaxNGram = nGram;
        }
        public DynamicLMClassifier<TokenizedLM> create() {
            return new NaiveBayesClassifier(CATEGORIES,
                                            SPACE_TOKENIZER_FACTORY,
                                            mMaxNGram);
        }
        public String toString() {
            return "Naive Bayes with " + mMaxNGram + "-gram Char Smoothing";
        }
    }
    
    public static final TokenizerFactory SPACE_TOKENIZER_FACTORY
        = new RegExTokenizerFactory("\\S+");

    public static void main(String[] args) throws Exception {
        new PolarityWhole(args).run();
    }


}


import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Files;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.Iterator;

public class PolarityHierarchical {

    File mPolarityDir;
    String[] mCategories;
    DynamicLMClassifier<NGramProcessLM> mClassifier;
    JointClassifier<CharSequence> mSubjectivityClassifier;

    PolarityHierarchical(String[] args) 
        throws ClassNotFoundException, IOException {

        System.out.println("\nHIERARCHICAL POLARITY DEMO");
        mPolarityDir = new File(args[0],"txt_sentoken");
        System.out.println("\nData Directory=" + mPolarityDir);
        mCategories = mPolarityDir.list();
        int nGram = 8;
        mClassifier 
            = DynamicLMClassifier
              .createNGramProcess(mCategories,nGram);
        File modelFile = new File("subjectivity.model");
        System.out.println("\nReading Compiled Model from file=" + modelFile);
        FileInputStream fileIn = new FileInputStream(modelFile);
        ObjectInputStream objIn = new ObjectInputStream(fileIn);
        @SuppressWarnings("unchecked")
        JointClassifier<CharSequence> subjectivityClassifier
            = (JointClassifier<CharSequence>) objIn.readObject();
        mSubjectivityClassifier = subjectivityClassifier;
        objIn.close();
    }

    void run() throws ClassNotFoundException, IOException {
        train();
        evaluate();
    }

    boolean isTrainingFile(File file) {
        return file.getName().charAt(2) != '9';  // test on fold 9
    }

    void train() throws IOException {
        int numTrainingCases = 0;
        int numTrainingChars = 0;
        System.out.println("\nTraining.");
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            Classification classification
                = new Classification(category);
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (isTrainingFile(trainFile)) {
                    ++numTrainingCases;
                    String review = Files.readFromFile(trainFile,"ISO-8859-1");
                    numTrainingChars += review.length();
                    Classified<CharSequence> classified
                        = new Classified<CharSequence>(review,classification);
                    mClassifier.handle(classified);
                }
            }
        }
        System.out.println("  # Training Cases=" + numTrainingCases);
        System.out.println("  # Training Chars=" + numTrainingChars);
        // if you want to write the polarity model out for future use, 
        // uncomment the following line
        // com.aliasi.util.AbstractExternalizable.compileTo(mClassifier,new File("polarity.model"));
    }

    void evaluate() throws IOException {
        boolean storeInstances = false;
        BaseClassifierEvaluator<CharSequence> evaluator
            = new BaseClassifierEvaluator<CharSequence>(null,mCategories,storeInstances);
        for (int i = 0; i < mCategories.length; ++i) {
            String category = mCategories[i];
            File file = new File(mPolarityDir,mCategories[i]);
            File[] trainFiles = file.listFiles();
            for (int j = 0; j < trainFiles.length; ++j) {
                File trainFile = trainFiles[j];
                if (!isTrainingFile(trainFile)) {
                    String review = Files.readFromFile(trainFile,"ISO-8859-1");
                    String subjReview = subjectiveSentences(review);
                    Classification classification
                        = mClassifier.classify(subjReview);
                    evaluator.addClassification(category,classification,null);
                }
            }
        }
        System.out.println();
        System.out.println(evaluator.toString());
    }


    String subjectiveSentences(String review) {
        String[] sentences = review.split("\n");
        BoundedPriorityQueue<ScoredObject<String>> pQueue 
            = new BoundedPriorityQueue<ScoredObject<String>>(ScoredObject.comparator(),
                                                             MAX_SENTS);
        for (int i = 0; i < sentences.length; ++i) {
            String sentence = sentences[i];
            ConditionalClassification subjClassification
                = (ConditionalClassification) 
                mSubjectivityClassifier.classify(sentences[i]);
            double subjProb;
            if (subjClassification.category(0).equals("quote"))
                subjProb = subjClassification.conditionalProbability(0);
            else
                subjProb = subjClassification.conditionalProbability(1);
            pQueue.offer(new ScoredObject<String>(sentence,subjProb));
        }
        StringBuilder reviewBuf = new StringBuilder();
        Iterator<ScoredObject<String>> it = pQueue.iterator();
        for (int i = 0; it.hasNext(); ++i) {
            ScoredObject<String> so = it.next();
            if (so.score() < .5 && i >= MIN_SENTS) break;
            reviewBuf.append(so.getObject() + "\n");
        }
        String result = reviewBuf.toString().trim();
        return result;
    }

    static int MIN_SENTS = 5;
    static int MAX_SENTS = 25;

    public static void main(String[] args) {
        try {
            new PolarityHierarchical(args).run();
        } catch (Throwable t) {
            System.out.println("Thrown: " + t);
            t.printStackTrace(System.out);
        }
    }

}


import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.KnnClassifier;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.TfIdfClassifierTrainer;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.io.FileLineReader;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TokenizedLM;

import com.aliasi.matrix.CosineDistance;
import com.aliasi.matrix.TaxicabDistance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.Files;
import com.aliasi.util.Proximity;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Senseval3 {

    // label for unknown sense
    static final String UNKNOWN_SENSE = "U";

    static SenseEvalDict mDict;

    static int sClassifierNumber = -1;

    static final TokenizerFactory NGRAM_TOKENIZER_FACTORY
        = new NGramTokenizerFactory(4,6);

    static final TokenizerFactory SPACE_TOKENIZER_FACTORY
        = new RegExTokenizerFactory("\\S+");

    static final TokenizerFactory NORM_TOKENIZER_FACTORY
        = normTokenizerFactory();

    static TokenizerFactory normTokenizerFactory() {
        TokenizerFactory factory = SPACE_TOKENIZER_FACTORY;
        factory = new LowerCaseTokenizerFactory(factory);
        // factory = EnglishStopTokenizerFactory(factory);
        // factory = PorterStemmerTokenizerFactory(factory);
        return factory;
    }
        

    public static void main(String[] args)
        throws ClassNotFoundException, IOException {

        File dictFile = new File(args[0]);
        File trainFile = new File(args[1]);
        File testFile = new File(args[2]);
        File responseFile = new File(args[3]);
        sClassifierNumber = Integer.valueOf(args[4]);

        System.out.println("Dictionary File="
                           + dictFile.getCanonicalPath());
        System.out.println("Training File="
                           + trainFile.getCanonicalPath());
        System.out.println("Testing File="
                           + testFile.getCanonicalPath());
        System.out.println("System Response File="
                           + responseFile.getCanonicalPath());
        System.out.println("classifier id=" + sClassifierNumber);

        System.out.println();
        System.out.println("Reading Dictionary.");
        SenseEvalDict dict = new SenseEvalDict(dictFile);
        System.out.println("     #entries=" + dict.size());

        System.out.println();
        System.out.println("Reading Training Data.");
        TrainingData trainingData = new TrainingData(trainFile);
        System.out.println("     #training words=" + trainingData.size());

        System.out.println();
        System.out.println("Reading Test Data.");
        TestData testData = new TestData(testFile);
        System.out.println("     #test cases=" + testData.mWordsPlusCats.size());

        System.out.println();
        System.out.println("Training and Compiling Models.");
        SenseEvalModel model = new SenseEvalModel(dict,trainingData);
        System.out.println("     finished training.");

        System.out.println();
        System.out.println("Running Model over Test Data.");
        respond(model,testData,responseFile);
        System.out.println("     finished test data.");

        System.out.println();
        System.out.println("FINISHED.");

    }


    static ObjectHandler<Classified<CharSequence>> createClassifierTrainer(String[] senseIds) {

        switch (sClassifierNumber) {

        case 0:  // DEFAULT CHARACTER LM CLASSIFIER
            return DynamicLMClassifier.createNGramProcess(senseIds,5);

        case 1:  // CONFIGURABLE CHARACTER LM CLASSIFIER
            LanguageModel.Dynamic[] lms5 = new LanguageModel.Dynamic[senseIds.length];
            for (int i = 0; i < lms5.length; ++i)
                lms5[i] = new NGramProcessLM(6,     // n-gram
                                             128,   // num chars
                                             1.0);  // interpolation ratio
            return new DynamicLMClassifier<LanguageModel.Dynamic>(senseIds,lms5);

        case 2:  // DEFAULT NAIVE BAYES CLASSIFIER
            return new NaiveBayesClassifier(senseIds,NORM_TOKENIZER_FACTORY);

        case 3: // DEFAULT TOKEN UNIGRAM LM CLASSIFIER
            return DynamicLMClassifier.createTokenized(senseIds,
                                                       NORM_TOKENIZER_FACTORY,
                                                       1);

        case 4: // DEFAULT TOKEN BIGRAM LM CLASSIFIER
            return DynamicLMClassifier.createTokenized(senseIds,
                                                       NORM_TOKENIZER_FACTORY,
                                                       2);

        case 5:  // CONFIGURABLE TOKENIZED LM CLASSIFIER W. CHARACTER BOUNDARY LM SMOOTHING
            LanguageModel.Dynamic[] lms2 = new LanguageModel.Dynamic[senseIds.length];
            for (int i = 0; i < lms2.length; ++i)
                lms2[i] = new TokenizedLM(NORM_TOKENIZER_FACTORY,
                                          3, // n-gram length
                                          new NGramBoundaryLM(4,128,0.5,'\uFFFF'),
                                          new NGramBoundaryLM(4,128,0.5,'\uFFFF'),
                                          0.1); // interpolation param
            return new DynamicLMClassifier<LanguageModel.Dynamic>(senseIds,lms2);

        case 6:  // TF-IDF CLASSIFIER
            FeatureExtractor<CharSequence> featureExtractor5
                = new TokenFeatureExtractor(SPACE_TOKENIZER_FACTORY);
            return new TfIdfClassifierTrainer<CharSequence>(featureExtractor5);

        case 7:  // K-NEAREST NEIGHBORS DEFAULT CLASSIFIER (EUCLIDEAN DISTANCE)
            FeatureExtractor<CharSequence> featureExtractor7
                = new TokenFeatureExtractor(SPACE_TOKENIZER_FACTORY);
            return new KnnClassifier<CharSequence>(featureExtractor7,
                                                   16);  // num neighbors to average

        case 8:  // K-NEAREST NEIGHBORS DEFAULT CLASSIFIER (COSINE DISTANCE)
            FeatureExtractor<CharSequence> featureExtractor8
                = new TokenFeatureExtractor(NGRAM_TOKENIZER_FACTORY);
            return new KnnClassifier<CharSequence>(featureExtractor8,
                                                   5, // num neighbors to average
                                                   new CosineDistance(),
                                                   true);

        default:
            String msg = "classifier id must be between 0 and 3."
                + " found id=" + sClassifierNumber;
            throw new IllegalArgumentException(msg);
        }
    }


    static void respond(SenseEvalModel model, TestData testData, File file)
        throws IOException {

        FileOutputStream fileOut = new FileOutputStream(file);
        OutputStreamWriter osWriter = new OutputStreamWriter(fileOut,"ISO-8859-1");
        BufferedWriter bufWriter = new BufferedWriter(osWriter);

        for (int i = 0; i < testData.mWordsPlusCats.size(); ++i) {
            String wordPlusCat = testData.mWordsPlusCats.get(i);
            BaseClassifier<CharSequence> classifier = model.get(wordPlusCat);
            String instanceId = testData.mInstanceIds.get(i);
            String textToClassify = testData.mTextsToClassify.get(i);

            Classification classification = classifier.classify(textToClassify);
            bufWriter.write(wordPlusCat + " " + wordPlusCat + ".bnc." + instanceId);
            if (classification instanceof ConditionalClassification) {
                ConditionalClassification condClassification
                    = (ConditionalClassification) classification;
                for (int rank = 0; rank < condClassification.size(); ++rank) {
                    int conditionalProb = (int) java.lang.Math.round(1000.0
                                                                     * condClassification.conditionalProbability(rank));
                    if (rank > 0 && conditionalProb < 1) break;
                    String category = condClassification.category(rank);
                    bufWriter.write(" " + category + "/" + conditionalProb);
                }
            } else {
                bufWriter.write(" " + classification.bestCategory());
            }
            bufWriter.write("\n");
        }
        bufWriter.close();
    }


    static int seek(String lineStartString, String[] lines, int pos) {
        if (pos == -1) return -1;
        for ( ; pos < lines.length; ++pos)
            if (lines[pos].startsWith(lineStartString))
                return pos;
        return -1;
    }



    static class TestData {
        List<String> mWordsPlusCats = new ArrayList<String>();
        List<String> mInstanceIds = new ArrayList<String>();
        List<String> mTextsToClassify = new ArrayList<String>();
        TestData(File file) throws IOException {
            String[] lines = FileLineReader.readLineArray(file,"ISO-8859-1");
            int pos = 0;
            while ((pos = seek("<instance",lines,pos)) >= 0) {
                pos = parse(lines,pos);
            }
        }
        int parse(String[] lines, int pos) {
            String id = extractAttribute("id",lines[pos]);
            int endIndex = id.indexOf('.',id.indexOf('.')+1);
            String wordPlusCat = id.substring(0,endIndex);
            int startIndex = id.lastIndexOf('.') + 1;
            String instanceId = id.substring(startIndex);
            String textToClassify = lines[pos+2];
            mWordsPlusCats.add(wordPlusCat);
            mInstanceIds.add(instanceId);
            mTextsToClassify.add(textToClassify);
            return pos + 2;
        }
    }


    static class SenseEvalModel extends HashMap<String,BaseClassifier<CharSequence>> {
        static final long serialVersionUID = -6343177898894927184L;

        SenseEvalModel(SenseEvalDict dict, TrainingData trainingData)
            throws ClassNotFoundException, IOException  {

            for (String wordPlusCat : trainingData.keySet()) {
                Map<String,List<String>> senseToTextList = trainingData.get(wordPlusCat);
                String[] senseIds = senseToTextList.keySet().<String>toArray(new String[0]);
                System.out.println("    " + wordPlusCat + " [" + senseIds.length + " senses]");
                ObjectHandler<Classified<CharSequence>> trainer
                    = createClassifierTrainer(senseIds);
                for (String senseId : senseToTextList.keySet()) {
                    Classification classificationForSenseId = new Classification(senseId);
                    List<String> trainingTextList = senseToTextList.get(senseId);
                    for (String trainingText : trainingTextList) {
                        Classified<CharSequence> classified
                            = new Classified<CharSequence>(trainingText,classificationForSenseId);
                        trainer.handle(classified);
                    }
                }
                @SuppressWarnings("unchecked")
                BaseClassifier<CharSequence> classifier
                    = (BaseClassifier<CharSequence>)
                    AbstractExternalizable.compile((Compilable)trainer);
                put(wordPlusCat,classifier);
            }
        }

    }





    // wordPlusCat -> senseId -> { training-text }
    static class TrainingData extends HashMap<String,Map<String,List<String>>> {
        static final long serialVersionUID = 8094465899104433829L;
        public TrainingData(File file) throws IOException {
            String[] lines = FileLineReader.readLineArray(file,"ISO-8859-1");
            for (int pos = 0; (pos = seek("<lexelt",lines,pos)) >= 0; )
                pos = trainLexElt(lines,pos);
        }
        int trainLexElt(String[] lines, int pos) {
            String wordPlusCat = extractAttribute("item",lines[pos++]);
            while (pos < lines.length) {
                if (lines[pos].startsWith("</lexelt"))
                    return pos+1;
                if (lines[pos].startsWith("<instance"))
                    pos = trainInstance(wordPlusCat,lines,pos+1);
                else
                    ++pos;
            }
            return pos;
        }
        int trainInstance(String wordPlusCat, String[] lines, int pos) {
            Set<String> idSet = new HashSet<String>();
            for ( ; lines[pos].startsWith("<answer"); ++pos)
                idSet.add(extractAttribute("senseid",lines[pos]));
            if (!lines[pos++].startsWith("<context"))
                throw new IllegalStateException("context missing");
            String text = lines[pos];
            trainInstance(wordPlusCat,text,idSet);
            return pos+1; // skip end context, end instance

        }
        void trainInstance(String wordPlusCat, String trainingText,
                           Set<String> idSet) {
            for (String senseId : idSet) {
                if (senseId.equals(UNKNOWN_SENSE)) {
                    continue;
                }
                Map<String,List<String>> senseToTextListMap = get(wordPlusCat);
                if (senseToTextListMap == null) {
                    senseToTextListMap = new HashMap<String,List<String>>();
                    put(wordPlusCat,senseToTextListMap);
                }
                List<String> trainingTextList = senseToTextListMap.get(senseId);
                if (trainingTextList == null) {
                    trainingTextList = new ArrayList<String>();
                    senseToTextListMap.put(senseId,trainingTextList);
                }
                trainingTextList.add(trainingText);
            }
        }
    }


    // wordpluscat -> sense
    static class SenseEvalDict extends HashMap<String,Sense[]> {
        static final long serialVersionUID = -8332185573089002878L;
        SenseEvalDict(File file) throws IOException {
            String[] lines = FileLineReader.readLineArray(file,"ISO-8859-1");
            for (int pos = 0; (pos = seek("<lexelt",lines,pos)) >= 0; )
                pos = readDictionary(lines,pos);
        }
        int readDictionary(String[] lines, int pos) {
            String wordPlusCat = extractAttribute("item",lines[pos]);

            List<Sense> senseList = new ArrayList<Sense>();
            while (lines[++pos].startsWith("<sense"))
                senseList.add(new Sense(lines[pos]));

            Sense[] senses = senseList.<Sense>toArray(new Sense[senseList.size()]);
            put(wordPlusCat,senses);

            return pos;
        }
    }

    static class Sense {
        String mId;
        String mSource;
        String mSynset;
        String mGloss;
        Sense(String line) {
            mId = extractAttribute("id",line);
            mSource = extractAttribute("source",line);
            mSynset = extractAttribute("synset",line);
            mGloss = extractAttribute("gloss",line);
        }
        public String toString() {
            return "ID=" + mId
                + " SRC=" + mSource
                + " SYNSET=" + mSynset
                + " GLOSS=" + mGloss;
        }
    }

    static String extractAttribute(String att, String line) {
        int start = line.indexOf(att + "=") + att.length()+2;
        int end = line.indexOf('"',start);
        return line.substring(start,end);
    }


}




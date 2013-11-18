import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.*;

public class TrainLanguageId {

    // java TrainLanguageId <dataDir>:dir 
    //                      <modelFile>:file 
    //                      <ngram>:int 
    //                      <numChars>:int
    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        if (!dataDir.isDirectory()) {
            String msg = "Set first argument to the data directory."
                + " Found dataDir=" + dataDir;
            throw new IllegalArgumentException(msg);
        }
        File modelFile = new File(args[1]);
        int nGram = Integer.valueOf(args[2]);
        int numChars = Integer.valueOf(args[3]);
        System.out.println("nGram=" + nGram + " numChars=" + numChars);
        int minCount = args.length > 4
            ? Integer.valueOf(args[4])
            : 10;

        String[] categories = dataDir.list();
        DynamicLMClassifier<NGramProcessLM> classifier
            = DynamicLMClassifier
            .createNGramProcess(categories,nGram);

        char[] csBuf = new char[numChars]; 
        for (int i = 0; i < categories.length; ++i) {
            String category = categories[i];
            System.out.println("Training category=" + category);
            File trainingFile = new File(new File(dataDir,category),
                                         category + ".txt");
            FileInputStream fileIn 
                = new FileInputStream(trainingFile);
            InputStreamReader reader 
                = new InputStreamReader(fileIn,Strings.UTF8);
            reader.read(csBuf);
            String text = new String(csBuf,0,numChars);
            Classification c = new Classification(category);
            Classified<CharSequence> classified
                = new Classified<CharSequence>(text,c);
            classifier.handle(classified);
            reader.close();
        }

        // prune substring counts by eliminating counts below 10
        for (String cat : categories)
            classifier.languageModel(cat).substringCounter().prune(minCount);

        System.out.println("\nCompiling model to file=" + modelFile);
        AbstractExternalizable.compileTo(classifier,modelFile);
    }

}



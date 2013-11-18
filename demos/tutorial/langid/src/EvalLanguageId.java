import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.BaseClassifierEvaluator;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.*;

public class EvalLanguageId {

    // java EvalLanguageId <corpusDir>: dir
    //                     <modelFile>:file 
    //                     <trainSize>:int
    //                     <testSize>:int
    //                     <numTestsPerCat>:int
    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        File modelFile = new File(args[1]);
        int numChars = Integer.valueOf(args[2]);
        int testSize = Integer.valueOf(args[3]);
        int numTests = Integer.valueOf(args[4]);

        char[] csBuf = new char[testSize];
        
        String[] categories = dataDir.list();

        boolean boundSequences = false;

        System.out.println("Reading classifier from file=" + modelFile);
        @SuppressWarnings("unchecked") // required for deserialization
        BaseClassifier<CharSequence> classifier 
            = (BaseClassifier<CharSequence>) 
            AbstractExternalizable.readObject(modelFile);

        boolean storeInputs = false;
        BaseClassifierEvaluator<CharSequence> evaluator
            = new BaseClassifierEvaluator<CharSequence>(classifier,categories,storeInputs);

        for (int i = 0; i < categories.length; ++i) {
            String category = categories[i];
            System.out.println("Evaluating category=" + category);
	    File trainingFile = new File(new File(dataDir,category),
					 category + ".txt");
	    FileInputStream fileIn 
		= new FileInputStream(trainingFile);
	    InputStreamReader reader
		= new InputStreamReader(fileIn,Strings.UTF8);

	    reader.skip(numChars); // skip training data

	    for (int k = 0; k < numTests; ++k) {
		reader.read(csBuf);
                Classification c = new Classification(category);
                Classified<CharSequence> cl
                    = new Classified<CharSequence>(new String(csBuf),c);
		evaluator.handle(cl);
	    }
	    reader.close();
	}

        System.out.println("TEST RESULTS");
        System.out.println(evaluator.toString());
    }

}

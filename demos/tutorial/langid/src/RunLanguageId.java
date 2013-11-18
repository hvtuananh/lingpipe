import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.Classification;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;

public class RunLanguageId {

    public static void main(String[] args) throws Exception {
	File modelFile = new File(args[0]);
	System.out.println("Reading classifier from " + modelFile +"\n");
        @SuppressWarnings("unchecked") // required for deserialization
        BaseClassifier<CharSequence> classifier 
	    = (BaseClassifier<CharSequence>) 
            AbstractExternalizable.readObject(modelFile);
	for (int i = 1; i < args.length; ++i) {
	    System.out.println("Input=" + args[i]);
	    Classification classification = classifier.classify(args[i]);
	    System.out.println(classification.toString());
	}
    }

}

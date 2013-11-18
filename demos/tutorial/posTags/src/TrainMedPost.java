import com.aliasi.io.FileExtensionFilter;

import com.aliasi.corpus.Parser;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tag.Tagging;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class TrainMedPost {

    // language model parameters for HMM emissions
    static int N_GRAM = 8;
    static int NUM_CHARS = 256;
    static double LAMBDA_FACTOR = 8.0;

    public static void main(String[] args) throws IOException {

        // set up parser with estimator as handler
        HmmCharLmEstimator estimator
            = new HmmCharLmEstimator(N_GRAM,NUM_CHARS,LAMBDA_FACTOR);
        Parser<ObjectHandler<Tagging<String>>> parser = new MedPostPosParser();
        parser.setHandler(estimator);

        // train on files in data directory ending in "ioc"
        File dataDir = new File(args[0]);
        File[] files = dataDir.listFiles(new FileExtensionFilter("ioc"));
        for (File file : files) {
            System.out.println("Training file=" + file);
            parser.parse(file);
        }

        // write output to file
        File modelFile = new File(args[1]);
        AbstractExternalizable.compileTo(estimator,modelFile);
    }

}

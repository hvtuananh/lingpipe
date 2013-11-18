import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.cluster.HierarchicalClusterer;
import com.aliasi.cluster.ClusterScore;
import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;

import com.aliasi.util.Counter;
import com.aliasi.util.Distance;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import com.aliasi.tokenizer.*;


import java.io.*;
import java.util.*;

public class TokenCosineDocCluster {

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);

        Set<Set<Document>> referencePartition
            = new HashSet<Set<Document>>();

        for (File catDir : dir.listFiles()) {
            System.out.println("Category from file=" + catDir);
            Set<Document> docsForCat = new HashSet<Document>();
            referencePartition.add(docsForCat);
            for (File file : catDir.listFiles()) {
                Document doc = new Document(file);
                docsForCat.add(doc);
            }
        }

        Set<Document> docSet = new HashSet<Document>();
        for (Set<Document> cluster : referencePartition)
            docSet.addAll(cluster);

        // eval clusterers
        HierarchicalClusterer<Document> clClusterer
            = new CompleteLinkClusterer<Document>(COSINE_DISTANCE);
        Dendrogram<Document> completeLinkDendrogram
            = clClusterer.hierarchicalCluster(docSet);

        HierarchicalClusterer<Document> slClusterer
            = new SingleLinkClusterer<Document>(COSINE_DISTANCE);
        Dendrogram<Document> singleLinkDendrogram
            = slClusterer.hierarchicalCluster(docSet);


        System.out.println();
        System.out.println(" --------------------------------------------------------");
        System.out.println("|  K  |  Complete      |  Single        |  Cross         |");
        System.out.println("|     |  P    R    F   |  P    R    F   |  P    R    F   |");
        System.out.println(" --------------------------------------------------------");
        for (int k = 1; k <= docSet.size(); ++k) {
            Set<Set<Document>> clResponsePartition
                = completeLinkDendrogram.partitionK(k);
            Set<Set<Document>> slResponsePartition
                = singleLinkDendrogram.partitionK(k);

            ClusterScore<Document> scoreCL
                = new ClusterScore<Document>(referencePartition,
                                             clResponsePartition);
            PrecisionRecallEvaluation clPrEval = scoreCL.equivalenceEvaluation();

            ClusterScore<Document> scoreSL
                = new ClusterScore<Document>(referencePartition,
                                             slResponsePartition);
            PrecisionRecallEvaluation slPrEval = scoreSL.equivalenceEvaluation();

            ClusterScore<Document> scoreX
                = new ClusterScore<Document>(clResponsePartition,
                                             slResponsePartition);
            PrecisionRecallEvaluation xPrEval = scoreX.equivalenceEvaluation();

            System.out.printf("| %3d | %3.2f %3.2f %3.2f | %3.2f %3.2f %3.2f | %3.2f %3.2f %3.2f |\n",
                              k,
                              clPrEval.precision(),
                              clPrEval.recall(),
                              clPrEval.fMeasure(),
                              slPrEval.precision(),
                              slPrEval.recall(),
                              slPrEval.fMeasure(),
                              xPrEval.precision(),
                              xPrEval.recall(),
                              xPrEval.fMeasure()
                              );
        }
        System.out.println(" --------------------------------------------------------");
    }

    static class Document {
        final File mFile;
        final char[] mText; // don't really need to store
        final ObjectToCounterMap<String> mTokenCounter
            = new ObjectToCounterMap<String>();
        final double mLength;
        Document(File file) throws IOException {
            mFile = file; // includes name
            mText = Files.readCharsFromFile(file,Strings.UTF8);
            Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(mText,0,mText.length);
            String token;
            while ((token = tokenizer.nextToken()) != null)
                mTokenCounter.increment(token.toLowerCase());
            mLength = length(mTokenCounter);
        }
        double cosine(Document thatDoc) {
            return product(thatDoc) / (mLength * thatDoc.mLength);
        }
        double product(Document thatDoc) {
            double sum = 0.0;
            for (String token : mTokenCounter.keySet()) {
                int count = thatDoc.mTokenCounter.getCount(token);
                if (count == 0) continue;
                // tf = sqrt(count); sum += tf1 * tf2
                sum += Math.sqrt(count * mTokenCounter.getCount(token));
            }
            return sum;
        }
        public String toString() {
            return mFile.getParentFile().getName() + "/"  + mFile.getName();
        }
        static double length(ObjectToCounterMap<String> otc) {
            double sum = 0.0;
            for (Counter counter : otc.values()) {
                double count = counter.doubleValue();
                sum += count;  // tf =sqrt(count); sum += tf * tf
            }
            return Math.sqrt(sum);
        }
    }

    static final TokenizerFactory TOKENIZER_FACTORY = tokenizerFactory();

    static TokenizerFactory tokenizerFactory() {
        TokenizerFactory factory = IndoEuropeanTokenizerFactory.INSTANCE;
        // factory  = new LowerCaseTokenizerFactory(factory);
        // factory = new EnglishStopTokenizerFactory(factory);
        // factory = new PorterStemmerTokenizerFactory(factory);
        return factory;
    }


    static final Distance<Document> COSINE_DISTANCE
        = new Distance<Document>() {
            public double distance(Document doc1, Document doc2) {
                double oneMinusCosine = 1.0 - doc1.cosine(doc2);
                if (oneMinusCosine > 1.0)
                    return 1.0;
                else if (oneMinusCosine < 0.0)
                    return 0.0;
                else
                    return oneMinusCosine;
            }
        };

}

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.Arrays;

import java.util.Random;

// example from: Steyvers and Griffiths. 2007. Probabilistic topic models.
// In Landauer, McNamara, Dennis and Kintsch (eds.) Handbook of Latent Semantic Analysis.
// Laurence Erlbaum.

public class LdaFixed {

    static short NUM_TOPICS = 2;
    static double DOC_TOPIC_PRIOR = 0.1;
    static double TOPIC_WORD_PRIOR = 0.01;

    static int BURNIN_EPOCHS = 0;
    static int SAMPLE_LAG = 1;
    static int NUM_SAMPLES = 16;

    static Random RANDOM = new Random(43);

    static String[] WORDS = new String[] {
        "river",  "stream",  "bank",  "money",  "loan"
    };

    static SymbolTable SYMBOL_TABLE = new MapSymbolTable();
    static {
        for (String word : WORDS)
            SYMBOL_TABLE.getOrAddSymbol(word);
    }

    static final int[][] DOC_WORDS = new int[][] {
        { 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4 },
        { 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4 },
        { 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4 },
        { 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4 },
        { 2, 2, 2, 2, 2, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4 },
        { 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 4, 4, 4, 4 },
        { 0, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4 },
        { 0, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4 },
        { 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4 },
        { 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3, 4, 4, 4, 4 },
        { 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 4 },
        { 0, 0, 0, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 3 },
        { 0, 0, 0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 4 },
        { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2 },
        { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 },
        { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2 }
    };

    static {
        for (int[] words : DOC_WORDS)
            Arrays.permute(words,RANDOM);
    }

    public static void main(String[] args) throws Exception {
        LdaReportingHandler handler
            = new LdaReportingHandler(SYMBOL_TABLE);

        LatentDirichletAllocation.GibbsSample sample
            = LatentDirichletAllocation
            .gibbsSampler(DOC_WORDS,

                          NUM_TOPICS,
                          DOC_TOPIC_PRIOR,
                          TOPIC_WORD_PRIOR,

                          BURNIN_EPOCHS,
                          SAMPLE_LAG,
                          NUM_SAMPLES,

                          RANDOM,

                          handler);

        handler.fullReport(sample,5,2,true);
    }


}

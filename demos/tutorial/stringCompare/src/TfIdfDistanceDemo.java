import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class TfIdfDistanceDemo {


    public static void main(String[] args) {
        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);

        for (String s : args)
            tfIdf.handle(s);

        System.out.printf("\n  %18s  %8s  %8s\n",
                          "Term", "Doc Freq", "IDF");
        for (String term : tfIdf.termSet())
            System.out.printf("  %18s  %8d  %8.2f\n",
                              term,
                              tfIdf.docFrequency(term),
                              tfIdf.idf(term));

        for (String s1 : args) {
            for (String s2 : args) {
                System.out.println("\nString1=" + s1);
                System.out.println("String2=" + s2);
                System.out.printf("distance=%4.2f  proximity=%4.2f\n",
                                  tfIdf.distance(s1,s2),
                                  tfIdf.proximity(s1,s2));
            }
        }
    }
}
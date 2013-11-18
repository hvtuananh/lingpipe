import com.aliasi.spell.JaccardDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class JaccardDistanceDemo {



    public static void main(String[] args) {
        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        JaccardDistance jaccard = new JaccardDistance(tokenizerFactory);

        for (String s1 : args) {
            for (String s2 : args) {
                System.out.println("\nString1=" + s1);
                System.out.println("String2=" + s2);
                System.out.printf("distance=%4.2f  proximity=%4.2f\n",
                                  jaccard.distance(s1,s2),
                                  jaccard.proximity(s1,s2));
            }
        }
    }
}
import com.aliasi.spell.CompiledSpellChecker;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;

import java.io.File;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestBestCase {

    public static void main(String[] args) throws Exception {
        File modelFile = new File(args[0]);

        System.out.println("Reading compiled model file=" + modelFile);
        CompiledSpellChecker bestCaser
            = (CompiledSpellChecker)
            AbstractExternalizable.readObject(modelFile);
        System.out.println("     finished read.");

        bestCaser.setEditDistance(new BestCaseEditDistance());

        bestCaser.setAllowDelete(false);
        bestCaser.setAllowInsert(false);
        bestCaser.setAllowTranspose(false);
        bestCaser.setAllowMatch(true);
        bestCaser.setAllowSubstitute(true);
        bestCaser.setNumConsecutiveInsertionsAllowed(0);
        bestCaser.setMinimumTokenLengthToCorrect(1);
        bestCaser.setFirstCharEditCost(0.0);
        bestCaser.setSecondCharEditCost(-2.0);
        bestCaser.setKnownTokenEditCost(0.0);
        bestCaser.setDoNotEditTokens(new HashSet<String>());
        bestCaser.setNBest(128*1024);

        /*
        // this one's critical
        Set<String> tokenSet = bestCaser.tokenSet();
        Set<String> reducedTokenSet = new HashSet<String>();
        for (String token : tokenSet) {
            String lowTok = token.toLowerCase();
            if (token.equals(lowTok)
                || !tokenSet.contains(lowTok))
                reducedTokenSet.add(token);
        }

        Set<String> reversedTokenSet = new HashSet<String>();
        for (String token : tokenSet)
            reversedTokenSet.add(reverse(token).toString());

        // bestCaser.setTokenSet(reducedTokenSet);
        // bestCaser.setTokenSet(reversedTokenSet);

        */


        for (int i = 1; i < args.length; ++i) {
            String input = args[i].toLowerCase().toString();
            // String reversedInput = reverse(input).toString();
            // System.out.println("\n IN: " + input);

            String dym = bestCaser.didYouMean(input);
            System.out.println("DYM=" + dym);

            /*
            Iterator<ScoredObject<String>> nBestIterator
                = bestCaser.didYouMeanNBest(input);
            for (int n = 0; nBestIterator.hasNext() && n < 16; ++n) {
                ScoredObject<String> correction = nBestIterator.next();
                String output = correction.getObject();
                if (n == 0) {
                    System.out.println("1st=best=" + output.equals(dym));
                }
                System.out.println("\nLogP " + correction.score());
                System.out.println(n + "   " + output);
                //System.out.println("OUT: " + reverse(correction.getObject()));
            }
            */

        }
    }

    static CharSequence reverse(CharSequence in) {
        StringBuilder sb = new StringBuilder(in.length());
        for (int i = in.length(); --i >= 0; )
            sb.append(in.charAt(i));
        return sb;
    }
}
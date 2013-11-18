import com.aliasi.crf.ChainCrf;

import com.aliasi.tag.TagLattice;

import com.aliasi.tag.Tagging;
import com.aliasi.tag.ScoredTagging;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SimplePosTag {

    public static void main(String[] args) 
        throws ClassNotFoundException, IOException {

        File modelFile = new File(args[0]);
        @SuppressWarnings("unchecked")
        ChainCrf<String> crf
            = (ChainCrf<String>) 
            AbstractExternalizable.readObject(modelFile);
        
        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];
            List<String> tokens = Arrays.asList(arg.split(" "));

            System.out.println("\nFIRST BEST");
            Tagging<String> tagging = crf.tag(tokens);
            System.out.println(tagging);

            int maxNBest = 5;
            System.out.println("\n" + maxNBest + " BEST CONDITIONAL");
            System.out.println("Rank log p(tags|tokens)  Tagging");
            Iterator<ScoredTagging<String>> it
                = crf.tagNBestConditional(tokens,maxNBest);
            for (int rank = 0; rank < maxNBest && it.hasNext(); ++rank) {
                ScoredTagging<String> scoredTagging = it.next();
                System.out.println(rank + "    " + scoredTagging);
            }
            
            System.out.println("\nMARGINAL TAG PROBABILITIES");
            System.out.println("Token .. Tag log p(tag|pos,tokens)");
            TagLattice<String> fbLattice
                = crf.tagMarginal(tokens);
            for (int n = 0; n < tokens.size(); ++n) {
                System.out.println(tokens.get(n));
                for (int k = 0; k < fbLattice.numTags(); ++k) {
                    String tag = fbLattice.tag(k);
                    double prob = fbLattice.logProbability(n,k);
                    System.out.println("     " + fbLattice.tag(k) + " " + fbLattice.logProbability(n,k));
                }
            }
            
        }
    }

}
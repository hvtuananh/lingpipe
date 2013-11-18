import com.aliasi.io.FileLineReader;

import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.FixedWeightEditDistance;

import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;


public class AutoCompleteCommand {

    public static void main(String[] args) throws IOException {
        File wordsFile = new File(args[0]);
        String[] lines = FileLineReader.readLineArray(wordsFile,"ISO-8859-1");
        Map<String,Float> counter = new HashMap<String,Float>(200000);
        for (String line : lines) {
            int i = line.lastIndexOf(' ');
            if (i < 0) continue;
            String phrase = line.substring(0,i);
            String countString = line.substring(i+1);
            Float count = Float.valueOf(countString);
            counter.put(phrase,count);
        }

        double matchWeight = 0.0;
        double insertWeight = -10.0;
        double substituteWeight = -10.0;
        double deleteWeight = -10.0;
        double transposeWeight = Double.NEGATIVE_INFINITY;
        FixedWeightEditDistance editDistance
            = new FixedWeightEditDistance(matchWeight,
                                          deleteWeight,
                                          insertWeight,
                                          substituteWeight,
                                          transposeWeight);

        int maxResults = 5;
        int maxQueueSize = 10000;
        double minScore = -25.0;
        AutoCompleter completer
            = new AutoCompleter(counter, editDistance,
                                maxResults, maxQueueSize, minScore);

        for (int i = 1; i < args.length; ++i) {
            System.out.println("\n|" + args[i] + "|");
            SortedSet<ScoredObject<String>> completions
                = completer.complete(args[i]);
            for (ScoredObject<String> so : completions)
                System.out.printf("%6.2f %s\n", so.score(), so.getObject());
        }


    }

}


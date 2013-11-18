import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Corpus;

import com.aliasi.io.FileLineReader;

import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Arrays;
import com.aliasi.util.ObjectToSet;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TwentyNewsgroupsCorpus 
    extends Corpus<ObjectHandler<Classified<CharSequence>>> {
    
    final Map<String,String[]> mTrainingCatToTexts;
    final Map<String,String[]> mTestCatToTexts;
    int mMaxSupervisedInstancesPerCategory = 1;

    public TwentyNewsgroupsCorpus(File path) throws IOException {
        File trainDir = new File(path,"20news-bydate-train");
        File testDir = new File(path,"20news-bydate-test");
        mTrainingCatToTexts = read(trainDir);
        mTestCatToTexts = read(testDir);
    }

    public Set<String> categorySet() {
        return mTrainingCatToTexts.keySet();
    }
                                 
    public void permuteInstances(Random random) {
        for (String[] xs : mTrainingCatToTexts.values())
            Arrays.permute(xs,random);
    }

    public void setMaxSupervisedInstancesPerCategory(int max) {
        mMaxSupervisedInstancesPerCategory = max;
    }


    public void visitTrain(ObjectHandler<Classified<CharSequence>> handler) {
        visit(mTrainingCatToTexts,handler,mMaxSupervisedInstancesPerCategory);
    }

    public void visitTest(ObjectHandler<Classified<CharSequence>> handler) {
        visit(mTestCatToTexts,handler,Integer.MAX_VALUE);
    }

    public Corpus<ObjectHandler<CharSequence>> unlabeledCorpus() {
        return new Corpus<ObjectHandler<CharSequence>>() {
            public void visitTest(ObjectHandler<CharSequence> handler) {
                throw new UnsupportedOperationException();
            }
            public void visitTrain(ObjectHandler<CharSequence> handler) {
                for (String[] texts : mTrainingCatToTexts.values())
                    for (int i = mMaxSupervisedInstancesPerCategory; 
                         i < texts.length; 
                         ++i)
                        handler.handle(texts[i]);
            }
        };
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int totalTrain = 0;
        int totalTest = 0;
        for (String cat : new TreeSet<String>(mTrainingCatToTexts.keySet())) {
            sb.append(cat); 
            int train = mTrainingCatToTexts.get(cat).length;
            int test = mTestCatToTexts.get(cat).length;
            totalTrain += train;
            totalTest += test;
            sb.append(" #train=" + train);
            sb.append(" #test=" + test);
            sb.append('\n');
        }
        sb.append("TOTALS: #train=" + totalTrain
                  + " #test=" + totalTest
                  + " #combined=" + (totalTrain + totalTest));
        sb.append('\n');
        return sb.toString();
    }

    static final String HEADER_REGEX = "^\\w+: ";
    static final Pattern HEADER_PATTERN = Pattern.compile(HEADER_REGEX);

    private static Map<String,String[]> read(File dir) 
        throws IOException {
        ObjectToSet<String,String> catToTexts 
            = new ObjectToSet<String,String>();
        for (File catDir : dir.listFiles()) {
            String cat = catDir.getName();
            for (File file : catDir.listFiles()) {
                String[] lines 
                    = FileLineReader.readLineArray(file,"ISO-8859-1");
                String text = extractText(lines);
                if (text != null)
                    catToTexts.addMember(cat,text);
            }
        }
        Map<String,String[]> map = new HashMap<String,String[]>();
        for (Map.Entry<String,Set<String>> entry : catToTexts.entrySet())
            map.put(entry.getKey(),
                    entry.getValue().toArray(new String[0]));
        return map;
    }

    private static String extractText(String[] lines) {

        // skip header
        int i = 0;
        while ((i < lines.length) && isHeader(lines[i]))
            ++i;
        
        // accumulate rest
        StringBuilder sb = new StringBuilder();
        for ( ; i < lines.length; ++i)
            sb.append(lines[i] + " ");
        String text = sb.toString().trim();

        return atLeastThreeTokens(text) ? text : null;
    }

    private static boolean atLeastThreeTokens(String text) {
        char[] cs = text.toCharArray();
        Tokenizer tokenizer 
            = EmTwentyNewsgroups
            .TOKENIZER_FACTORY
            .tokenizer(cs,0,cs.length);
        if (tokenizer.nextToken() == null) return false;
        if (tokenizer.nextToken() == null) return false;
        return true;
    }

    private static boolean isHeader(String line) {
        return HEADER_PATTERN.matcher(line).find();
    }

    private static void visit(Map<String,String[]> catToItems,
                              ObjectHandler<Classified<CharSequence>> handler,
                              int maxItems) {
        for (Map.Entry<String,String[]> entry : catToItems.entrySet()) {
            String cat = entry.getKey();
            Classification c = new Classification(cat);
            String[] texts = entry.getValue();
            for (int i = 0; i < maxItems && i < texts.length; ++i) {
                Classified<CharSequence> classifiedText
                    = new Classified<CharSequence>(texts[i],c);
                handler.handle(classifiedText);
            }
        }
    }
    

}
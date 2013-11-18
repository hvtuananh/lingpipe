import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Set;

public class CelexSyllableCorpus {

    public static void main(String[] args) throws Exception {
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        // read input data
        String text = Files.readFromFile(inputFile,"ASCII");
        String[] lines = text.split("\\n");

        Set<String> hyphenationSet = new TreeSet<String>();
        for (String line : lines)
            extractLine(line,hyphenationSet);
        System.out.println("# syllabifications=" + hyphenationSet.size());

        // report duplicates
        ObjectToSet<String,String> wordToHyphenationSetMap
            = new ObjectToSet<String,String>();
        for (String hyphenation : hyphenationSet) {
            String word = hyphenation.replaceAll(" ","");
            wordToHyphenationSetMap.addMember(word,hyphenation);
        }
        System.out.println("# unique words=" + wordToHyphenationSetMap.size());
        for (Set<String> hyphenationSetForWord : wordToHyphenationSetMap.values())
            if (hyphenationSetForWord.size() > 1)
                System.out.println(hyphenationSetForWord);



        // generate output file
        Set<String> finalHyphenationSet = new TreeSet<String>();
        for (Set<String> hyphenations : wordToHyphenationSetMap.values())
            finalHyphenationSet.addAll(hyphenations);
        StringBuilder sb = new StringBuilder();
        for (String hyphenatedWord : finalHyphenationSet) {
            if (sb.length() > 0) sb.append('\n');
            sb.append(hyphenatedWord);
        }
        Files.writeStringToFile(sb.toString(),outputFile,Strings.UTF8);
    }

    static void extractLine(String line, Set<String> hyphenationSet) {
        String[] fields = line.split("\\\\");
        if (fields.length < 9) return;
        if (fields[1].indexOf(' ') >= 0 || fields[1].indexOf('-') >= 0) return;  // remove compounds
        for (int i = 8; i < fields.length; i += 4) {
            String hyphenatedWord = fields[i];
            if (hyphenatedWord.length() > 0) {
                String wordHyphenation 
                    = hyphenatedWord.replaceAll("\\]\\["," ").replaceAll("\\[","").replaceAll("\\]","").replaceAll("@","+");
                hyphenationSet.add(wordHyphenation);
            }
        }
    }

    // could use this to push back into IPA, but still wind up with compound symbols
    // LHS is all the symbols of length more than one
    static String toIpa(String word) {
        // escape " with \\x22 and ^ with \\x5E
        return word.replaceAll("tS","?")
            .replaceAll("dZ","?")
            .replaceAll("N,","?")
            .replaceAll("m,","?")
            .replaceAll("n,","?")
            .replaceAll("l,","?")
            .replaceAll("r*","?")
            .replaceAll("i:","?")
            .replaceAll("A:","?")
            .replaceAll("O:","?")
            .replaceAll("u:","?")
            .replaceAll("3:","?")
            .replaceAll("eI","?")
            .replaceAll("aI","?")
            .replaceAll("OI","?")
            .replaceAll("@U","?")
            .replaceAll("aU","?")
            .replaceAll("I@","?")
            .replaceAll("E@","?")
            .replaceAll("U@","?")
            .replaceAll("&~","?")
            .replaceAll("A~:","?")
            .replaceAll("&~:","?")
            .replaceAll("O~:","?");
    }

    static char[] DIACRITICS = new char[] { '#', '`', '\"', '^', ',', '~', '@' };

    // for German
    // $ -> ss (Eszett)

}
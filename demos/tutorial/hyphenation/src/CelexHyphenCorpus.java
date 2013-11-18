import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.Set;

public class CelexHyphenCorpus {

    public static void main(String[] args) throws Exception {
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        // read input data
        String text = Files.readFromFile(inputFile,"ASCII");
        String[] lines = text.split("\\n");

        Set<String> hyphenationSet = new TreeSet<String>();
        for (String line : lines)
            extractLine(line,hyphenationSet);
        System.out.println("# hyphenations=" + hyphenationSet.size());

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


        Set<String> escapeSet = new HashSet<String>();
        for (String word : wordToHyphenationSetMap.keySet()) {
            for (Character c : DIACRITICS) {
                int d = word.indexOf(c);
                if (d < 0) continue;
                if (word.length() >= d+2)
                    escapeSet.add(word.substring(d,d+2));
                else
                    System.out.println("ERR=" + word + " DIACRITIC=" + c);
            }
        }
        System.out.println("\nRESIDUAL ESCAPES=" + escapeSet);

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
        for (int i = 8; i < fields.length; i += 5) {
            String hyphenation = fields[i];
            for (String hyphenatedWord : hyphenation.split("\\-\\-| |, ")) {
                if (hyphenatedWord.length() > 0) {
                    String wordHyphenation 
                        = hyphenatedWord.toLowerCase().replaceAll("\\-"," ");
                    hyphenationSet.add(restoreDiacritics(wordHyphenation));
                }
            }
        }
    }

    static String restoreDiacritics(String word) {
        // escape " with \\x22 and ^ with \\x5E
        return word.replaceAll("\"a","\u00E4")
            .replaceAll("#e","\u00E9")
            .replaceAll("\\x22u","\u00FC" )
            .replaceAll("~n","\u00F1")
            .replaceAll("\\x22i","\u00EF" )
            .replaceAll(",c","\u00E7" )
            .replaceAll("\\x5Ea","\u00E2" )
            .replaceAll("`a","\u00E0" )
            .replaceAll("\\x5Eo","\u00F4" )
            .replaceAll("\\x22o","\u00F6" )
            .replaceAll("\\x5Ee","\u00EA")
            .replaceAll("`e","\u00E8")
            .replaceAll("\\x5Ei","\u00EE")
            .replaceAll("\\x22e","\u00EB")
            .replaceAll("@a","\u00E5")
            .replaceAll("\\x5E","\u00FB")
            .replaceAll("#a","\u00E0")
            .replaceAll("#o","\u00F2");
    }

    static char[] DIACRITICS = new char[] { '#', '`', '\"', '^', ',', '~', '@' };

    // for German
    // $ -> ss (Eszett)

}
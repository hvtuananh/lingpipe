import com.aliasi.io.FileLineReader;


import java.io.File;

import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.util.Set;
import java.util.TreeSet;

public class MobyHyphenCorpus {

    public static void main(String[] args) throws Exception {
        File dataInFile = new File(args[0]);
        File dataOutFile = new File(args[1]);
        System.out.println("Raw Input=" + dataInFile);
        System.out.println("Hyphenation Output=" + dataOutFile);
        String[] hyphenations = FileLineReader.readLineArray(dataInFile,"ASCII");
        System.out.println("Found #input lines=" + hyphenations.length);
        Set<String> hyphenationSet = new TreeSet<String>();
        for (int i = 0; i < hyphenations.length; ++i)
            for (String hyphenation : hyphenations[i].split(" "))
                hyphenationSet.add(hyphenation.replaceAll("\uFFFD"," ")
                                   .trim()
                                   .replaceAll("\\s+"," ")
                                   .toLowerCase());
        String[] finalHyphenations = hyphenationSet.<String>toArray(new String[0]);
        System.out.println("Found #hyphenations=" + finalHyphenations.length);
        String dataOut = Strings.concatenate(finalHyphenations,"\n");
        Files.writeStringToFile(dataOut,dataOutFile,Strings.UTF8);
    }

}
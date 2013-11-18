import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebstersHyphensCorpus {

    public static void main(String[] args) throws Exception {
        File rawFile = new File(args[0]);
        File outFile = new File(args[1]);
        String data = Files.readFromFile(rawFile,"ASCII");
        Pattern hwPattern = Pattern.compile("(<hw>)+(.*?)</hw>");
        Matcher matcher = hwPattern.matcher(data);
        Set<String> entrySet = new TreeSet<String>();
        int numBadEntries = 0;
        while (matcher.find()) {
            String entry = matcher.group(2).toLowerCase();
            for (String subEntry : entry.split(" |-")) {
                String splitEntry = subEntry.replaceAll("\\x22|\\x2A|\\x60", " ").trim().replaceAll("\\s+"," "); // split on " or ` or *
                if (goodWord(splitEntry))
                    entrySet.add(splitEntry);
                else {
                    System.out.println("BAD: " + splitEntry);
                    ++numBadEntries;
                }
                
            }
        }
        System.out.println("# rejected entries=" + numBadEntries);
        System.out.println("# retained entries=" + entrySet.size());
        String[] entries = entrySet.<String>toArray(new String[0]);
        String dataOut = Strings.concatenate(entries,"\n");
        Files.writeStringToFile(dataOut,outFile,Strings.UTF8);
        System.out.println("DONE.");
    }

    static boolean goodWord(String w) {
        for (int i = 0; i < w.length(); ++i) {
            char c = w.charAt(i);
            if (Character.isLetter(c) || c == ' ' || c == '\'')
                continue;
            return false;
        }
        return true;
    }

}
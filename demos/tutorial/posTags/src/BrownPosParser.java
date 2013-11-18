import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class BrownPosParser 
    extends StringParser<ObjectHandler<Tagging<String>>> {

    @Override
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i)
            if (!Strings.allWhitespace(sentences[i]))
                processSentence(sentences[i]);
    }

    public String normalizeTag(String rawTag) {
        String tag = rawTag;
        String startTag = tag;
        // remove plus, default to first
        int splitIndex = tag.indexOf('+');
        if (splitIndex >= 0)
            tag = tag.substring(0,splitIndex);

        int lastHyphen = tag.lastIndexOf('-');
        if (lastHyphen >= 0) {
            String first = tag.substring(0,lastHyphen);
            String suffix = tag.substring(lastHyphen+1);
            if (suffix.equalsIgnoreCase("HL") 
                || suffix.equalsIgnoreCase("TL")
                || suffix.equalsIgnoreCase("NC")) {
                tag = first;
            }
        }

        int firstHyphen = tag.indexOf('-');
        if (firstHyphen > 0) {
            String prefix = tag.substring(0,firstHyphen);
            String rest = tag.substring(firstHyphen+1);
            if (prefix.equalsIgnoreCase("FW")
                || prefix.equalsIgnoreCase("NC")
                || prefix.equalsIgnoreCase("NP"))
                tag = rest;
        }

        // neg last, and only if not whole thing
        int negIndex = tag.indexOf('*');
        if (negIndex > 0) {
            if (negIndex == tag.length()-1)
                tag = tag.substring(0,negIndex);
            else
                tag = tag.substring(0,negIndex)
                    + tag.substring(negIndex+1);
        }
        // multiple runs to normalize
        return tag.equals(startTag) ? tag : normalizeTag(tag);
    }

    private void processSentence(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        List<String> tokenList = new ArrayList<String>(tagTokenPairs.length);
        List<String> tagList = new ArrayList<String>(tagTokenPairs.length);
    
        for (String pair : tagTokenPairs) {
            int j = pair.lastIndexOf('/');
            String token = pair.substring(0,j);
            String tag = normalizeTag(pair.substring(j+1)); 
            tokenList.add(token);
            tagList.add(tag);
        }
        Tagging<String> tagging
            = new Tagging<String>(tokenList,tagList);
        getHandler().handle(tagging);
    }
    
}

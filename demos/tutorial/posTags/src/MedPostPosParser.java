import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.List;


public class MedPostPosParser 
    extends StringParser<ObjectHandler<Tagging<String>>> {

    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i) {
            if (Strings.allWhitespace(sentences[i])) continue;
            if (sentences[i].indexOf('_') < 0) continue;
            processSentence(sentences[i]);
        }
    }

    private void processSentence(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        List<String> tokenList = new ArrayList<String>(tagTokenPairs.length);
        List<String> tagList = new ArrayList<String>(tagTokenPairs.length);
        
        for (String pair : tagTokenPairs) {
            int j = pair.lastIndexOf('_');
            String token = pair.substring(0,j).trim();
            String tag = pair.substring(j+1).trim();
            tokenList.add(token);
            tagList.add(tag);
        }
        Tagging<String> tagging
            = new Tagging<String>(tokenList,tagList);
        getHandler().handle(tagging);
    }




}




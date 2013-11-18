import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.tag.Tagging;

import com.aliasi.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class GeniaPosParser 
    extends StringParser<ObjectHandler<Tagging<String>>> {


    public void parseString(char[] cs, int start, int end) {
        List<String> tokList = new ArrayList<String>();
        List<String> tagList = new ArrayList<String>();

        String input = new String(cs,start,end-start);
        String[] lines = input.split("\n");
        for (int i = 0; i < lines.length; ++i) {
            if (lines[i].startsWith("====================")) {
                handle(tokList,tagList);
            } else {
                int split = lines[i].lastIndexOf('/');
                if (split < 0) continue;
                String tok = lines[i].substring(0,split);
                String tag = lines[i].substring(split+1).trim();
                int splitIndex = tag.indexOf('|');
                if (splitIndex >= 0)
                    tag = tag.substring(0,splitIndex);
                tokList.add(tok);
                tagList.add(tag);
            }
        }
        handle(tokList,tagList);
    }

    private void handle(List<String> tokList, List<String> tagList) {
        Tagging<String> tagging
            = new Tagging<String>(tokList,tagList);
        getHandler().handle(tagging);
    }

    private static String[] clearToArray(List<String> xList) {
        String[] xs = xList.<String>toArray(Strings.EMPTY_STRING_ARRAY);
        xList.clear();
        return xs;
    }

}

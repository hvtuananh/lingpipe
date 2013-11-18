package com.aliasi.test.unit.tag;

import com.aliasi.tag.Tagging;
import com.aliasi.tag.LineTaggingParser;

import com.aliasi.corpus.ObjectHandler;

import java.util.Arrays;


import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertArrayEquals;

import static com.aliasi.test.unit.Asserts.succeed;

public class LineTaggingParserTest {
    
    @Test
    public void testLfCr() {
        String lfcr = "foo Noun\r\n";
        char[] cs = lfcr.toCharArray();
        LineTaggingParser parser 
            = new LineTaggingParser("(foo) (Noun)",1,2,"nothing","nothing");

        parser.setHandler(new ObjectHandler<Tagging<String>> () {
                public void handle(Tagging<String> tagging) {
                    // System.out.println(tagging);
                }
            });
        
        parser.parseString(cs,0,cs.length);
    }
    
    @Test
        public void testLfCrMulti() {
        String lfcr = "foo Noun\r\nfoo Noun\r\r";
        char[] cs = lfcr.toCharArray();
        LineTaggingParser parser 
            = new LineTaggingParser("(foo) (Noun)",1,2,"nothing","nothing");

        parser.setHandler(new ObjectHandler<Tagging<String>> () {
                public void handle(Tagging<String> tagging) {
                    // System.out.println(tagging);
                }
            });
        
        parser.parseString(cs,0,cs.length);
    }
    
    @Test //just not dying right now.
        public void testLf() {
        String lfcr = "foo Noun\n";
        char[] cs = lfcr.toCharArray();
        LineTaggingParser parser 
            = new LineTaggingParser("(foo) (Noun)",1,2,"nothing","nothing");

        parser.setHandler(new ObjectHandler<Tagging<String>> () {
                public void handle(Tagging<String> tagging) {
                    // System.out.println(tagging);
                }
            });
        
        parser.parseString(cs,0,cs.length);
    }

    @Test 
        public void testCr() {
        String lfcr = "foo Noun\r";
        char[] cs = lfcr.toCharArray();
        LineTaggingParser parser 
            = new LineTaggingParser("(foo) (Noun)",1,2,"nothing","nothing");

        parser.setHandler(new ObjectHandler<Tagging<String>> () {
                public void handle(Tagging<String> tagging) {
                    // System.out.println(tagging);
                }
            });
        
        parser.parseString(cs,0,cs.length);
    }

}
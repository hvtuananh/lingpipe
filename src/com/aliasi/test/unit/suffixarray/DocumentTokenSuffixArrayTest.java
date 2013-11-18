package com.aliasi.test.unit.suffixarray;

import com.aliasi.suffixarray.DocumentTokenSuffixArray;
import com.aliasi.suffixarray.TokenSuffixArray;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.CollectionUtils;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aliasi.suffixarray.DocumentTokenSuffixArray.largestWithoutGoingOver;

public class DocumentTokenSuffixArrayTest {

    @Test
    public void testDocs() {
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        String boundaryToken = "-----";
        Map<String,String> textMap
            = new HashMap<String,String>();
        textMap.put("Doc2","Mary ran home too");
        textMap.put("Doc1","John ran home");
        textMap.put("Doc4","The kid ran to the park");
        textMap.put("Doc3","John ran to the store");
        // John ran home # Mary ran home too #  John ran to the store #  The kid ran to the park
        // 0    1   2    3 4    5   6    7   8  9    10  11 12  13    14 15  16  17  18 19  20
        DocumentTokenSuffixArray dtsa
            = new DocumentTokenSuffixArray(textMap,
                                           tf, 
                                           Integer.MAX_VALUE,
                                           boundaryToken);
        TokenSuffixArray tsa = dtsa.suffixArray();
        Tokenization tokenization = tsa.tokenization();
        int pos = 0;
        List<int[]> matches = tsa.prefixMatches(3);
        assertEquals(1,matches.size());
        int[] match_span = matches.get(0);
        assertEquals(2,match_span[1]-match_span[0]);
        int idx1 = match_span[0];
        int idx2 = idx1 + 1;
        String match_string1 = tsa.substring(idx1,3);
        String match_string2 = tsa.substring(idx1+1,3);
        assertEquals(match_string1,match_string2);
        assertEquals("ran to the", match_string1);

        assertEquals(0,dtsa.docStartToken("Doc1"));
        assertEquals(3,dtsa.docEndToken("Doc1"));
        assertEquals(4,dtsa.docStartToken("Doc2"));
        assertEquals(8,dtsa.docEndToken("Doc2"));
        assertEquals(9,dtsa.docStartToken("Doc3"));
        assertEquals(14,dtsa.docEndToken("Doc3"));
        assertEquals(15,dtsa.docStartToken("Doc4"));
        assertEquals(21,dtsa.docEndToken("Doc4"));

        for (int i = 0; i < 3; ++i)
            assertEquals("Doc1",dtsa.textPositionToDocId(i));
        for (int i = 4; i < 8; ++i)
            assertEquals("Doc2",dtsa.textPositionToDocId(i));
        for (int i = 9; i < 14; ++i)
            assertEquals("Doc3",dtsa.textPositionToDocId(i));
        for (int i = 15; i < 21; ++i)
            assertEquals("Doc4",dtsa.textPositionToDocId(i));

        int pos1 = tsa.suffixArray(idx1);
        int pos2 = tsa.suffixArray(idx1+1); // idx2 is end, so is beyond
        int first_pos = Math.min(pos1,pos2);
        int second_pos = Math.max(pos1,pos2);

        String doc_id1 = dtsa.textPositionToDocId(pos1);
        String doc_id2 = dtsa.textPositionToDocId(pos2);

        assertEquals(CollectionUtils.asSet("Doc1","Doc2","Doc3","Doc4"),
                     dtsa.documentNames());

        
        // for (String token : tokenization.tokens())
        //     System.out.println(pos++ + " " + token);
        // System.out.println(pos1 + " / " + idx1 + "=" + doc_id1);
        // System.out.println(pos2 + " / " + idx2 + "=" + doc_id2);

        // for (int i : mDocStarts) {
        //     System.out.println("doc start=" + i);
        // }

        Set<String> expected_doc_set = CollectionUtils.asSet("Doc3","Doc4");
        Set<String> doc_set = CollectionUtils.asSet(doc_id1,doc_id2);
        assertEquals(expected_doc_set, doc_set);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadBoundary() {
        Map<String,String> textMap
            = new HashMap<String,String>();
        textMap.put("doc","Hello world");
        String boundaryToken = "DOC BOUNDARY";
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        DocumentTokenSuffixArray dtsa
            = new DocumentTokenSuffixArray(textMap,
                                           tf,
                                           Integer.MAX_VALUE,
                                           boundaryToken);
    }

    @Test
    public void testSimple() {
        Map<String,String> textMap
            = new HashMap<String,String>();
        textMap.put("doc1","John ran home.");
        textMap.put("doc2","Mary also ran home.");
        String boundaryToken = "DOCBOUNDARY";
        TokenizerFactory tf = IndoEuropeanTokenizerFactory.INSTANCE;
        DocumentTokenSuffixArray dtsa
            = new DocumentTokenSuffixArray(textMap,
                                           tf,
                                           Integer.MAX_VALUE,
                                           boundaryToken);
        assertNotNull(dtsa);
        // assertEquals(0,
    }              
        

    @Test
    public void testPriceIsRightEmpty() {
        int[] vs = { };
        assertEquals(-1,largestWithoutGoingOver(vs,3));
    }

    @Test
    public void testPriceIsRight2() {
        int[] vs = { 0, 17, 23, 152, 153, 190 };
        assertEquals(-1, largestWithoutGoingOver(vs,-10));
        for (int k = 0; k + 1 < vs.length; ++k)
            for (int i = vs[k]; i < vs[k+1]; ++i)
                assertEquals(k, largestWithoutGoingOver(vs,i));
        assertEquals(vs.length-1, 
                     largestWithoutGoingOver(vs,190));
        assertEquals(vs.length-1, 
                     largestWithoutGoingOver(vs,195));
        assertEquals(vs.length-1, 
                     largestWithoutGoingOver(vs,Integer.MAX_VALUE));
    }

}

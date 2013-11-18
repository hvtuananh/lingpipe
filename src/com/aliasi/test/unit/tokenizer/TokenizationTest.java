package com.aliasi.test.unit.tokenizer;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.tokenizer.*;

import static com.aliasi.test.unit.Asserts.assertFullEquals;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import java.util.Arrays;

public class TokenizationTest {

    @Test
    public void testConstructorBase() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("","",""),
                                       new int[] { 0, 5 }, 
                                       new int[] { 4, 8 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorEx1() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("",""), // mismatch # toks
                                       new int[] { 0, 5 },  
                                       new int[] { 4, 8 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorEx2() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("","",""),
                                       new int[] { 0, -5 },  // negative start
                                       new int[] { 4, 8 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorEx3() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("","",""),
                                       new int[] { 0, 5 },  
                                       new int[] { 4, 9 })); // too large an end
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorEx4() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("","",""),
                                       new int[] { 0, 5, 6 },  // too long
                                       new int[] { 4, 8 }));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testConstructorEx5() {
        assertNotNull(new Tokenization("John ran", 
                                       Arrays.asList("John","ran"), 
                                       Arrays.asList("","",""),
                                       new int[] { 0 }, // too short
                                       new int[] { 4, 8 })); 
    }

    @Test
    public void testDirect() {
        Tokenization tok1 
            = new Tokenization("John ran.\n",IndoEuropeanTokenizerFactory.INSTANCE);
        Tokenization tok2
            = new Tokenization("John ran.\n",
                               Arrays.asList("John","ran","."),
                               Arrays.asList(""," ","","\n"),
                               new int[] { 0, 5, 8 },
                               new int[] { 4, 8, 9 });
        assertFullEquals(tok1,tok2);
    }

    @Test
    public void testGetters() {
        Tokenization tok = new Tokenization("John ran.\n",IndoEuropeanTokenizerFactory.INSTANCE);
        assertEquals("John ran.\n",tok.text());
        //            0123456789
        assertEquals(3,tok.numTokens());
        assertEquals("John",tok.token(0));
        assertEquals("ran",tok.token(1));
        assertEquals(".",tok.token(2));
        assertEquals("",tok.whitespace(0));
        assertEquals(" ",tok.whitespace(1));
        assertEquals("",tok.whitespace(2));
        assertEquals("\n",tok.whitespace(3));
        assertEquals(0,tok.tokenStart(0));
        assertEquals(5,tok.tokenStart(1));
        assertEquals(8,tok.tokenStart(2));
        assertEquals(4,tok.tokenEnd(0));
        assertEquals(8,tok.tokenEnd(1));
        assertEquals(9,tok.tokenEnd(2));
        assertEquals(Arrays.asList("John","ran","."),tok.tokenList());
        assertEquals(Arrays.asList(""," ","","\n"),tok.whitespaceList());
        
        assertArrayEquals(new String[] { "John", "ran", "." }, tok.tokens());
        assertArrayEquals(new String[] { "", " ", "", "\n" }, tok.whitespaces());
    }

    @Test
    public void testDifferent() throws IOException {
        Tokenization tok1 = new Tokenization("John ran.",IndoEuropeanTokenizerFactory.INSTANCE);
        Tokenization tok2 = new Tokenization("Mary ran.",IndoEuropeanTokenizerFactory.INSTANCE);
        assertFalse(tok1.equals(tok2));
        assertFalse(tok2.equals(tok1));
        Tokenization tok1s = (Tokenization) AbstractExternalizable.serializeDeserialize(tok1);
        Tokenization tok2s = (Tokenization) AbstractExternalizable.serializeDeserialize(tok2);
        assertFalse(tok1.equals(tok2s));
        assertFalse(tok2.equals(tok1s));
        assertFalse(tok1s.equals(tok2));
        assertFalse(tok2s.equals(tok1));
    }

    @Test
    public void testToks() {
        assertTokenizations("");
        assertTokenizations("John");
        assertTokenizations("John ran.");
        assertTokenizations("he does not run and i do   be\nwalk.");
    }

    void assertTokenizations(String text) {
        assertTokenizations(text,
                            IndoEuropeanTokenizerFactory.INSTANCE,
                            CharacterTokenizerFactory.INSTANCE,
                            new RegExTokenizerFactory("\\s+"),
                            new NGramTokenizerFactory(2,3),
                            new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE),
                            new EnglishStopTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE)),
                            new WhitespaceNormTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE)
                            );
    }

    void assertTokenizations(String text, TokenizerFactory... factories) {
        for (TokenizerFactory factory : factories) {
            char[] cs = ("AB" + text + "C").toCharArray();
            int start = 2;
            int length = text.length();
            Tokenization tok1 = new Tokenization(cs,start,length,factory);
            Tokenization tok2 = new Tokenization(text,factory);
            assertFullEquals(tok1,tok2);
        }
    }

}

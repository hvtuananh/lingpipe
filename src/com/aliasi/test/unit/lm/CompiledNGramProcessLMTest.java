package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;


import com.aliasi.lm.CompiledNGramProcessLM;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;

public class CompiledNGramProcessLMTest  {

    @Test
    public void testOne() throws ClassNotFoundException, IOException {
    NGramProcessLM lm = new NGramProcessLM(3,128);
    char[] abracadabra = "abracadabra".toCharArray();
    lm.train(abracadabra,0,abracadabra.length);

    CompiledNGramProcessLM clm = (CompiledNGramProcessLM) AbstractExternalizable.compile(lm);

    // System.out.println(clm.toString());
    assertArrayEquals(new char[] { 'a', 'b', 'c', 'd', 'r' },
              clm.observedCharacters());

    assertEquals(0,clm.longestContextIndex(""));
    assertEquals(0,clm.longestContextIndex("x"));
    assertEquals(0,clm.longestContextIndex("xyz"));
    assertEquals(1,clm.longestContextIndex("a"));
    assertEquals(1,clm.longestContextIndex("xa"));
    assertEquals(2,clm.longestContextIndex("b"));
    assertEquals(2,clm.longestContextIndex("xb"));
    assertEquals(2,clm.longestContextIndex(",mcjkl;jhaefbb"));
    assertEquals(3,clm.longestContextIndex("xc"));
    assertEquals(4,clm.longestContextIndex("d"));
    assertEquals(5,clm.longestContextIndex(",,r"));
    assertEquals(6,clm.longestContextIndex("ab"));
    assertEquals(6,clm.longestContextIndex("xab"));
    assertEquals(7,clm.longestContextIndex("ac"));
    assertEquals(7,clm.longestContextIndex("dac"));
    assertEquals(8,clm.longestContextIndex("ad"));
    assertEquals(8,clm.longestContextIndex("bad"));
    assertEquals(9,clm.longestContextIndex("br"));
    assertEquals(9,clm.longestContextIndex("xbr"));
    assertEquals(10,clm.longestContextIndex("xca"));
    assertEquals(10,clm.longestContextIndex("ca"));
    assertEquals(11,clm.longestContextIndex("da"));
    assertEquals(11,clm.longestContextIndex("bda"));
    assertEquals(11,clm.longestContextIndex("abrabda"));
    assertEquals(12,clm.longestContextIndex("ra"));
    assertEquals(12,clm.longestContextIndex("bra"));
    assertEquals(12,clm.longestContextIndex("abra"));
    }

}

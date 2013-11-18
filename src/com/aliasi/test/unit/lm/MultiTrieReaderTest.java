package com.aliasi.test.unit.lm;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;


import com.aliasi.io.BitInput;
import com.aliasi.io.BitOutput;

import com.aliasi.lm.BitTrieReader;
import com.aliasi.lm.BitTrieWriter;
import com.aliasi.lm.MultiTrieReader;
import com.aliasi.lm.TrieReader;
import com.aliasi.lm.TrieCharSeqCounter;

import java.io.*;

public class MultiTrieReaderTest  {

    static int NGRAM = 3;

    @Test
    public void testOne() 
        throws IOException {

        TrieCharSeqCounter c1 = new TrieCharSeqCounter(NGRAM);
        c1.incrementSubstrings("ax");
        c1.incrementSubstrings("ax");
        c1.incrementSubstrings("ax");

        c1.incrementSubstrings("ay");
        c1.incrementSubstrings("ay");
    
        c1.incrementSubstrings("bz");

        c1.incrementSubstrings("");

    
        TrieCharSeqCounter c2 = new TrieCharSeqCounter(NGRAM);
        c2.incrementSubstrings("bz");
        c2.incrementSubstrings("bz");
    
        c2.incrementSubstrings("d");

        // System.out.println("\nc1=\n" + c1);
        // System.out.println("\nc2=\n" + c2);

        TrieCharSeqCounter c3 = merge(c1,c2);
        // System.out.println("\nmerged counter=\n" + c3);

        String[] tests = new String[] {
            "", "a", "x", "ax", "ay", "bz", "d", "zd"
        };
        assertMultiCounts(c1,c2,c3,tests);
    }

    @Test
    public void testTwo() 
        throws IOException {

        TrieCharSeqCounter c1 = new TrieCharSeqCounter(NGRAM);
        c1.incrementSubstrings("abracadabra");
        c1.incrementSubstrings("beezelbop");
        c1.incrementSubstrings("beelzebub");
        c1.incrementSubstrings("dweezle");
        c1.incrementSubstrings("zappa");
        c1.incrementSubstrings("zappa");
    
        TrieCharSeqCounter c2 = new TrieCharSeqCounter(NGRAM);
        c2.incrementSubstrings("frankincense");
        c2.incrementSubstrings("myrh");
        c2.incrementSubstrings("myrh");
        c2.incrementSubstrings("zoology");
        c2.incrementSubstrings("zapata");
        c2.incrementSubstrings("zapata");
        c2.incrementSubstrings("zine");
        c2.incrementSubstrings("ezine");
        c2.incrementSubstrings("bob");
    
        // System.out.println("\nc1=\n" + c1);
        // System.out.println("\nc2=\n" + c2);

        TrieCharSeqCounter c3 = merge(c1,c2);
        // System.out.println("\nmerged counter=\n" + c3);

        String[] tests = new String[] {
            "a", "b", "r", "c", "d",
            "ab", "br", "ra", "ac", "ca", "ad", "da",
            "abr", "bra", "rac", "aca", "cad", "ada",
            "zap", "ppa", "ap", "p", "z", "zi", "apa"
        };
        assertMultiCounts(c1,c2,c3,tests);
    }


    void assertMultiCounts(TrieCharSeqCounter c1,
                           TrieCharSeqCounter c2,
                           TrieCharSeqCounter c12,
                           String[] tests) {
        for (int i = 0; i < tests.length; ++i) {
            String test = tests[i];
            assertEquals(c1.count(test) + c2.count(test),
                         c12.count(test));
        }
    }
    
    public TrieCharSeqCounter merge(TrieCharSeqCounter c1,
                                    TrieCharSeqCounter c2)
        throws IOException {

        TrieReader reader1 = toReader(c1);
        TrieReader reader2 = toReader(c2);
    
        MultiTrieReader multiReader = new MultiTrieReader(reader1,reader2);
        return TrieCharSeqCounter.readCounter(multiReader,NGRAM);
    }

    public TrieReader toReader(TrieCharSeqCounter counter)
        throws IOException {

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        BitOutput bitsOut = new BitOutput(bytesOut);
        BitTrieWriter writer = new BitTrieWriter(bitsOut);
        TrieCharSeqCounter.writeCounter(counter,writer,128);
        bitsOut.flush();
        byte[] bytes = bytesOut.toByteArray();

        ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
        BitInput bitsIn = new BitInput(bytesIn);
        return new BitTrieReader(bitsIn);
    }



}

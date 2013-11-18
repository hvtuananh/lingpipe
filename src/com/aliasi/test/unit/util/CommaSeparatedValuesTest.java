package com.aliasi.test.unit.util;

import com.aliasi.util.CommaSeparatedValues;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import static org.junit.Assert.assertArrayEquals;



import java.io.*;

public class CommaSeparatedValuesTest  {

    @Test
    public void testUnexpected() throws IOException {
        String input = "\"DVP-NC655P\",\"DVP-NC655CP\",FALSE\n\"DVP-NC655P\",\"#NAME?\",\"#NAME?\"\n\"DVP-NC655P\",\"DVP-NC655C\",FALSE";
        // System.out.println("INPUT=\n" + input + "\n");
        assertCsv(input,
                  new String[][] { { "DVP-NC655P", "DVP-NC655CP", "FALSE" },
                                   { "DVP-NC655P", "#NAME?", "#NAME?" },
                                   { "DVP-NC655P", "DVP-NC655C", "FALSE" } });
    }

    @Test
    public void testUnquoted() throws IOException {

        assertCsv("", new String[][] { });

        assertCsv("a",new String[][] { { "a" } });
        assertCsv("a,b",new String[][] { { "a", "b" } });
        assertCsv("a,b\nc",new String[][] { { "a", "b" },
                                            { "c"} });

        String input1 = "aa,b,c\nd,e,f";
        String[][] expected1 = {
            { "aa", "b", "c" },
            { "d", "e", "f" }
        };
        assertCsv(input1,expected1);

        String input2 = " aa ,b,  c\nd  ,e  ,   f";
        String[][] expected2 = expected1;
        assertCsv(input2,expected2);

        assertEqualsCsv("1997,Ford,E350",
                        "1997,   Ford   , E350");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testIllegalA() throws IOException {
        assertCsv("a\",b",null); // illegal quote
    }


    @Test
    public void testQuoted() throws IOException {
        assertEqualsCsv("1997,Ford,E350",
                        "\"1997\",Ford,E350");

        // from Wikipedia example
        String in = "1997,Ford,E350,\"ac, abs, moon\",3000.00"
            + "\n"
            + "1999,Chevy,\"Venture \"\"Extended Edition\"\"\",4900.00"
            + "\n"
            + "1996,Jeep,Grand Cherokee,\"MUST SELL!\nair, moon roof, loaded\",4799.00";
        String[][] expected = {
            {"1997", "Ford", "E350","ac, abs, moon","3000.00"},
            {"1999","Chevy","Venture \"Extended Edition\"","4900.00"},
            {"1996","Jeep","Grand Cherokee","MUST SELL!\nair, moon roof, loaded","4799.00"}
        };
        assertCsv(in,expected);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testErrorB() throws IOException {
        assertCsv("\"abc",null); // premature quote end
    }

    @Test(expected=IllegalArgumentException.class)
    public void testErrorC() throws IOException {
        assertCsv("\"abc\"  d,e",null); // unexpected chars after close quote
    }


    void assertEqualsCsv(String input1, String input2) throws IOException {
        Reader reader1 = new CharArrayReader(input1.toCharArray());
        CommaSeparatedValues csv
            = new CommaSeparatedValues(reader1);
        String[][] expected = csv.getArray();
        assertCsv(input2,expected);
    }


    void assertCsv(String input, String[][] expected) throws IOException {
        Reader reader = new CharArrayReader(input.toCharArray());
        CommaSeparatedValues csv
            = new CommaSeparatedValues(reader);
        assertVals(csv,expected);
    }

    void assertVals(CommaSeparatedValues csv,
                    String[][] expected) {

        String[][] found = csv.getArray();
        assertEquals(found.length,expected.length);
        for (int i = 0; i < expected.length; ++i)
            assertArrayEquals(expected[i],found[i]);
    }

}

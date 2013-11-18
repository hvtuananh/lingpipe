/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.test.unit.util;

import com.aliasi.util.Strings;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static com.aliasi.test.unit.Asserts.assertFullEquals;
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.CharBuffer;

public class StringsTest  {

    @Test
    public void testIsLegalUnicode() {
        assertLegal("");
        assertLegal("abc");
        assertLegal("\uFFFF");
        assertLegal("\uFFFE");

        // 0xD800 is a high surrogate and 0xDC00 a low surrogate
        assertLegal("\uD800\uDC00");
        assertLegal("\uD800\uDC00a");
        assertLegal("a\uD800\uDC00");
        assertLegal("a\uD800\uDC00bbb");

        // System.out.println("cp=" + Integer.toHexString("\uD800\uDC00".codePointAt(0)));

        assertIllegal("\uDC00\uD800");
        assertIllegal("a\uDC00\uD800");
        assertIllegal("\uDC00\uD800b");
        assertIllegal("aa\uDC00\uD800bb");

    }

    void assertLegal(String s) {
        assertTrue(s,Strings.isLegalUtf16(s));
    }

    void assertIllegal(String s) {
        assertFalse(s,Strings.isLegalUtf16(s));
    }

    @Test
    public void testSharedPrefixLength() {
        testPref("","",0);
        testPref("a","",0);
        testPref("","a",0);
        testPref("a","a",1);
        testPref("ab","a",1);
        testPref("a","ab",1);
        testPref("abc","abcd",3);
        testPref("abcdefg","abc",3);
        testPref("bcd","cde",0);
    }

    void testPref(String a, String b, int expectedVal) {
        assertEquals(expectedVal,Strings.sharedPrefixLength(a,b));
    }

    @Test
    public void testHashCode() {
        testHashCode("");
        testHashCode("abc");
        testHashCode("xyz kdkdkpq984yuro8iuz");
    }

    void testHashCode(String input) {
        int expectedHash = input.hashCode();
        assertEquals(expectedHash,Strings.hashCode(input));

        StringBuilder sb = new StringBuilder();
        sb.append(input);
        assertEquals(expectedHash,Strings.hashCode(sb));

        char[] cs = input.toCharArray();
        CharBuffer buf = CharBuffer.wrap(cs);
        assertEquals(expectedHash,Strings.hashCode(buf));
    }

    @Test
    public void testReverse() {
        assertReverse("","");
        assertReverse("a","a");
        assertReverse("ab","ba");
        assertReverse("abc","cba");
    }

    void assertReverse(String x, String xRev) {
        assertEquals(xRev,Strings.reverse(x));
    }

    @Test
    public void testUTF8IsSupported() {
        boolean threw = false;
        try {
            new String(new byte[] { (byte)'a' },
                       Strings.UTF8);
        } catch (UnsupportedEncodingException e) {
            threw = true;
        }
        assertFalse(threw);
    }

    @Test
    public void testAllLetters() {
        assertTrue(Strings.allLetters("abc".toCharArray()));
        assertTrue(Strings.allLetters("".toCharArray()));
        assertFalse(Strings.allLetters("abc1".toCharArray()));
    }

    @Test
    public void testAllUpperCase() {
        assertTrue(Strings.allUpperCase("ABC".toCharArray()));
        assertTrue(Strings.allUpperCase("".toCharArray()));
        assertFalse(Strings.allUpperCase("ABC1".toCharArray()));
    }

    @Test
    public void testCapitalized() {
        assertFalse(Strings.capitalized("".toCharArray()));
        assertTrue(Strings.capitalized("Abc".toCharArray()));
        assertFalse(Strings.capitalized("Abc1".toCharArray()));
    }

    @Test
    public void testContainsDigits() {
        assertTrue(Strings.containsDigits("123".toCharArray()));
        assertFalse(Strings.containsDigits("".toCharArray()));
        assertTrue(Strings.containsDigits("abc1".toCharArray()));
        assertFalse(Strings.containsDigits("abc".toCharArray()));
    }

    @Test
    public void testContainsLetter() {
        assertTrue(Strings.containsLetter("abc".toCharArray()));
        assertFalse(Strings.containsLetter("".toCharArray()));
        assertTrue(Strings.containsLetter("abc1".toCharArray()));
        assertFalse(Strings.containsLetter("123".toCharArray()));
    }

    @Test
    public void testAllPunctuation() {
        assertTrue(Strings.allPunctuation(";..?!".toCharArray()));
        assertTrue(Strings.allPunctuation("".toCharArray()));
        assertFalse(Strings.allPunctuation("\".".toCharArray()));
    }

    @Test
    public void testAllPunctuationString() {
        assertTrue(Strings.allPunctuation(";..?!"));
        assertTrue(Strings.allPunctuation(""));
        assertFalse(Strings.allPunctuation("\"."));
    }

    @Test
    public void testAllSymbols() {
        assertTrue(Strings.allSymbols(";..?!".toCharArray()));
        assertTrue(Strings.allSymbols("".toCharArray()));
        assertTrue(Strings.allSymbols("\".".toCharArray()));
        assertFalse(Strings.allSymbols("$%^&*abc".toCharArray()));
    }


    @Test
    public void testContainsChar() {
        assertTrue(Strings.containsChar("abc",'a'));
        assertTrue(Strings.containsChar("abc",'b'));
        assertTrue(Strings.containsChar("abc",'c'));
        assertFalse(Strings.containsChar("abc",'d'));
        assertFalse(Strings.containsChar("",'a'));
    }

    @Test
    public void testAllWhitespace() {
        assertTrue(Strings.allWhitespace(""));
        assertTrue(Strings.allWhitespace(" \n \t"));
        assertFalse(Strings.allWhitespace("  a  "));
    }

    @Test
    public void testAllWhitespaceSB() {
        assertTrue(Strings.allWhitespace(new StringBuilder("")));
        assertTrue(Strings.allWhitespace(new StringBuilder(" \n \t")));
        assertFalse(Strings.allWhitespace(new StringBuilder("  a  ")));
    }

    @Test
    public void testAllWhitespaceArray() {
        assertTrue(Strings.allWhitespace("".toCharArray(),0,0));
        assertTrue(Strings.allWhitespace(" \n \t ".toCharArray(),0,3));
        assertTrue(Strings.allWhitespace("     a  ".toCharArray(),1,2));
        assertFalse(Strings.allWhitespace("     a  ".toCharArray(),3,3));
    }


    @Test
    public void testAllDigits() {
        assertTrue(Strings.allDigits(""));
        assertTrue(Strings.allDigits("123"));
        assertFalse(Strings.allDigits("1.23"));
        assertFalse(Strings.allDigits("1ab"));
    }

    @Test
    public void testAllDigitsArray() {
        assertTrue(Strings.allDigits("".toCharArray(),0,0));
        assertTrue(Strings.allDigits("123".toCharArray(),0,3));
        assertFalse(Strings.allDigits("1.23".toCharArray(),0,4));
        assertFalse(Strings.allDigits("1ab".toCharArray(),0,3));
    }

    @Test
    public void testIsWhitespace() {
        assertTrue(Strings.isWhitespace((char)160));
        assertTrue(Strings.isWhitespace(' '));
        assertTrue(Strings.isWhitespace('\n'));
        assertFalse(Strings.isWhitespace('a'));
    }

    @Test
    public void testIsPunctuation() {
        assertTrue(Strings.isPunctuation('!'));
        assertTrue(Strings.isPunctuation('?'));
        assertTrue(Strings.isPunctuation(';'));
        assertFalse(Strings.isPunctuation('"'));
        assertFalse(Strings.isPunctuation('a'));
    }

    @Test
    public void testPower() {
        assertEquals("",Strings.power("abc",0));
        assertEquals("",Strings.power("",3));
        assertEquals("aaa",Strings.power("a",3));
    }

    @Test
    public void testConcatenateObjectArray() {
        assertEquals("a b",Strings.concatenate(new Object[] { "a", "b" }));
        assertEquals("a b c",
                     Strings.concatenate(new Object[] { "a", "b", "c" }));
        assertEquals("",Strings.concatenate(new Object[] { }));
    }

    @Test
    public void testConcatenateObjectArraySpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "a", "b", "c" },
                                         ""));
        assertEquals("",Strings.concatenate(new Object[] { }, " "));
    }

    @Test
    public void testConcatenateObjectArrayStartSpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         0, ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "e", "a", "b", "c" },
                                         1, ""));
        assertEquals("",Strings.concatenate(new Object[] { },
                                            15, " "));
    }

    @Test
    public void testConcatenateObjectArrayStartEndSpacer() {
        assertEquals("a,b",
                     Strings.concatenate(new Object[] { "a", "b" },
                                         0, 2, ","));
        assertEquals("abc",
                     Strings.concatenate(new Object[] { "e", "a", "b",
                                                        "c", "f" },
                                         1, 4, ""));
    }

    @Test
    public void testIndent() {
        StringBuilder sb = new StringBuilder();
        Strings.indent(sb,3);
        assertEquals(sb.toString(),"\n   ");
        sb = new StringBuilder();
        Strings.indent(sb,0);
        assertEquals(sb.toString(),"\n");
    }


    @Test
    public void testPadding() {
        assertEquals("",Strings.padding(0));
        assertEquals("  ",Strings.padding(2));
    }

    @Test
    public void testPaddingSB() {
        StringBuilder sb = new StringBuilder();
        Strings.padding(sb,0);
        assertEquals("",sb.toString());
        sb = new StringBuilder();
        Strings.padding(sb,2);
        assertEquals("  ",sb.toString());
    }

    @Test
    public void testFunctionArgs() {
        assertEquals("a()",Strings.functionArgs("a",new Object[] { }));
        assertEquals("a(1)",
                     Strings.functionArgs("a",
                                          new Object[] { Integer.valueOf(1) }));
        assertEquals("a(1,b)",
                     Strings.functionArgs("a",
                                          new Object[] { Integer.valueOf(1),
                                                         "b" }));
    }

    @Test
    public void testArgsList() {
        assertEquals("()",Strings.functionArgsList(new Object[] { }));
        assertEquals("(a)", Strings.functionArgsList(new Object[] { "a" }));
        assertEquals("(a,b)", Strings.functionArgsList(new Object[] { "a",
                                                                      "b" }));
    }

    @Test
    public void testTitleCase() {
        assertEquals("",Strings.titleCase(""));
        assertEquals("A",Strings.titleCase("a"));
        assertEquals("1ab",Strings.titleCase("1ab"));
        assertEquals("Abc",Strings.titleCase("abc"));
    }

    @Test
    public void testConstants() {
        assertEquals(160,Strings.NBSP_CHAR);
        assertEquals('\n',Strings.NEWLINE_CHAR);
        assertEquals(' ',Strings.DEFAULT_SEPARATOR_CHAR);
        assertEquals(" ",Strings.DEFAULT_SEPARATOR_STRING);

    }

    @Test
    public void testSplit() {
        assertArrayEquals(new String[] { "" },
                          Strings.split("",' '));

        assertArrayEquals(new String[] { "a" },
                          Strings.split("a",' '));

        assertArrayEquals(new String[] { "a", "" },
                          Strings.split("a ",' '));

        assertArrayEquals(new String[] { "", "a" },
                          Strings.split(" a",' '));

        assertArrayEquals(new String[] { "", "a", "" },
                          Strings.split(" a ",' '));

        assertArrayEquals(new String[] { "", "aa", "" },
                          Strings.split(" aa ",' '));
        assertArrayEquals(new String[] { "a", "b" },
                          Strings.split("a b",' '));
        assertArrayEquals(new String[] { "a", "b", "c" },
                          Strings.split("a b c",' '));

        assertArrayEquals(new String[] { "aaa" },
                          Strings.split("aaa",' '));
        assertArrayEquals(new String[] { "aaa", "bb" },
                          Strings.split("aaa bb",' '));
        assertArrayEquals(new String[] { "aaa", "bb", "c" },
                          Strings.split("aaa bb c",' '));

    }


    @Test
    public void testMsToString() {
        assertEquals(":00",Strings.msToString(0));
        assertEquals(":00",Strings.msToString(999));
        assertEquals(":01",Strings.msToString(1001));
        assertEquals(":32",Strings.msToString(32000));
        assertEquals("1:01",Strings.msToString(61000));
        assertEquals("3:12:03",Strings.msToString((60*60*3 + 60*12 + 3)*1000));
        assertEquals("33:00:00",Strings.msToString((60*60*33)*1000));
    }


    @Test
    public void testNormalizeWhitespace() {
        assertWhitespaceNormalized("abc","abc");
        assertWhitespaceNormalized("abc de fg"," abc de  \t fg\n\n");
        assertWhitespaceNormalized("a b"," a\tb\n");
        assertWhitespaceNormalized("a b","a\t\t\t b");
        assertWhitespaceNormalized("","");
        assertWhitespaceNormalized(""," ");
    }

    private void assertWhitespaceNormalized(String expected, String input) {
        StringBuilder sb = new StringBuilder();
        Strings.normalizeWhitespace(input,sb);
        assertEquals(expected,sb.toString());
    }

    @Test
    public void testEqualsCharSeqs() {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        assertTrue(Strings.equalCharSequence(sb1,sb2));
        sb1.append("abc");
        assertFalse(Strings.equalCharSequence(sb1,sb2));
        assertTrue(Strings.equalCharSequence("abc",sb1));
    }


    @Test
    public void testTextPositions() {
        assertTextPositions("","");
        assertTextPositions("a","a\n0");
        assertTextPositions("ab","ab\n01");
        assertTextPositions("abcdefghi","abcdefghi\n012345678");
        assertTextPositions("abcdefghijklm",
                            "abcdefghijklm\n0123456789012\n0         1  ");
    }

    void assertTextPositions(String in, String expected) {
        String found = Strings.textPositions(in);
        assertEquals(expected,found);
    }

}

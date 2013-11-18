package com.aliasi.test.unit.util;

import com.aliasi.util.Sgml;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;


public class SgmlTest  {

    @Test
    public void test() {
        assertNull(Sgml.entityToCharacter("foobar"));
        assertEquals(Character.valueOf('\u200C'), Sgml.entityToCharacter("zwnj"));

        assertSub("","");
        assertSub("foobar","foobar");
        assertSub("&Agr;","\u0391");
        assertSub("foo&Agr;","foo\u0391");
        assertSub("&Agr;foo","\u0391foo");
        assertSub("foo&Agr;bar","foo\u0391bar");
        assertSub("&Foobar;","?");
        assertSub("&aleph;","\u2135");
        assertSub("&aleph;&acute;","\u2135\u00B4");
        assertSub("&aleph;foo&acute;","\u2135foo\u00B4");
        assertSub("foo&aleph;bar&acute;baz","foo\u2135bar\u00B4baz");
        assertSub("&Foobar;","baz","baz");
    }

    void assertSub(String in, String expected) {
        assertSub(in,expected,null);
    }

    void assertSub(String in, String expected, String unk) {
        assertEquals(expected,
                     unk != null
                     ? Sgml.replaceEntities(in,unk)
                     : Sgml.replaceEntities(in));


    }

}

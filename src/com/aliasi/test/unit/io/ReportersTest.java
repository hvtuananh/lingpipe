package com.aliasi.test.unit.io;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.util.Files;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;

public class ReportersTest {

    @Test
    public void testOne() {
        CharArrayWriter writer = new CharArrayWriter();
        Reporter reporter = Reporters.writer(writer);
        reporter.setLevel(LogLevel.DEBUG);
        reporter.report(LogLevel.TRACE,"AAA");
        reporter.report(LogLevel.DEBUG,"BBB");
        reporter.report(LogLevel.INFO,"CCC");
        reporter.setLevel(LogLevel.WARN);
        reporter.report(LogLevel.INFO,"DDD");
        reporter.report(LogLevel.ERROR,"EEE");
        reporter.close();
        String s = new String(writer.toCharArray());

        assertMsgs(s);
    }
    
    void assertMsgs(String s) {
        assertFalse(s.indexOf("AAA") >= 0);
        assertTrue(s.indexOf("BBB") >= 0);
        assertTrue(s.indexOf("CCC")>= 0);
        assertFalse(s.indexOf("DDD") >= 0);
        assertTrue(s.indexOf("EEE") >= 0);
    }

    @Test
    public void testTwo() throws IOException {
        CharArrayWriter writer = new CharArrayWriter();
        Reporter reporter1 = Reporters.writer(writer);
        reporter1.setLevel(LogLevel.DEBUG);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Reporter reporter2 = Reporters.stream(out,"UTF-16BE");
        reporter2.setLevel(LogLevel.DEBUG);
        
        File f = File.createTempFile("ReportersTest","1");
        Reporter reporter3 = Reporters.file(f,"UTF-16LE");
        reporter3.setLevel(LogLevel.DEBUG);

        Reporter reporter4 = Reporters.silent();
        
        Reporter tee1 = Reporters.tee(reporter1,reporter2,reporter3,reporter4);
        tee1.setLevel(LogLevel.DEBUG);
        tee1.report(LogLevel.TRACE,"AAA");
        tee1.report(LogLevel.DEBUG,"BBB");
        tee1.report(LogLevel.INFO,"CCC");
        tee1.setLevel(LogLevel.WARN);
        tee1.report(LogLevel.DEBUG,"___");
        tee1.report(LogLevel.INFO,"DDD");
        tee1.report(LogLevel.ERROR,"EEE");

        tee1.close();
        
        String s1 = new String(writer.toCharArray());
        String s2 = new String(out.toByteArray(),"UTF-16BE");
        String s3 = Files.readFromFile(f,"UTF-16LE");
        assertMsgs(s1);
        assertMsgs(s2);
        assertMsgs(s3);
    }


}
